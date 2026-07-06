package br.com.servicetrack.domain.servico

import br.com.servicetrack.domain.servico.vo.ServicoId
import br.com.servicetrack.domain.shared.exception.DomainException
import br.com.servicetrack.domain.shared.vo.ValorMonetario
import java.time.LocalDateTime

class Servico private constructor(
    val id: ServicoId,
    val nomeServico: String,
    val descricaoServico: String,
    var valorReferencia: ValorMonetario? = null,
    val dataCriacao: LocalDateTime,
    private var dataAtualizacao: LocalDateTime,
    private var ativo: Boolean = true,
) {

    companion object {
        fun gerar(
            nomeServico: String,
            descricaoServico: String,
            valorReferencia: ValorMonetario? = null,
        ): Servico {
            require(nomeServico.isNotBlank()) { "Nome do serviço não pode ser vazio" }
            require(descricaoServico.isNotBlank()) { "Descrição do serviço não pode ser vazia" }

            val agora = LocalDateTime.now()

            return Servico(
                id = ServicoId.gerar(),
                nomeServico = nomeServico,
                descricaoServico = descricaoServico,
                valorReferencia = valorReferencia,
                dataCriacao = agora,
                dataAtualizacao = agora,
                ativo = true,
            )
        }

        fun reconstituir(
            id: ServicoId,
            nomeServico: String,
            descricaoServico: String,
            valorReferencia: ValorMonetario?,
            dataCriacao: LocalDateTime,
            dataAtualizacao: LocalDateTime,
            ativo: Boolean = true,
        ): Servico = Servico(
            id = id,
            nomeServico = nomeServico,
            descricaoServico = descricaoServico,
            valorReferencia = valorReferencia,
            dataCriacao = dataCriacao,
            dataAtualizacao = dataAtualizacao,
            ativo = ativo,
        )
    }

    fun estaAtivo(): Boolean = ativo

    fun desativar() {
        check(ativo) { "Serviço já está desativado" }
        ativo = false
        dataAtualizacao = LocalDateTime.now()
    }

    fun atualizarValorReferencia(novoValor: ValorMonetario) {
        valorReferencia = novoValor
        dataAtualizacao = LocalDateTime.now()
    }

    fun atualizarDescricao(novaDescricao: String) {
        if (novaDescricao.isBlank()) {
            throw DomainException("Descrição do serviço não pode ser vazia")
        }
        dataAtualizacao = LocalDateTime.now()
    }
}
