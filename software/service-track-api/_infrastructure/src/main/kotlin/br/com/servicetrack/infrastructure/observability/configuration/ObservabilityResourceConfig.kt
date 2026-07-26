package br.com.servicetrack.infrastructure.observability.configuration

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.resources.Resource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class ObservabilityResourceConfig {

    @Produces
    @ApplicationScoped
    fun observabilityResource(
        @ConfigProperty(name = "servicetrack.observability.service-namespace", defaultValue = "servicetrack")
        serviceNamespace: String,
        @ConfigProperty(name = "servicetrack.observability.environment", defaultValue = "local")
        environment: String,
    ): Resource =
        Resource.create(
            Attributes.builder()
                .put(AttributeKey.stringKey("service.namespace"), serviceNamespace)
                .put(AttributeKey.stringKey("deployment.environment.name"), environment)
                .build()
        )
}
