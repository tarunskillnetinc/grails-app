package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.reporting.Location
import uk.co.wonderlane.wlpos.reporting.TenderMovement

import java.time.ZonedDateTime

class SnapshotController {

    def springSecurityService
    def snapshotService
    def locationService
    def reportingService
    def reasonCodeService
    def safeService

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        def storeSafes = safeService.getStoreSafes()

        [startDate: startDate, endDate: endDate, safes: storeSafes, shiftStartDate: params.shiftStartDate, shiftEndDate: params.shiftEndDate, shiftTillId: params.shiftTillId]
    }

    def ajaxGetSnapshots() {
        def safeId = null
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        DateTime startDate = DateTime.parse(params.startDate, dateFormatter)
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter)

        def storeSafes = safeService.getStoreSafes()
        def snapshots = snapshotService.getSnapshots(startDate, endDate)

        if (params.safeId != null && params.safeId.length() > 0) {
            safeId = Integer.parseInt(params.safeId);

            /* Filters only the selected safe */
            snapshots = snapshots.findAll { it.safeId == safeId }
        }

        def safeDescriptions = storeSafes.collectEntries { [(it.id): it.description] }

        render (template: "snapshotViewerResults", model: [snapshots: snapshots, descriptions: safeDescriptions])
    }

    def ajaxGetSafe(int id) {
        def safes = safeService.getStoreSafes()

        if (id == 0) {
            if (safes.collect().isEmpty()) {
                def safe = safeService.createDefaultSafe()
                locationService.createSafeLocation(safe.id)
            }

            if (safes.collect().size() == 1) {
                Snapshot safeSnapshot = snapshotService.getSnapshotForSafe((safes.collect()[0] as Safe).id)
                render(template: "snapshotModal", model: [safeLocations: safes, snapshot: safeSnapshot])
            } else {
                render(template: "snapshotModal", model: [safeLocations: safes, snapshot: null])
            }
        } else {
            Snapshot safeSnapshot = snapshotService.getSnapshotForSafe(id)

            if (safeSnapshot) {
                render(template: "snapshotModal", model: [safeLocations: safes, snapshot: safeSnapshot])
            } else {
                render "Unable to retrieve safe."
            }
        }
    }

    def ajaxGetSnapshot(int id) {
        def safe = null
        def snapshot = snapshotService.getSnapshot(id)

        if (snapshot != null) {
            safe = safeService.getSafeById(snapshot.safeId)
        }

        if (snapshot) {
            render(template: "snapshotSummaryModal", model: [snapshot: snapshot, description: safe.description])
        } else {
            render "Unable to retrieve snapshot"
        }
    }

    def ajaxSaveSafeCount(SaveSafeCommand safeCommand) {
        def snapshot = snapshotService.getSnapshot(safeCommand.snapshotId)

        ReconciliationTotal cashTotal = snapshot.totals.find {it.tenderType == TenderType.CASH} ?: null
        if (cashTotal == null) {
            cashTotal = new ReconciliationTotal(TenderType.CASH)
            snapshot.totals.add(cashTotal)
        }

        if (safeCommand.cashUpBy == "VALUE") {
            cashTotal.value = safeCommand.fiftyPounds + safeCommand.twentyPounds + safeCommand.tenPounds + safeCommand.fivePounds + safeCommand.twoPounds + safeCommand.onePounds + safeCommand.fiftyPences + safeCommand.twentyPences + safeCommand.tenPences + safeCommand.fivePences + safeCommand.twoPences + safeCommand.onePences
        } else if (safeCommand.cashUpBy == "DENOMINATION") {
            cashTotal.value = safeCommand.fiftyPounds * 50 + safeCommand.twentyPounds * 20 + safeCommand.tenPounds * 10 + safeCommand.fivePounds * 5 + safeCommand.twoPounds * 2 + safeCommand.onePounds * 1 + safeCommand.fiftyPences * 0.50 + safeCommand.twentyPences * 0.20 + safeCommand.tenPences * 0.10 + safeCommand.fivePences * 0.05 + safeCommand.twoPences * 0.02 + safeCommand.onePences * 0.01
        } else {
            cashTotal.value = safeCommand.cashTotal
        }

        cashTotal.variance = (cashTotal.value ?: BigDecimal.ZERO) - (snapshot.expectedTotals.findAll { it.tenderType == TenderType.CASH }?.sum { it.value } ?: BigDecimal.ZERO)

        ReconciliationTotal vouchersTotal = snapshot.totals?.find { it.tenderType == TenderType.VOUCHER }
        if (vouchersTotal == null) {
            vouchersTotal = new ReconciliationTotal(TenderType.VOUCHER)
            snapshot.totals.add(vouchersTotal)
        }

        vouchersTotal.value = safeCommand.vouchersTotal
        vouchersTotal.variance = (vouchersTotal.value ?: BigDecimal.ZERO) - (snapshot.expectedTotals.findAll { it.tenderType == TenderType.VOUCHER }?.sum { it.value } ?: BigDecimal.ZERO)

        snapshot.variance = new BigDecimal(snapshot.totals.sum { it.variance.abs() })

        snapshotService.saveSnapshot(snapshot)

        def varianceReasons = reasonCodeService.getReasonCodesByType(snapshot.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)

        render(template: "snapshotSummaryModal", model: [ snapshot: snapshot, varianceReasons: varianceReasons ])
    }

    def ajaxSaveSnapshot(SaveSnapshotCommand snapshotCommand) {
        def snapshot = snapshotService.getSnapshot(snapshotCommand.snapshotId)

        if (snapshotCommand.varianceReason != null) {
            snapshot.varianceReason = snapshotCommand.varianceReason
            snapshot.varianceReasonText = snapshotCommand.varianceReasonText
        }

        // Remove the time offset by setting the time zone to UTC
        snapshot.countDate = DateTime.now().withZone(DateTimeZone.UTC)
        snapshot.countedByUserId = springSecurityService.principal.id
        snapshot.countedByUsersName = springSecurityService.principal.usersName

        snapshotService.saveSafeSnapshot(snapshot)

        def varianceReasons = reasonCodeService.getReasonCodesByType(snapshot.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)

        render(template: "snapshotSummaryModal", model: [ snapshot: snapshot, varianceReasons: varianceReasons ])
    }

    Location getOrCreateLocationForSafe(int safeId) {
        /* Get the location from the safe id */
        def location = locationService.getLocationBySafeId(safeId)

        if (location == null) {
            /* Should not happen, but if necessary create a location for the safe */
            locationService.createSafeLocation(safeId)
        }

        return location
    }

    def ajaxBanking() {
        def safeLocations = safeService.getStoreSafes()

        render(template: "bankingModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveBanking(BankingCommand bankingCommand) {
        if (bankingCommand.cashTotal == BigDecimal.ZERO && bankingCommand.vouchersTotal == BigDecimal.ZERO) {
            render(template: "bankingModal", model: [safeLocations: safeService.getStoreSafes(), error: "At least one tender total must be non-zero."])
            return
        } else if (bankingCommand.cashTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "bankingModal", model: [safeLocations: safeService.getStoreSafes(), error: "Cash totals cannot exceed more than 10 million."])
            return
        } else if (bankingCommand.vouchersTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "bankingModal", model: [safeLocations: safeService.getStoreSafes(), error: "Voucher totals cannot exceed more than 10 million."])
            return
        }
        
        Snapshot fromSnapshot = snapshotService.getSnapshotForSafe(bankingCommand.fromLocation)
        def movements = new ArrayList<TenderMovement>()

        if (bankingCommand.cashTotal > BigDecimal.ZERO) {
            TenderTotal cashExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.CASH } ?: null
            if (cashExpected == null || cashExpected.value.subtract(bankingCommand.cashTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "bankingModal", model: [safeLocations: safeService.getStoreSafes(), error: "The amount entered for Cash is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.BANKING,
                    TenderType.CASH,
                    getOrCreateLocationForSafe(bankingCommand.fromLocation),
                    null,
                    bankingCommand.cashTotal))

            cashExpected.value = cashExpected.value.subtract(bankingCommand.cashTotal)
        }

        if (bankingCommand.vouchersTotal > BigDecimal.ZERO) {
            TenderTotal voucherExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.VOUCHER } ?: null
            if (voucherExpected == null || voucherExpected.value.subtract(bankingCommand.vouchersTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "bankingModal", model: [safeLocations: safeService.getStoreSafes(), error: "The amount entered for Voucher is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.BANKING,
                    TenderType.VOUCHER,
                    getOrCreateLocationForSafe(bankingCommand.fromLocation),
                    null,
                    bankingCommand.vouchersTotal))

            voucherExpected.value = voucherExpected.value.subtract(bankingCommand.vouchersTotal)
        }

        if (snapshotService.saveSnapshot(fromSnapshot) > 0) {
            movements.each { reportingService.saveTenderMovement(it) }
            render "OK"
        }
    }

    def ajaxCashInbound() {
        def safeLocations = safeService.getStoreSafes()

        render(template: "cashInboundModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveCashInbound(CashInboundCommand cashInboundCommand) {
        if (cashInboundCommand.cashTotal == BigDecimal.ZERO) {
            render(template: "cashInboundModal", model: [safeLocations: safeService.getStoreSafes(), error: "Cash total must be non-zero."])
            return
        } else if (cashInboundCommand.cashTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "cashInboundModal", model: [safeLocations: safeService.getStoreSafes(), error: "Cash total cannot exceed more than 10 million."])
            return
        }

        Snapshot toSnapshot = snapshotService.getSnapshotForSafe(cashInboundCommand.toLocation)

        //Add to safe total
        def cashExpected = toSnapshot.expectedTotals.find{it.tenderType == TenderType.CASH} ?: null
        if (cashExpected == null) {
            cashExpected = new TenderTotal(TenderType.CASH)
            toSnapshot.expectedTotals.add(cashExpected)
        }
        cashExpected.value = cashExpected.value.add(cashInboundCommand.cashTotal)


        if (snapshotService.saveSnapshot(toSnapshot) > 0) {
            def movement = reportingService.createNewTenderMovement(TenderMovementType.CASH_INBOUND,
                    TenderType.CASH,
                    null,
                    getOrCreateLocationForSafe(cashInboundCommand.toLocation),
                    cashInboundCommand.cashTotal)
            reportingService.saveTenderMovement(movement)
            render "OK"
        }
    }

    def ajaxCashLift() {
        def safeLocations = safeService.getStoreSafes()

        render(template: "cashLiftModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveCashLift(CashLiftCommand cashLiftCommand) {
        def safeLocations = safeService.getStoreSafes()

        if (!cashLiftCommand.fromLocation || !cashLiftCommand.toLocation) {
            render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "A Cash Lift operation requires both safe locations to be set."])
            return
        }

        if (cashLiftCommand.fromLocation == cashLiftCommand.toLocation) {
            render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "A cash lift cannot be performed from/to the same safe."])
            return
        }

        if (cashLiftCommand.cashTotal == BigDecimal.ZERO && cashLiftCommand.vouchersTotal == BigDecimal.ZERO) {
            render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "At least one tender total must be non-zero."])
            return
        } else if (cashLiftCommand.cashTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "Cash totals cannot exceed more than 10 million."])
            return
        } else if (cashLiftCommand.vouchersTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "Voucher totals cannot exceed more than 10 million."])
            return
        }

        Snapshot fromSnapshot = snapshotService.getSnapshotForSafe(cashLiftCommand.fromLocation)
        Snapshot toSnapshot = snapshotService.getSnapshotForSafe(cashLiftCommand.toLocation)
        def movements = new ArrayList<TenderMovement>()

        if (cashLiftCommand.cashTotal > BigDecimal.ZERO) {
            TenderTotal fromCashExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.CASH } ?: null
            if (fromCashExpected == null || fromCashExpected.value.subtract(cashLiftCommand.cashTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "The amount entered for Cash is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.CASH_LIFT,
                    TenderType.CASH,
                    getOrCreateLocationForSafe(cashLiftCommand.fromLocation),
                    getOrCreateLocationForSafe(cashLiftCommand.toLocation),
                    cashLiftCommand.cashTotal))

            fromCashExpected.value = fromCashExpected.value.subtract(cashLiftCommand.cashTotal)


            TenderTotal toCashExpected = toSnapshot.expectedTotals.find{it.tenderType == TenderType.CASH} ?: null
            if (toCashExpected == null) {
                toCashExpected = new TenderTotal(TenderType.CASH)
                toSnapshot.expectedTotals.add(toCashExpected)
            }

            toCashExpected.value = toCashExpected.value.add(cashLiftCommand.cashTotal)
        }

        if (cashLiftCommand.vouchersTotal > BigDecimal.ZERO) {
            TenderTotal voucherExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.VOUCHER } ?: null
            if (voucherExpected == null || voucherExpected.value.subtract(cashLiftCommand.vouchersTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "cashLiftModal", model: [safeLocations: safeLocations, error: "The amount entered for Voucher is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.CASH_LIFT,
                    TenderType.VOUCHER,
                    getOrCreateLocationForSafe(cashLiftCommand.fromLocation),
                    getOrCreateLocationForSafe(cashLiftCommand.toLocation),
                    cashLiftCommand.vouchersTotal))

            voucherExpected.value = voucherExpected.value.subtract(cashLiftCommand.vouchersTotal)
        }

        if (snapshotService.saveSnapshot(fromSnapshot) > 0 && snapshotService.saveSnapshot(toSnapshot) > 0) {
            movements.each { reportingService.saveTenderMovement(it) }
            render "OK"
        }
    }

}

class SaveSafeCommand {
    int snapshotId
    String type // Type being navigated TO.
    String cashUpBy // Type being navigated FROM.
    BigDecimal fiftyPounds = BigDecimal.ZERO
    BigDecimal twentyPounds = BigDecimal.ZERO
    BigDecimal tenPounds = BigDecimal.ZERO
    BigDecimal fivePounds = BigDecimal.ZERO
    BigDecimal twoPounds = BigDecimal.ZERO
    BigDecimal onePounds = BigDecimal.ZERO
    BigDecimal fiftyPences = BigDecimal.ZERO
    BigDecimal twentyPences = BigDecimal.ZERO
    BigDecimal tenPences = BigDecimal.ZERO
    BigDecimal fivePences = BigDecimal.ZERO
    BigDecimal twoPences = BigDecimal.ZERO
    BigDecimal onePences = BigDecimal.ZERO
    BigDecimal cashTotal = BigDecimal.ZERO
    BigDecimal chequesTotal = BigDecimal.ZERO
    BigDecimal vouchersTotal = BigDecimal.ZERO
}

class SaveSnapshotCommand {
    int snapshotId
    String varianceReason
    String varianceReasonText
}

class CashLiftCommand {
    Integer fromLocation
    Integer toLocation
    BigDecimal cashTotal = BigDecimal.ZERO
    BigDecimal vouchersTotal = BigDecimal.ZERO
}

class BankingCommand {
    Integer fromLocation
    BigDecimal cashTotal = BigDecimal.ZERO
    BigDecimal vouchersTotal = BigDecimal.ZERO
}

class CashInboundCommand {
    Integer toLocation
    BigDecimal cashTotal = BigDecimal.ZERO
}
