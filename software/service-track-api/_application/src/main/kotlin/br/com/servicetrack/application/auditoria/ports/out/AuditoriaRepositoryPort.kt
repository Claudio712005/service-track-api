package br.com.servicetrack.application.auditoria.ports.out

import br.com.servicetrack.domain.auditoria.Auditoria

interface AuditoriaRepositoryPort {

    fun salvar(auditoria: Auditoria): Auditoria
}