package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.usuario.dto.request.CadastrarClienteReqDTO
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.application.usuario.mapper.toDomain
import br.com.servicetrack.application.usuario.ports.`in`.CriarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`out`.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Senha
import br.com.servicetrack.domain.usuario.vo.Telefone

@ApplicationService
class CriarUsuarioService(
    private val repository: UsuarioRepositoryPort,
    private val criptografia: CriptografiaPort
) : CriarUsuarioUseCase {

    @Auditavel(
        TipoEntidade.CLIENTE,
        TipoEventoAuditoria.CRIADO
    )
    override fun criarUsuario(req: CadastrarClienteReqDTO): ClienteResDTO {
        if (repository.existePorEmailOuCpf(req.email, req.cpf)) {
            throw UsuarioJaExisteException(req.email, req.cpf)
        }

        Senha.criar(req.senha)
        val senhaHash = criptografia.criptografar(req.senha)

        val usuarioInativo = repository.buscarInativoPorCpf(req.cpf)
        if (usuarioInativo != null) {
            usuarioInativo.ativar()
            usuarioInativo.atualizar(req.nome, Email(req.email), Telefone(req.telefone))
            usuarioInativo.alterarSenha(Senha.deHash(senhaHash))
            repository.atualizar(usuarioInativo)
            return ClienteResDTO.de(usuarioInativo)
        }

        val novoUsuario = req.toDomain(senhaHash)
        repository.salvar(novoUsuario)
        return ClienteResDTO.de(novoUsuario)
    }
}
