package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.usuario.dto.request.AtualizarUsuarioReqDTO
import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.application.usuario.ports.`in`.AtualizarUsuarioUseCase
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Email
import br.com.servicetrack.domain.usuario.vo.Telefone
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class AtualizarUsuarioService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : AtualizarUsuarioUseCase {

    @Auditavel(entidade = TipoEntidade.CLIENTE, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun atualizarUsuario(id: UsuarioId, req: AtualizarUsuarioReqDTO): ClienteResDTO {
        val solicitanteId = jwt.getUsuarioId()

        val solicitante = usuarioRepository.buscarPorId(solicitanteId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(solicitanteId.valor))

        val alvo = usuarioRepository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(id.valor))

        validarPermissao(solicitante, alvo, solicitanteId, id)

        if (usuarioRepository.existeEmailEmOutroUsuario(req.email, id)) {
            throw DomainException("E-mail '${req.email}' já está em uso por outro usuário")
        }

        alvo.atualizar(
            novoNome = req.nome,
            novoEmail = Email(req.email),
            novoTelefone = Telefone(req.telefone)
        )

        usuarioRepository.atualizar(alvo)

        return ClienteResDTO.de(alvo)
    }

    private fun validarPermissao(solicitante: Usuario, alvo: Usuario, solicitanteId: UsuarioId, alvoId: UsuarioId) {
        if (solicitanteId == alvoId) return

        if (solicitante.ehCliente()) {
            throw OperacaoNegadaException(
                "atualização de usuário",
                "Cliente só pode atualizar seus próprios dados"
            )
        }

        if (solicitante.ehMecanico() && alvo.ehMecanico()) {
            throw OperacaoNegadaException(
                "atualização de usuário",
                "Mecânico não pode atualizar dados de outro mecânico"
            )
        }
    }
}
