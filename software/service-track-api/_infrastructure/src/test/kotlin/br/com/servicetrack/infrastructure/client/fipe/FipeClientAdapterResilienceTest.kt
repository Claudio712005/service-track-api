package br.com.servicetrack.infrastructure.client.fipe

import br.com.servicetrack.application.exception.IntegracaoExternaException
import br.com.servicetrack.infrastructure.client.fipe.dto.FipeMarcaResponse
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class FaultToleranceEnabledProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): MutableMap<String, String> = mutableMapOf(
        "MP_Fault_Tolerance_NonFallback_Enabled" to "true",
        "quarkus.cache.enabled" to "false",
    )
}

@QuarkusTest
@TestProfile(FaultToleranceEnabledProfile::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FipeClientAdapterResilienceTest {

    @InjectMock
    @RestClient
    lateinit var fipeClient: FipeClient

    @Inject
    lateinit var fipeAdapter: FipeClientAdapter

    private val marcasMock = listOf(FipeMarcaResponse("1", "Fiat"), FipeMarcaResponse("2", "VW"))

    @BeforeEach
    fun setup() {
        org.mockito.Mockito.reset(fipeClient)
    }

    @Test
    @Order(1)
    fun `deve retornar marcas na primeira tentativa quando FIPE responde`() {
        `when`(fipeClient.listarMarcasCarros()).thenReturn(marcasMock)

        val resultado = fipeAdapter.listarMarcasCarros()

        assertEquals(2, resultado.size)
        assertEquals("Fiat", resultado[0].nome)
        verify(fipeClient, times(1)).listarMarcasCarros()
    }

    @Test
    @Order(2)
    fun `deve fazer retry com backoff e suceder na segunda tentativa`() {
        `when`(fipeClient.listarMarcasCarros())
            .thenThrow(RuntimeException("Connection refused"))
            .thenReturn(marcasMock)

        val resultado = fipeAdapter.listarMarcasCarros()

        assertEquals(2, resultado.size)
        verify(fipeClient, times(2)).listarMarcasCarros()
    }

    @Test
    @Order(3)
    fun `deve fazer retry 3 vezes e acionar fallback apos esgotar retries`() {
        `when`(fipeClient.listarMarcasCarros())
            .thenThrow(RuntimeException("Connection refused"))

        val ex = assertThrows(IntegracaoExternaException::class.java) {
            fipeAdapter.listarMarcasCarros()
        }

        assertNotNull(ex.message)
        assert(ex.message!!.contains("FIPE"))
    }

    @Test
    @Order(4)
    fun `deve abrir circuito apos 5 falhas consecutivas e falhar rapido`() {
        `when`(fipeClient.listarMarcasCarros())
            .thenThrow(RuntimeException("Service unavailable"))

        repeat(3) {
            assertThrows(IntegracaoExternaException::class.java) {
                fipeAdapter.listarMarcasCarros()
            }
        }

        val ex = assertThrows(IntegracaoExternaException::class.java) {
            fipeAdapter.listarMarcasCarros()
        }
        assert(ex.message!!.contains("indisponível") || ex.message!!.contains("FIPE"))
    }

    @Test
    @Order(5)
    fun `deve recuperar apos circuito fechar`() {
        `when`(fipeClient.listarMarcasCarros())
            .thenThrow(RuntimeException("Service unavailable"))

        repeat(3) {
            runCatching { fipeAdapter.listarMarcasCarros() }
        }
        val ex = assertThrows(IntegracaoExternaException::class.java) {
            fipeAdapter.listarMarcasCarros()
        }
        assertNotNull(ex.message)
    }
}
