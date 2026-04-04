package br.com.servicetrack.domain.veiculo

import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.usuario.vo.UsuarioId
import br.com.servicetrack.domain.veiculo.vo.Placa
import br.com.servicetrack.domain.veiculo.vo.VeiculoId
import java.time.LocalDateTime

class Veiculo private constructor(
    val id: VeiculoId,
    var proprietarioId: UsuarioId,
    private var placa: Placa,
    private var modelo: String,
    private var marca: String,
    private var ano: Int,
    private val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime
) {

    companion object {

        fun criar(
            proprietarioId: UsuarioId,
            placa: Placa,
            modelo: String,
            marca: String,
            ano: Int
        ): Veiculo {

            if (modelo.isBlank()) {
                throw DomainException("Modelo não pode ser vazio")
            }

            if (marca.isBlank()) {
                throw DomainException("Marca não pode ser vazia")
            }

            if (ano < 1900) {
                throw DomainException("Ano inválido")
            }

            val agora = LocalDateTime.now()

            return Veiculo(
                id = VeiculoId.gerar(),
                proprietarioId = proprietarioId,
                placa = placa,
                modelo = modelo,
                marca = marca,
                ano = ano,
                dataCriacao = agora,
                dataAtualizacao = agora
            )
        }
    }

    fun alterarPlaca(novaPlaca: Placa) {
        if (this.placa == novaPlaca) {
            throw DomainException("A nova placa deve ser diferente da atual")
        }

        this.placa = novaPlaca
        atualizarData()
    }

    fun alterarDados(
        modelo: String,
        marca: String,
        ano: Int
    ) {

        if (modelo.isBlank()) {
            throw DomainException("Modelo não pode ser vazio")
        }

        if (marca.isBlank()) {
            throw DomainException("Marca não pode ser vazia")
        }

        if (ano < 1900) {
            throw DomainException("Ano inválido")
        }

        this.modelo = modelo
        this.marca = marca
        this.ano = ano

        atualizarData()
    }

    fun pertenceAoUsuario(usuarioId: UsuarioId): Boolean {
        return this.proprietarioId == usuarioId
    }

    fun atualizarProprietario(novoProprietarioId: UsuarioId) {
        if (this.proprietarioId == novoProprietarioId) {
            throw DomainException("O veículo já pertence a este usuário")
        }

        this.proprietarioId = novoProprietarioId
        atualizarData()
    }

    private fun atualizarData() {
        this.dataAtualizacao = LocalDateTime.now()
    }

    fun obterDados(): DadosVeiculo {
        return DadosVeiculo(
            id = id,
            proprietarioId = proprietarioId,
            placa = placa,
            modelo = modelo,
            marca = marca,
            ano = ano
        )
    }
}