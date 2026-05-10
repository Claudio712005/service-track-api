# RFC – 003: Adoção do Kotlin como Linguagem Principal

## Data
18/04/2026

---

## Status
- Encerrada – Aprovada

---

## Resumo

Proposta para adoção do Kotlin como linguagem principal de desenvolvimento do back-end, com objetivo de maximizar segurança de tipos, produtividade da equipe e qualidade da modelagem de domínio DDD, mantendo plena interoperabilidade com o ecossistema JVM.

---

## Problema

O desenvolvimento de um sistema DDD com domínio rico apresenta desafios que linguagens mais verbosas e menos seguras dificultam:

- `NullPointerException` em Java é a causa mais comum de erros em produção em sistemas JVM; não há garantia em tempo de compilação
- Modelagem de Value Objects e entidades imutáveis em Java exige boilerplate extenso (construtores, getters, `equals`, `hashCode`, `toString`) ou dependência de Lombok, que adiciona complexidade ao processo de build e ao K2 compiler
- `sealed class` e `when` exhaustivo para máquinas de estados do domínio são inexistentes em Java; requerem workarounds
- Código verboso aumenta carga cognitiva na revisão e manutenção, especialmente em equipes pequenas
- Falta de suporte nativo a funções de extensão dificulta enriquecimento de objetos de domínio sem herança

---

## Proposta Técnica

### Linguagem escolhida: Kotlin (JVM target)

#### Null safety por design

O sistema de tipos do Kotlin distingue explicitamente tipos nullable (`String?`) de non-nullable (`String`):

```kotlin
// Kotlin — erro em tempo de compilação se nome for null
fun processarCliente(nome: String) { ... }

// Java — NullPointerException silencioso em runtime
void processarCliente(String nome) { ... }
```

Toda entrada de dado externo (REST, banco) é validada na fronteira da infraestrutura antes de entrar no domínio como tipo non-nullable.

#### Modelagem de domínio com data class e sealed class

```kotlin
// Value Object conciso e imutável
data class Cpf(val valor: String) {
    init { require(isValid(valor)) { "CPF inválido" } }
}

// Máquina de estados com sealed class — when exhaustivo garantido pelo compilador
sealed class StatusOS {
    object Aberta : StatusOS()
    object EmExecucao : StatusOS()
    object Concluida : StatusOS()
    object Cancelada : StatusOS()
}

fun processar(status: StatusOS) = when (status) {
    is StatusOS.Aberta      -> iniciarExecucao()
    is StatusOS.EmExecucao  -> concluir()
    is StatusOS.Concluida   -> error("OS já concluída")
    is StatusOS.Cancelada   -> error("OS cancelada")
    // Compilador garante que todos os casos são cobertos
}
```

#### Redução de boilerplate nas camadas de aplicação

```kotlin
// Application Service em Kotlin — sem Lombok, sem geração de código
@ApplicationScoped
class CadastrarClienteService(
    private val repository: ClienteRepositoryPort,
    private val criptografia: CriptografiaPort
) : CadastrarClienteUseCase {

    override fun cadastrar(comando: CadastrarClienteComando) {
        val cliente = Cliente.criar(comando.nome, comando.email, comando.cpf)
        repository.salvar(cliente)
    }
}
```

#### Funções de extensão para enriquecimento sem herança

```kotlin
// Enriquecer entidades sem quebrar encapsulamento ou criar herança
fun Mecanico.podeAtenderOS(os: OrdemServico): Boolean =
    this.ativo && os.status == StatusOS.Aberta
```

#### Coroutines para operações assíncronas (evolução futura)

Kotlin oferece suporte nativo a coroutines para I/O assíncrono sem callback hell, preparando o sistema para escala sem mudança de paradigma de linguagem.

#### Interoperabilidade com Java

- 100% compatível com bibliotecas Java: Quarkus, Hibernate, Jackson, Flyway
- Chamadas bidirecionais sem adaptadores: código Kotlin chama Java e vice-versa
- Compilação para bytecode JVM idêntico em performance

---

## Alternativas Consideradas

### Opção 1: Java (versões modernas — 17/21)

- Linguagem principal do ecossistema JVM com grande base de desenvolvedores
- Prós: comunidade enorme, documentação extensa, estabilidade garantida, facilidade de contratação, Records (Java 14+) reduzem boilerplate para Value Objects simples
- Contras: null ainda é válido sem enforcement em tempo de compilação; Records são imutáveis mas sem suporte a validação no construtor de forma limpa; sealed classes existem desde Java 17 mas sem `when` exhaustivo; Lombok necessário para produtividade mas incompatível com alguns cenários de processadores de anotação; código mais verboso aumenta custo de manutenção

---

## Pontos em Aberto

- Estratégia de treinamento formal em Kotlin para membros da equipe vindos do Java
- Política de uso de Coroutines: quando adotar programação assíncrona no back-end
- Convenções de codificação Kotlin para o projeto (ktlint, detekt como linters)
- Compatibilidade de versão do compilador Kotlin com versão do Quarkus em uso (Kotlin 2.x + K2 compiler)

---

## Impactos

### Positivos
- Eliminação de `NullPointerException` na maior parte do código via sistema de tipos
- Código de domínio mais expressivo e próximo da linguagem ubíqua do negócio
- Redução significativa de linhas de código sem perda de legibilidade
- Maior produtividade da equipe no desenvolvimento do MVP

### Negativos
- Curva de aprendizado para desenvolvedores exclusivamente Java, especialmente em idioms avançados (DSLs, coroutines, reified generics)
- Pool de candidatos no mercado menor que Java, podendo impactar contratações futuras
- Kotlin 2.x com K2 compiler impõe restrições adicionais (ex.: soft keywords como `out`/`in` em pacotes requerem backticks) que podem surpreender equipes acostumadas com versões anteriores

---

## Próximos Passos

- Revisão pelo time de desenvolvimento
- Definição das convenções de código e configuração de linters (ktlint/detekt)
- Planejamento de onboarding técnico para membros com background Java
- Aprovação formal e geração da ADR-003 correspondente
