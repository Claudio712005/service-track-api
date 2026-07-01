package br.com.servicetrack.domain.ordemServico

enum class StatusOrdemServicoEnum(
    val ordem: Int,
    val descricao: String,
    val corStatus: String,
) {
    CANCELADA(0, "Cancelada", "#FF6B6B"),
    RECEBIDA(1, "Recebida", "#4ECDC4"),
    EM_DIAGNOSTICO(2, "Em Diagnóstico", "#FFE66D"),
    AGUARDANDO_APROVACAO(3, "Aguardando Aprovação", "#95E1D3"),
    EM_EXECUCAO(4, "Em Execução", "#6BCB77"),
    FINALIZADA(5, "Finalizada", "#4D96FF"),
    ENTREGUE(6, "Entregue", "#2D3436");

}