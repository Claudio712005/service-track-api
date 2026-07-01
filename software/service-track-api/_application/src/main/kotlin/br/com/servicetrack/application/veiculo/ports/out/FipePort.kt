package br.com.servicetrack.application.veiculo.ports.out

import br.com.servicetrack.application.veiculo.dto.fipe.AnoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.DetalheVeiculoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.MarcaFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.ModeloFipeDTO

interface FipePort {
    fun listarMarcasCarros(): List<MarcaFipeDTO>
    fun listarModelosCarros(codigoMarca: String): List<ModeloFipeDTO>
    fun listarAnosCarros(codigoMarca: String, codigoModelo: String): List<AnoFipeDTO>
    fun consultarDetalhesCarros(codigoMarca: String, codigoModelo: String, ano: String): DetalheVeiculoFipeDTO
}
