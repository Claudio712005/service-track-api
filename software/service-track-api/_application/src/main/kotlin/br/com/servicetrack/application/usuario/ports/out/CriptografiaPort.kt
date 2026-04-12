package br.com.servicetrack.application.usuario.ports.out

interface CriptografiaPort {
    fun criptografar(senha: String): String
    fun comparar(senhaHash: String, senha: String): Boolean
}
