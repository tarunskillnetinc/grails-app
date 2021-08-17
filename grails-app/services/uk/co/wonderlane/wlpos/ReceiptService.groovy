package uk.co.wonderlane.wlpos

import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

import java.text.NumberFormat

@Transactional("transactions")
class ReceiptService {

    def springSecurityService

    private final int BASKET_ITEM_LENGTH = 23
    private final int RECEIPT_BARCODE_WIDTH = 450
    private final int RECEIPT_BARCODE_HEIGHT = 75

    def getReceipts(DateTime fromDate, DateTime toDate, Integer tillId, Integer transactionId, int offset, int max) {
        def receiptsCriteria = Receipt.createCriteria()

        def results = receiptsCriteria.list([offset: offset, max: max]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", springSecurityService.principal.storeId)

            gte("dateGenerated", fromDate)
            lt("dateGenerated", toDate)

            if (tillId) {
                eq("tillId", tillId)
            }

            if (transactionId) {
                eq("transactionId", transactionId)
            }

            order("dateGenerated", "DESC")
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = results.totalCount

        return results
    }

    def getReceipt(int receiptId) {
        return Receipt.findByIdAndRetailerIdAndStoreId(receiptId, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }
}