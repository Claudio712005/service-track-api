package br.com.servicetrack.infrastructure.mock

import br.com.servicetrack.application.veiculo.ports.out.UnsplashPort
import io.quarkus.test.Mock
import jakarta.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
class UnsplashPortMock : UnsplashPort {

    override fun buscarImagensVeiculo(marca: String, modelo: String, quantidade: Int): List<String> = listOf(
        "https://images.unsplash.com/photo-mock-1",
        "https://images.unsplash.com/photo-mock-2",
        "https://images.unsplash.com/photo-mock-3"
    )
}
