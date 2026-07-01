package br.com.servicetrack.domain.shared.vo

import br.com.servicetrack.domain.shared.exception.DomainException

@JvmInline
value class ImagemUrl private constructor(val url: String) {

    companion object {
        fun criar(url: String): ImagemUrl {
            validarUrl(url)
            return ImagemUrl(url)
        }

        private fun validarUrl(url: String) {
            if (url.isBlank()) throw DomainException("URL da imagem não pode ser vazia")
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw DomainException("URL da imagem deve começar com http:// ou https://")
            }
        }
    }
}