package br.com.servicetrack.infrastructure.jwt

import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.shared.enums.Role
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.quarkus.security.UnauthorizedException
import io.smallrye.jwt.auth.principal.JWTParser
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.ws.rs.core.HttpHeaders
import java.time.Duration

@ApplicationScoped
class JwtAdapter(
    @ConfigProperty(name = "servicetrack.jwt.expiracao-horas", defaultValue = "8")
    private val expiracaoHoras: Long,
    private val jwtParser: JWTParser,
    private val headers: HttpHeaders
) : JwtPort {

    override fun gerarToken(usuarioId: String, email: String, roles: Set<Role>): String =
        Jwt.upn(email)
            .issuer("service-track-api")
            .subject(usuarioId)
            .groups(roles.map { it.name }.toSet())
            .expiresIn(Duration.ofHours(expiracaoHoras))
            .sign()

    override fun getUsuarioId(token: String): String? {
        val rawToken = token.removePrefix("Bearer ").trim()
        if (rawToken.isBlank()) return null

        return try {
            jwtParser.parse(rawToken).subject
        } catch (_: Exception) {
            null
        }
    }

    override fun getToken(): String {
        return headers.getHeaderString("Authorization")
    }

    override fun getUsuarioId(): UsuarioId {
        val token = getToken()
        return UsuarioId(getUsuarioId(token) ?: throw UnauthorizedException("Usuário ou token inválido"))
    }
}