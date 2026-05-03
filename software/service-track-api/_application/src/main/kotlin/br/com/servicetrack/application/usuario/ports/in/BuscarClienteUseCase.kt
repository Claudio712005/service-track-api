package br.com.servicetrack.application.usuario.ports.`in`

import br.com.servicetrack.application.usuario.dto.response.ClienteResDTO
import br.com.servicetrack.domain.usuario.vo.UsuarioId

interface BuscarClienteUseCase {
    fun buscarCliente(id: UsuarioId): ClienteResDTO
}
