package br.com.servicetrack.infrastructure.auditoria.persistence

import br.com.servicetrack.application.auditoria.ports.out.AuditoriaRepositoryPort
import br.com.servicetrack.domain.auditoria.Auditoria
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class AuditoriaRepositoryAdapter(
    private val objectMapper: ObjectMapper,
) : AuditoriaRepositoryPort {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun salvar(auditoria: Auditoria): Auditoria {
        val dadosJson = serializarDados(auditoria)
        AuditoriaEntity.de(auditoria, dadosJson).persist()
        return auditoria
    }

    private fun serializarDados(auditoria: Auditoria): String {
        val lista = auditoria.dados.alteracoes?.map { campo ->
            mapOf(
                "campo" to campo.campo,
                "valorAntes" to campo.valorAntes?.toString(),
                "valorDepois" to campo.valorDepois?.toString(),
                "tipo" to campo.tipo.name,
            )
        }
        return objectMapper.writeValueAsString(lista)
    }
}
