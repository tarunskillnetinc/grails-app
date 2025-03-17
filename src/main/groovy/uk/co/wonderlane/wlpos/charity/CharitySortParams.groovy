package uk.co.wonderlane.wlpos.charity

import grails.validation.Validateable

class CharitySortParams implements Validateable {

    int max = 50
    int offset = 0
    String sortColumn = "organisationName"
    String sortOrder = "asc"

    void validateParams(def availableSortColumns) {
        if (max < 1 || max > 500) {
            max = 50
        }

        if (offset < 0 || offset > 5000) {
            offset = 0
        }

        if (!sortColumn || !availableSortColumns.contains(sortColumn)) {
            sortColumn = availableSortColumns.get(0)
        }

        if (!sortOrder || (!sortOrder.equals("asc") && !sortOrder.equals("desc"))) {
            sortOrder = "asc"
        }
    }
}