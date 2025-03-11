package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured

class DeliveryController {
    def branchOrderService
    //List<BranchOrderResult> deliveryValidationResults = []
    //List<BranchOrder> validDeliveries = []

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
        def supplierReferences = JSON.parse(command.supplierReferences)
        for (String supplierReference : supplierReferences) {
            checkValidDelivery(supplierReference)
        }

        render(template: "deliveryImportResults", model: [deliveries: session.VALIDATIONRESULTS])
    }

    def checkValidDelivery(String supplierReference) {
        ArrayList<BranchOrder> branchOrderList = branchOrderService.getBranchOrderBySupplierReference(supplierReference)
        def hasValidBranchOrders = branchOrderList != null && !branchOrderList.isEmpty()
        def result = BranchOrderResult.newInstance([
                supplierReference: supplierReference,
                valid            : hasValidBranchOrders
        ])
        if (hasValidBranchOrders) {
            if (session.VALIDDELIVERIES == null) {
                session.VALIDDELIVERIES = []
            }
            session.VALIDDELIVERIES.add(branchOrderList.first())
        }

        if (session.VALIDATIONRESULTS == null) {
            session.VALIDATIONRESULTS = []
        }
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
}