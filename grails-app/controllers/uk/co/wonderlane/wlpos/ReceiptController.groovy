package uk.co.wonderlane.wlpos

import io.micronaut.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

class ReceiptController {

    def receiptService

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate: startDate, endDate: endDate]
    }

    def ajaxGetReceipts() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        DateTime startDate = DateTime.parse(params.startDate, dateFormatter).withTimeAtStartOfDay()
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter).withTimeAtStartOfDay().plusDays(1)
        String inputErrors = ""
        Integer tillId = null
        Integer transactionId = null

        String sort = params.sort
        String order = params.order

        def availableColumns = [ "storeId", "tillId", "dateGenerated", "transactionId", "transactionAmount", "paymentMethod" ]

        if (!availableColumns.contains(sort)) {
            sort = "dateGenerated"
        }

        if ("asc" != order && "desc" != order) {
            order = "desc"
        }

        if (params.tillId) {
            try {
                tillId = Integer.parseInt(params.tillId)
            } catch (NumberFormatException e) {
                //Treat the entered Till ID as zero if parsing failed - This can occur if the entered value is beyond the range of an Integer (2147483647) or is non-numeric.
                //The zero value will cause this function to report an error in the input data.
                tillId = 0
            }
        }

        if (params.transactionId) {
            try {
                transactionId = Integer.parseInt(params.transactionId)
            } catch (NumberFormatException e) {
                //Treat the entered Till ID as zero if parsing failed - This can occur if the entered value is beyond the range of an Integer (2147483647) or is non-numeric.
                //The zero value will cause this function to report an error in the input data.
                transactionId = 0
            }
        }

        if (tillId == 0) {
            inputErrors += "<li>Till ID filter must be between 1 and 99999999.</li>"
        }

        if (transactionId == 0) {
            inputErrors += "<li>Transaction Number filter must be between 1 and 999999999.</li>"
        }

        if (inputErrors.length() != 0) {
            render(status: HttpStatus.BAD_REQUEST.code, inputErrors)
        } else {
            def (results, totalCount) = receiptService.getReceipts(startDate, endDate, tillId, transactionId, sort, order, offset, max)
            render(template: "receiptViewerResults", model: [receipts: results, totalCount: totalCount, sort: sort, order: order, offset: offset, max: max, startDate: params.startDate, endDate: params.endDate, tillId: params.tillId, transactionId: params.transactionId, totalReceiptLineType: ReceiptLineType.TOTAL])
        }
    }

    def ajaxGetReceipt(int receiptId) {
        def receipt = receiptService.getReceipt(receiptId)

        render (template: "receipt", model: [receipt: receipt,
                                             containsModifiers: receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                             firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1,
                                             maxTotalLength: receipt.receiptLines?.findAll { it.type == ReceiptLineType.BASKET_ITEM}?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0,
                                             maxVatLength: receipt.receiptLines?.findAll { it.type == ReceiptLineType.VAT_ITEM}?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0])
    }

    def ajaxGetReceiptByTransaction(int transactionId, int storeId, int terminalId) {
        def receipt = receiptService.getReceipt(transactionId, storeId, terminalId)
        if (receipt != null) {
            render(template: "receipt", model: [receipt              : receipt,
                                                containsModifiers    : receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                                firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1,
                                                maxTotalLength       : receipt.receiptLines?.findAll { it.type == ReceiptLineType.BASKET_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0,
                                                maxVatLength         : receipt.receiptLines?.findAll { it.type == ReceiptLineType.VAT_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0])
        }
    }
}