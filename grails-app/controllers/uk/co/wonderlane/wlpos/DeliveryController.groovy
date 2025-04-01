package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus

class DeliveryController {
    private static final String VALID = "Valid"
    private static final String INVALID_NO_MATCHING_PRODUCT_LIST = "Invalid - No matching delivery"
    private static final String INVALID_MULTIPLE_MATCHES = "Invalid - Multiple deliveries with this supplier reference"
    private static final String INVALID_DUPLICATE = "Invalid - Duplicate supplier reference"
    private static final String INVALID_DELIVERY_ALREADY_COMPLETE = "Invalid - Delivery has already been completed"

    def branchOrderService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        session.VALIDATIONRESULTS = []
        session.VALIDDELIVERIES = []
        redirect(action: "deliveries", model: [deliveries: session.VALIDATIONRESULTS])
    }

    def deliveries() {
        render(view: "deliveries", model: [deliveries: session.VALIDATIONRESULTS])
    }

    def ajaxCheckValidDeliveries(BranchOrderValidCommand command) {
        session.VALIDATIONRESULTS = []
        session.VALIDDELIVERIES = []
        def supplierReferences = JSON.parse(command.supplierReferences)
        for (String supplierReference : supplierReferences) {
            checkValidDelivery(supplierReference)
        }

        render(template: "deliveryImportResults", model: [deliveries: session.VALIDATIONRESULTS])
    }

    def checkValidDelivery(String supplierReference) {
        supplierReference = supplierReference.trim()
        if (session.VALIDDELIVERIES == null) {
            session.VALIDDELIVERIES = []
        }
        if (session.VALIDATIONRESULTS == null) {
            session.VALIDATIONRESULTS = []
        }

        ArrayList<BranchOrder> branchOrderList = branchOrderService.getBranchOrderBySupplierReference(supplierReference)

        def hasValidBranchOrders = branchOrderList != null && !branchOrderList.isEmpty()
        def supplierReferenceAlreadyExists = session.VALIDDELIVERIES.any { delivery -> delivery.supplierReference == supplierReference }

        def validationResult = VALID
        if (hasValidBranchOrders && branchOrderList.size() > 1) {
            validationResult = INVALID_MULTIPLE_MATCHES
        } else if (hasValidBranchOrders && branchOrderList.first().status == ProductListStatus.COMPLETE) {
            validationResult = INVALID_DELIVERY_ALREADY_COMPLETE
        } else if (hasValidBranchOrders && !supplierReferenceAlreadyExists) {
            session.VALIDDELIVERIES.add(branchOrderList.first())
        } else if (supplierReferenceAlreadyExists) {
            validationResult = INVALID_DUPLICATE
        } else {
            validationResult = INVALID_NO_MATCHING_PRODUCT_LIST
        }

        def result = BranchOrderResult.newInstance([
                supplierReference: supplierReference,
                valid            : hasValidBranchOrders && !supplierReferenceAlreadyExists,
                validationMessage: validationResult
        ])
        session.VALIDATIONRESULTS.add(result)
    }

    def ajaxCancelDeliveries() {
        session.VALIDATIONRESULTS = []
        session.VALIDDELIVERIES = []

        render(template: "deliveryImportResults", model: [deliveries: session.VALIDATIONRESULTS])
    }

    def ajaxImportDeliveries() {
        branchOrderService.setBranchOrdersScheduled(session.VALIDDELIVERIES)

        response.setStatus(200)
        render status: 200
    }
}

class BranchOrderValidCommand {
    String supplierReferences
}

class BranchOrderResult implements Serializable {
    String supplierReference
    boolean valid
    String validationMessage
}