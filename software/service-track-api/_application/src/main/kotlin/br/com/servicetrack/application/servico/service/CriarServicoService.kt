package br.com.servicetrack.application.servico.service

import br.com.servicetrack.application.auditoria.annotation.Auditavel
import br.com.servicetrack.application.servico.dto.CriarServicoReqDTO
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.ports.`in`.CriarServicoUseCase
import br.com.servicetrack.application.servico.ports.`out`.ServicoRepositoryPort
import br.com.servicetrack.domain.auditoria.enums.TipoEntidade
import br.com.servicetrack.domain.auditoria.enums.TipoEventoAuditoria
import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.shared.vo.ValorMonetario

class CriarServicoService(
    private val repository: ServicoRepositoryPort
) : CriarServicoUseCase {

    @Auditavel(evento = TipoEventoAuditoria.CRIADO, entidade = TipoEntidade.SERVICO)
    override fun criarServico(req: CriarServicoReqDTO): ServicoResDTO {
        val servico = Servico.gerar(
            nomeServico = req.nomeServico,
            descricaoServico = req.descricaoServico,
            valorReferencia = req.valorReferencia?.let { ValorMonetario(it) }
        )
        repository.salvar(servico)
        return ServicoResDTO.de(servico)
    }
}
