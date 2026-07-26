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
    ): OtlpMeterRegistry {
        val config = object : OtlpConfig {
            override fun get(key: String): String? = null
            override fun url(): String = metricsUrl
            override fun step(): Duration = Duration.ofSeconds(stepSeconds)
        }
        return OtlpMeterRegistry(config, Clock.SYSTEM)
    }
}
