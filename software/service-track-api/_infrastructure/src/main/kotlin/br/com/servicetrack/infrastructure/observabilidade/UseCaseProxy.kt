package br.com.servicetrack.infrastructure.observabilidade

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.auditoria.context.AuditoriaContextoHolder
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.observabilidade.annotation.Observavel
import br.com.servicetrack.application.observabilidade.enums.CodigoUseCase
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@ApplicationScoped
class UseCaseProxy(
    private val registry: MeterRegistry,
    private val tracer: Tracer,
) {

    private val log = Logger.getLogger(UseCaseProxy::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> envolver(
        instancia: T,
        contrato: Class<T>,
        auditoriaPort: RegistrarAuditoriaPort,
        antesProvider: ((Array<Any?>) -> Any?)? = null,
    ): T = Proxy.newProxyInstance(
        contrato.classLoader,
        arrayOf(contrato),
    ) { _, metodo, args ->
        val argumentos = args ?: emptyArray()
        val metodoImpl = runCatching {
            instancia.javaClass.getMethod(metodo.name, *metodo.parameterTypes)
        }.getOrNull()

        val observavel = metodoImpl?.getAnnotation(Observavel::class.java)
        if (observavel == null) {
            return@newProxyInstance invocar(instancia, metodo, argumentos, metodoImpl, auditoriaPort, antesProvider)
        }

        val codigo = observavel.codigo
        val span = tracer.spanBuilder(codigo.name).setSpanKind(SpanKind.INTERNAL).startSpan()
        val amostra = Timer.start(registry)
        val campos = ExtratorDeCampos.extrair(metodo, argumentos, null)

        comContexto(codigo.name, codigo.entidade.name, campos) {
            log.info("inicio do caso de uso")
        }

        try {
            val resultado = span.makeCurrent().use {
                invocar(instancia, metodo, argumentos, metodoImpl, auditoriaPort, antesProvider)
            }
            val duracao = registrar(amostra, codigo, "sucesso")
            val camposFinais = campos + ExtratorDeCampos.extrair(metodo, argumentos, resultado)
            comContexto(codigo.name, codigo.entidade.name, camposFinais, duracao) {
                log.info("caso de uso concluido")
            }
            span.setStatus(StatusCode.OK)
            resultado
        } catch (erro: Throwable) {
            val duracao = registrar(amostra, codigo, "erro")
            val classificacao = ClassificadorDeErro.classificar(erro)
            span.setStatus(StatusCode.ERROR, classificacao.tipo)
            span.recordException(ClassificadorDeErro.desembrulhar(erro))

            comContexto(codigo.name, codigo.entidade.name, campos, duracao) {
                MDC.put("erro_codigo", "${codigo.name}_FALHA")
                MDC.put("erro_tipo", classificacao.tipo)
                classificacao.mensagem?.let { MDC.put("erro_mensagem", it) }
                if (classificacao.curado) {
                    log.warn("caso de uso recusado por regra de negocio")
                } else {
                    log.error("falha inesperada no caso de uso")
                }
            }
            throw erro
        } finally {
            span.end()
        }
    } as T

    private fun invocar(
        instancia: Any,
        metodo: Method,
        argumentos: Array<Any?>,
        metodoImpl: Method?,
        auditoriaPort: RegistrarAuditoriaPort,
        antesProvider: ((Array<Any?>) -> Any?)?,
    ): Any? {
        if (antesProvider != null) {
            runCatching {
                antesProvider(argumentos)?.let { AuditoriaContextoHolder.registrarAntes(it) }
            }
        }

        val resultado = try {
            metodo.invoke(instancia, *argumentos)
        } catch (e: InvocationTargetException) {
            AuditoriaContextoHolder.limpar()
            throw e.cause ?: e
        } catch (e: Exception) {
            AuditoriaContextoHolder.limpar()
            throw e
        }

        runCatching {
            val auditavel = metodoImpl?.getAnnotation(Auditavel::class.java) ?: return@runCatching
            val referencia = extrairId(resultado) ?: extrairId(argumentos.firstOrNull()) ?: return@runCatching
            auditoriaPort.registrar(
                entidade = auditavel.entidade,
                evento = auditavel.evento,
                referenciaId = referencia,
                antes = AuditoriaContextoHolder.obterAntes(),
                depois = resultado,
            )
        }.also { AuditoriaContextoHolder.limpar() }

        return resultado
    }

    private fun registrar(amostra: Timer.Sample, codigo: CodigoUseCase, resultado: String): Long {
        val entidade = codigo.entidade.name
        val timer = Timer.builder("servicetrack.usecase.duracao")
            .tag("use_case", codigo.name)
            .tag("entidade", entidade)
            .tag("resultado", resultado)
            .register(registry)
        val nanos = amostra.stop(timer)
        registry.counter(
            "servicetrack.usecase.execucoes",
            "use_case", codigo.name,
            "entidade", entidade,
            "resultado", resultado,
        ).increment()
        return nanos / 1_000_000
    }

    private fun comContexto(
        useCase: String,
        entidade: String,
        campos: Map<String, String>,
        duracaoMs: Long? = null,
        acao: () -> Unit,
    ) {
        MDC.put("use_case", useCase)
        MDC.put("entidade", entidade)
        duracaoMs?.let { MDC.put("duracao_ms", it.toString()) }
        campos.forEach { (chave, valor) -> MDC.put(chave, valor) }
        try {
            acao()
        } finally {
            listOf("use_case", "entidade", "duracao_ms", "erro_codigo", "erro_tipo", "erro_mensagem")
                .forEach { MDC.remove(it) }
            campos.keys.forEach { MDC.remove(it) }
        }
    }

    private fun extrairId(alvo: Any?): String? {
        if (alvo == null) return null
        if (alvo is String) return alvo
        return runCatching {
            listOf("id", "usuarioId", "valor").firstNotNullOfOrNull { nome ->
                runCatching {
                    val campo = alvo.javaClass.getDeclaredField(nome)
                    campo.isAccessible = true
                    campo.get(alvo)?.toString()
                }.getOrNull()
            }
        }.getOrNull()
    }
}
