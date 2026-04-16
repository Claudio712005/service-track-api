package br.com.servicetrack.infrastructure.insumo

import br.com.servicetrack.application.insumo.dto.AtualizarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.CriarInsumoReqDTO
import br.com.servicetrack.application.insumo.dto.InsumoResDTO
import br.com.servicetrack.application.insumo.dto.InsumoResumoResDTO
import br.com.servicetrack.infrastructure.api.dto.AtualizarInsumoRequest
import br.com.servicetrack.infrastructure.api.dto.CatalogoInsumoResponse
import br.com.servicetrack.infrastructure.api.dto.CriarInsumoRequest
import br.com.servicetrack.infrastructure.api.dto.InsumoResponse
import java.math.BigDecimal
import java.util.UUID

internal fun CriarInsumoRequest.toApplicationDTO() = CriarInsumoReqDTO(
    nome = nome,
    descricao = descricao,
    custo = BigDecimal.valueOf(custo),
    qtdEstoqueInicial = qtdEstoqueInicial ?: 0,
    estoqueMinimo = estoqueMinimo ?: 0
)

internal fun AtualizarInsumoRequest.toApplicationDTO() = AtualizarInsumoReqDTO(
    nome = nome,
    descricao = descricao,
    custo = custo?.let { BigDecimal.valueOf(it) },
    estoqueMinimo = estoqueMinimo
)

internal fun InsumoResDTO.toInsumoResponse(): InsumoResponse = InsumoResponse()
    .id(UUID.fromString(id))
    .nome(nome)
    .descricao(descricao)
    .custo(custo.toDouble())
    .estoqueMinimo(estoqueMinimo)
    .qtdEstoque(qtdEstoque)

internal fun InsumoResumoResDTO.toCatalogoInsumoResponse(): CatalogoInsumoResponse = CatalogoInsumoResponse()
    .id(UUID.fromString(id))
    .nome(nome)
    .descricao(descricao)
