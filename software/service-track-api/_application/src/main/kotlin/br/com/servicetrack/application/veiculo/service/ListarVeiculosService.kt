package br.com.servicetrack.application.veiculo.service

import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.application.veiculo.dto.response.DadosveiculoResDTO
import br.com.servicetrack.application.veiculo.ports.`in`.ListarVeiculosUseCase
import br.com.servicetrack.application.veiculo.ports.`out`.VeiculoRepositoryPort
import br.com.servicetrack.domain.usuario.Usuario

class ListarVeiculosService(
    private val repository: VeiculoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : ListarVeiculosUseCase {

    override fun listarVeiculos(): List<DadosveiculoResDTO> {
        val usuarioId = jwt.getUsuarioId()
        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(usuarioId.valor))

        val veiculos = if (usuario.ehMecanico()) {
            repository.listarTodos()
        } else {
            repository.listarPorProprietario(usuarioId)
        }

        return veiculos.map { DadosveiculoResDTO.de(it) }
    }
}
