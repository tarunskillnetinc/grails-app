package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.transactions.Shift

@Transactional("transactions")
class CashReportingService {

    def springSecurityService
    def storeService

    List<Shift> getFinalisedShiftsForTill(Integer storeNumber, int tillId, DateTime startDate, DateTime endDate) {

        int storeId
        if (storeNumber) {
            storeId = storeService.getStoreIdByStoreNumber(storeNumber)
        } else {
            storeId = springSecurityService.principal.storeId
        }

        def criteria = Shift.withTransaction { Shift.createCriteria() }
        return criteria.list([sort: "shiftNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)
            eq("tillId", tillId)
            eq("shiftStatus", ShiftStatus.FINALISED.toString())
            between("dateCreated", startDate, endDate)
        }
    }

    Shift getShiftForShiftNumber(Integer storeNumber, int tillId, int shiftNumber) {

        int storeId
        if (storeNumber) {
            storeId = storeService.getStoreIdByStoreNumber(storeNumber)
        } else {
            storeId = springSecurityService.principal.storeId
        }

        def criteria = Shift.withTransaction { Shift.createCriteria() }
        return criteria.get() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)
            eq("tillId", tillId)
            eq("shiftNumber", shiftNumber)
        }
    }
}
