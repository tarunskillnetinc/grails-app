package uk.co.wonderlane.wlpos

import io.micronaut.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.basketv2.BasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.BasketUser
import uk.co.wonderlane.wlpos.entities.basketv2.DiscountBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.PaidInBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.PaidOutBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.PayPointBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.ProductBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.PromotionBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.ReduceToClearBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.RefundBasketItem
import uk.co.wonderlane.wlpos.entities.basketv2.SimpleDiscountBasketItem
import uk.co.wonderlane.wlpos.entities.transactionv2.BasketTransaction
import uk.co.wonderlane.wlpos.entities.transactionv2.TillControlEvent
import uk.co.wonderlane.wlpos.enums.ReceiptLineType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.reporting.TransactionBasketItem

class TransactionController {

    def transactionService
    def springSecurityService
    def storeService
    def basketTransactionService
    int lastShownReceiptId

    def index() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/YYYY").withZone(DateTimeZone.UTC)

        DateTime startDate = params.startDate ? DateTime.parse(params.startDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        DateTime endDate = params.endDate ? DateTime.parse(params.endDate, dateFormatter) : DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [
                startDate    : startDate,
                endDate      : endDate,
                tillId       : params.tillId,
                transactionId: params.transactionId,
                sort         : params.sort,
                order        : params.order,
                offset       : params.offset,
                max          : params.max
        ]
    }

    private def populateTransactionBasketItems(List<BasketItem> basketItems) {
        Map<Integer, TransactionBasketItem> transactionBasketItems = [:]
        BigDecimal preDiscountTotal = 0
        BigDecimal discountableAmount = 0
        def priorAddedSeqNum = 0

        basketItems.eachWithIndex { basketItem, seqNum ->
            if (basketItem instanceof ProductBasketItem || basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem) {
                def transactionBasketItem = new TransactionBasketItem()

                transactionBasketItem.seqNum = basketItem.id

                if (basketItem instanceof PaidInBasketItem) {
                    transactionBasketItem.type = "ReasonCodeType." + basketItem.paidInReason.type
                } else if (basketItem instanceof PaidOutBasketItem) {
                    transactionBasketItem.type = "ReasonCodeType." + basketItem.paidOutReason.type
                } else {
                    transactionBasketItem.type = "BasketItemType." + basketItem.type
                }

                if (basketItem.voided) {
                    transactionBasketItem.type = "Voided"
                }

                transactionBasketItem.entryMethod = basketItem.scanned ? "Scanned" : "Key-in"

                if (basketItem instanceof ProductBasketItem) {
                    transactionBasketItem.productCode = basketItem.product?.itemCode
                } else {
                    transactionBasketItem.productCode = "-"
                }

                if (basketItem instanceof ProductBasketItem) {
                    transactionBasketItem.productDescription = basketItem.product?.description
                } else if (basketItem instanceof PaidInBasketItem) {
                    transactionBasketItem.productDescription = basketItem.paidInReason.description
                } else if (basketItem instanceof PaidOutBasketItem) {
                    transactionBasketItem.productDescription = basketItem.paidOutReason.description
                } else {
                    transactionBasketItem.productDescription = "-"
                }

                if (basketItem.barcodeScanned) { // if this top level is set, use that
                    transactionBasketItem.barcode = basketItem.barcodeScanned
                } else if (basketItem instanceof ProductBasketItem) {
                    transactionBasketItem.barcode = basketItem.product.variants[0].barcodes[0]
                } else {
                    transactionBasketItem.barcode = "-"
                }

                transactionBasketItem.qty = basketItem.qty ?: "-"

                if (basketItem.qty && basketItem instanceof ProductBasketItem) {
                    transactionBasketItem.unitPrice = basketItem.product.variants[0].retailPrice
                } else {
                    transactionBasketItem.unitPrice = null
                }

                transactionBasketItem.totalPrice = (basketItem.total ?: BigDecimal.ZERO)

                if (basketItem instanceof ProductBasketItem && basketItem.priceDetails && basketItem.priceDetails.size() > 0) {
                    def vat_individual_total = new BigDecimal(0)
                    basketItem.priceDetails.each { detail ->
                        vat_individual_total += detail.vatAmount
                    }
                    transactionBasketItem.vat = vat_individual_total
                } else {
                    transactionBasketItem.vat = null
                }

                if (basketItem instanceof ProductBasketItem) {
                    def ageValue = (basketItem.product?.category?.restrictions?.buyerAgeRestriction ?: 0 > (basketItem.product?.restrictions?.buyerAgeRestriction ?: 0)
                            ? basketItem.product?.category?.restrictions?.buyerAgeRestriction : basketItem.product?.restrictions?.buyerAgeRestriction)
                    transactionBasketItem.ageVerification = ageValue ?: "-"
                } else {
                    transactionBasketItem.ageVerification = "-"
                }

                if (basketItem instanceof RefundBasketItem) {
                    transactionBasketItem.returnReason = basketItem.refundReason?.description
                } else {
                    transactionBasketItem.returnReason = "-"
                }

                if (basketItem instanceof ReduceToClearBasketItem) {
                    transactionBasketItem.rtc = "&#10003;"
                } else {
                    transactionBasketItem.rtc = "-"
                }

                transactionBasketItem.promotionsType = "-"

                transactionBasketItems[transactionBasketItem.seqNum] = transactionBasketItem

                priorAddedSeqNum = transactionBasketItem.seqNum

                if (basketItem.product?.restrictions?.discountAllowed && basketItem.product?.restrictions?.discountAllowed == true) {
                    discountableAmount = discountableAmount + basketItem.total
                }
                preDiscountTotal = preDiscountTotal + basketItem.total
            }

            if (basketItem instanceof PromotionBasketItem) {
                transactionBasketItems[priorAddedSeqNum].promotionsType = basketItem.promotion?.receiptDescription ?: "-"
            }

            if (basketItem instanceof PayPointBasketItem) {
                def transactionBasketItem = new TransactionBasketItem()

                transactionBasketItem.seqNum = seqNum
                transactionBasketItem.type = "BasketItemType." + basketItem.type
                transactionBasketItem.entryMethod = basketItem.scanned ? "Scanned" : "Key-in"
                transactionBasketItem.productCode = message(code: "PPItemType." + basketItem.itemType)
                transactionBasketItem.productDescription = basketItem.basketDescription
                transactionBasketItem.barcode = basketItem.barcodeScanned ?: "-"
                transactionBasketItem.qty = null
                transactionBasketItem.unitPrice = null
                transactionBasketItem.totalPrice = basketItem.total ?: BigDecimal.ZERO
                transactionBasketItem.vat = null
                transactionBasketItem.ageVerification = "-"
                transactionBasketItem.returnReason = "-"
                transactionBasketItem.priceChange = null
                transactionBasketItem.rtc = "-"
                transactionBasketItem.promotionsType = "-"
                transactionBasketItem.voided = "-"

                transactionBasketItems[transactionBasketItem.seqNum] = transactionBasketItem

                priorAddedSeqNum = transactionBasketItem.seqNum

                discountableAmount = discountableAmount + basketItem.total
                preDiscountTotal = preDiscountTotal + basketItem.total
            }
        }

        return [transactionBasketItems: transactionBasketItems, preDiscountTotal: preDiscountTotal, discountableAmount: discountableAmount]
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
            BasketUser basketuser = basketTransaction?.getUser()

            def user = User.findByRetailerIdAndUsername(receipt.retailerId, basketuser.username)
            // Assuming that I'm using the User from the DB in priority.

            if (!user) { // The user appears to have disappeared. Unlikely event.
                user = new User()
                user.setName(basketuser?.name)
                user.setId(basketuser?.id)
                user.setRetailerUserId(basketuser?.retailerUserId)
            }

            def eventLines = []
            basketTransaction?.getBasket()?.getTillControlEvents()?.forEach { event ->
                def seqnum
                def basketItem

                if (event.basketItemId != null) {
                    seqnum = event.basketItemId + 1
                } else {
                    seqnum = "-"
                }

                def amountchange = event.amount
                if (event.type == TillControlEventType.MARKDOWN) {
                    amountchange = -amountchange
                }

                eventLines.add([eventType        : event.type,
                                seqnum: seqnum, // pull from product/variant, somehow.
                                amount: amountchange,
                                reason           : event.reason ?: "-",
                                overrideUsersName: event.overrideUser?.name ?: user?.name])
            }

            def discountCard = fetchDiscountCard(basketTransaction)
            def basketItemsEtc = populateTransactionBasketItems(basketTransaction?.getBasket()?.getBasketItems())

            def postDiscountsTotal = basketItemsEtc.preDiscountTotal
            def discountableAmount = basketItemsEtc.discountableAmount

            def promotionItems = []
            basketTransaction?.getBasket()?.getBasketItems()?.forEach { item ->
                if (item instanceof PromotionBasketItem) {
                    def description = item.promotion.receiptDescription
                    def amount = -item.totalSavings

                    postDiscountsTotal -= item.totalSavings

                    promotionItems.add([description: description, amount: amount])
                }
            }

            def discountItems = []
            basketTransaction?.getBasket()?.getBasketItems()?.forEach { item ->
                if (item instanceof DiscountBasketItem || item instanceof SimpleDiscountBasketItem) {
                    def description = item.receiptDescription
                    def amount;

                    if (item.total) {
                        amount = -item.total
                        postDiscountsTotal -= item.total
                    } else if (item.discountPercentage) { // calculate the amount the discount card will yield.
                        BigDecimal percentage = new BigDecimal(item.discountPercentage).divide(new BigDecimal(100))
                        def discountAmount = discountableAmount * percentage

                        amount = -discountAmount
                        postDiscountsTotal -= discountAmount
                        discountableAmount -= discountAmount
                    }

                    discountItems.add([description: description, amount: amount])
                }
            }

            def grandTotal = postDiscountsTotal

            [
                    user              : user,
                    basketTransaction : basketTransaction,
                    basket            : basketTransaction.basket,
                    basketItems           : basketTransaction.basket.basketItems,
                    transactionBasketItems: basketItemsEtc.transactionBasketItems,
                    store             : store,
                    receipt           : receipt,
                    eventLines        : eventLines,
                    discountCard      : discountCard,
                    discountItems     : discountItems,
                    promotionItems    : promotionItems,
                    preDiscountTotal  : basketItemsEtc.preDiscountTotal,
                    discountableAmount: basketItemsEtc.discountableAmount,
                    postDiscountsTotal: postDiscountsTotal,
                    grandTotal        : grandTotal,
                    startDate         : params.startDate, // these params are used to construct the URL pointing back to the main search.
                    endDate           : params.endDate,
                    tillId            : params.tillId,
                    transactionId     : params.transactionId,
                    sort              : params.sort,
                    order             : params.order,
                    offset            : params.offset,
                    max               : params.max
            ]
        }
        catch (Exception ex) {
            def inputErrors = "Transaction details could not be fetched.<br/>"
            inputErrors += "<pre>" + exceptionToString(ex) + "</pre>"
            flash.error = "The selected transaction is not able to be viewed on this page and may be older data."

            [exception: inputErrors]
        }
    }

    private String fetchDiscountCard(BasketTransaction basketTransaction) {
        def discountCard = "-"
        basketTransaction?.getBasket()?.getBasketItems()?.forEach { item ->
            if (item instanceof DiscountBasketItem) {
                discountCard = item.cardNumber
            }
        }
        return discountCard
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

        def availableColumns = ["storeId", "tillId", "dateGenerated", "transactionId", "transactionAmount", "paymentMethod"]

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
                    totalCount     : totalCount,
                    sort           : sort,
                    order          : order,
                    offset         : offset,
                    max            : max,
                    startDate      : params.startDate,
                    endDate        : params.endDate,
                    tillId         : params.tillId,
                    transactionId  : params.transactionId,
                    totalReceiptLineType: ReceiptLineType.TOTAL
            ])
        }
    }

    def ajaxGetReceipt(int receiptId) {
        def receipt = transactionService.getReceipt(receiptId)
        lastShownReceiptId = receiptId;

        render(template: "receipt", model: [receipt              : receipt,
                                            containsModifiers    : receipt.receiptLines.find { it.type == ReceiptLineType.MODIFIER } ?: false,
                                            firstHorizontalLineId: receipt.receiptLines.sort { it.id }.find { it.type == ReceiptLineType.H_LINE }?.id ?: -1,
                                            maxTotalLength       : receipt.receiptLines?.findAll { it.type == ReceiptLineType.BASKET_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0,
                                            maxVatLength         : receipt.receiptLines?.findAll { it.type == ReceiptLineType.VAT_ITEM }?.max { it.total?.toString()?.length() }?.total?.toString()?.length() ?: 0])
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

    def saveReceiptPrinted() {
        if (lastShownReceiptId > 0) {
            transactionService.saveReceiptPrinted(lastShownReceiptId);
            lastShownReceiptId = null;
        }
    }
}