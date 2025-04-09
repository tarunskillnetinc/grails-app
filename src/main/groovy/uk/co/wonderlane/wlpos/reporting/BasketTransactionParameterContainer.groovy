package uk.co.wonderlane.wlpos.reporting

class BasketTransactionParameterContainer {
    Integer storeId
    Integer tillId
    Integer transactionId

    BasketTransactionParameterContainer(Integer instoreId, Integer intillId, Integer intransactionId) {
        storeId = instoreId
        tillId = intillId
        transactionId = intransactionId
    }
}
