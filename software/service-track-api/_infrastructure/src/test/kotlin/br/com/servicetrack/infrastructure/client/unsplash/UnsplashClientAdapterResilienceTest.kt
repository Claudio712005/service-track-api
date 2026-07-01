package br.com.servicetrack.infrastructure.client.unsplash

import br.com.servicetrack.infrastructure.client.fipe.FaultToleranceEnabledProfile
import br.com.servicetrack.infrastructure.client.unsplash.dto.UnsplashBuscaResponse
import br.com.servicetrack.infrastructure.client.unsplash.dto.UnsplashFotoResponse
import br.com.servicetrack.infrastructure.client.unsplash.dto.UnsplashUrlsResponse
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@QuarkusTest
@TestProfile(FaultToleranceEnabledProfile::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UnsplashClientAdapterResilienceTest {

    @InjectMock
    @RestClient
    lateinit var unsplashClient: UnsplashClient

    @Inject
    lateinit var unsplashAdapter: UnsplashClientAdapter

    private val respostaMock = UnsplashBuscaResponse(
        resultados = listOf(
            UnsplashFotoResponse(urls = UnsplashUrlsResponse(regular = "https://img.unsplash.com/photo1", small = "https://img.unsplash.com/photo1-s", full = "https://img.unsplash.com/photo1-f")),
            UnsplashFotoResponse(urls = UnsplashUrlsResponse(regular = "https://img.unsplash.com/photo2", small = "https://img.unsplash.com/photo2-s", full = "https://img.unsplash.com/photo2-f")),
        ),
        total = 2,
        totalPaginas = 1,
    )

    @BeforeEach
    fun setup() {
        org.mockito.Mockito.reset(unsplashClient)
    }

    @Test
    @Order(1)
    fun `deve retornar imagens na primeira tentativa`() {
        `when`(unsplashClient.buscarFotos(anyString(), anyInt(), anyString()))
            .thenReturn(respostaMock)

        val resultado = unsplashAdapter.buscarImagensVeiculo("Fiat", "Uno", 2)

        assertEquals(2, resultado.size)
        assertEquals("https://img.unsplash.com/photo1", resultado[0])
        verify(unsplashClient, times(1)).buscarFotos(anyString(), anyInt(), anyString())
    }

    @Test
    @Order(2)
    fun `deve fazer retry e suceder na segunda tentativa`() {
        `when`(unsplashClient.buscarFotos(anyString(), anyInt(), anyString()))
            .thenThrow(RuntimeException("Connection timeout"))
            .thenReturn(respostaMock)

        val resultado = unsplashAdapter.buscarImagensVeiculo("Fiat", "Uno", 2)

        assertEquals(2, resultado.size)
        verify(unsplashClient, times(2)).buscarFotos(anyString(), anyInt(), anyString())
    }

    @Test
    @Order(3)
    fun `deve retornar lista vazia via fallback apos esgotar retries`() {
        `when`(unsplashClient.buscarFotos(anyString(), anyInt(), anyString()))
            .thenThrow(RuntimeException("Service unavailable"))

        val resultado = unsplashAdapter.buscarImagensVeiculo("Fiat", "Uno", 2)

        assertTrue(resultado.isEmpty())
    }

    @Test
    @Order(4)
    fun `deve retornar lista vazia apos circuito abrir por falhas consecutivas`() {
        `when`(unsplashClient.buscarFotos(anyString(), anyInt(), anyString()))
            .thenThrow(RuntimeException("Service unavailable"))

        repeat(5) {
            val resultado = unsplashAdapter.buscarImagensVeiculo("Fiat", "Uno", 2)
            assertTrue(resultado.isEmpty())
        }

        val resultado = unsplashAdapter.buscarImagensVeiculo("Fiat", "Uno", 2)
        assertTrue(resultado.isEmpty())
    }
}
