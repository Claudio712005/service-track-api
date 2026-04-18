package br.com.servicetrack.infrastructure.auditoria

import br.com.servicetrack.application.auditoria.dto.AuditoriaContextoDTO
import br.com.servicetrack.application.auditoria.ports.out.AuditoriaRepositoryPort
import br.com.servicetrack.application.auditoria.ports.out.RegistrarAuditoriaPort
import br.com.servicetrack.application.usuario.ports.out.JwtPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.auditoria.vo.EnderecoIp
import br.com.servicetrack.domain.auditoria.vo.ReferenciaId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.infrastructure.auditoria.factory.AuditoriaStrategyFactory
import io.vertx.ext.web.RoutingContext

class RegistrarAuditoriaAdapter(
    private val jwtPort: JwtPort,
    private val strategyFactory: AuditoriaStrategyFactory,
    private val repository: AuditoriaRepositoryPort,
    private val routingContext: RoutingContext,
) : RegistrarAuditoriaPort {

    override fun registrar(
        entidade: TipoEntidade,
        evento: TipoEventoAuditoria,
        referenciaId: String,
        antes: Any?,
        depois: Any?,
    ) {
        val ctx = AuditoriaContextoDTO(
            entidade = entidade,
            evento = evento,
            referenciaId = ReferenciaId(referenciaId),
            antes = antes,
            depois = depois,
            enderecoIp = obterIp(),
            responsavelAcao = jwtPort.getUsuarioId(),
        )
        val strategy = strategyFactory.obter(evento)
        val auditoria = strategy.executar(ctx)
        repository.salvar(auditoria)
    }

    private fun obterIp(): EnderecoIp {
        return try {
            val host = routingContext.request().remoteAddress()?.host() ?: "127.0.0.1"
            EnderecoIp.criar(host)
        } catch (e: DomainException) {
            EnderecoIp.criar("127.0.0.1")
        }
    }
}
