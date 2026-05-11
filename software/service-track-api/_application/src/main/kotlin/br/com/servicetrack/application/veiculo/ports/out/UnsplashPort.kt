package br.com.servicetrack.application.veiculo.ports.out

interface UnsplashPort {
    fun buscarImagensVeiculo(marca: String, modelo: String, quantidade: Int): List<String>
}
