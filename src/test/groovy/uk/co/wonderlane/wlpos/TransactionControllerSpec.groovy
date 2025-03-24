package uk.co.wonderlane.wlpos


import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

class TransactionControllerSpec extends Specification implements ControllerUnitTest<TransactionController> {


    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve start and end dates on index page"() {
        given:

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.startDate != null
        controllerResponse.endDate != null
    }

    //-------------------------------ajaxGetReceipts function Unit tests----------------------------//
    class FakeSearchResultList<T> extends ArrayList<T> {
        public int totalCount = 0
    }

    void "should retrieve receipts on request"() {
        given:
        controller.transactionService = Stub(TransactionService) {
            getReceipts(_, _, _, _, _, _) >> new FakeSearchResultList()
        }
        params.offset = offset
        params.max = max
        params.startDate = startDate
        params.endDate = endDate
        params.tillId = tillId
        params.transactionId = transactionId

        when: 'ajaxGetReceipts action is executed'
        controller.ajaxGetReceipts()

        then: 'ajaxGetReceipts action response is correct'
        response.status == HttpStatus.OK.value()
        model.receipts != null

        where:
        ID | startDate    | endDate      | tillId    | transactionId | offset | max
        1  | "01/01/2022" | "01/01/2023" | "0"       | "0"           | "100"  | "1000"
        2  | "01/01/2022" | "01/01/2023" | null      | null          | null   | null
        2  | "01/01/2022" | "01/01/2023" | "INVALID" | "INVALID"     | null   | null
    }

    //-------------------------------ajaxGetReceipt by id function Unit tests----------------------------//

    void "should retrieve receipt by id on request"() {
        given:
        Receipt testReceipt = new Receipt()
        ReceiptLine containsModifiers = new ReceiptLine(type: ReceiptLineType.MODIFIER)
        ReceiptLine firstHorizontalLine = new ReceiptLine(type: ReceiptLineType.H_LINE)
        ReceiptLine basketItem = new ReceiptLine(type: ReceiptLineType.BASKET_ITEM)
        ReceiptLine vatItem = new ReceiptLine(type: ReceiptLineType.VAT_ITEM)
        containsModifiers.setId(100)
        firstHorizontalLine.setId(101)
        basketItem.setId(102)
        vatItem.setId(103)

        testReceipt.receiptLines = new HashSet<>()
        testReceipt.receiptLines.add(containsModifiers)
        testReceipt.receiptLines.add(firstHorizontalLine)
        testReceipt.receiptLines.add(basketItem)
        testReceipt.receiptLines.add(vatItem)

        controller.transactionService = Stub(TransactionService) {
            getReceipt(_) >> testReceipt
        }

        // mock the view since TagLibs are not supported in tests
        def mockView = '<div class="receipt"> </div>'
        views['/receipt/_receipt.gsp'] = mockView

        when: 'ajaxGetReceipt action is executed'
        controller.ajaxGetReceipt(100)

        then: 'ajaxGetReceipt action response is correct'
        response.status == HttpStatus.OK.value()
        model.receipt != null
        model.containsModifiers
        model.firstHorizontalLineId == 101
        model.maxTotalLength == null
        model.maxVatLength == null
    }

    void "should retrieve receipt by id on request without receipt lines"() {
        given:
        Receipt testReceipt = new Receipt()
        testReceipt.receiptLines = new HashSet<>()

        controller.transactionService = Stub(TransactionService) {
            getReceipt(_) >> testReceipt
        }

        // mock the view since TagLibs are not supported in tests
        def mockView = '<div class="receipt"> </div>'
        views['/receipt/_receipt.gsp'] = mockView

        when: 'ajaxGetReceipt action is executed'
        controller.ajaxGetReceipt(100)

        then: 'ajaxGetReceipt action response is correct'
        response.status == HttpStatus.OK.value()
        model.receipt != null
        !model.containsModifiers
        model.firstHorizontalLineId == -1
        model.maxTotalLength == null
        model.maxVatLength == null
    }

}
