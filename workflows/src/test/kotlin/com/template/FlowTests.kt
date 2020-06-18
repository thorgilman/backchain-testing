package com.template

import com.template.flows.IssueAssetFlow
import com.template.flows.TransferAssetFlow
import com.template.states.AssetState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )))
    private val issuer = network.createNode()
    private val partyA = network.createNode()
    private val partyB = network.createNode()
    private val partyC = network.createNode()

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `test`() {

        // Issue
        val future = issuer.startFlow(IssueAssetFlow("Cash"))
        network.runNetwork()
        val signedTransaction = future.getOrThrow()

        val assetState = signedTransaction.tx.outRefsOfType<AssetState>().get(0).state.data
        val linearId = assetState.linearId

        assert(issuer.hasStateInVault(linearId))

        // Transfer 1
        val future2 = issuer.startFlow(TransferAssetFlow(linearId, partyA.identity()))
        network.runNetwork()
        val signedTransaction2 = future2.getOrThrow()

        assert(partyA.hasStateInVault(linearId))
        assert(!issuer.hasStateInVault(linearId))

    }

    fun StartedMockNode.hasStateInVault(linearId: UniqueIdentifier): Boolean {
        val linearStateCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId), status = Vault.StateStatus.UNCONSUMED)
        val states = this.services.vaultService.queryBy<AssetState>(linearStateCriteria).states
        return states.size == 1
    }

    fun StartedMockNode.identity(): Party {
        return this.info.legalIdentities.single()
    }

}