package br.com.servicetrack.domain.mecanico.vo

import br.com.servicetrack.domain.mecanico.NivelMecanicoEnum
import br.com.servicetrack.domain.shared.exception.DomainException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class NivelMecanicoTest{

    @Test
    fun `deve criar NivelMecanico válido`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)
        assert(nivel.valor == NivelMecanicoEnum.PLENO)
    }

    @Test
    fun `deve promover júnior para pleno quando nivel válido`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.JUNIOR)

        assertEquals(NivelMecanicoEnum.PLENO, nivel.proximoNivel())
    }

    @Test
    fun `deve promover pleno para senior quando nivel válido`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)

        assertEquals(NivelMecanicoEnum.SENIOR, nivel.proximoNivel())
    }

    @Test
    fun `deve lançar exceção quando tentar promover nível senior`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.SENIOR)

        val exception = assertThrows<DomainException> {
            nivel.proximoNivel()
        }

        assertEquals("O nível SENIOR não pode ser promovido", exception.message)
    }

    @Test
    fun `deve retornar multiplicador correspondente ao nível júnior`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.JUNIOR)

        assertEquals(1, nivel.multiplicador())
    }

    @Test
    fun `deve retornar multiplicador correspondente ao nível pleno`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.PLENO)

        assertEquals(2, nivel.multiplicador())
    }

    @Test
    fun `deve retornar multiplicador correspondente ao nível senior`(){
        val nivel = NivelMecanico.criar(NivelMecanicoEnum.SENIOR)

        assertEquals(3, nivel.multiplicador())
    }
}