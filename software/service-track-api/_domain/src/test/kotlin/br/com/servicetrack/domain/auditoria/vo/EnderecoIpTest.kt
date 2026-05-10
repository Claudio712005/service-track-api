package br.com.servicetrack.domain.auditoria.vo

import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EnderecoIpTest {

    @Test
    fun `deve criar EnderecoIp com endereço IPv4 válido`() {
        val ip = EnderecoIp.criar("192.168.0.1")
        assertEquals("192.168.0.1", ip.value)
    }

    @Test
    fun `deve criar EnderecoIp com endereço de loopback`() {
        val ip = EnderecoIp.criar("127.0.0.1")
        assertEquals("127.0.0.1", ip.value)
    }

    @Test
    fun `deve criar EnderecoIp com octetos nos limites máximos`() {
        val ip = EnderecoIp.criar("255.255.255.255")
        assertEquals("255.255.255.255", ip.value)
    }

    @Test
    fun `deve criar EnderecoIp com octetos zerados`() {
        val ip = EnderecoIp.criar("0.0.0.0")
        assertEquals("0.0.0.0", ip.value)
    }

    @Test
    fun `deve lançar exceção com endereço IP com octeto acima de 255`() {
        val exception = assertThrows<DomainException> {
            EnderecoIp.criar("256.0.0.1")
        }
        assertEquals("Endereço IP inválido: 256.0.0.1", exception.message)
    }

    @Test
    fun `deve lançar exceção com endereço IP com formato incorreto`() {
        assertThrows<DomainException> {
            EnderecoIp.criar("192.168.0")
        }
    }

    @Test
    fun `deve lançar exceção com endereço IP com cinco octetos`() {
        assertThrows<DomainException> {
            EnderecoIp.criar("192.168.0.1.1")
        }
    }

    @Test
    fun `deve lançar exceção com endereço IP com caracteres alfabéticos`() {
        assertThrows<DomainException> {
            EnderecoIp.criar("abc.def.ghi.jkl")
        }
    }

    @Test
    fun `deve lançar exceção com endereço IP vazio`() {
        assertThrows<DomainException> {
            EnderecoIp.criar("")
        }
    }

    @Test
    fun `deve lançar exceção com endereço IP com espaços`() {
        assertThrows<DomainException> {
            EnderecoIp.criar("192.168. 0.1")
        }
    }
}
