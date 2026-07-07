package br.com.servicetrack.domain.auditoria

import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.shared.exception.DomainException

class EventoAuditoria(
    val entidade: TipoEntidade,
    val tipo: TipoEventoAuditoria,
    val descricao: String
) {

    companion object {
        private fun ehUsuario(entidade: TipoEntidade) {
            if(entidade != TipoEntidade.CLIENTE && entidade != TipoEntidade.MECANICO && entidade != TipoEntidade.USUARIO){
                throw DomainException("Evento de login só pode ser criado para Cliente ou Mecânico")
            }
        }

        fun criacao(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.CRIADO, "${entidade.descricao} criado(a)"
        )

        fun atualizado(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.ATUALIZADO, "${entidade.descricao} atualizado(a)"
        )

        fun removido(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.REMOVIDO, "${entidade.descricao} removido(a)"
        )

        fun desativado(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.DESATIVADO, "${entidade.descricao} desativado(a)"
        )

        fun ativado(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.ATIVADO, "${entidade.descricao} ativado(a)"
        )

        fun login(entidade: TipoEntidade): EventoAuditoria{
            ehUsuario(entidade)

            return EventoAuditoria(
                entidade, TipoEventoAuditoria.LOGIN, "${entidade.descricao} realizou login"
            )
        }

        fun logout(entidade: TipoEntidade): EventoAuditoria{
            ehUsuario(entidade)

            return EventoAuditoria(
                entidade, TipoEventoAuditoria.LOGOUT, "${entidade.descricao} realizou logout"
            )
        }

        fun mudancaSensivel(entidade: TipoEntidade) = EventoAuditoria(
            entidade, TipoEventoAuditoria.ALTERACAO_SENSIVEL, "${entidade.descricao} sofreu mudança sensível"
        )
    }

}
