package br.com.servicetrack.domain.veiculo

import br.com.servicetrack.domain.shared.enums.IndicativoSimNao
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ImagemUrl
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.time.LocalDateTime

class Veiculo private constructor(
    val id: VeiculoId,
    private var proprietarioId: UsuarioId,
    private var placa: Placa,
    private var modelo: String,
    private var marca: String,
    private var ano: Int,
    private var imagemUrl: ImagemUrl? = null,
    private var codigoFipe: String? = null,
    private val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var ativo: IndicativoSimNao = IndicativoSimNao.S
) {

    companion object {

        fun criar(
            proprietarioId: UsuarioId,
            placa: Placa,
            modelo: String,
            marca: String,
            ano: Int,
            imagemUrl: ImagemUrl? = null
        ): Veiculo {
            validarDados(modelo, marca, ano)

            val agora = LocalDateTime.now()

            return Veiculo(
                id = VeiculoId.gerar(),
                proprietarioId = proprietarioId,
                placa = placa,
                modelo = modelo,
                marca = marca,
                ano = ano,
                imagemUrl = imagemUrl,
                dataCriacao = agora,
                dataAtualizacao = agora
            )
        }

        private fun validarDados(modelo: String, marca: String, ano: Int) {
            if (modelo.isBlank()) throw DomainException("Modelo não pode ser vazio")
            if (marca.isBlank()) throw DomainException("Marca não pode ser vazia")
            if (ano < 1900) throw DomainException("Ano inválido")
        }

        fun reconstituir(
            id: VeiculoId,
            marca: String,
            placa: Placa,
            ano: Int,
            proprietarioId: UsuarioId,
            modelo: String,
            dataCriacao: LocalDateTime,
            dataAtualizacao: LocalDateTime,
            ativo: IndicativoSimNao,
            imagemUrl: ImagemUrl? = null,
            codigoFipe: String? = null
        ) = Veiculo(
            id = id,
            proprietarioId = proprietarioId,
            placa = placa,
            modelo = modelo,
            marca = marca,
            ano = ano,
            imagemUrl = imagemUrl,
            codigoFipe = codigoFipe,
            dataCriacao = dataCriacao,
            dataAtualizacao = dataAtualizacao,
            ativo = ativo
        )
    }

    fun alterarPlaca(novaPlaca: Placa) {
        if (!veiculoAtivo()) {
            throw DomainException("Veículo desativado não pode ter a placa alterada")
        }

        if (this.placa == novaPlaca) {
            throw DomainException("A nova placa deve ser diferente da atual")
        }
        this.placa = novaPlaca
        atualizarData()
    }

    fun alterarDados(modelo: String, marca: String, ano: Int) {
        if (!veiculoAtivo()) {
            throw DomainException("Veículo desativado não pode ter os dados alterados")
        }

        validarDados(modelo, marca, ano)
        this.modelo = modelo
        this.marca = marca
        this.ano = ano
        atualizarData()
    }

    fun definirImagemUrl(novaImagemUrl: ImagemUrl?) {
        if (!veiculoAtivo()) {
            throw DomainException("Veículo desativado não pode ter a imagem alterada")
        }
        this.imagemUrl = novaImagemUrl
        atualizarData()
    }

    fun pertenceAoUsuario(usuarioId: UsuarioId): Boolean {
        return this.proprietarioId == usuarioId
    }

    fun atualizarProprietario(novoProprietarioId: UsuarioId) {
        if (!veiculoAtivo()) {
            throw DomainException("Veículo inativo não pode ter o proprietário alterado")
        }

        if (this.proprietarioId == novoProprietarioId) {
            throw DomainException("O veículo já pertence a este usuário")
        }
        this.proprietarioId = novoProprietarioId
        atualizarData()
    }

    fun obterDados(): DadosVeiculo {
        return DadosVeiculo(
            id = id,
            proprietarioId = proprietarioId,
            placa = placa,
            modelo = modelo,
            marca = marca,
            ano = ano,
            imagemUrl = imagemUrl,
            codigoFipe = codigoFipe
        )
    }

    fun veiculoAtivo(): Boolean {
        return ativo == IndicativoSimNao.S
    }

    fun desativarVeiculo() {
        if (!veiculoAtivo()) {
            throw DomainException("O veículo já está desativado")
        }

        this.ativo = IndicativoSimNao.N
        atualizarData()
    }

    private fun atualizarData() {
        this.dataAtualizacao = LocalDateTime.now()
    }
}
