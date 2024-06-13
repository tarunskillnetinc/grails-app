package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import uk.co.wonderlane.wlpos.entities.BarcodeSignifier
import uk.co.wonderlane.wlpos.enums.BarcodeSignifierType

class BarcodeConfigController {

    def springSecurityService
    def barcodeSignifierService

    static final int MAX = 50;

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        [signifierTypes: BarcodeSignifierType.values()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchForBarcodeSignifiers() {

        String typeValue = params.typeFilter ? params.typeFilter : null
        String patternValue = params.patternFilter ? params.patternFilter : null
        String descriptionValue = params.descriptionFilter ? params.descriptionFilter: null
        Integer retailerIdValue = springSecurityService.principal.retailerId

        def sortParams = [:]

        if (!params.sort) {
            sortParams = [max: MAX, offset: 0, sort: "storeNumber", order: "ASC"]
        } else {
            sortParams.max = Integer.parseInt(params.max)
            sortParams.offset = Integer.parseInt(params.offset)
            sortParams.sort = params.sort
            sortParams.order = params.order
        }

        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : MAX

        String sortBy = null;
        if (params.sort != null && params.order != null) {
            sortBy = String.format("%s %s", params.sort , params.order)
        }

        def signifiers = barcodeSignifierService.getSignifiersByFilters(retailerIdValue, typeValue,
                patternValue, descriptionValue, sortBy)

        def totalResults = signifiers.size()
        signifiers = signifiers.drop(offset).take(max);

        render (template: "signifiersSearchResults", model: [signifiers: signifiers, sortParams: sortParams, offset: offset, max: max, totalResults: totalResults])
    }
}
