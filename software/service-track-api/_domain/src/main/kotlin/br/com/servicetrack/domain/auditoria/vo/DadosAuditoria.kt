package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.auditoria.CampoAlterado
import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.time.temporal.Temporal

class DadosAuditoria(
    val alteracoes: List<CampoAlterado<*>>?,
) {
    companion object {

        fun criacao(depois: Any): DadosAuditoria {
            val campos = extrairCampos(fonte = depois, incluirAntes = false)
            return DadosAuditoria(campos)
        }

        fun atualizacao(antes: Any, depois: Any): DadosAuditoria {
            val campos = extrairDiferencas(antes, depois)
            return DadosAuditoria(campos)
        }

        fun atualizacaoSemAntes(depois: Any): DadosAuditoria {
            val campos = extrairCampos(fonte = depois, incluirAntes = false)
            return DadosAuditoria(campos)
        }

        fun remocao(antes: Any): DadosAuditoria {
            val campos = extrairCampos(fonte = antes, incluirAntes = true)
            return DadosAuditoria(campos)
        }

        fun remocaoSemEstado(): DadosAuditoria = DadosAuditoria(
            listOf(CampoAlterado(campo = "estado", valorAntes = "ATIVO", valorDepois = "REMOVIDO", tipo = TipoDadoAuditoria.STRING))
        )

        fun evento(): DadosAuditoria = DadosAuditoria(null)

        private fun extrairCampos(fonte: Any, incluirAntes: Boolean): List<CampoAlterado<*>> {
            return fonte.javaClass.declaredFields
                .filter { !Modifier.isStatic(it.modifiers) }
                .map { field ->
                    field.isAccessible = true
                    val valor = runCatching { field.get(fonte) }.getOrNull()
                    val tipo = resolverTipo(valor)
                    if (incluirAntes)
                        CampoAlterado(campo = field.name, valorAntes = valor, valorDepois = null, tipo = tipo)
                    else
                        CampoAlterado(campo = field.name, valorAntes = null, valorDepois = valor, tipo = tipo)
                }
        }

        private fun extrairDiferencas(antes: Any, depois: Any): List<CampoAlterado<*>> {
            return antes.javaClass.declaredFields
                .filter { !Modifier.isStatic(it.modifiers) }
                .mapNotNull { field ->
                    field.isAccessible = true
                    val valorAntes = runCatching { field.get(antes) }.getOrNull()
                    val valorDepois = runCatching { field.get(depois) }.getOrNull()
                    if (valorAntes == valorDepois) null
                    else CampoAlterado(
                        campo = field.name,
                        valorAntes = valorAntes,
                        valorDepois = valorDepois,
                        tipo = resolverTipo(valorAntes ?: valorDepois),
                    )
                }
        }

        private fun resolverTipo(valor: Any?): TipoDadoAuditoria = when (valor) {
            is String -> TipoDadoAuditoria.STRING
            is Int, is Long, is Short, is Byte -> TipoDadoAuditoria.INTEGER
            is Float, is Double, is BigDecimal -> TipoDadoAuditoria.DECIMAL
            is Boolean -> TipoDadoAuditoria.BOOLEAN
            is Temporal -> TipoDadoAuditoria.DATA
            is Enum<*> -> TipoDadoAuditoria.ENUM
            is Collection<*>, is Array<*> -> TipoDadoAuditoria.ARRAY
            null -> TipoDadoAuditoria.STRING
            else -> TipoDadoAuditoria.OBJETO
        }
    }

    fun temAlteracoes(): Boolean = alteracoes == null || alteracoes.isNotEmpty()
}
