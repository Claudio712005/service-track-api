package br.com.servicetrack.infrastructure.auditoria.persistence

import br.com.servicetrack.application.auditoria.ports.out.AuditoriaRepositoryPort
import br.com.servicetrack.domain.auditoria.Auditoria

class AuditoriaRepositoryAdapter: AuditoriaRepositoryPort {

    override fun salvar(auditoria: Auditoria): Auditoria {
        TODO("Not yet implemented")
    }
}