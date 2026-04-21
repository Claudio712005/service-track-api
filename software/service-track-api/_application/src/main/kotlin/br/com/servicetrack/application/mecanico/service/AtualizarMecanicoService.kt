package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.exception.EntidadeNaoEncontradaException
import br.com.servicetrack.application.exception.OperacaoNegadaException
import br.com.servicetrack.application.mecanico.dto.request.AtualizarMecanicoReqDTO
import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.application.mecanico.ports.`in`.AtualizarMecanicoUseCase
import br.com.servicetrack.application.mecanico.ports.out.MecanicoRepositoryPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.application.usuario.ports.out.UsuarioRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.mecanico.Mecanico
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class AtualizarMecanicoService(
    private val repository: MecanicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort,
    private val jwt: JwtPort
) : AtualizarMecanicoUseCase {

    @Auditavel(entidade = TipoEntidade.MECANICO, evento = TipoEventoAuditoria.ATUALIZADO)
    override fun atualizarMecanico(id: String, req: AtualizarMecanicoReqDTO): MecanicoResDTO {
        val idUsuarioLogado = jwt.getUsuarioId()

        if (idUsuarioLogado.valor == id) {
            throw OperacaoNegadaException("atualização de mecânico", "Mecânico não pode atualizar a si mesmo")
        }

        val mecanicoAlvo = repository.buscarPorId(id)
            ?: throw EntidadeNaoEncontradaException("Mecânico", arrayOf(id))

        val mecanicoLogado = repository.buscarPorId(idUsuarioLogado.valor)
            ?: throw EntidadeNaoEncontradaException("Mecânico", arrayOf(idUsuarioLogado.valor))

        if (mecanicoLogado.obterNivel().valor != NivelMecanicoEnum.SENIOR) {
            throw OperacaoNegadaException(
                "atualização de mecânico",
                "Apenas mecânicos com nível Sênior podem atualizar outros mecânicos"
            )
        }

        AuditoriaContextoHolder.registrarAntes(mecanicoAlvo)

        val mecanicoAtualizado = Mecanico.criar(
            UsuarioId(id),
            ValorHora(req.valorHora),
            NivelMecanico.criar(req.nivel)
        )

        val mecanicoPersistido = repository.atualizar(mecanicoAtualizado)
            ?: throw EntidadeNaoEncontradaException("Mecânico", arrayOf(id))

        val usuario = usuarioRepository.buscarPorId(mecanicoPersistido.usuarioId)
            ?: throw EntidadeNaoEncontradaException("Usuário", arrayOf(id))

        return MecanicoResDTO.de(usuario, mecanicoPersistido)
    }
}
