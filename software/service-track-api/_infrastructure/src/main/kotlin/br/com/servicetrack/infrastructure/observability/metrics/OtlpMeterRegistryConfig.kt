package br.com.servicetrack.infrastructure.observability.metrics

import io.micrometer.core.instrument.Clock
import io.micrometer.registry.otlp.OtlpConfig
import io.micrometer.registry.otlp.OtlpMeterRegistry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration

@ApplicationScoped
class OtlpMeterRegistryConfig {

    @Produces
    @Singleton
    fun otlpMeterRegistry(
        @ConfigProperty(name = "servicetrack.observability.otlp.metrics-url", defaultValue = "http://localhost:4318/v1/metrics")
        metricsUrl: String,
        @ConfigProperty(name = "servicetrack.observability.otlp.step-seconds", defaultValue = "15")
        stepSeconds: Long,
        @ConfigProperty(name = "quarkus.application.name", defaultValue = "service-track-api")
        nomeDoServico: String,
        @ConfigProperty(name = "servicetrack.observability.service-namespace", defaultValue = "servicetrack")
        namespaceDoServico: String,
        @ConfigProperty(name = "servicetrack.observability.environment", defaultValue = "local")
        ambiente: String,
    ): OtlpMeterRegistry {
        val config = object : OtlpConfig {
            override fun get(key: String): String? = null
            override fun url(): String = metricsUrl
            override fun step(): Duration = Duration.ofSeconds(stepSeconds)
            override fun resourceAttributes(): Map<String, String> = mapOf(
                "service.name" to nomeDoServico,
                "service.namespace" to namespaceDoServico,
                "deployment.environment" to ambiente,
            )
        }
        return OtlpMeterRegistry(config, Clock.SYSTEM)
    }
}
