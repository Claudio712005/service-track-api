package br.com.servicetrack.application.ordemServico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.ordemServico.dto.request.FiltroOrdemServicoDTO
import br.com.servicetrack.application.ordemServico.dto.response.ResumoOrdemServicoResDTO
import br.com.servicetrack.application.ordemServico.ports.`in`.ListarOrdensServicoUseCase
import br.com.servicetrack.application.ordemServico.ports.`out`.OrdemServicoRepositoryPort
import br.com.servicetrack.application.shared.dto.PageResDTO
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort

class ListarOrdensServicoService(
    private val repository: OrdemServicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort,
) : ListarOrdensServicoUseCase {

    override fun listarOrdensServico(filtro: FiltroOrdemServicoDTO): PageResDTO<ResumoOrdemServicoResDTO> {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(solicitanteId.valor))

        val filtroEfetivo = if (solicitante.ehCliente()) {
            filtro.copy(clienteId = solicitanteId.valor)
        } else {
            filtro
        }

        val pagina = repository.listar(filtroEfetivo)
        return PageResDTO.de(
            content = pagina.content.map { ResumoOrdemServicoResDTO.de(it) },
            page = pagina.page,
            size = pagina.size,
            total = pagina.total,
        )
    }
}
