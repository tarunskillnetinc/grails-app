package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.SafeSessionStatus
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.transactions.SafeSession
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

    List<SafeSession> getFinalisedSafeSessionsForSafe(Integer storeNumber, int safeId, DateTime startDate, DateTime endDate) {

        int storeId
        if (storeNumber) {
            storeId = storeService.getStoreIdByStoreNumber(storeNumber)
        } else {
            storeId = springSecurityService.principal.storeId
        }

        def criteria = SafeSession.withTransaction { SafeSession.createCriteria() }
        return criteria.list([sort: "sessionNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)
            eq("safeId", safeId)
            eq("sessionStatus", SafeSessionStatus.FINALISED.toString())
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

    SafeSession getSafeSessionForSessionNumber(Integer storeNumber, int safeId, int sessionNumber) {

        int storeId
        if (storeNumber) {
            storeId = storeService.getStoreIdByStoreNumber(storeNumber)
        } else {
            storeId = springSecurityService.principal.storeId
        }

        def criteria = SafeSession.withTransaction { SafeSession.createCriteria() }
        return criteria.get() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)
            eq("safeId", safeId)
            eq("sessionNumber", sessionNumber)
        }
    }

    def getShiftsForStoreAndTillIds(int storeId, List<Integer> tillIds, DateTime startDate, DateTime endDate, String sortColumn, String sortOrder) {
        def shiftCriteria = Shift.createCriteria()

        def shifts = shiftCriteria.list() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)
            eq("shiftStatus", "FINALISED")
            
            if (tillIds) {
                inList("tillId", tillIds)
            }

            between("dateCreated", startDate, endDate)
            order(sortColumn ?: "tillId", sortOrder ?: "asc")
        }

        return shifts
    }
}
