package br.com.servicetrack.application.notificacao.event

import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.AprovacaoOrcamentoLinkPort
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.servico.ports.out.ServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.ordemServico.OrdemServico
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase
import jakarta.transaction.Transactional
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ApplicationScoped
open class OrdemServicoStatusAlteradoListener(
    private val enfileirar: EnfileirarNotificacaoUseCase,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val aprovacaoOrcamentoLink: AprovacaoOrcamentoLinkPort,
    private val ordemServicoRepository: OrdemServicoRepositoryPort,
    private val veiculoRepository: VeiculoRepositoryPort,
    private val servicoRepository: ServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
) {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    open fun aoAlterarStatus(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) evento: OrdemServicoStatusAlteradoEvent,
    ) {
        val nomeCliente = usuarioRepository.buscarPorId(evento.clienteId)
            ?.obterDados()
            ?.nome
            ?: return

        enfileirar.executar(montarNotificacaoStatus(evento, nomeCliente))

        if (evento.novoStatus == StatusOrdemServicoEnum.AGUARDANDO_APROVACAO) {
            enfileirar.executar(montarSolicitacaoAprovacao(evento, nomeCliente))
        }
    }

    private fun montarNotificacaoStatus(
        evento: OrdemServicoStatusAlteradoEvent,
        nomeCliente: String,
    ): EnfileirarNotificacaoCommand {
        val variaveis = VariaveisTemplate.de(
            mapOf(
                "os" to evento.ordemServicoId.valor,
                "novoStatus" to evento.novoStatus.descricao,
                "nomeCliente" to nomeCliente,
            ),
        )

        return EnfileirarNotificacaoCommand(
            assunto = AssuntoNotificacao("Atualização da sua OS ${evento.ordemServicoId.valor}"),
            titulo = TituloNotificacao(TipoConteudoNotificacao.MUDANCA_STATUS_OS.titulo),
            descricao = DescricaoNotificacao("Status atualizado para ${evento.novoStatus.descricao}"),
            variaveis = variaveis,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = evento.clienteId,
        )
    }

    private fun montarSolicitacaoAprovacao(
        evento: OrdemServicoStatusAlteradoEvent,
        nomeCliente: String,
    ): EnfileirarNotificacaoCommand {
        val links = aprovacaoOrcamentoLink.gerarLinks(evento.ordemServicoId, evento.clienteId)
        val os = ordemServicoRepository.buscarPorId(evento.ordemServicoId)

        val variaveis = VariaveisTemplate.de(
            montarVariaveisAprovacao(evento, nomeCliente, os, links.aprovarUrl, links.reprovarUrl),
        )

        return EnfileirarNotificacaoCommand(
            assunto = AssuntoNotificacao("Aprove o orçamento da sua OS ${evento.ordemServicoId.valor}"),
            titulo = TituloNotificacao(TipoConteudoNotificacao.SOLICITACAO_APROVACAO_ORCAMENTO_OS.titulo),
            descricao = DescricaoNotificacao("Seu orçamento está pronto para aprovação"),
            variaveis = variaveis,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.SOLICITACAO_APROVACAO_ORCAMENTO_OS,
            destinatario = evento.clienteId,
        )
    }

    private fun montarVariaveisAprovacao(
        evento: OrdemServicoStatusAlteradoEvent,
        nomeCliente: String,
        os: OrdemServico?,
        aprovarUrl: String,
        reprovarUrl: String,
    ): Map<String, String> {
        val base = mutableMapOf(
            "os" to evento.ordemServicoId.valor,
            "nomeCliente" to nomeCliente,
            "aprovarUrl" to aprovarUrl,
            "reprovarUrl" to reprovarUrl,
        )

        if (os == null) {
            return base
        }

        val veiculo = veiculoRepository.buscarPorId(os.veiculoId)?.obterDados()
        val mecanico = usuarioRepository.buscarPorId(os.obterMecanicoId())?.obterDados()?.nome
        val orcamento = os.obterOrcamento()

        base["motivo"] = os.motivo
        base["dataAbertura"] = formatarData(os.dataCriacao)
        base["prazoConclusao"] = formatarData(os.obterPrazoConclusao())
        base["veiculo"] = veiculo?.let { "${it.marca} ${it.modelo} (${it.ano})" } ?: "Não informado"
        base["placa"] = veiculo?.placa?.valor ?: "-"
        base["mecanico"] = mecanico ?: "-"
        base["custoMaoDeObra"] = formatarMoeda(orcamento?.custoMaoDeObra)
        base["custoInsumos"] = formatarMoeda(orcamento?.custoInsumos)
        base["valorTotal"] = formatarMoeda(orcamento?.valorTotal)
        base["servicosHtml"] = montarServicosHtml(os)
        base["servicosTexto"] = montarServicosTexto(os)
        base["insumosHtml"] = montarInsumosHtml(os)
        base["insumosTexto"] = montarInsumosTexto(os)

        return base
    }

    private fun montarServicosHtml(os: OrdemServico): String =
        os.listarServicos().joinToString("") { item ->
            val nome = servicoRepository.buscarPorId(item.servicoId)?.nomeServico ?: item.servicoId.valor
            """<tr>
                <td style="padding:8px 12px; border-bottom:1px solid #eef2f7; font-size:14px; color:#1f2937;">$nome</td>
                <td style="padding:8px 12px; border-bottom:1px solid #eef2f7; font-size:14px; color:#1f2937; text-align:right; white-space:nowrap;">${formatarMoeda(item.valor)}</td>
            </tr>"""
        }

    private fun montarServicosTexto(os: OrdemServico): String =
        os.listarServicos().joinToString("\n") { item ->
            val nome = servicoRepository.buscarPorId(item.servicoId)?.nomeServico ?: item.servicoId.valor
            "  - $nome: ${formatarMoeda(item.valor)}"
        }

    private fun montarInsumosHtml(os: OrdemServico): String =
        os.listarInsumos().groupingBy { it }.eachCount().entries.joinToString("") { (id, qtd) ->
            val insumo = insumoRepository.buscarPorId(id)
            val nome = insumo?.nome ?: id.valor
            val subtotal = insumo?.calcularCusto(qtd)
            """<tr>
                <td style="padding:8px 12px; border-bottom:1px solid #eef2f7; font-size:14px; color:#1f2937;">$nome</td>
                <td style="padding:8px 12px; border-bottom:1px solid #eef2f7; font-size:14px; color:#1f2937; text-align:center; white-space:nowrap;">${qtd}x</td>
                <td style="padding:8px 12px; border-bottom:1px solid #eef2f7; font-size:14px; color:#1f2937; text-align:right; white-space:nowrap;">${formatarMoeda(subtotal)}</td>
            </tr>"""
        }

    private fun montarInsumosTexto(os: OrdemServico): String =
        os.listarInsumos().groupingBy { it }.eachCount().entries.joinToString("\n") { (id, qtd) ->
            val insumo = insumoRepository.buscarPorId(id)
            val nome = insumo?.nome ?: id.valor
            "  - $nome (${qtd}x): ${formatarMoeda(insumo?.calcularCusto(qtd))}"
        }

    private fun formatarMoeda(valor: ValorMonetario?): String {
        val bruto = (valor?.valor ?: java.math.BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        return "R$ " + bruto.toPlainString().replace('.', ',')
    }

    private fun formatarData(data: LocalDateTime?): String =
        data?.format(FORMATO_DATA) ?: "A combinar"

    private companion object {
        val FORMATO_DATA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    }
}
