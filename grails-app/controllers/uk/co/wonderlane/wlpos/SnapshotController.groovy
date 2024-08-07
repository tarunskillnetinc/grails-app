package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.reporting.Location
import uk.co.wonderlane.wlpos.reporting.TenderMovement

class SnapshotController {

    def springSecurityService
    def snapshotService
    def locationService
    def reportingService

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate: startDate, endDate: endDate, safeLocations: locationService.getStoreSafeLocations(), shiftStartDate: params.shiftStartDate, shiftEndDate: params.shiftEndDate, shiftTillId: params.shiftTillId]
    }

    def ajaxGetSnapshots() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        DateTime startDate = DateTime.parse(params.startDate, dateFormatter)
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter)

        render (template: "snapshotViewerResults", model: [snapshots: snapshotService.getSnapshots(startDate, endDate)])
    }

    def ajaxGetSafe(int id) {
        def locations = locationService.getStoreSafeLocations()
        if (id == 0) {
            if (locations.collect().isEmpty()) {
                locationService.generateDefaultSafeLocation()
                locations = locationService.getStoreSafeLocations()
            }

            if (locations.collect().size() == 1) {
                Snapshot safeSnapshot = snapshotService.getSnapshotForLocation((locations.collect()[0] as Location).id)
                render(template: "snapshotModal", model: [safeLocations: locations, snapshot: safeSnapshot])
            } else {
                render(template: "snapshotModal", model: [safeLocations: locations, snapshot: null])
            }
        } else {
            Snapshot safeSnapshot = snapshotService.getSnapshotForLocation(id)

            if (safeSnapshot) {
                render(template: "snapshotModal", model: [safeLocations: locations, snapshot: safeSnapshot])
            } else {
                render "Unable to retrieve safe."
            }
        }
    }

    def ajaxGetSnapshot(int id) {
        Snapshot snapshot = snapshotService.getSnapshot(id)

        if (snapshot) {
            render(template: "snapshotSummaryModal", model: [ snapshot: snapshot])
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

        render(template: "snapshotSummaryModal", model: [ snapshot: snapshot, varianceReasons: TenderReconciliationVarianceReason.values() ])
    }

    def ajaxSaveSnapshot(SaveSnapshotCommand snapshotCommand) {
        def snapshot = snapshotService.getSnapshot(snapshotCommand.snapshotId)

        if (snapshotCommand.varianceReason != null) {
            snapshot.varianceReason = snapshotCommand.varianceReason
            snapshot.varianceReasonText = snapshotCommand.varianceReasonText
        }

        snapshot.countDate = DateTime.now()
        snapshot.countedByUserId = springSecurityService.principal.id
        snapshot.countedByUsersName = springSecurityService.principal.usersName

        snapshotService.saveSafeSnapshot(snapshot)

        render(template: "snapshotSummaryModal", model: [ snapshot: snapshot, varianceReasons: TenderReconciliationVarianceReason.values() ])
    }

    def ajaxBanking() {
        def safeLocations = locationService.getStoreSafeLocations()

        render(template: "bankingModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveBanking(BankingCommand bankingCommand) {
        if (bankingCommand.cashTotal == BigDecimal.ZERO && bankingCommand.vouchersTotal == BigDecimal.ZERO) {
            render(template: "bankingModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "At least one tender total must be non-zero."])
            return
        } else if (bankingCommand.cashTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "bankingModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Cash totals cannot exceed more than 10 million."])
            return
        } else if (bankingCommand.vouchersTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "bankingModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Voucher totals cannot exceed more than 10 million."])
            return
        }
        
        Snapshot fromSnapshot = snapshotService.getSnapshotForLocation(bankingCommand.fromLocation)
        def movements = new ArrayList<TenderMovement>()

        if (bankingCommand.cashTotal > BigDecimal.ZERO) {
            TenderTotal cashExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.CASH } ?: null
            if (cashExpected == null || cashExpected.value.subtract(bankingCommand.cashTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "bankingModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "The amount entered for Cash is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.BANKING,
                    TenderType.CASH,
                    locationService.getLocation(bankingCommand.fromLocation) as Location,
                    null,
                    bankingCommand.cashTotal))

            cashExpected.value = cashExpected.value.subtract(bankingCommand.cashTotal)
        }

        if (bankingCommand.vouchersTotal > BigDecimal.ZERO) {
            TenderTotal voucherExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.VOUCHER } ?: null
            if (voucherExpected == null || voucherExpected.value.subtract(bankingCommand.vouchersTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "bankingModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "The amount entered for Voucher is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.BANKING,
                    TenderType.VOUCHER,
                    locationService.getLocation(bankingCommand.fromLocation) as Location,
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
        def safeLocations = locationService.getStoreSafeLocations()

        render(template: "cashInboundModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveCashInbound(CashInboundCommand cashInboundCommand) {
        if (cashInboundCommand.cashTotal == BigDecimal.ZERO) {
            render(template: "cashInboundModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Cash total must be non-zero."])
            return
        } else if (cashInboundCommand.cashTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "cashInboundModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Cash total cannot exceed more than 10 million."])
            return
        }

        Snapshot toSnapshot = snapshotService.getSnapshotForLocation(cashInboundCommand.toLocation)

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
                    locationService.getLocation(cashInboundCommand.toLocation) as Location,
                    cashInboundCommand.cashTotal)
            reportingService.saveTenderMovement(movement)
            render "OK"
        }
    }

    def ajaxCashLift() {
        def safeLocations = locationService.getStoreSafeLocations()

        render(template: "cashLiftModal", model: [safeLocations: safeLocations])
    }

    def ajaxSaveCashLift(CashLiftCommand cashLiftCommand) {
        def safeLocations = locationService.getStoreSafeLocations()

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
            render(template: "cashLiftModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Cash totals cannot exceed more than 10 million."])
            return
        } else if (cashLiftCommand.vouchersTotal >= BigDecimal.valueOf(10000000)) { //Allow up to £10 million, but not a penny more.
            render(template: "cashLiftModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "Voucher totals cannot exceed more than 10 million."])
            return
        }

        Snapshot fromSnapshot = snapshotService.getSnapshotForLocation(cashLiftCommand.fromLocation)
        Snapshot toSnapshot = snapshotService.getSnapshotForLocation(cashLiftCommand.toLocation)
        def movements = new ArrayList<TenderMovement>()

        if (cashLiftCommand.cashTotal > BigDecimal.ZERO) {
            TenderTotal fromCashExpected = fromSnapshot.expectedTotals.find{ it.tenderType == TenderType.CASH } ?: null
            if (fromCashExpected == null || fromCashExpected.value.subtract(cashLiftCommand.cashTotal) < BigDecimal.ZERO) {
                // expected value would become below zero. Advise to count safe first
                render(template: "cashLiftModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "The amount entered for Cash is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.CASH_LIFT,
                    TenderType.CASH,
                    locationService.getLocation(cashLiftCommand.fromLocation) as Location,
                    locationService.getLocation(cashLiftCommand.toLocation) as Location,
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
                render(template: "cashLiftModal", model: [safeLocations: locationService.getStoreSafeLocations(), error: "The amount entered for Voucher is greater than the expected value in the safe. Please count the safe to account for discrepancies."])
                return
            }

            movements.add(reportingService.createNewTenderMovement(TenderMovementType.CASH_LIFT,
                    TenderType.VOUCHER,
                    locationService.getLocation(cashLiftCommand.fromLocation) as Location,
                    locationService.getLocation(cashLiftCommand.toLocation) as Location,
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
    TenderReconciliationVarianceReason varianceReason
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
