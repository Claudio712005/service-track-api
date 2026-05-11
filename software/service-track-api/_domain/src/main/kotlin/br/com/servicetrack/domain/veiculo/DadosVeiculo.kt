package br.com.servicetrack.domain.veiculo

import br.com.servicetrack.domain.shared.vo.ImagemUrl
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId

data class DadosVeiculo(
    val id: VeiculoId,
    val proprietarioId: UsuarioId,
    val placa: Placa,
    val modelo: String,
    val marca: String,
    val ano: Int,
    val imagemUrl: ImagemUrl? = null,
    val codigoFipe: String? = null
)
