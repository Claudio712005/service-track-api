package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.annotation.ApplicationService
import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.exception.UsuarioJaExisteException
import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.application.mecanico.mapper.toDomain
import br.com.servicetrack.application.mecanico.ports.`in`.CadastrarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.`out`.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.`out`.CriptografiaPort
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.usuario.vo.Senha

@ApplicationService
class CadastrarMecanicoService(
    private val usuarioRepository: UsuarioRepositoryPort,
    private val mecanicoRepository: MecanicoRepositoryPort,
    private val criptografia: CriptografiaPort
) : CadastrarMecanicoUseCase {

    @Auditavel(entidade = TipoEntidade.MECANICO, evento = TipoEventoAuditoria.CRIADO)
    override fun cadastrarMecanico(req: CadastrarMecanicoReqDTO): MecanicoResDTO {
        if (usuarioRepository.existePorEmailOuCpf(req.email, req.cpf)) {
            throw UsuarioJaExisteException(req.email, req.cpf)
        }

        Senha.criar(req.senha)

        val senhaHash = criptografia.criptografar(req.senha)
        val usuario = req.toDomain(senhaHash)

        val mecanico = Mecanico.criar(
            usuarioId = usuario.id,
            valorHora = ValorHora(req.valorHora),
            nivel = NivelMecanico.criar(req.nivel)
        )

        usuarioRepository.salvar(usuario)
        mecanicoRepository.salvar(mecanico)

        return MecanicoResDTO.de(usuario, mecanico)
    }
}
