package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime

import java.security.SecureRandom

class TillAssignmentController {

    def springSecurityService
    def tillAssignmentService
    def storeService
    def stores
    def configuration
    def editingTill = false

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        stores = storeService.getStores(springSecurityService.principal.retailerId)

        [stores: stores]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
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
    def saveNewTill(TillConfiguration tillConfiguration) {
        tillConfiguration.save()
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddTill() {
        stores = storeService.getStores(springSecurityService.principal.retailerId)
        def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)
        render(template: "addTill", model: [stores: stores, serialNumbers: serialNumbers, enableEdit: false])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxEditTill() {
        stores = storeService.getStores(springSecurityService.principal.retailerId)
        configuration = TillConfiguration.findByRetailerIdAndStoreIdAndTillId(springSecurityService.principal.retailerId, Integer.parseInt(params.get("storeId").toString()), Integer.parseInt(params.get("tillId").toString()))
        editingTill = true
        def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)

        // Append the selected Serial Number to the list
        def currentSerial = TillStock.findBySerialNumber(params.get("serialNumber").toString())
        if (currentSerial != null) {
            serialNumbers.add(currentSerial)
        }

        render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: serialNumbers, enableEdit: true])
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
    def ajaxSaveTill() {
        def serialNumbers = TillStock.findAllByRetailerIdAndStoreIdIsNullAndTillIdIsNull(springSecurityService.principal.retailerId)
        if (!params.containsKey("storeId")) {
            if (configuration != null) {
                def serial = TillStock.findBySerialNumber(configuration.serialNumber)
                if (serial != null) {
                    serialNumbers.add(serial)
                }
                render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: serialNumbers, saveStoreError: true, enableEdit: editingTill])
            } else {
                render(template: "addTill", model: [stores: stores, serialNumbers: serialNumbers, saveStoreError: true, enableEdit: editingTill])
            }
            return
        }

        //Non-Numerical inputs for TillID will be considered as Blank ("")
        if (params.get("tillId").toString().isBlank()) {
            if (configuration != null) {
                serialNumbers.add(TillStock.findBySerialNumber(configuration.serialNumber))
                render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: serialNumbers, saveTillError: true, enableEdit: editingTill])
            } else {
                render(template: "addTill", model: [stores: stores, serialNumbers: serialNumbers, saveTillError: true, enableEdit: editingTill])
            }
            return
        } else {
            // check that the Till ID hasn't been previously added
            def entry
            if (editingTill) {
                if (configuration.tillId != (Integer.parseInt(params.get("tillId").toString()))) {
                    entry = tillAssignmentService.getTillsByTillId(Integer.parseInt(params.get("tillId").toString()))
                }
            } else {
                entry = tillAssignmentService.getTillsByTillId(Integer.parseInt(params.get("tillId").toString()))
            }

            if (entry != null && entry.size != 0) {
                if (configuration != null) {
                    serialNumbers.add(TillStock.findBySerialNumber(configuration.serialNumber))
                    render(template: "addTill", model: [till: configuration, stores: stores, serialNumbers: serialNumbers, saveTillError: true, enableEdit: editingTill])
                } else {
                    render(template: "addTill", model: [stores: stores, serialNumbers: serialNumbers, saveTillError: true, enableEdit: editingTill])
                }
                return
            }
        }

        if (configuration != null) {
            // Check for existing Till Configuration for this serial number
            def existingConfig = TillConfiguration.findByRetailerIdAndStoreIdAndTillId(springSecurityService.principal.retailerId, configuration.storeId, configuration.tillId)
            if (existingConfig != null) {
                // Update the configuration with the new details (if any)
                existingConfig.storeId = Integer.parseInt(params.get("storeId").toString())
                existingConfig.tillId = Integer.parseInt(params.get("tillId").toString())
                existingConfig.description = params.get("description").toString()

                if (params.containsKey("serialNumber")) {
                    existingConfig.serialNumber = params.get("serialNumber").toString()
                    tillAssignmentService.updateTillStock(existingConfig)

                    // If the serial number has changed, update Till Stock to reflect the Serial Number becoming free
                    if (configuration.serialNumber != params.get("serialNumber").toString()) {
                        tillAssignmentService.updateTillStockBySerial(configuration.serialNumber)
                    }
                }
                existingConfig.dateTimeUpdated = DateTime.now()

                tillAssignmentService.saveTill(existingConfig)

                configuration = null
                editingTill = false
                render "OK"
            } else {
                configuration = null
                editingTill = false
                render(template: "addTill", model: [till: newTill, stores: stores, serialNumbers: serialNumbers])
            }
        } else {
            def newTill = new TillConfiguration()
            newTill.retailerId = springSecurityService.principal.retailerId
            newTill.storeId = Integer.parseInt(params.get("storeId").toString())
            newTill.tillId = Integer.parseInt(params.get("tillId").toString())
            newTill.description = params.get("description").toString()

            if (params.containsKey("serialNumber")) {
                newTill.serialNumber = params.get("serialNumber").toString()
            }

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
                // If we don't have a serial Number
                if (newTill.serialNumber != null) {
                    tillAssignmentService.updateTillStock(newTill)
                }
                render "OK"
            } else {
                render(template: "addTill", model: [till: newTill, stores: stores, serialNumbers: serialNumbers])
            }
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
        // Generate 8 digit code
        def random = new SecureRandom()
        int minimumValue = 10000000
        int maximumValue = 99999999
        int pin = random.nextInt((maximumValue - minimumValue) + 1) + minimumValue

        def expiry = DateTime.now().plusHours(2)

        tillAssignmentService.updateTillConfiguration(configuration.serialNumber, pin, expiry)

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
}
