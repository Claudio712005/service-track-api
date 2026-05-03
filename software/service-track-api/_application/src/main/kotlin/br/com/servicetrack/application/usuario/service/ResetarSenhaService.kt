package br.com.servicetrack.application.usuario.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.CredenciaisInvalidasException
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.usuario.dto.request.ResetarSenhaReqDTO
import br.com.servicetrack.application.usuario.ports.`in`.ResetarSenhaUseCase
import br.com.servicetrack.application.usuario.ports.out.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.Usuario
import br.com.servicetrack.domain.usuario.vo.Senha

class ResetarSenhaService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val criptografia: CriptografiaPort,
    private val jwt: JwtPort
) : ResetarSenhaUseCase {

    @Auditavel(entidade = TipoEntidade.USUARIO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun resetarSenha(req: ResetarSenhaReqDTO) {
        val usuarioId = jwt.getUsuarioId()

        val usuario = usuarioRepository.buscarPorId(usuarioId)
            ?: throw EntidadeNaoEncontradaException(Usuario::class.java.name, arrayOf(usuarioId.valor))

        usuario.validarAtivo()

        if (!criptografia.comparar(usuario.obterSenhaHash().valor, req.senhaAtual)) {
            throw CredenciaisInvalidasException()
        }

        if (req.novaSenha != req.confirmacaoNovaSenha) {
            throw DomainException("Nova senha e confirmação não conferem")
        }

        Senha.criar(req.novaSenha)

        val novoHash = criptografia.criptografar(req.novaSenha)
        usuario.alterarSenha(Senha.deHash(novoHash))

        usuarioRepository.atualizar(usuario)
    }
}
