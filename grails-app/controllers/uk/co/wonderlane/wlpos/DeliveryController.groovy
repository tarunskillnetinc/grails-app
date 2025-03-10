package uk.co.wonderlane.wlpos

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured

class DeliveryController {
    def branchOrderService
    List<BranchOrderResult> deliveryValidationResults = []
    List<BranchOrder> validDeliveries = []

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        deliveryValidationResults = []
        validDeliveries = []
        redirect(action: "deliveries", model: [deliveries: deliveryValidationResults])
    }

    def deliveries() {
        render(view: "deliveries", model: [deliveries: deliveryValidationResults])
    }

    def ajaxCheckValidDeliveries(BranchOrderValidCommand command) {
        def supplierReferences = JSON.parse(command.supplierReferences)
        for (String supplierReference : supplierReferences) {
            checkValidDelivery(supplierReference)
        }

        render(template: "deliveryImportResults", model: [deliveries: deliveryValidationResults])
    }

    def checkValidDelivery(String supplierReference) {
        ArrayList<BranchOrder> branchOrderList = branchOrderService.getBranchOrderBySupplierReference(supplierReference)
        def hasValidBranchOrders = branchOrderList != null && !branchOrderList.isEmpty()
        def result = BranchOrderResult.newInstance([
                supplierReference: supplierReference,
                valid            : hasValidBranchOrders
        ])
        if (hasValidBranchOrders) {
            validDeliveries.add(branchOrderList.first())
        }

        deliveryValidationResults.add(result)
    }

    def ajaxCancelDeliveries() {
        deliveryValidationResults = []
        validDeliveries = []

        render(template: "deliveryImportResults", model: [deliveries: deliveryValidationResults])
    }

    def ajaxImportDeliveries() {
        branchOrderService.setBranchOrdersScheduled(validDeliveries)

        response.setStatus(200)
        render status: 200
    }
}

class BranchOrderValidCommand {
    String supplierReferences
}

class BranchOrderResult {
    String supplierReference
    boolean valid
}