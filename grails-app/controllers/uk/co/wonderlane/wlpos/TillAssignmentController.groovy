package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

import java.text.SimpleDateFormat

class TillAssignmentController {

    def springSecurityService
    def tillAssignmentService
    def stores

    def index() {
        stores = StoreSettings.findAllByRetailerIdAndStoreIdIsNotNull(springSecurityService.principal.retailerId)

        [stores: stores]
    }

    def ajaxSearchForTills() {
        Integer storeIdValue = params.storeIdFilter ? Integer.parseInt(params.storeIdFilter) : null
        Integer tillIdValue = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null
        String serialNumberValue = params.serialNumberFilter ? String.valueOf(params.serialNumberFilter) : null

        /* There's several different combinations of filters we can have:
        1. Store ID + Till ID + Serial Number
        2. Store ID + Till ID
        3. Store ID + Serial Number
        4. Till ID + Serial Number
        5. Store ID Only
        6. Till ID Only
        7. Serial Number Only
        8. No filters at all
        */

        def tills;
        if (storeIdValue != null && tillIdValue != null && serialNumberValue != null) { // 1
            tills = tillAssignmentService.getTillsByAllFilters(storeIdValue, tillIdValue, serialNumberValue)
        } else if (storeIdValue != null && tillIdValue != null) { // 2
            tills = tillAssignmentService.getTillsByStoreIdAndTillId(storeIdValue, tillIdValue)
        } else if (storeIdValue != null && serialNumberValue != null) { // 3
            tills = tillAssignmentService.getTillsByStoreIdAndSerialNumber(storeIdValue, serialNumberValue)
        } else if (tillIdValue != null && serialNumberValue != null) { // 4
            tills = tillAssignmentService.getTillsByTillIdAndSerialNumber(tillIdValue, serialNumberValue)
        } else if (storeIdValue != null) { // 5
            tills = tillAssignmentService.getTillsByStoreId(storeIdValue)
        } else if (tillIdValue != null) { // 6
            tills = tillAssignmentService.getTillsByTillId(tillIdValue)
        } else if (serialNumberValue != null) { // 7
            tills = tillAssignmentService.getTillsBySerialNumber(serialNumberValue)
        } else { // 8
            tills = tillAssignmentService.getTills()
        }

        render (template: "tills", model: [tillList: tills])
    }

    def ajaxDeleteTill(int storeId, int tillId, String serialNumber) {
        try {
            tillAssignmentService.deleteEntryForStoreIdAndTillId(storeId, tillId)
            tillAssignmentService.updateTillStock(serialNumber)
            render status: 200, text: "Till " + tillId + "has been deleted from Store"
        } catch (Exception e) {
            e.printStackTrace()
            render status: 500, text: "Error deleting Till from store"
        }
    }

    def saveNewTill(TillConfiguration tillConfiguration){
        tillConfiguration.save()
    }

    def ajaxAddTill() {
        def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)
        render(template: "addTill", model: [stores: stores, serialNumbers: serialNumbers, enableEdit: false])
    }

    def ajaxEditTill() {
        def configuration = TillConfiguration.findBySerialNumber(params.get("serialNumber").toString())
        def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)

        //Append the selected Serial Number to the list
        serialNumbers.add(TillStock.findBySerialNumber(params.get("serialNumber").toString()))

        render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: serialNumbers, enableEdit: true])
    }

    def ajaxSaveTill() {
        def newTill = new TillConfiguration()

        newTill.retailerId = springSecurityService.principal.retailerId
        newTill.storeId = Integer.parseInt(params.get("storeId").toString())
        newTill.tillId = Integer.parseInt(params.get("tillId").toString())
        newTill.description = params.get("description").toString()
        newTill.serialNumber = params.get("serialNumber").toString()

        // Set temp values to be done via other modals / generated later
        newTill.scpTxnEndIndicator = ""
        newTill.pposControlBar = ""
        newTill.pposAdmin = false
        newTill.pposRefund = false
        newTill.pposSmartToken = false
        newTill.printCardReceipts = false
        newTill.baudRate = 0
        newTill.pin = 0
        newTill.pinExpiry = DateTime.now()
        newTill.dateTimeCreated = DateTime.now()
        newTill.dateTimeUpdated = DateTime.now()

        if (newTill.validate()) {
            // Store the New Till within the Till Configuration table
            tillAssignmentService.saveTill(newTill)
            tillAssignmentService.updateTillStock(newTill)
            render "OK"
        } else {
            def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)
            render(template: "addTill", model: [till: newTill, stores: stores, serialNumbers: serialNumbers])
        }
    }

    def ajaxGeneratePin() {
        // Generate 8 digit code
        def random = new Random()
        int minimumValue = 10000000
        int maximumValue = 99999999
        int pin = random.nextInt((maximumValue - minimumValue) + 1) + minimumValue

        def expiry = DateTime.now().plusHours(1)

        tillAssignmentService.updateTillConfiguration(params.get("serialNumber").toString(), pin, expiry)

        render "The registration code for this till is ${pin} and will expire in one hour."

    }
}
