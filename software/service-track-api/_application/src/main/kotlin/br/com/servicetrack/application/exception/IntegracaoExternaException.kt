package br.com.servicetrack.application.exception

class IntegracaoExternaException(servico: String, causa: String) : RuntimeException(
    "Falha na integração com o serviço '$servico': $causa"
)
