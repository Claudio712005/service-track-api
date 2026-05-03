package br.com.servicetrack.domain.ordemServico

enum class StatusOrdemServicoEnum(
    val ordem: Int,
    val descricao: String,
) {
    CANCELADA(0, "Cancelada"),
    RECEBIDA(1, "Recebida"),
    EM_DIAGNOSTICO(2, "Em Diagnóstico"),
    AGUARDANDO_APROVACAO(3, "Aguardando Aprovação"),
    EM_EXECUCAO(4, "Em Execução"),
    FINALIZADA(5, "Finalizada"),
    ENTREGUE(6, "Entregue");


}