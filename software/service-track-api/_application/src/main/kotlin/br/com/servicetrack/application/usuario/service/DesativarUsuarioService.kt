package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.ports.`in`.DesativarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class DesativarUsuarioService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : DesativarUsuarioUseCase {

    @Auditavel(entidade = TipoEntidade.CLIENTE, evento = TipoEventoAuditoria.REMOVIDO)
    override fun desativarUsuario(id: UsuarioId) {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(solicitanteId.valor))

        val alvo = usuarioRepository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(id.valor))

        validarPermissao(solicitante, alvo, solicitanteId, id)

        alvo.desativar()
        usuarioRepository.desativar(id)
    }

    private fun validarPermissao(solicitante: Usuario, alvo: Usuario, solicitanteId: UsuarioId, alvoId: UsuarioId) {
        if (solicitanteId == alvoId) return

        if (solicitante.ehCliente()) {
            throw OperacaoNegadaException(
                "desativação de usuário",
                "Cliente só pode desativar a si mesmo"
            )
        }

        if (solicitante.ehMecanico() && alvo.ehMecanico()) {
            throw OperacaoNegadaException(
                "desativação de usuário",
                "Mecânico não pode desativar outro mecânico"
            )
        }
    }
}
