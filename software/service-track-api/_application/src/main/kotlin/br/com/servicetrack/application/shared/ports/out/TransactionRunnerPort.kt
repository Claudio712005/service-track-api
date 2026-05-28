package br.com.servicetrack.application.shared.ports.out

interface TransactionRunnerPort {

    fun <T> executarEmNovaTransacao(block: () -> T): T
}
