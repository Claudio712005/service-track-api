package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.usuario.dto.request.LoginReqDTO
import br.com.servicetrack.application.usuario.dto.response.LoginResDTO
import br.com.servicetrack.application.usuario.ports.`in`.LoginUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.`out`.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.`out`.JwtPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria

@ApplicationService
class LoginService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val criptografia: CriptografiaPort,
    private val jwt: JwtPort
) : LoginUsuarioUseCase {

    @Auditavel(evento = TipoEventoAuditoria.LOGIN, entidade = TipoEntidade.USUARIO)
    override fun login(req: LoginReqDTO): LoginResDTO {
        val usuario = usuarioRepository.buscarPorEmail(req.email)
            ?: throw CredenciaisInvalidasException()

        val dados = usuario.obterDados()

        if (!criptografia.comparar(usuario.obterSenhaHash().valor, req.senha)) {
            throw CredenciaisInvalidasException()
        }

        val token = jwt.gerarToken(
            usuarioId = dados.id.valor,
            email = dados.email.valor,
            roles = dados.roles
        )

        return LoginResDTO(
            token = token,
            usuarioId = dados.id.valor,
            nome = dados.nome,
            email = dados.email.valor,
            roles = dados.roles
        )
    }
}
