package br.com.servicetrack.infrastructure.support

import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import io.quarkus.arc.Arc
import io.smallrye.jwt.build.Jwt
import java.time.Duration

object TokenTeste {

    private const val ISSUER = "service-track-api"
    private val VALIDADE = Duration.ofHours(8)

    fun para(email: String): String {
        val dados = dados(email)
        return Jwt.upn(dados.email.valor)
            .issuer(ISSUER)
            .subject(dados.id.valor)
            .groups(dados.roles.map { it.name }.toSet())
            .claim("cpf", dados.cpf.valor)
            .expiresIn(VALIDADE)
            .sign()
    }

    fun idDe(email: String): String = dados(email).id.valor

    private fun dados(email: String) =
        repositorio().buscarPorEmail(email)?.obterDados()
            ?: error("Usuário de teste não encontrado para o e-mail $email")

    private fun repositorio(): UsuarioRepositoryPort =
        Arc.container().instance(UsuarioRepositoryPort::class.java).get()
}
