create table auditorias (
    data_criacao timestamp(6) not null,
    id uuid not null,
    responsavel_acao varchar(36) not null,
    endereco_ip varchar(45) not null,
    tipo_entidade varchar(50) not null,
    tipo_evento varchar(50) not null,
    referencia_id varchar(60) not null,
    dados TEXT not null,
    descricao_evento TEXT not null,
    primary key (id));

create table insumos (
    ativo boolean not null,
    custo numeric(12,2) not null,
    estoque_minimo integer not null,
    qtd_estoque integer not null,
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    id uuid not null,
    nome varchar(150) not null,
    descricao TEXT not null,
    primary key (id));

create table itens_ordem_servico (
    feito boolean not null,
    valor numeric(12,2) not null,
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    data_realizacao timestamp(6),
    id uuid not null,
    mecanico_responsavel_id uuid,
    ordem_servico_id uuid not null,
    servico_id uuid not null,
    observacao TEXT,
    primary key (id));

create table mecanicos (
    valor_hora numeric(10,2) not null,
    usuario_id uuid not null,
    nivel varchar(255) not null check (nivel in ('JUNIOR','PLENO','SENIOR')),
    primary key (usuario_id));

create table notificacao_copias (
    notificacao_id uuid not null,
    usuario_id uuid not null);

create table notificacoes (
    tentativas_envio integer not null,
    visualizada boolean not null,
    data_criacao timestamp(6) not null,
    data_envio timestamp(6),
    data_visualizacao timestamp(6),
    destinatario_id uuid not null,
    id uuid not null,
    status_envio varchar(20) not null check (status_envio in ('PENDENTE','ENVIADA','FALHA_ENVIO')),
    tipo_notificacao varchar(30) not null check (tipo_notificacao in ('EMAIL')),
    tipo_conteudo_notificacao varchar(60) not null check (tipo_conteudo_notificacao in ('MUDANCA_STATUS_OS')),
    assunto varchar(150) not null,
    titulo varchar(200) not null,
    descricao TEXT not null,
    ultimo_erro TEXT,
    variaveis_json TEXT not null,
    primary key (id));

create table orcamentos (
    aprovado boolean not null,
    custo_insumos numeric(12,2) not null,
    custo_mao_de_obra numeric(12,2) not null,
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    id uuid not null,
    ordem_servico_id uuid not null unique,
    observacao TEXT,
    primary key (id));

create table ordem_servico_insumos (
    ordem_servico_id uuid not null,
    insumo_id varchar(255) not null);

create table ordens_servico (
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    prazo_conclusao timestamp(6),
    cliente_id uuid not null,
    id uuid not null,
    mecanico_id uuid not null,
    veiculo_id uuid not null,
    motivo varchar(500) not null,
    observacao TEXT,
    status varchar(255) not null check (status in ('CANCELADA','RECEBIDA','EM_DIAGNOSTICO','AGUARDANDO_APROVACAO','EM_EXECUCAO','FINALIZADA','ENTREGUE')),
    primary key (id));

create table servicos (
    ativo boolean not null,
    valor_referencia numeric(12,2),
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    id uuid not null,
    nome_servico varchar(150) not null,
    descricao_servico TEXT not null,
    primary key (id));

create table usuario_roles (
    usuario_id uuid not null,
    role varchar(255) check (role in ('CLIENTE','MECANICO')));

create table usuarios (
    ativo boolean not null,
    data_nascimento date not null,
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    id uuid not null,
    senha_hash varchar(100) not null,
    cpf varchar(255) not null unique,
    email varchar(255) not null unique,
    nome varchar(255) not null,
    telefone varchar(255) not null,
    primary key (id));

create table veiculos (
    ano integer not null,
    data_atualizacao timestamp(6) not null,
    data_criacao timestamp(6) not null,
    proprietario_id uuid not null,
    veiculo_id uuid not null,
    codigo_fipe varchar(20),
    imagem_url varchar(2048),
    ativo varchar(255) not null check (ativo in ('S','N')),
    marca varchar(255) not null,
    modelo varchar(255) not null,
    placa varchar(255) not null unique,
    primary key (veiculo_id));

create index idx_auditoria_referencia_id on auditorias (referencia_id);
create index idx_auditoria_data_criacao on auditorias (data_criacao);

alter table if exists itens_ordem_servico
    add constraint fk_item_mecanico_responsavel foreign key (mecanico_responsavel_id) references usuarios;
alter table if exists itens_ordem_servico
    add constraint fk_item_ordem_servico foreign key (ordem_servico_id) references ordens_servico;
alter table if exists itens_ordem_servico
    add constraint fk_item_servico foreign key (servico_id) references servicos;
alter table if exists notificacao_copias
    add constraint fk_notificacao_copias_notificacao foreign key (notificacao_id) references notificacoes;
alter table if exists orcamentos
    add constraint fk_orcamento_ordem_servico foreign key (ordem_servico_id) references ordens_servico;
alter table if exists ordem_servico_insumos
    add constraint fk_ordem_servico_insumos_ordem foreign key (ordem_servico_id) references ordens_servico;
alter table if exists ordens_servico
    add constraint fk_ordem_cliente foreign key (cliente_id) references usuarios;
alter table if exists ordens_servico
    add constraint fk_ordem_mecanico foreign key (mecanico_id) references usuarios;
alter table if exists ordens_servico
    add constraint fk_ordem_veiculo foreign key (veiculo_id) references veiculos;
alter table if exists usuario_roles
    add constraint fk_usuario_roles_usuario foreign key (usuario_id) references usuarios;
alter table if exists veiculos
    add constraint fk_veiculo_proprietario foreign key (proprietario_id) references usuarios;
