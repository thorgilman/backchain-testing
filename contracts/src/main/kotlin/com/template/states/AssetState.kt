package com.template.states

import com.template.contracts.AssetContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty

@BelongsToContract(AssetContract::class)
data class AssetState(val name: String,
                      override val owner: AbstractParty,
                      override val linearId: UniqueIdentifier = UniqueIdentifier()) : ContractState, LinearState, OwnableState {

    override val participants: List<AbstractParty> = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState(AssetContract.Commands.Transfer(), this.copy(owner = newOwner))
    }
}
