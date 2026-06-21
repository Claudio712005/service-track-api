package br.com.servicetrack.application.auditoria.context

object AuditoriaContextoHolder {

    private val contexto = ThreadLocal<Any?>()

    @Deprecated("Use antesProvider lambda em AuditoriaProxy.envolver()", level = DeprecationLevel.WARNING)
    fun registrarAntes(objeto: Any) {
        contexto.set(objeto)
    }

    fun obterAntes(): Any? = contexto.get()

    fun limpar() {
        contexto.remove()
    }
}
