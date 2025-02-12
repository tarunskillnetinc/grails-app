package uk.co.wonderlane.wlpos

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.SafeSessionStatus
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.TenderMovement
import uk.co.wonderlane.wlpos.transactions.SafeSession
import uk.co.wonderlane.wlpos.transactions.SafeSessionAudit
import uk.co.wonderlane.wlpos.transactions.Shift
import uk.co.wonderlane.wlpos.transactions.ShiftAudit

@Transactional("transactions")
class CashReportingService {

    def springSecurityService
    def storeService
    def financialWeekService

    List<Shift> getAllShiftsForTill(Integer storeNumber, int tillId, DateTime startDate, DateTime endDate) {
        def criteria = Shift.withTransaction { Shift.createCriteria() }

        return criteria.list([sort: "shiftNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("tillId", tillId)
            lt("dateCreated", endDate)
            gte("dateUpdated", startDate)
        }
    }

    List<Shift> getFinalisedShiftsForTill(Integer storeNumber, int tillId, DateTime startDate, DateTime endDate) {
        def criteria = Shift.withTransaction { Shift.createCriteria() }

        return criteria.list([sort: "shiftNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("tillId", tillId)
            eq("shiftStatus", ShiftStatus.FINALISED.toString())
            lt("dateCreated", endDate)
            gte("dateUpdated", startDate)
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
            lt("dateCreated", endDate)
            gte("dateUpdated", startDate)
        }
    }

    List<SafeSession> getSafeSessionsForSafe(Integer storeNumber, int safeId, DateTime startDate, DateTime endDate) {
        def criteria = SafeSession.withTransaction { SafeSession.createCriteria() }

        return criteria.list([sort: "sessionNumber", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", getStoreId(storeNumber))
            eq("safeId", safeId)
            lt("dateCreated", endDate)
            gte("dateUpdated", startDate)
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

            lt("dateCreated", endDate)
            gte("dateUpdated", startDate)
            order(sortColumn ?: "tillId", sortOrder ?: "asc")
        }

        return shifts
    }

    @ReadOnly('reportingReadOnly')
    def getTenderMovements(DateTime startDate, DateTime endDate, TenderMovementType tenderMovementType, TenderType tenderType, Integer storeId, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tenderMovementCriteria = TenderMovement.withTransaction { TenderMovement.createCriteria() }

        def results = tenderMovementCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            } else if (storeId) {
                eq ("storeId", storeId)
            }

            if (tenderType) {
                eq("tenderTypeId", tenderType.id)
            }

            if (tenderMovementType) {
                eq("type", tenderMovementType)
            }

            between ("timestamp", startDate, endDate)
        }

        int totalCount = TenderMovement.withTransaction { results.totalCount }
        return [totalCount: totalCount, tenderMovements: results]
    }

    @ReadOnly('reportingReadOnly')
    def getBankingTenderMovements(DateTime startDate, DateTime endDate, List<TenderMovementType> tenderMovementTypes, Integer storeId, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        def tenderMovementCriteria = TenderMovement.withTransaction { TenderMovement.createCriteria() }

        def results = tenderMovementCriteria.list([sort: sortColumn, order: sortOrder, offset: startIndex, max: maxResults]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            } else if (storeId) {
                eq ("storeId", storeId)
            }

            if (tenderMovementTypes) {
                inList("type", tenderMovementTypes)
            }

            between ("timestamp", startDate, endDate)
        }

        // Criteria.list() with max and offset returns a totalCount, but for some reason I am having to read that value otherwise an error is thrown when trying to use it back in the controller.
        // I believe this may be related to the domain class being in an alternate datasource, but I think it's a bug in Grails. Actually, I think it's because the totalCount is lazily loaded
        // to prevent the double query immediately. But it's throwing a Hibernate session error if I don't request it here.
        int totalCount = TenderMovement.withTransaction { results.totalCount }
        return [totalCount: totalCount, tenderMovements: results]
    }

    TenderMovement createNewTenderMovement(TenderMovementType movementType, Integer tenderTypeId, String tenderTypeName, uk.co.wonderlane.wlpos.reporting.Location fromLocation, uk.co.wonderlane.wlpos.reporting.Location toLocation , String reasonCode, String bankingDate,
                                           String bank, String bankReferenceNumber, String comments, BigDecimal amount) {

        def tenderMovement = new TenderMovement()
        def financialWeek = financialWeekService.getFinancialWeek(springSecurityService.principal.retailerId)

        tenderMovement.setRetailerId(springSecurityService.principal.retailerId)
        tenderMovement.setStoreId(springSecurityService.principal.storeId)
        tenderMovement.setUserId(springSecurityService.principal.id)
        tenderMovement.setUserName(springSecurityService.principal.username)
        tenderMovement.setUsersRealName(springSecurityService.principal.usersName)
        tenderMovement.setType(movementType)
        tenderMovement.setTenderTypeId(tenderTypeId)
        tenderMovement.setTenderTypeName(tenderTypeName)
        tenderMovement.setFromLocation(fromLocation)
        tenderMovement.setToLocation(toLocation)
        tenderMovement.setReason(reasonCode)
        tenderMovement.setAmount(amount)
        tenderMovement.setTimestamp(DateTime.now(DateTimeZone.UTC))
        tenderMovement.setBankName(bank)
        tenderMovement.setBankReference(bankReferenceNumber)
        tenderMovement.setComment(comments)
        tenderMovement.setFinancialWeekId(financialWeek?.getId())
        tenderMovement.setFinancialWeekNumber(financialWeek?.getWeekNumber())

        if (bankingDate) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            try {
                tenderMovement.setBankingDate(formatter.parseDateTime(bankingDate))
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse banking date: $bankingDate", e)
                throw new RuntimeException("Invalid banking date format. Expected dd/MM/yyyy", e)
            }
        }

        return tenderMovement
    }

    def saveTenderMovement(TenderMovement tenderMovement) {
        if (tenderMovement.validate()) {
            tenderMovement.save(flush: true)
            return Integer.valueOf(tenderMovement.id)
        } else {
            String errorMessage = tenderMovement.errors.allErrors.collect { error ->
                return error.toString()
            }.join("; ")
            log.error("Tender movement validation failed. errors: ${errorMessage}")
            throw new RuntimeException("Tender movement validation failed, errors ${errorMessage}")
        }
    }

    @ReadOnly('reportingReadOnly')
    def getReportColumns(ReportType reportType) {
        return ReportColumns.withTransaction { ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, reportType) }
    }

    @Transactional("reporting")
    def saveReportColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }
}
