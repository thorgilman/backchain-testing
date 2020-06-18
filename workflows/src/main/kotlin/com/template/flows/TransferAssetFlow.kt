package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AssetContract
import com.template.states.AssetState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class TransferAssetFlow(val linearId: UniqueIdentifier, val destParty: Party) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        if (destParty == ourIdentity) throw FlowException("You cannot transfer an asset to yourself")

        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val linearStateCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId), status = Vault.StateStatus.UNCONSUMED)
        val stateAndRef = serviceHub.vaultService.queryBy<AssetState>(linearStateCriteria).states.get(0)
        val inputState = stateAndRef.state.data
        val outputState = inputState.withNewOwner(destParty).ownableState

        val command = Command(AssetContract.Commands.Transfer(), listOf(ourIdentity.owningKey, destParty.owningKey))

        val txBuilder = TransactionBuilder(notary)
        txBuilder.addCommand(command)
        txBuilder.addInputState(stateAndRef)
        txBuilder.addOutputState(outputState)

        txBuilder.verify(serviceHub)

        val ptx = serviceHub.signInitialTransaction(txBuilder)
        val targetSession = initiateFlow(destParty)
        val stx = subFlow(CollectSignaturesFlow(ptx, listOf(targetSession)))
        return subFlow(FinalityFlow(stx, listOf(targetSession)))
    }
}

@InitiatedBy(TransferAssetFlow::class)
class TransferAssetFlowesponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {}
        }
        val txWeJustSigned = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSigned.id))
    }
}