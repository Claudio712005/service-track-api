package br.com.servicetrack.application.mecanico.service

import br.com.servicetrack.application.mecanico.dto.response.MecanicoResDTO
import br.com.servicetrack.application.mecanico.ports.`in`.ListarMecanicosUseCase
import br.com.servicetrack.application.mecanico.ports.`out`.MecanicoRepositoryPort
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import br.com.servicetrack.application.usuario.ports.`out`.UsuarioRepositoryPort
import br.com.servicetrack.domain.usuario.vo.UsuarioId

class ListarMecanicosService(
    private val mecanicoRepository: MecanicoRepositoryPort,
    private val usuarioRepository: UsuarioRepositoryPort
) : ListarMecanicosUseCase {

    @Observavel(codigo = CodigoUseCase.MECANICO_LISTAR)

    override fun listarMecanicos(): List<MecanicoResDTO> =
        mecanicoRepository.listarTodos().mapNotNull { mecanico ->
            val usuario = usuarioRepository.buscarPorId(UsuarioId(mecanico.usuarioId.valor))
            usuario?.takeIf { it.estaAtivo() }?.let { MecanicoResDTO.de(it, mecanico) }
        }
}
