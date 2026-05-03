package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.insumo.ports.`out`.InsumoRepositoryPort
import br.com.servicetrack.application.ordemServico.dto.response.OrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.GerarOrcamentoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.PrazoConclusao
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GerarOrcamentoService(
    private val osRepository: OrdemServicoRepositoryPort,
    private val insumoRepository: InsumoRepositoryPort,
    private val jwt: JwtPort,
) : GerarOrcamentoUseCase {

    @Auditavel(entidade = TipoEntidade.ORCAMENTO, evento = TipoEventoAuditoria.CRIADO)
    override fun gerarOrcamento(ordemServicoId: String, prazoConclusao: LocalDate): OrdemServicoResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val os = osRepository.buscarPorId(OrdemServicoId(ordemServicoId))
            ?: throw EntidadeNaoEncontradaException("Ordem de Serviço", arrayOf(ordemServicoId))

        AuditoriaContextoHolder.registrarAntes(os)

        if (os.obterMecanicoId() != solicitanteId) {
            throw OperacaoNegadaException(
                "geração de orçamento",
                "Apenas o mecânico vinculado à OS pode gerar o orçamento"
            )
        }

        if (os.obterStatus() != StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            throw OperacaoNegadaException(
                "geração de orçamento",
                "O orçamento só pode ser gerado quando a OS está em diagnóstico"
            )
        }

        if (os.listarServicos().isEmpty()) {
            throw DomainException("São necessários pelo menos 1 serviço para gerar o orçamento")
        }

        if (os.listarInsumos().isEmpty()) {
            throw DomainException("São necessários pelo menos 1 insumo para gerar o orçamento")
        }

        if (prazoConclusao.isBefore(LocalDate.now())) {
            throw DomainException("O prazo de conclusão deve ser igual ou posterior à data atual")
        }

        os.definirPrazoConclusao(LocalDateTime.of(prazoConclusao, LocalTime.MAX))

        val custoMaoDeObra = os.listarServicos()
            .fold(ValorMonetario.zero()) { acc, item -> acc.somar(item.valor) }

        val custoInsumos = os.listarInsumos()
            .groupBy { it }
            .entries
            .fold(ValorMonetario.zero()) { acc, (insumoId, ocorrencias) ->
                val insumo = insumoRepository.buscarPorId(insumoId)
                    ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(insumoId.valor))
                acc.somar(insumo.calcularCusto(ocorrencias.size))
            }

        os.listarInsumos()
            .groupBy { it }
            .forEach { (insumoId, ocorrencias) ->
                val insumo = insumoRepository.buscarPorId(insumoId)
                    ?: throw EntidadeNaoEncontradaException("Insumo", arrayOf(insumoId.valor))
                insumo.reservar(ocorrencias.size)
                insumoRepository.atualizar(insumo)
            }

        os.gerarOrcamento(custoMaoDeObra, custoInsumos)

        return OrdemServicoResDTO.de(osRepository.atualizar(os))
    }
}
