package br.com.servicetrack.application.servico.ports.out

import br.com.servicetrack.domain.servico.Servico
import br.com.servicetrack.domain.servico.vo.ServicoId

interface ServicoRepositoryPort {

    fun salvar(servico: Servico)
    fun buscarPorId(id: ServicoId): Servico?
    fun listarTodos(): List<Servico>
    fun atualizar(servico: Servico)
    fun desativar(id: ServicoId)
}
