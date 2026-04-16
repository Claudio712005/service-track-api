package br.com.servicetrack.application.mecanico.mapper

import br.com.servicetrack.application.mecanico.dto.request.CadastrarMecanicoReqDTO
import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class MecanicoMapper {

    @Test
    fun `deve mapear corretamente para domain quando valores válidos`() {
        val req = CadastrarMecanicoReqDTO(
            nome = "Cláudio da Silva Araújo Filho",
            email = "email@email.com",
            senha = "#12345Teste",
            telefone = "11111111111",
            cpf = "14716682072",
            dataNascimento = LocalDate.of(2005, 1, 7),
            nivel = NivelMecanicoEnum.PLENO,
            valorHora = "50.00".toBigDecimal()
        )

        assertDoesNotThrow {
            req.toDomain("hash")
        }
    }

    @Test
    fun `deve lançar exceção para CPF inválido`() {
        val req = CadastrarMecanicoReqDTO(
            nome = "Cláudio da Silva Araújo Filho",
            email = "email@email.com",
            senha = "#12345Teste",
            telefone = "11111111111",
            cpf = "1471662ttt",
            dataNascimento = LocalDate.of(2005, 1, 7),
            nivel = NivelMecanicoEnum.PLENO,
            valorHora = "50.00".toBigDecimal()
        )

        val exception = assertThrows<DomainException> {
            req.toDomain("hash")
        }

        assertEquals("CPF deve conter 11 dígitos", exception.message)
    }
}