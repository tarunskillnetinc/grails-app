package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class LoyaltyController {

    def loyaltyService
    def loyaltyMemberService

    def index() {}
    def loyaltyMembers() {}
    def loyaltySegment() {}

    def showMemberDetails(String cardNumber) {
        def member = loyaltyMemberService.findByCardNumber(cardNumber)
        render(view: "loyaltyMemberDetails", model: [member: member])
    }

    def memberUpdateSave() {
        String cardNumber
        String firstName
        String lastName
        String email
        String mobile_no
        Boolean updated = false

        try {
            cardNumber = params.cardNumber
            firstName = params.firstName
            lastName = params.lastName
            email = params.email
            mobile_no = params.mobile_no
        } catch (Exception e) {
            e.printStackTrace()
            response.status = 400
            return
        }

        def member = loyaltyMemberService.findByCardNumber(cardNumber)

        if (member) {
            if (member.firstName != firstName) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "firstName", firstName)
            }

            if (member.lastName != lastName) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "lastName", lastName)
            }

            if (member.email != email) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "email", email)
            }

            if (member.mobile_no != mobile_no) {
                updated = true
                loyaltyMemberService.updateMemberField(cardNumber, "mobile_no", mobile_no)
            }
        }

        if (updated) {
            flash.message = "Member updated successfully"
        }

        redirect(action: "loyaltyMembers")
    }

    /* Called from the membership management page when searching for loyalty members */
    def ajaxSearchMembers() {
        String searchBy
        String searchTerm
        Integer max
        Integer offset
        String sortColumn
        String sortOrder
        def results

        try {
            searchBy = params.searchBy
            searchTerm = params.searchTerm
            max = params.max ? Integer.parseInt(params.max) : null
            offset = params.offset ? Integer.parseInt(params.offset) : null
            sortColumn = validateSortColumn(params.sortColumn)
            sortOrder = validateSortOrder(params.sortOrder)
        } catch (Exception e) {
            e.printStackTrace()
            response.status = 400
            return
        }

        if (searchBy == "email") {
            results = loyaltyMemberService.findByEmail(searchTerm)
        } else {
            results = loyaltyMemberService.findByCardNumber(searchTerm, max, offset, sortColumn, sortOrder)
        }

        render(template: "loyaltySearchResults", model: [members: results["members"],
                                                         searchTerm: params.searchTerm,
                                                         offset: params.offset,
                                                         max: params.max,
                                                         sortColumn: params.sortColumn,
                                                         sortOrder: params.sortOrder,
                                                         totalResults: results["totalResults"]])
    }

    private String validateSortColumn(String sortColumn) {
        def availableColumns = [ "cardNumber", "email", "firstName", "lastName" ]

        if (!sortColumn) {
            return null
        } else if (availableColumns.contains(sortColumn)) {
            return sortColumn
        } else {
            throw new RuntimeException("Bad request")
        }
    }

    private String validateSortOrder(String sortOrder) {
        def availableOrders = [ "asc", "desc" ]

        if (!sortOrder) {
            return null
        } else if (availableOrders.contains(sortOrder.toLowerCase())) {
            return sortOrder
        } else {
            throw new RuntimeException("Bad request")
        }
    }

    def ajaxSearchLoyaltySegment() {
        int defaultPagination = 20
        int defaultOffSet = 0
        try {
            def segment = loyaltyService.getSegment(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : defaultPagination,
                    params.offset ? Integer.parseInt(params.offset) : defaultOffSet, "id", "asc")

            render(template: "loyaltySegmentSearchResults", model: [segments    : segment?.segments,
                                                                    loyaltySegmentTerm  : params.loyaltySegmentTerm,
                                                                    loyaltySegmentSearchBy    : params.loyaltySegmentSearchBy,
                                                                    max         : params.max ?: defaultPagination,
                                                                    offset      : params.offset ?: defaultOffSet,
                                                                    totalCount  : segment?.totalCount
            ])
        }catch(Exception ex){
            ex.printStackTrace()
            log.error("Error when loading loyalty segment search results, Search by " + params.searchBy + " search term " + params.searchTerm + " Exception "  + ex)
            response.setStatus(500)
            render (view: "_loyaltyGenericError", contentType: "text/html", model: [
                                                                                    error_header : "Loyalty Segment Search Error",
                                                                                    error_body   : "Error when loading loyalty segment"
            ])
        }
    }
}