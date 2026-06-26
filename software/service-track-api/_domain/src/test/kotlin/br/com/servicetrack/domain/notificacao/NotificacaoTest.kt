package br.com.servicetrack.domain.notificacao

import br.com.servicetrack.domain.notificacao.vo.AssuntoNotificacao
import br.com.servicetrack.domain.notificacao.vo.DescricaoNotificacao
import br.com.servicetrack.domain.notificacao.vo.NotificacaoId
import br.com.servicetrack.domain.notificacao.vo.TituloNotificacao
import br.com.servicetrack.domain.notificacao.vo.VariaveisTemplate
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NotificacaoTest {

    private fun usuario() = UsuarioId.gerar()

    private fun build(
        destinatario: UsuarioId = usuario(),
        copias: List<UsuarioId> = emptyList(),
    ): Notificacao = Notificacao.gerar(
        assunto = AssuntoNotificacao("Atualização da OS"),
        titulo = TituloNotificacao("Sua OS foi atualizada"),
        descricao = DescricaoNotificacao("A ordem de serviço mudou de status."),
        variaveis = VariaveisTemplate.de(mapOf("os" to "123")),
        tipoNotificacao = TipoNotificacao.EMAIL,
        tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
        destinatario = destinatario,
        copias = copias,
    )

    @Test
    fun `deve gerar notificacao com estado inicial PENDENTE`() {
        val n = build()

        assertNotNull(n.id)
        assertEquals(StatusEnvio.PENDENTE, n.statusEnvio)
        assertNull(n.dataEnvio)
        assertFalse(n.visualizada)
        assertNull(n.dataVisualizacao)
        assertNotNull(n.dataCriacao)
        assertTrue(n.copias.isEmpty())
    }

    @Test
    fun `deve gerar notificacao com copias`() {
        val cc1 = usuario()
        val cc2 = usuario()
        val n = build(copias = listOf(cc1, cc2))

        assertEquals(listOf(cc1, cc2), n.copias)
    }

    @Test
    fun `deve realizar copia defensiva da lista de copias`() {
        val cc = usuario()
        val origem = mutableListOf(cc)
        val n = Notificacao.gerar(
            assunto = AssuntoNotificacao("a"),
            titulo = TituloNotificacao("t"),
            descricao = DescricaoNotificacao("d"),
            variaveis = VariaveisTemplate.VAZIO,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = usuario(),
            copias = origem,
        )

        origem.add(usuario())

        assertEquals(1, n.copias.size)
    }

    @Test
    fun `deve lancar excecao quando destinatario estiver em copias`() {
        val dest = usuario()
        val ex = assertThrows<IllegalArgumentException> {
            build(destinatario = dest, copias = listOf(dest))
        }
        assertEquals("destinatário não pode também estar em copias", ex.message)
    }

    @Test
    fun `deve lancar excecao quando copias possuir duplicatas`() {
        val cc = usuario()
        val ex = assertThrows<IllegalArgumentException> {
            build(copias = listOf(cc, cc))
        }
        assertEquals("copias não pode conter UsuarioId duplicado", ex.message)
    }

    @Test
    fun `deve marcar como enviada definindo dataEnvio`() {
        val n = build()

        n.marcarComoEnviada()

        assertEquals(StatusEnvio.ENVIADA, n.statusEnvio)
        assertNotNull(n.dataEnvio)
    }

    @Test
    fun `marcarComoEnviada deve ser idempotente preservando dataEnvio`() {
        val n = build()
        n.marcarComoEnviada()
        val primeiraData = n.dataEnvio

        n.marcarComoEnviada()

        assertEquals(StatusEnvio.ENVIADA, n.statusEnvio)
        assertSame(primeiraData, n.dataEnvio)
    }

    @Test
    fun `deve lancar excecao ao marcar como enviada apos falha de envio`() {
        val n = build()
        n.marcarFalhaEnvio()

        val ex = assertThrows<DomainException> { n.marcarComoEnviada() }
        assertEquals(
            "Notificação com falha de envio não pode ser marcada como enviada sem reenvio",
            ex.message,
        )
    }

    @Test
    fun `deve marcar falha de envio quando pendente`() {
        val n = build()

        n.marcarFalhaEnvio()

        assertEquals(StatusEnvio.FALHA_ENVIO, n.statusEnvio)
        assertNull(n.dataEnvio)
    }

    @Test
    fun `deve lancar excecao ao marcar falha em notificacao ja enviada`() {
        val n = build()
        n.marcarComoEnviada()

        val ex = assertThrows<DomainException> { n.marcarFalhaEnvio() }
        assertEquals("Notificação já enviada não pode ser marcada como falha", ex.message)
    }

    @Test
    fun `deve visualizar notificacao apos envio`() {
        val n = build()
        n.marcarComoEnviada()

        n.visualizar()

        assertTrue(n.visualizada)
        assertNotNull(n.dataVisualizacao)
    }

    @Test
    fun `visualizar deve ser idempotente preservando dataVisualizacao`() {
        val n = build()
        n.marcarComoEnviada()
        n.visualizar()
        val primeira = n.dataVisualizacao

        n.visualizar()

        assertTrue(n.visualizada)
        assertSame(primeira, n.dataVisualizacao)
    }

    @Test
    fun `deve lancar excecao ao visualizar notificacao pendente`() {
        val n = build()

        val ex = assertThrows<DomainException> { n.visualizar() }
        assertEquals("Só é possível visualizar uma notificação ENVIADA", ex.message)
    }

    @Test
    fun `deve lancar excecao ao visualizar notificacao com falha de envio`() {
        val n = build()
        n.marcarFalhaEnvio()

        assertThrows<DomainException> { n.visualizar() }
    }

    @Test
    fun `deve considerar igualdade apenas pelo id`() {
        val n1 = build()
        val n2 = build()

        assertNotEquals(n1, n2)
        assertEquals(n1, n1)
        assertEquals(n1.hashCode(), n1.hashCode())
    }

    @Test
    fun `notificacoes reconstruidas com mesmo id devem ser iguais`() {
        val id = NotificacaoId.gerar()
        val agora = LocalDateTime.now()
        val dest = usuario()

        val a = Notificacao.restaurar(
            id = id,
            assunto = AssuntoNotificacao("a"),
            titulo = TituloNotificacao("t"),
            descricao = DescricaoNotificacao("d"),
            variaveis = VariaveisTemplate.VAZIO,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = dest,
            copias = emptyList(),
            dataCriacao = agora,
            statusEnvio = StatusEnvio.PENDENTE,
            dataEnvio = null,
            visualizada = false,
            dataVisualizacao = null,
            tentativasEnvio = 0,
            ultimoErro = null,
        )
        val b = Notificacao.restaurar(
            id = id,
            assunto = AssuntoNotificacao("outro"),
            titulo = TituloNotificacao("outro"),
            descricao = DescricaoNotificacao("outro"),
            variaveis = VariaveisTemplate.VAZIO,
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = dest,
            copias = emptyList(),
            dataCriacao = agora,
            statusEnvio = StatusEnvio.ENVIADA,
            dataEnvio = agora,
            visualizada = false,
            dataVisualizacao = null,
            tentativasEnvio = 1,
            ultimoErro = "diferente",
        )

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `deve restaurar notificacao preservando todos os campos`() {
        val id = NotificacaoId.gerar()
        val dest = usuario()
        val criacao = LocalDateTime.now().minusHours(2)
        val envio = LocalDateTime.now().minusHours(1)
        val visualizacao = LocalDateTime.now()

        val n = Notificacao.restaurar(
            id = id,
            assunto = AssuntoNotificacao("Assunto"),
            titulo = TituloNotificacao("Título"),
            descricao = DescricaoNotificacao("Descrição"),
            variaveis = VariaveisTemplate.de(mapOf("x" to "1")),
            tipoNotificacao = TipoNotificacao.EMAIL,
            tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
            destinatario = dest,
            copias = emptyList(),
            dataCriacao = criacao,
            statusEnvio = StatusEnvio.ENVIADA,
            dataEnvio = envio,
            visualizada = true,
            dataVisualizacao = visualizacao,
            tentativasEnvio = 2,
            ultimoErro = "timeout SMTP",
        )

        assertEquals(id, n.id)
        assertEquals(criacao, n.dataCriacao)
        assertEquals(envio, n.dataEnvio)
        assertEquals(visualizacao, n.dataVisualizacao)
        assertTrue(n.visualizada)
        assertEquals(StatusEnvio.ENVIADA, n.statusEnvio)
        assertEquals(2, n.tentativasEnvio)
        assertEquals("timeout SMTP", n.ultimoErro)
    }

    @Test
    fun `deve lancar excecao ao restaurar ENVIADA sem dataEnvio`() {
        val ex = assertThrows<IllegalArgumentException> {
            Notificacao.restaurar(
                id = NotificacaoId.gerar(),
                assunto = AssuntoNotificacao("a"),
                titulo = TituloNotificacao("t"),
                descricao = DescricaoNotificacao("d"),
                variaveis = VariaveisTemplate.VAZIO,
                tipoNotificacao = TipoNotificacao.EMAIL,
                tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
                destinatario = usuario(),
                copias = emptyList(),
                dataCriacao = LocalDateTime.now(),
                statusEnvio = StatusEnvio.ENVIADA,
                dataEnvio = null,
                visualizada = false,
                dataVisualizacao = null,
                tentativasEnvio = 0,
                ultimoErro = null,
            )
        }
        assertEquals("notificação ENVIADA deve possuir dataEnvio", ex.message)
    }

    @Test
    fun `deve lancar excecao ao restaurar visualizada sem dataVisualizacao`() {
        val agora = LocalDateTime.now()
        val ex = assertThrows<IllegalArgumentException> {
            Notificacao.restaurar(
                id = NotificacaoId.gerar(),
                assunto = AssuntoNotificacao("a"),
                titulo = TituloNotificacao("t"),
                descricao = DescricaoNotificacao("d"),
                variaveis = VariaveisTemplate.VAZIO,
                tipoNotificacao = TipoNotificacao.EMAIL,
                tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
                destinatario = usuario(),
                copias = emptyList(),
                dataCriacao = agora,
                statusEnvio = StatusEnvio.ENVIADA,
                dataEnvio = agora,
                visualizada = true,
                dataVisualizacao = null,
                tentativasEnvio = 0,
                ultimoErro = null,
            )
        }
        assertEquals("notificação visualizada deve possuir dataVisualizacao", ex.message)
    }

    @Test
    fun `deve lancar excecao ao restaurar visualizada com status nao ENVIADA`() {
        val agora = LocalDateTime.now()
        val ex = assertThrows<IllegalArgumentException> {
            Notificacao.restaurar(
                id = NotificacaoId.gerar(),
                assunto = AssuntoNotificacao("a"),
                titulo = TituloNotificacao("t"),
                descricao = DescricaoNotificacao("d"),
                variaveis = VariaveisTemplate.VAZIO,
                tipoNotificacao = TipoNotificacao.EMAIL,
                tipoConteudoNotificacao = TipoConteudoNotificacao.MUDANCA_STATUS_OS,
                destinatario = usuario(),
                copias = emptyList(),
                dataCriacao = agora,
                statusEnvio = StatusEnvio.PENDENTE,
                dataEnvio = null,
                visualizada = true,
                dataVisualizacao = agora,
                tentativasEnvio = 0,
                ultimoErro = null,
            )
        }
        assertEquals("só é possível visualizar uma notificação ENVIADA", ex.message)
    }

    @Test
    fun `deve iniciar com tentativasEnvio zero e ultimoErro nulo`() {
        val n = build()
        assertEquals(0, n.tentativasEnvio)
        assertEquals(null, n.ultimoErro)
    }

    @Test
    fun `deve incrementar tentativas e manter PENDENTE abaixo do limite`() {
        val n = build()

        n.registrarTentativaFalha("timeout", maxTentativas = 3)

        assertEquals(1, n.tentativasEnvio)
        assertEquals("timeout", n.ultimoErro)
        assertEquals(StatusEnvio.PENDENTE, n.statusEnvio)
    }

    @Test
    fun `deve transitar para FALHA_ENVIO ao atingir maxTentativas`() {
        val n = build()

        n.registrarTentativaFalha("erro 1", maxTentativas = 3)
        n.registrarTentativaFalha("erro 2", maxTentativas = 3)
        n.registrarTentativaFalha("erro 3", maxTentativas = 3)

        assertEquals(3, n.tentativasEnvio)
        assertEquals("erro 3", n.ultimoErro)
        assertEquals(StatusEnvio.FALHA_ENVIO, n.statusEnvio)
    }

    @Test
    fun `deve lancar excecao ao registrar tentativa em notificacao ENVIADA`() {
        val n = build()
        n.marcarComoEnviada()

        val ex = assertThrows<DomainException> {
            n.registrarTentativaFalha("qualquer", maxTentativas = 3)
        }
        assertEquals("Notificação já enviada não pode registrar nova tentativa", ex.message)
    }

    @Test
    fun `deve lancar excecao ao usar maxTentativas nao positivo`() {
        val n = build()

        assertThrows<IllegalArgumentException> {
            n.registrarTentativaFalha("erro", maxTentativas = 0)
        }
    }

    @Test
    fun `marcarComoEnviada deve limpar ultimoErro apos sucesso`() {
        val n = build()
        n.registrarTentativaFalha("timeout", maxTentativas = 3)
        assertEquals("timeout", n.ultimoErro)

        n.marcarComoEnviada()

        assertEquals(null, n.ultimoErro)
        assertEquals(StatusEnvio.ENVIADA, n.statusEnvio)
    }
}

