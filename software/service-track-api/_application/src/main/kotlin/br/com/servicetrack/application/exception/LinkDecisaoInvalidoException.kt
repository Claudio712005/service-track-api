package br.com.servicetrack.application.exception

class LinkDecisaoInvalidoException(
    motivo: String = "Link inválido ou expirado",
) : RuntimeException(motivo)
