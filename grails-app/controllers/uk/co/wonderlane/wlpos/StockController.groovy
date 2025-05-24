package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

import java.util.UUID
import java.util.Date
import uk.co.wonderlane.wlpos.entities.Job
import uk.co.wonderlane.wlpos.enums.JobType
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import grails.converters.JSON

class StockController {
    def springSecurityService
    def storeService
    def rabbitService
    def stockService
    

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {

        [storeNumberFilter: params.storeNumberFilter,
         storeNameFilter: params.storeNameFilter,
         showDeletedFilter: params.showDeletedFilter,
         max: params.max,
         offset: params.offset,
         sort: params.sort,
         order: params.order]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetStores() {
        Integer storeNumberFilter
        String storeNameFilter
        boolean showDeletedFilter = false

        def sortParams = [:]

        try {
            if (!params.sort) {
                sortParams = [max: 50, offset: 0, sort: "storeNumber", order: "ASC"]
            } else {
                sortParams.max = Integer.parseInt(params.max)
                sortParams.offset = Integer.parseInt(params.offset)
                sortParams.sort = params.sort
                sortParams.order = params.order
            }

            if (params.storeNumberFilter && params.storeNumberFilter.isNumber()) {
                storeNumberFilter = Integer.parseInt(params.storeNumberFilter)
            }
            if (params.storeNameFilter && params.storeNameFilter != "null") {
                storeNameFilter = params.storeNameFilter
            }
            if (params.showDeletedFilter == "true") {
                showDeletedFilter = true
            }

            def (stores, storeCount) = storeService.searchStores(springSecurityService.principal.retailerId, storeNumberFilter, storeNameFilter, showDeletedFilter, sortParams)

            render(template: "storeSearchResults", model: [stores: stores,
                                                           totalResults: storeCount,
                                                           sortParams: sortParams,
                                                           storeNameFilter: storeNameFilter ?: "",
                                                           storeNumberFilter: storeNumberFilter ?: "",
                                                           showDeletedFilter: showDeletedFilter])
        } catch (Exception e) {
            render status: 500, text:" Error searching for stores."
        }
    }
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxResetInventoryTemplate() {
        def selectedStores = params.list('selectedStore')
        def storeNumber
        def storeName
        def selectedStoreId = null // Initialize selectedStoreId

        selectedStores.each { value ->
            def parts = value.split("-", 2)
            storeNumber = parts[0]?.trim()
            storeName = parts[1]?.trim()

            // Attempt to find the store by store number to get its ID
            def store = storeService.getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber?.toInteger())
            if (store) {
                selectedStoreId = store.id // Store the selected store's ID
            }
        }

        // Store the selected store ID in the session
        session.selectedStoreIdForReset = selectedStoreId

        render(template: "resetInventory", model: [storeName: storeName,storeNumber:storeNumber])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def resetInventory() {
        // 1. Receive Input and Basic Validation
        def storeNumber = params.storeNumber
        def username = params.username

        if (!storeNumber || !username) {
            render status: 400, text: "Missing required parameters."
            return
        }

        // Retrieve the selected store ID from the session
        def selectedStoreIdInteger = session.selectedStoreIdForReset
        if (!selectedStoreIdInteger) {
             render status: 400, text: "Selected store information not found in session. Please re-select the store."
             return
        }


        // 2. Retrieve Selected Store based on ID from session
        def selectedStore = storeService.getStore(springSecurityService.principal.retailerId, selectedStoreIdInteger)
        if (!selectedStore) {
            render status: 400, text: "Invalid selected store ID from session."
            return
        }

        // 3. Validate Store Number against the selected store's number
        // Assuming store number is stored in config as JSON_EXTRACT(config, '$.storeNumber')
//        def storeConfig = selectedStore.config ? new JSON(selectedStore.config).parse() : [:]
        def actualStoreNumber = selectedStore.retailerStoreId?.toString() // Convert to string for comparison

        if (storeNumber != actualStoreNumber) {
             render status: 400, text: "Provided Store ID does not match the selected store."
             return
        }

        // 4. Validate Username
        def loggedInUsername = springSecurityService.principal.username
        if (username != loggedInUsername) {
            render status: 400, text: "Provided Username does not match the logged-in user."
            return
        }

        // 5. Initiate Stock Reset Job via Service
        def jobInstance = stockService.initiateStockResetJob(selectedStore, loggedInUsername, springSecurityService.principal.retailerId)

        if (!jobInstance) {
            render status: 500, text: "Failed to initiate stock reset job."
            return
        }

        // 6. Return Success
        render status: 200, text: "Stock reset job initiated successfully."
    }
}
