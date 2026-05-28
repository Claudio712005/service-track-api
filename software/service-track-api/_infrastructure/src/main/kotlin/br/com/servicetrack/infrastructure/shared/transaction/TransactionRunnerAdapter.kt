package br.com.servicetrack.infrastructure.shared.transaction

import br.com.servicetrack.application.shared.ports.out.TransactionRunnerPort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class TransactionRunnerAdapter : TransactionRunnerPort {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    override fun <T> executarEmNovaTransacao(block: () -> T): T = block()
}
