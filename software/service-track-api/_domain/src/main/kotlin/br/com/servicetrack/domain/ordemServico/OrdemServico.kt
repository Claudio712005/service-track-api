package br.com.servicetrack.domain.ordemServico

import br.com.servicetrack.domain.insumo.vo.InsumoId
import br.com.servicetrack.domain.orcamento.Orcamento
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.PrazoConclusao
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.time.LocalDateTime

class OrdemServico private constructor(
    val id: OrdemServicoId,
    val motivo: String,
    private var observacao: String,
    val clienteId: UsuarioId,
    private var mecanicoId: UsuarioId,
    val veiculoId: VeiculoId,
    val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var status: StatusOrdemServico,
    private var prazoConclusao: PrazoConclusao?,
    private var orcamento: Orcamento?,
    private val insumos: MutableList<InsumoId>
) {

    companion object {

        fun abrir(
            motivo: String,
            clienteId: UsuarioId,
            mecanicoId: UsuarioId,
            veiculoId: VeiculoId,
            observacao: String = ""
        ): OrdemServico {
            require(motivo.isNotBlank()) { "Motivo da OS não pode ser vazio" }

            val agora = LocalDateTime.now()

            return OrdemServico(
                id = OrdemServicoId.gerar(),
                motivo = motivo,
                observacao = observacao,
                clienteId = clienteId,
                mecanicoId = mecanicoId,
                veiculoId = veiculoId,
                dataCriacao = agora,
                dataAtualizacao = agora,
                status = StatusOrdemServico.deEnum(StatusOrdemServicoEnum.RECEBIDA),
                prazoConclusao = null,
                orcamento = null,
                insumos = mutableListOf()
            )
        }
    }

    fun obterStatus(): StatusOrdemServicoEnum = status.valor

    fun obterOrcamento(): Orcamento? = orcamento

    fun listarInsumos(): List<InsumoId> = insumos.toList()

    fun obterMecanicoId(): UsuarioId = mecanicoId

    fun iniciarDiagnostico() {
        alterarStatus(StatusOrdemServicoEnum.EM_DIAGNOSTICO)
    }

    fun finalizar() {
        alterarStatus(StatusOrdemServicoEnum.FINALIZADA)
    }

    fun entregar() {
        alterarStatus(StatusOrdemServicoEnum.ENTREGUE)
    }

    fun cancelar(motivo: String = "") {
        if (motivo.isNotBlank()) {
            observacao += "\nCancelada: $motivo"
        }
        alterarStatus(StatusOrdemServicoEnum.CANCELADA)
    }

    fun adicionarInsumo(insumoId: InsumoId) {
        check(status.valor == StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            "Insumos só podem ser adicionados durante o diagnóstico"
        }
        insumos.add(insumoId)
        dataAtualizacao = LocalDateTime.now()
    }

    fun removerInsumo(insumoId: InsumoId) {
        check(status.valor == StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            "Insumos só podem ser removidos durante o diagnóstico"
        }
        check(insumos.remove(insumoId)) { "Insumo não encontrado na OS" }
        dataAtualizacao = LocalDateTime.now()
    }

    fun gerarOrcamento(custoMaoDeObra: ValorMonetario, custoInsumos: ValorMonetario) {
        check(status.valor == StatusOrdemServicoEnum.EM_DIAGNOSTICO) {
            "Orçamento só pode ser gerado durante o diagnóstico"
        }
        orcamento = Orcamento.gerar(custoMaoDeObra, custoInsumos)
        alterarStatus(StatusOrdemServicoEnum.AGUARDANDO_APROVACAO)
    }

    fun aprovarOrcamento() {
        val orc = orcamento ?: throw DomainException("OS não possui orçamento gerado")
        check(status.valor == StatusOrdemServicoEnum.AGUARDANDO_APROVACAO) {
            "Aprovação só é possível quando a OS está aguardando aprovação"
        }
        orc.aprovar()
        alterarStatus(StatusOrdemServicoEnum.EM_EXECUCAO)
    }

    fun reprovarOrcamento(motivo: String) {
        val orc = orcamento ?: throw DomainException("OS não possui orçamento gerado")
        check(status.valor == StatusOrdemServicoEnum.AGUARDANDO_APROVACAO) {
            "Reprovação só é possível quando a OS está aguardando aprovação"
        }
        orc.reprovar(motivo)
        alterarStatus(StatusOrdemServicoEnum.CANCELADA)
    }

    fun definirPrazoConclusao(prazo: LocalDateTime) {
        check(prazoConclusao == null) { "Prazo de conclusão já definido" }
        if (prazo.isBefore(LocalDateTime.now())) {
            throw DomainException("Prazo de conclusão não pode estar no passado")
        }
        prazoConclusao = PrazoConclusao(prazo)
        dataAtualizacao = LocalDateTime.now()
    }

    fun reassinarMecanico(novoMecanicoId: UsuarioId) {
        check(mecanicoId != novoMecanicoId) { "O mecânico já está atribuído a esta OS" }
        mecanicoId = novoMecanicoId
        dataAtualizacao = LocalDateTime.now()
    }

    private fun alterarStatus(novoStatus: StatusOrdemServicoEnum) {
        status = status.transitarPara(novoStatus)
        dataAtualizacao = LocalDateTime.now()
    }
}
