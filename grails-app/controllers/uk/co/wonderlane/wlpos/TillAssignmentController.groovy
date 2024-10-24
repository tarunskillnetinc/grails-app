package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import java.security.SecureRandom

class TillAssignmentController {

    def springSecurityService
    def tillAssignmentService
    def storeService
    def configuration
    def editingTill = false

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        def stores = storeService.getStores(springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }

        [stores: stores]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchForTills() {
        Integer storeIdValue = params.storeIdFilter ? Integer.parseInt(params.storeIdFilter) : null
        Integer tillIdValue = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null
        String serialNumberValue = params.serialNumberFilter ? String.valueOf(params.serialNumberFilter) : null
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50

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

        def totalResults = tills.size()
        tills = tills.drop(offset).take(max)

        render (template: "tillSearchResults", model: [tillList: tills, offset: offset, max: max, totalResults: totalResults])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteTill(int storeId, int tillId, String serialNumber) {
        try {
            tillAssignmentService.deleteEntryForStoreIdAndTillId(storeId, tillId)
            tillAssignmentService.updateTillStockBySerial(serialNumber)
            render status: 200, text: "Till " + tillId + "has been deleted from Store"
        } catch (Exception e) {
            e.printStackTrace()
            render status: 500, text: "Error deleting Till from store"
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddTill() {
        def stores = storeService.getStores(springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }
        def unassignedSerialNumbers = tillAssignmentService.getUnassignedTillStock()

        render(template: "addTill", model: [stores: stores, serialNumbers: unassignedSerialNumbers, enableEdit: false])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditTill() {
        def stores = storeService.getStores(springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }
        def configuration = TillConfiguration.findByRetailerIdAndStoreIdAndTillId(springSecurityService.principal.retailerId, Integer.parseInt(params.get("storeId").toString()), Integer.parseInt(params.get("tillId").toString()))
        editingTill = true
        def unassignedSerialNumbers = tillAssignmentService.getUnassignedTillStock()

        // Append the selected Serial Number to the list
        def currentSerial = TillStock.findBySerialNumber(params.get("serialNumber").toString())
        if (currentSerial != null) {
            unassignedSerialNumbers.add(currentSerial)
        }

        render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: unassignedSerialNumbers, enableEdit: true])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxUnassignSerial() {
        def tillConfiguration = TillConfiguration.findById(params.tillConfigId)

        if (!tillConfiguration?.retailerId == springSecurityService.principal.retailerId) {
            render status: 404, text: "Till configuration not found."
            return
        }

        tillAssignmentService.updateTillStockBySerial(tillConfiguration.serialNumber)

        tillConfiguration.serialNumber = null
        tillAssignmentService.saveTill(tillConfiguration)

        render status: 200, text: "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveTill(AddEditTillCommand addEditTillCommand) {
        if (!addEditTillCommand.validate()) {
            def stores = storeService.getStores(springSecurityService.principal.retailerId)?.sort { it.config.storeNumber + " - " + it.config.storeName }
            def unusedSerialNumbers = tillAssignmentService.getUnassignedTillStock()

            render(status: 500, template: "addTill", model: [till: addEditTillCommand, stores: stores, serialNumbers: unusedSerialNumbers, enableEdit: editingTill])
        } else {
            TillConfiguration till

            if (addEditTillCommand.id) {
                till = TillConfiguration.findByRetailerIdAndId(springSecurityService.principal.retailerId, addEditTillCommand.id)
            } else {
                till = new TillConfiguration()

                till.scpTxnEndIndicator = ""
                till.pposControlBar = ""
                till.pin = 0
                till.dateTimeCreated = DateTime.now(DateTimeZone.UTC)
            }

            till.retailerId = springSecurityService.principal.retailerId
            till.storeId = addEditTillCommand.storeId
            till.tillId = addEditTillCommand.tillId
            till.description = addEditTillCommand.description
            till.serialNumber = addEditTillCommand.serialNumber
            till.pinExpiry = DateTime.now(DateTimeZone.UTC)
            till.dateTimeUpdated = DateTime.now(DateTimeZone.UTC)
            till.cashManagementEnabled = ("on" == addEditTillCommand.cashManagementEnabled)

            tillAssignmentService.saveTill(till)

            render "OK"
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxCancelTill() {
        editingTill = false
        configuration = null
        render "OK"
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGeneratePin() {
        def tillConfig = tillAssignmentService.getTill(Integer.parseInt(params.id))

        // Generate 8 digit code
        def random = new SecureRandom()
        int minimumValue = 10000000
        int maximumValue = 99999999
        int pin = random.nextInt((maximumValue - minimumValue) + 1) + minimumValue

        def expiry = DateTime.now(DateTimeZone.UTC).plusHours(1)

        tillAssignmentService.updateTillConfiguration(tillConfig?.serialNumber, pin, expiry)

        render "The registration code for this till is ${pin} and will expire in one hour."
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAdvancedConfiguration(String serialNumber) {
        configuration = TillConfiguration.findBySerialNumber(serialNumber)
        render(template: "advancedConfig", model: [config: configuration])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveAdvancedConfiguration() {
        configuration.scpTxnEndIndicator = params.get("scpTxnIndicator").toString()
        configuration.pposControlBar = params.get("pposControlBar").toString()

        //Numerical field protection against Non-numerical character input
        if (params.get("baudRate").toString() == "") {
            configuration.baudRate = 0
        } else {
            configuration.baudRate = Integer.parseInt(params.get("baudRate").toString())
        }

        configuration.pposAdmin = params.getBoolean("pposAdmin")
        configuration.pposRefund = params.getBoolean("pposRefund")
        configuration.pposSmartToken = params.getBoolean("pposSmartToken")
        configuration.printCardReceipts = params.getBoolean("printCardReceipts")

        if (configuration.validate()) {
            // Store the New Till within the Till Configuration table
            tillAssignmentService.saveTill(configuration)
            configuration = null
            render "OK"
        } else {
            render(template: "advancedConfig", model: [config: configuration])
        }
    }

    def isValidTillId(String tillIdStr) {
        if (tillIdStr == null || tillIdStr.isEmpty()) {
            return false
        }
        int id
        try {
            id = Integer.parseInt(tillIdStr)
        } catch (NumberFormatException ignore) {
            return false
        }
        return id >= 0
    }

    def addTillStockToList(String searchNo, def serialNumbers) {
        def tillStock = TillStock.findBySerialNumber(searchNo)
        if (tillStock != null) {
            serialNumbers.add(tillStock)
        }
    }
}

class AddEditTillCommand implements Validateable {

    def springSecurityService

    int id
    int tillId
    int storeId // Actually store number.
    String description
    String serialNumber
    String cashManagementEnabled

    static constraints = {
        id nullable: true
        tillId nullable: false, min: 1, validator: { val, obj ->
            // Can only check for till ID uniqueness per store, if a store has been selected. Sounds obvious, right?
            if (obj.storeId) {
                def existingTills = TillConfiguration.findAllByRetailerIdAndStoreIdAndTillId(obj.springSecurityService.principal.retailerId, obj.storeId, val)

                if (existingTills?.size() > 1 || (existingTills?.size() == 1 && existingTills?.first()?.id != obj.id)) {
                    // Another till exists with this till ID at this store.
                    return false
                }
            }
        }
        storeId nullable: false, min: 1 // Actually store number.
        description nullable: true
        cashManagementEnabled nullable:true
        serialNumber nullable: true, validator: { val, obj ->
            if (val) {
                def existingTills = TillConfiguration.findAllByRetailerIdAndSerialNumber(obj.springSecurityService.principal.retailerId, val)

                if (existingTills?.size() > 1 || (existingTills?.size() == 1 && existingTills?.first()?.id != obj.id)) {
                    // Another till exists with this serial number which isn't this one.
                    return false
                }
            }
        }
    }
}