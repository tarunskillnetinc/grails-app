package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

class ReceiptController {

    def receiptService

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().plusDays(1)

        [startDate: startDate, endDate: endDate]
    }

    def ajaxGetReceipts() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        DateTime startDate = DateTime.parse(params.startDate, dateFormatter)
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter)
        Integer tillId = null
        Integer transactionId = null

        if (params.tillId) {
            try {
                tillId = Integer.parseInt(params.tillId)
            } catch (Exception e) {
                // Non-numeric input added, do nothing.
            }
        }

        if (params.transactionId) {
            try {
                transactionId = Integer.parseInt(params.transactionId)
            } catch (Exception e) {
                // Non-numeric input added, do nothing.
            }
        }

        render (template: "receiptViewerResults", model: [receipts: receiptService.getReceipts(startDate, endDate, tillId, transactionId, offset, max), offset: offset, max: max])
    }

    def ajaxGetReceipt(int receiptId) {
        def receipt = receiptService.getReceipt(receiptId)

        render (template: "receipt", model: [receipt: receipt,
                                             containsModifiers: receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                             firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1])
    }
}