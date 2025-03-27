package uk.co.wonderlane.wlpos

import io.micronaut.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

class TransactionController {

    def transactionService
    def springSecurityService
    def storeService
    def basketTransactionService
    int lastShownReceiptId

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate: startDate, endDate: endDate]
    }

    def details() {
        try {
            def receiptId
            if (params.receiptId) {
                receiptId = Integer.parseInt(params.receiptId)
            }

            Receipt receipt = transactionService.getReceipt(receiptId)
            Store store = storeService.getStoreByStoreNumber(receipt.retailerId, receipt.storeId)
            def basketTransaction = basketTransactionService.getBasketTransactionByReceipt(receipt, store)
            User user = User.findByRetailerIdAndUsername(receipt.retailerId, receipt.usersName)

            if (!user) { // The user appears to have disappeared. Unlikely event.
                def basketuser = basketTransaction?.getUser()

                user = new User()
                user.setName(basketuser?.name)
                user.setId(basketuser?.id)
            }

            [
                    user                 : user,
                    basketTransaction    : basketTransaction,
                    basket               : basketTransaction.basket,
                    basketItems          : basketTransaction.basket.basketItems,
                    store                : store,
                    receipt: receipt
            ]
        }
        catch (Exception ex) {
            def inputErrors = "Transaction details could not be fetched.<br/>"
            inputErrors += "<pre>" + exceptionToString(ex) + "</pre>"
            flash.error = "The selected transaction is not able to be viewed on this page and may be older data."
            [exception: inputErrors]
        }
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
            def (combinedResults, totalCount) = transactionService.getReceipts(startDate, endDate, tillId, transactionId, sort, order, offset, max)
            render(template: "receiptViewerResults", model: [
                    combinedResults: combinedResults,
                    totalCount: totalCount,
                    sort: sort,
                    order: order,
                    offset: offset,
                    max: max,
                    startDate: params.startDate,
                    endDate: params.endDate,
                    tillId: params.tillId,
                    transactionId: params.transactionId,
                    totalReceiptLineType: ReceiptLineType.TOTAL
            ])
        }
    }

    def ajaxGetReceipt(int receiptId) {
        def receipt = transactionService.getReceipt(receiptId)
        lastShownReceiptId = receiptId;

        render (template: "receipt", model: [receipt: receipt,
                                             containsModifiers: receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                             firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1,
                                             maxTotalLength: receipt.receiptLines?.findAll { it.type == ReceiptLineType.BASKET_ITEM}?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0,
                                             maxVatLength: receipt.receiptLines?.findAll { it.type == ReceiptLineType.VAT_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0])
    }

    def ajaxGetReceiptByTransaction(int transactionId, int storeId, int terminalId) {
        def receipt = transactionService.getReceipt(transactionId, storeId, terminalId)
        if (receipt != null) {
            render(template: "receipt", model: [receipt              : receipt,
                                                containsModifiers    : receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                                firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1,
                                                maxTotalLength       : receipt.receiptLines?.findAll { it.type == ReceiptLineType.BASKET_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0,
                                                maxVatLength         : receipt.receiptLines?.findAll { it.type == ReceiptLineType.VAT_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0])
        }
    }

    private def exceptionToString(Exception e) {
        def sw = new StringWriter()
        e.printStackTrace(new PrintWriter(sw))
        return sw.toString()
    }

    def saveReceiptPrinted(){
        if(lastShownReceiptId > 0){
            transactionService.saveReceiptPrinted(lastShownReceiptId);
            lastShownReceiptId = null;
        }
    }
}