package br.com.servicetrack.application.usuario.ports.out

import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface UsuarioRepositoryPort {

    fun salvar(usuario: Usuario)
    fun existePorEmailOuCpf(email: String, cpf: String): Boolean
    fun buscarPorEmail(email: String): Usuario?
    fun buscarPorId(id: UsuarioId): Usuario?
    fun atualizar(usuario: Usuario)
    fun desativar(id: UsuarioId)
    fun existeEmailEmOutroUsuario(email: String, idAtual: UsuarioId): Boolean
    fun buscarInativoPorCpf(cpf: String): Usuario?
}
