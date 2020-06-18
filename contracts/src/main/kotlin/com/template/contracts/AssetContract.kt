package com.template.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class AssetContract : Contract {

    companion object { const val ID = "com.template.contracts.AssetContract" }

    override fun verify(tx: LedgerTransaction) {}

    interface Commands : CommandData {
        class Issue : Commands
        class Transfer : Commands
    }
}