package br.com.servicetrack.infrastructure.observability.metrics

import io.micrometer.core.instrument.config.MeterFilter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class CommonMetricsTagsConfig {

    @Produces
    @Singleton
    fun commonMetricsTags(
        @ConfigProperty(name = "quarkus.application.name", defaultValue = "service-track-api")
        application: String,
        @ConfigProperty(name = "servicetrack.observability.environment", defaultValue = "local")
        environment: String,
    ): MeterFilter =
        MeterFilter.commonTags(
            listOf(
                io.micrometer.core.instrument.Tag.of("application", application),
                io.micrometer.core.instrument.Tag.of("environment", environment),
            )
        )
}
