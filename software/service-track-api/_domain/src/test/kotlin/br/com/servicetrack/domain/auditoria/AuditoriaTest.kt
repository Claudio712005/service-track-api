package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.enums.TipoDadoAuditoria
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.vo.DadosAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class AuditoriaTest {

    private fun buildDadosComAlteracoes(): DadosAuditoria {
        val campo = CampoAlterado("nome", "João", "Maria", TipoDadoAuditoria.STRING)
        return DadosAuditoria(listOf(campo))
    }

    private fun buildAuditoria(dados: DadosAuditoria = buildDadosComAlteracoes()): Auditoria {
        return Auditoria.registrar(
            enderecoIp = EnderecoIp.criar("192.168.0.1"),
            referenciaId = ReferenciaId.gerar(),
            eventoAuditoria = EventoAuditoria.criacao(TipoEntidade.CLIENTE),
            dados = dados,
            responsavelAcao = UsuarioId.gerar()
        )
    }

    @Test
    fun `deve registrar auditoria com dados válidos`() {
        val auditoria = buildAuditoria()
        assertNotNull(auditoria)
    }

    @Test
    fun `deve lançar exceção ao registrar auditoria sem alterações`() {
        val dadosVazios = DadosAuditoria(emptyList())

        val exception = assertThrows<DomainException> {
            buildAuditoria(dadosVazios)
        }
        assertEquals("Auditoria deve conter alterações", exception.message)
    }

    @Test
    fun `deve registrar auditoria com evento de atualizacao`() {
        val campo = CampoAlterado("status", "ATIVO", "INATIVO", TipoDadoAuditoria.ENUM)
        val dados = DadosAuditoria(listOf(campo))

        val auditoria = Auditoria.registrar(
            enderecoIp = EnderecoIp.criar("10.0.0.1"),
            referenciaId = ReferenciaId.gerar(),
            eventoAuditoria = EventoAuditoria.atualizado(TipoEntidade.VEICULO),
            dados = dados,
            responsavelAcao = UsuarioId.gerar()
        )
        assertNotNull(auditoria)
    }

    @Test
    fun `deve registrar auditoria com múltiplos campos alterados`() {
        val campos = listOf(
            CampoAlterado("nome", "João", "Maria", TipoDadoAuditoria.STRING),
            CampoAlterado("email", "joao@email.com", "maria@email.com", TipoDadoAuditoria.STRING),
            CampoAlterado("ativo", true, false, TipoDadoAuditoria.BOOLEAN),
        )
        val dados = DadosAuditoria(campos)

        val auditoria = buildAuditoria(dados)
        assertNotNull(auditoria)
    }
}
