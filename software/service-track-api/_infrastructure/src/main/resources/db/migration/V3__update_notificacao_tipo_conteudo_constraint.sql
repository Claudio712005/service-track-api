ALTER TABLE notificacoes DROP CONSTRAINT IF EXISTS notificacoes_tipo_conteudo_notificacao_check;

ALTER TABLE notificacoes ADD CONSTRAINT notificacoes_tipo_conteudo_notificacao_check
    CHECK (tipo_conteudo_notificacao IN (
        'MUDANCA_STATUS_OS',
        'SOLICITACAO_APROVACAO_ORCAMENTO_OS',
        'DECISAO_ORCAMENTO_OS'
    ));
