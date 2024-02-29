package uk.co.wonderlane.wlpos

class LoyaltyController {

    def loyaltyMemberService

    def loyaltyMembers() { }

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
}
