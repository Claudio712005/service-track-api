package br.com.servicetrack.domain.auditoria.enums

enum class TipoEntidade(
    val descricao: String
) {
    CLIENTE("Cliente"),
    MECANICO("Mecânico"),
    ORDEM_SERVICO("Ordem de serviço"),
    ORCAMENTO("Orçamento"),
    VEICULO("Veículo"),
    INSUMO("Insumo"),
    SERVICO("Serviço"),
    USUARIO("Usuário"),
    NOTIFICACAO("Notificação"),
}
