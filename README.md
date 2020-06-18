<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Backchain Testing

"O=Issuer,L=New York,C=GB,CN=US"
"O=PartyA,L=New York,C=US"
"O=PartyB,L=New York,C=US"


-> Issuer
flow start IssueAssetFlow assetName: "Cash"
run vaultQuery contractStateType: com.template.states.AssetState
flow start TransferAssetFlow linearId: {id}, destParty: "O=PartyA,L=New York,C=US"

-> PartyA
flow start TransferAssetFlow linearId: {id}, destParty: "O=PartyB,L=New York,C=US"