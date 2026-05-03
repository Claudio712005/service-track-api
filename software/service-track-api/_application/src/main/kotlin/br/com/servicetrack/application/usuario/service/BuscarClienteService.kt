package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.application.usuario.ports.`in`.BuscarClienteUseCase
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class BuscarClienteService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : BuscarClienteUseCase {

    override fun buscarCliente(id: UsuarioId): ClienteResDTO {
        val solicitanteId = jwt.getUsuarioId()
        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(solicitanteId.valor))

        if (solicitanteId != id && !solicitante.ehMecanico()) {
            throw OperacaoNegadaException(
                "consulta de cliente",
                "Apenas o próprio cliente ou um mecânico pode consultar este perfil"
            )
        }

        val cliente = usuarioRepository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(id.valor))

        return ClienteResDTO.de(cliente)
    }
}
