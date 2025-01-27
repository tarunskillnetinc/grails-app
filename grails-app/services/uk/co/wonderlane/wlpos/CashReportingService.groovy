package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.SafeSessionStatus
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.transactions.SafeSession
import uk.co.wonderlane.wlpos.transactions.SafeSessionAudit
import uk.co.wonderlane.wlpos.transactions.Shift
import uk.co.wonderlane.wlpos.transactions.ShiftAudit

@Transactional("transactions")
class CashReportingService {

    def springSecurityService
    def storeService

    List<Shift> getAllShiftsForTill(Integer storeNumber, int tillId, DateTime startDate, DateTime endDate) {
        def criteria = Shift.withTransaction { Shift.createCriteria() }
        return criteria.list([sort: "shiftNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("tillId", tillId)
            between("dateCreated", startDate, endDate)
        }
    }

    List<Shift> getFinalisedShiftsForTill(Integer storeNumber, int tillId, DateTime startDate, DateTime endDate) {
        def criteria = Shift.withTransaction { Shift.createCriteria() }
        return criteria.list([sort: "shiftNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("tillId", tillId)
            eq("shiftStatus", ShiftStatus.FINALISED.toString())
            between("dateCreated", startDate, endDate)
        }
    }

    List<SafeSessionAudit> getAuditEventsForSafe(Integer expectedSessionId) {
        def criteria = SafeSessionAudit.withTransaction { SafeSessionAudit.createCriteria() }
        return criteria.list() {
            eq("sessionId", expectedSessionId)
        }
    }

    List<SafeSession> getFinalisedSafeSessionsForSafe(Integer storeNumber, int safeId, DateTime startDate, DateTime endDate) {
        def criteria = SafeSession.withTransaction { SafeSession.createCriteria() }
        return criteria.list([sort: "sessionNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("safeId", safeId)
            eq("sessionStatus", SafeSessionStatus.FINALISED.toString())
            between("dateCreated", startDate, endDate)
        }
    }

    List<SafeSession> getSafeSessionsForSafe(Integer storeNumber, int safeId, DateTime startDate, DateTime endDate) {
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
            between("dateCreated", startDate, endDate)
        }
    }

    Shift getShiftForShiftNumber(Integer storeNumber, int tillId, int shiftNumber) {
        def criteria = Shift.withTransaction { Shift.createCriteria() }
        return criteria.get() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("tillId", tillId)
            eq("shiftNumber", shiftNumber)
        }
    }

    List<ShiftAudit> getShiftAuditRecords(int shiftId) {
        def criteria = ShiftAudit.withTransaction { ShiftAudit.createCriteria() }
        return criteria.list([sort: "id", order: "ASC"]) {
            eq("shiftId", shiftId)
        }
    }

    SafeSession getSafeSessionForSessionNumber(Integer storeNumber, int safeId, int sessionNumber) {
        def criteria = SafeSession.withTransaction { SafeSession.createCriteria() }
        return criteria.get() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("safeId", safeId)
            eq("sessionNumber", sessionNumber)
        }
    }

    private int getStoreId(Integer storeNumber) {
        if (storeNumber) {
            return storeService.getStoreIdByStoreNumber(storeNumber)
        } else {
            return springSecurityService.principal.storeId
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
