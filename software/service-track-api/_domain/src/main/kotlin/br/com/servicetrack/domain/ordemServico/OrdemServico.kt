package br.com.servicetrack.domain.ordemServico

import br.com.servicetrack.domain.contrato.Contrato
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.ordemServico.vo.StatusOrdemServico
import br.com.servicetrack.domain.ordemServico.vo.TempoExecucao
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.time.LocalDateTime

class OrdemServico private constructor(
    private val id: OrdemServicoId,
    private val motivo: String,
    private val observacao: String,
    private val clienteId: UsuarioId,
    private val mecanicoId: UsuarioId,
    private val veiculoId: VeiculoId,
    private val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var status: StatusOrdemServico,
    private var tempoExecucao: TempoExecucao?,
    private var contrato: Contrato?,
) {

    fun alterarStatus(novoStatus: StatusOrdemServicoEnum) {
        this.status = this.status.transitarPara(novoStatus)
        this.dataAtualizacao = LocalDateTime.now()
    }

    fun atualizarTempoExecucao(tempo: LocalDateTime) {
        if (this.tempoExecucao != null) {
            throw DomainException("Tempo de execução já definido")
        }

        if (tempo.isBefore(this.dataCriacao)) {
            throw DomainException("Tempo de execução não pode ser anterior à data de criação")
        }

        if (tempo.isBefore(LocalDateTime.now())) {
            throw DomainException("Tempo de execução não pode ser no passado")
        }

        this.tempoExecucao = TempoExecucao(tempo)
        this.dataAtualizacao = LocalDateTime.now()
    }
}