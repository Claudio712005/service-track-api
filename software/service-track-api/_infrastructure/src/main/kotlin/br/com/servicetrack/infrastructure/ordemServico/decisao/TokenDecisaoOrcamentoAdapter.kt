package br.com.servicetrack.infrastructure.ordemServico.decisao

import br.com.servicetrack.application.ordemServico.ports.out.DecisaoOrcamentoTokenClaims
import br.com.servicetrack.application.ordemServico.ports.out.TokenDecisaoOrcamentoPort
import br.com.servicetrack.domain.ordemServico.vo.OrdemServicoId
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import io.smallrye.jwt.auth.principal.JWTParser
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration

@ApplicationScoped
class TokenDecisaoOrcamentoAdapter(
    @ConfigProperty(name = "servicetrack.jwt.decisao-orcamento.expiracao-horas", defaultValue = "72")
    private val expiracaoHoras: Long,
    private val jwtParser: JWTParser,
) : TokenDecisaoOrcamentoPort {

    private companion object {
        const val ISSUER = "service-track-api"
        const val PURPOSE = "DECISAO_ORCAMENTO"
        const val AUDIENCE = "orcamento-decisao"
        const val CLAIM_PURPOSE = "purpose"
        const val CLAIM_OS = "osId"
    }

    override fun gerar(ordemServicoId: OrdemServicoId, clienteId: UsuarioId): String =
        Jwt.issuer(ISSUER)
            .subject(clienteId.valor)
            .audience(AUDIENCE)
            .claim(CLAIM_PURPOSE, PURPOSE)
            .claim(CLAIM_OS, ordemServicoId.valor)
            .expiresIn(Duration.ofHours(expiracaoHoras))
            .sign()

    override fun validar(token: String): DecisaoOrcamentoTokenClaims? {
        val rawToken = token.trim()
        if (rawToken.isBlank()) return null

        return try {
            val jwt = jwtParser.parse(rawToken)

            val purpose = jwt.getClaim<String?>(CLAIM_PURPOSE)
            if (purpose != PURPOSE) return null
            if (AUDIENCE !in (jwt.audience ?: emptySet())) return null

            val osId = jwt.getClaim<String?>(CLAIM_OS) ?: return null
            val subject = jwt.subject ?: return null

            DecisaoOrcamentoTokenClaims(
                ordemServicoId = OrdemServicoId(osId),
                clienteId = UsuarioId(subject),
            )
        } catch (_: Exception) {
            null
        }
    }
}
