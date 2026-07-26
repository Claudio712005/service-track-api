package br.com.servicetrack.infrastructure.config.jwt

import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.quarkus.security.UnauthorizedException
import io.smallrye.jwt.auth.principal.JWTParser
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.HttpHeaders

@ApplicationScoped
class JwtAdapter(
    private val jwtParser: JWTParser,
    private val headers: HttpHeaders
) : JwtPort {

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
