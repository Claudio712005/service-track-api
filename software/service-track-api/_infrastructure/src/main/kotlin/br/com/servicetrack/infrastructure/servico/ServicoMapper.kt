package br.com.servicetrack.infrastructure.servico

import br.com.servicetrack.application.servico.dto.AtualizarServicoReqDTO
import br.com.servicetrack.application.servico.dto.CriarServicoReqDTO
import br.com.servicetrack.application.servico.dto.ServicoResDTO
import br.com.servicetrack.application.servico.dto.ServicoResumoResDTO
import br.com.servicetrack.infrastructure.api.dto.AtualizarServicoRequest
import br.com.servicetrack.infrastructure.api.dto.CatalogoServicoResponse
import br.com.servicetrack.infrastructure.api.dto.CriarServicoRequest
import br.com.servicetrack.infrastructure.api.dto.ServicoResponse
import java.math.BigDecimal
import java.util.UUID

internal fun CriarServicoRequest.toApplicationDTO() = CriarServicoReqDTO(
    nomeServico = nomeServico,
    descricaoServico = descricaoServico,
    valorReferencia = valorReferencia?.let { BigDecimal.valueOf(it) }
)

internal fun AtualizarServicoRequest.toApplicationDTO() = AtualizarServicoReqDTO(
    nomeServico = nomeServico,
    descricaoServico = descricaoServico,
    valorReferencia = valorReferencia?.let { BigDecimal.valueOf(it) }
)

internal fun ServicoResDTO.toServicoResponse(): ServicoResponse = ServicoResponse()
    .id(UUID.fromString(id))
    .nomeServico(nomeServico)
    .descricaoServico(descricaoServico)
    .valorReferencia(valorReferencia?.toDouble())

internal fun ServicoResumoResDTO.toCatalogoServicoResponse(): CatalogoServicoResponse = CatalogoServicoResponse()
    .id(UUID.fromString(id))
    .nomeServico(nomeServico)
    .descricaoServico(descricaoServico)
