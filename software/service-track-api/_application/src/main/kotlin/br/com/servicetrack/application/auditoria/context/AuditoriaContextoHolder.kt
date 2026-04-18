package br.com.servicetrack.application.auditoria.context

/**
 * Holder de contexto por thread para captura do estado anterior (antes) de uma entidade,
 * utilizado em conjunto com o interceptor de auditoria.
 *
 * Services anotados com @Auditavel podem chamar [registrarAntes] antes de modificar a entidade
 * para que o sistema de auditoria produza um diff correto entre o estado anterior e o novo estado.
 */
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
