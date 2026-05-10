package br.com.servicetrack.application.auditoria.context

object AuditoriaContextoHolder {

    private val contexto = ThreadLocal<Any?>()

    fun registrarAntes(objeto: Any) {
        contexto.set(objeto)
    }

    fun obterAntes(): Any? = contexto.get()

    fun limpar() {
        contexto.remove()
    }
}
