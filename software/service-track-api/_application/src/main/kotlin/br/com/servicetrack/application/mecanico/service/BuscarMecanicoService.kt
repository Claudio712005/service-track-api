package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.application.mecanico.ports.`in`.BuscarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class BuscarMecanicoService(
    private val mecanicoRepository: MecanicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort
) : BuscarMecanicoUseCase {

    override fun buscarMecanico(id: String): MecanicoResDTO {
        val mecanico = mecanicoRepository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException("Mecanico", arrayOf(id))

        val usuario = usuarioRepository.buscarPorId(UsuarioId(mecanico.usuarioId.valor))
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(mecanico.usuarioId.valor))

        return MecanicoResDTO.de(usuario, mecanico)
    }
}
