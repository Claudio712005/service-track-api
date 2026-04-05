package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.usuario.dto.request.CriarUsuarioCommand
import br.com.servicetrack.application.usuario.dto.response.UsuarioResponse
import br.com.servicetrack.application.usuario.mapper.toDomain
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.vo.Senha

@ApplicationService
class CriarUsuarioService(
    private val repository: UsuarioRepositoryPort,
    private val criptografia: CriptografiaPort
) : CriarUsuarioUseCase {

    override fun criarUsuario(command: CriarUsuarioCommand): UsuarioResponse {
        if (repository.existePorEmailOuCpf(command.email, command.cpf)) {
            throw UsuarioJaExisteException(command.email, command.cpf)
        }

        Senha.criar(command.senha)

        val senhaHash = criptografia.criptografar(command.senha)
        val novoUsuario = command.toDomain(senhaHash)

        repository.salvar(novoUsuario)

        return UsuarioResponse.de(novoUsuario)
    }
}
