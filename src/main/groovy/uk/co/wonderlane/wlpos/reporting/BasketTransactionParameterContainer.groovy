package uk.co.wonderlane.wlpos.reporting

class BasketTransactionParameterContainer {
    Integer storeId
    Integer tillId
    Integer transactionId
    Integer receiptId

    BasketTransactionParameterContainer(Integer inStoreId, Integer inTillId, Integer inTransactionId, Integer inReceiptId) {
        storeId = inStoreId
        tillId = inTillId
        transactionId = inTransactionId
        receiptId = inReceiptId
    }
}
