package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AssetContract
import com.template.states.AssetState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class IssueAssetFlow(val assetName: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val assetState = AssetState(assetName, ourIdentity)
        val command = Command(AssetContract.Commands.Issue(), listOf(ourIdentity.owningKey))

        val txBuilder = TransactionBuilder(notary)
        txBuilder.addCommand(command)
        txBuilder.addOutputState(assetState)

        txBuilder.verify(serviceHub)

        val tx = serviceHub.signInitialTransaction(txBuilder)
        return subFlow(FinalityFlow(tx, emptyList()))
    }
}