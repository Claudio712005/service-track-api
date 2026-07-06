package br.com.servicetrack.application.ordemServico.service.support

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.insumo.ports.out.InsumoRepositoryPort
import br.com.servicetrack.application.notificacao.dto.EnfileirarNotificacaoCommand
import br.com.servicetrack.application.notificacao.event.OrdemServicoStatusAlteradoEvent
import br.com.servicetrack.application.notificacao.ports.`in`.EnfileirarNotificacaoUseCase
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.notificacao.TipoConteudoNotificacao
import br.com.servicetrack.domain.notificacao.TipoNotificacao
import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.ordemServico.OrdemServico
import jakarta.enterprise.event.Event

class DecididorOrcamento(
    private val osRepository: OrdemServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val enfileirarNotificacao: EnfileirarNotificacaoUseCase,
    private val statusAlteradoEvent: Event<OrdemServicoStatusAlteradoEvent>,
) {

    fun aprovar(os: OrdemServico): OrdemServico {
        os.aprovarOrcamento()
        return concluir(os, decisao = "aprovado", motivo = "")
    }

    fun reprovar(os: OrdemServico, motivo: String): OrdemServico {
        reporEstoque(os)
        os.reprovarOrcamento(motivo)
        return concluir(os, decisao = "reprovado", motivo = motivo)
    }

    private fun concluir(os: OrdemServico, decisao: String, motivo: String): OrdemServico {
        val atualizada = osRepository.atualizar(os)

        statusAlteradoEvent.fire(
            OrdemServicoStatusAlteradoEvent(
                ordemServicoId = atualizada.id,
                clienteId = atualizada.clienteId,
                novoStatus = atualizada.obterStatus(),
            ),
        )

        notificarMecanico(atualizada, decisao, motivo)
        return atualizada
    }

    private fun reporEstoque(os: OrdemServico) {
        os.listarInsumos()
            .groupBy { it }
            .forEach { (insumoId, ocorrencias) ->
                val insumo = insumoRepository.buscarPorId(insumoId)
                    ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(insumoId.valor))
                insumo.adicionarAoEstoque(ocorrencias.size)
                insumoRepository.atualizar(insumo)
            }
    }

    private fun notificarMecanico(os: OrdemServico, decisao: String, motivo: String) {
        val nomeCliente = usuarioRepository.buscarPorId(os.clienteId)
            ?.obterDados()
            ?.nome
            ?: "Cliente"

        val variaveis = VariaveisTemplate.de(
            mapOf(
                "os" to os.id.valor,
                "decisao" to decisao,
                "motivo" to motivo,
                "nomeCliente" to nomeCliente,
            ),
        )

        enfileirarNotificacao.executar(
            EnfileirarNotificacaoCommand(
                assunto = AssuntoNotificacao("Orçamento $decisao — OS ${os.id.valor}"),
                titulo = TituloNotificacao(TipoConteudoNotificacao.DECISAO_ORCAMENTO_OS.titulo),
                descricao = DescricaoNotificacao("O cliente $nomeCliente $decisao o orçamento da OS ${os.id.valor}"),
                variaveis = variaveis,
                tipoNotificacao = TipoNotificacao.EMAIL,
                tipoConteudoNotificacao = TipoConteudoNotificacao.DECISAO_ORCAMENTO_OS,
                destinatario = os.obterMecanicoId(),
            ),
        )
    }
}
