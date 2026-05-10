package br.com.servicetrack.domain.mecanico

import br.com.servicetrack.domain.mecanico.vo.HorasTrabalho
import br.com.servicetrack.domain.mecanico.vo.NivelMecanico
import br.com.servicetrack.domain.mecanico.vo.ValorHora
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MecanicoTest {

    private fun buildMecanico(nivel: NivelMecanicoEnum = NivelMecanicoEnum.JUNIOR): Mecanico {
        return Mecanico.criar(
            usuarioId = UsuarioId.gerar(),
            valorHora = ValorHora(BigDecimal("100.00")),
            nivel = NivelMecanico.criar(nivel)
        )
    }

    @Test
    fun `deve criar mecânico júnior válido`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.JUNIOR)

        assertNotNull(mecanico)
        assertEquals(NivelMecanicoEnum.JUNIOR, mecanico.obterNivel().valor)
    }

    @Test
    fun `deve criar mecânico com valor por hora correto`() {
        val mecanico = buildMecanico()
        assertEquals(BigDecimal("100.00"), mecanico.obterValorHora().valor)
    }

    @Test
    fun `deve promover mecânico júnior para pleno`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.JUNIOR)
        val promovido = mecanico.promover()
        assertEquals(NivelMecanicoEnum.PLENO, promovido.obterNivel().valor)
    }

    @Test
    fun `deve promover mecânico pleno para sênior`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.PLENO)
        val promovido = mecanico.promover()
        assertEquals(NivelMecanicoEnum.SENIOR, promovido.obterNivel().valor)
    }

    @Test
    fun `deve lançar exceção ao promover mecânico sênior`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.SENIOR)
        val exception = assertThrows<DomainException> {
            mecanico.promover()
        }
        assertEquals("O nível SENIOR não pode ser promovido", exception.message)
    }

    @Test
    fun `promover não deve alterar o mecânico original`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.JUNIOR)
        mecanico.promover()
        assertEquals(NivelMecanicoEnum.JUNIOR, mecanico.obterNivel().valor)
    }

    @Test
    fun `promovido deve manter o mesmo usuarioId`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.JUNIOR)
        val promovido = mecanico.promover()
        assertEquals(mecanico.usuarioId, promovido.usuarioId)
    }

    @Test
    fun `deve calcular custo correto para mecânico júnior (multiplicador 1)`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.JUNIOR)
        val custo = mecanico.calcularCusto(HorasTrabalho(5))
        assertEquals(BigDecimal("500.00"), custo.valor)
    }

    @Test
    fun `deve calcular custo correto para mecânico pleno (multiplicador 2)`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.PLENO)
        val custo = mecanico.calcularCusto(HorasTrabalho(3))
        assertEquals(BigDecimal("600.00"), custo.valor)
    }

    @Test
    fun `deve calcular custo correto para mecânico sênior (multiplicador 3)`() {
        val mecanico = buildMecanico(NivelMecanicoEnum.SENIOR)
        val custo = mecanico.calcularCusto(HorasTrabalho(2))
        assertEquals(BigDecimal("600.00"), custo.valor)
    }

    @Test
    fun `deve lançar exceção ao calcular custo com zero horas`() {
        val mecanico = buildMecanico()
        val exception = assertThrows<DomainException> {
            mecanico.calcularCusto(HorasTrabalho(0))
        }
        assertEquals("Horas de trabalho devem ser maior que zero", exception.message)
    }
}
