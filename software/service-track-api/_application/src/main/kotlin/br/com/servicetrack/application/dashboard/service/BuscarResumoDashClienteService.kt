package br.com.servicetrack.application.dashboard.service

import br.com.servicetrack.application.dashboard.dto.response.OrdensAtivasResDTO
import br.com.servicetrack.application.dashboard.dto.response.OrdensRecentesResDTO
import br.com.servicetrack.application.dashboard.dto.response.ResumoDashClienteResDTO
import br.com.servicetrack.application.dashboard.dto.response.ResumoResDTO
import br.com.servicetrack.application.dashboard.dto.response.VeiculoDashResDTO
import br.com.servicetrack.application.dashboard.ports.`in`.BuscarResumoDashClienteUseCase
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.ordemServico.ports.out.OrdemServicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.ports.out.VeiculoRepositoryPort
import br.com.servicetrack.domain.ordemServico.StatusOrdemServicoEnum
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BuscarResumoDashClienteService(
    private val usuarioRepositoryPort: UsuarioRepositoryPort,
    private val veiculoRepositoryPort: VeiculoRepositoryPort,
    private val ordemServicoRepositoryPort: OrdemServicoRepositoryPort,
    private val jwt: JwtPort,
) : BuscarResumoDashClienteUseCase {

    private val statusAtivos = listOf(
        StatusOrdemServicoEnum.RECEBIDA,
        StatusOrdemServicoEnum.EM_DIAGNOSTICO,
        StatusOrdemServicoEnum.AGUARDANDO_APROVACAO,
        StatusOrdemServicoEnum.EM_EXECUCAO,
        StatusOrdemServicoEnum.ENTREGUE,
    )

    private val statusConcluidos = setOf(
        StatusOrdemServicoEnum.FINALIZADA,
        StatusOrdemServicoEnum.ENTREGUE,
        StatusOrdemServicoEnum.CANCELADA,
    )

    override fun buscarResumo(clienteId: String): ResumoDashClienteResDTO {
        val solicitanteId = jwt.getUsuarioId()
        if (solicitanteId.valor != clienteId) {
            throw OperacaoNegadaException(
                "BuscarDashboardCliente",
                "Você pode acessar apenas seu próprio dashboard"
            )
        }

        val cliente = usuarioRepositoryPort.buscarPorId(UsuarioId(clienteId))
            ?: throw EntidadeNaoEncontradaException("Cliente", arrayOf(clienteId))

        val usuarioId = UsuarioId(clienteId)
        val veiculos = veiculoRepositoryPort.listarDashboardPorProprietario(usuarioId)
        val ordensAtivas = ordemServicoRepositoryPort.listarAtivasDashboardPorCliente(usuarioId, 10)
        val ordensRecentes = ordemServicoRepositoryPort.listarRecentesDashboardPorCliente(usuarioId, 10)
        val qtdAtivas = ordemServicoRepositoryPort.contarPorClienteEStatus(usuarioId, statusAtivos)
        val qtdConcluidas = ordemServicoRepositoryPort.contarPorClienteEStatus(usuarioId, listOf(StatusOrdemServicoEnum.FINALIZADA))
        val qtdCanceladas = ordemServicoRepositoryPort.contarPorClienteEStatus(usuarioId, listOf(StatusOrdemServicoEnum.CANCELADA))

        val now = LocalDateTime.now()

        val veiculosDash = veiculos.map { v ->
            val veiculoId = VeiculoId(v.id)
            val totalOrdens = ordemServicoRepositoryPort.contarTotalPorVeiculo(veiculoId, usuarioId)
            val totalGasto = ordemServicoRepositoryPort.somarGastoPorVeiculo(veiculoId, usuarioId)
            VeiculoDashResDTO(
                id = v.id,
                placa = v.placa,
                marca = v.marca,
                modelo = v.modelo,
                ano = v.ano,
                imagemUrl = v.imagemUrl,
                codigoFipe = v.codigoFipe,
                ativo = v.ativo,
                totalOrdens = totalOrdens.toInt(),
                totalGasto = totalGasto,
                dataCriacao = v.dataCriacao,
            )
        }

        val ordensAtivasDash = ordensAtivas.map { os ->
            OrdensAtivasResDTO(
                id = os.id,
                motivo = os.motivo,
                status = os.status.name,
                veiculoId = os.veiculoId,
                veiculoPlaca = os.veiculoPlaca,
                veiculoModelo = os.veiculoModelo,
                mecanicoId = os.mecanicoId,
                mecanicoNome = os.mecanicoNome,
                dataCriacao = os.dataCriacao,
                dataAtualizacao = os.dataAtualizacao,
                diasEmAndamento = ChronoUnit.DAYS.between(os.dataCriacao, now).toInt(),
                valorOrcado = os.valorOrcado,
                prazoConclusao = os.prazoConclusao,
            )
        }

        val ordensRecentesDash = ordensRecentes.map { os ->
            val isConcluida = os.status in statusConcluidos
            val dataConclusao = if (isConcluida) os.dataAtualizacao else null
            val diasParaConclusao = dataConclusao?.let {
                ChronoUnit.DAYS.between(os.dataCriacao, it).toInt()
            }
            OrdensRecentesResDTO(
                id = os.id,
                motivo = os.motivo,
                status = os.status,
                veiculoId = os.veiculoId,
                veiculoPlaca = os.veiculoPlaca,
                veiculoModelo = os.veiculoModelo,
                dataCriacao = os.dataCriacao,
                dataConclusao = dataConclusao,
                diasParaConclusao = diasParaConclusao,
                valorTotal = os.valorOrcado,
                mecanicoNome = os.mecanicoNome,
            )
        }

        return ResumoDashClienteResDTO(
            usuarioId = cliente.id.valor,
            usuarioNome = cliente.obterDados().nome,
            resumo = ResumoResDTO(
                ordensAtivas = qtdAtivas.toInt(),
                ordensConcluidas = qtdConcluidas.toInt(),
                ordensCanceladas = qtdCanceladas.toInt(),
                totalOrdens = (qtdAtivas + qtdConcluidas + qtdCanceladas).toInt(),
                veiculosCadastrados = veiculos.size,
            ),
            ordensAtivas = ordensAtivasDash,
            ordensRecentes = ordensRecentesDash,
            veiculos = veiculosDash,
            dataAtualizacao = now,
        )
    }
}
