package br.com.servicetrack.application.veiculo.dto.response

import br.com.servicetrack.domain.veiculo.Veiculo

data class DadosveiculoResDTO(
    val id: String,
    val proprietarioId: String,
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
