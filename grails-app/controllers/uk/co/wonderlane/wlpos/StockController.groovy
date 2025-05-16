package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured

class StockController {
    def springSecurityService

    def storeService

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
        def selectedStores = params.list('selectedStore') // always gives you a list
        def storeNumber
        def storeName
        selectedStores.each { value ->
            def parts = value.split("-", 2)
            storeNumber = parts[0]?.trim()
            storeName = parts[1]?.trim()
        }
        render(template: "resetInventory", model: [storeName: storeName,storeNumber:storeNumber])

    }


}
