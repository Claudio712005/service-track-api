package br.com.servicetrack.application.mecanico.ports.out

import br.com.servicetrack.domain.mecanico.Mecanico

interface MecanicoRepositoryPort {

    fun salvar(mecanico: Mecanico)
    fun buscarPorId(usuarioId: String): Mecanico?
}
