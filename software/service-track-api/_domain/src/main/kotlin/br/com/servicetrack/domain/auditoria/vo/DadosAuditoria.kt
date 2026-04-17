package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.auditoria.Auditoria
import br.com.servicetrack.domain.auditoria.CampoAlterado

class DadosAuditoria(
    val alteracoes: List<CampoAlterado<*>>,
) {
    companion object {
        fun criacao(depois: Any): DadosAuditoria {
            return DadosAuditoria(
                alteracoes =
            )
        }
    }

    fun temAlteracoes(): Boolean {
        return this.alteracoes.isNotEmpty()
    }
}