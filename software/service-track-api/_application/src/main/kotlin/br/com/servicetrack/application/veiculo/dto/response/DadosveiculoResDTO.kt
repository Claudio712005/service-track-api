package br.com.servicetrack.application.veiculo.dto.response

import br.com.servicetrack.application.observabilidade.annotation.Mascarado
import br.com.servicetrack.application.observabilidade.annotation.Rastreavel
import br.com.servicetrack.domain.veiculo.Veiculo

data class DadosveiculoResDTO(
    @field:Rastreavel
    val id: String,
    val proprietarioId: String,
    @field:Mascarado(visiveis = 3)
    val placa: String,
    val modelo: String,
    val marca: String,
    val ano: Int,
    val urlImagem: String? = null,
    val codigoFipe: String? = null
) {

    companion object {
        fun de(veiculo: Veiculo): DadosveiculoResDTO {
            val dados = veiculo.obterDados()
            return DadosveiculoResDTO(
                id = dados.id.valor.toString(),
                proprietarioId = dados.proprietarioId.valor,
                placa = dados.placa.valor,
                modelo = dados.modelo,
                marca = dados.marca,
                ano = dados.ano,
                urlImagem = dados.imagemUrl?.url,
                codigoFipe = dados.codigoFipe
            )
        }
    }
}
