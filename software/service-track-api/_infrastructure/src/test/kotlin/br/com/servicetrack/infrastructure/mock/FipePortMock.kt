package br.com.servicetrack.infrastructure.mock

import br.com.servicetrack.application.veiculo.dto.fipe.AnoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.DetalheVeiculoFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.MarcaFipeDTO
import br.com.servicetrack.application.veiculo.dto.fipe.ModeloFipeDTO
import br.com.servicetrack.application.veiculo.ports.out.FipePort
import io.quarkus.test.Mock
import jakarta.enterprise.context.ApplicationScoped

@Mock
@ApplicationScoped
class FipePortMock : FipePort {

    override fun listarMarcasCarros(): List<MarcaFipeDTO> = listOf(
        MarcaFipeDTO(codigo = "23", nome = "Honda"),
        MarcaFipeDTO(codigo = "59", nome = "Toyota"),
        MarcaFipeDTO(codigo = "25", nome = "Fiat"),
        MarcaFipeDTO(codigo = "39", nome = "Chevrolet"),
        MarcaFipeDTO(codigo = "77", nome = "Volkswagen"),
        MarcaFipeDTO(codigo = "74", nome = "Ford"),
        MarcaFipeDTO(codigo = "67", nome = "Renault"),
        MarcaFipeDTO(codigo = "24", nome = "Hyundai"),
        MarcaFipeDTO(codigo = "44", nome = "Mitsubishi"),
        MarcaFipeDTO(codigo = "65", nome = "Nissan"),
        MarcaFipeDTO(codigo = "71", nome = "Jeep")
    )

    override fun listarModelosCarros(codigoMarca: String): List<ModeloFipeDTO> = listOf(
        ModeloFipeDTO(codigo = "1100", nome = "Civic"),
        ModeloFipeDTO(codigo = "1101", nome = "Fit"),
        ModeloFipeDTO(codigo = "1102", nome = "HR-V")
    )

    override fun listarAnosCarros(codigoMarca: String, codigoModelo: String): List<AnoFipeDTO> = listOf(
        AnoFipeDTO(codigo = "2022-1", nome = "2022 Gasolina"),
        AnoFipeDTO(codigo = "2021-1", nome = "2021 Gasolina")
    )

    override fun consultarDetalhesCarros(codigoMarca: String, codigoModelo: String, ano: String): DetalheVeiculoFipeDTO =
        DetalheVeiculoFipeDTO(
            codigoFipe = "023013-8",
            marca = "Honda",
            modelo = "Civic",
            anoModelo = 2022,
            combustivel = "Gasolina",
            valor = "R\$ 120.000,00",
            mesReferencia = "abril de 2024"
        )
}
