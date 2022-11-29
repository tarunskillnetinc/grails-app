package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType

class SnapshotController {

    def springSecurityService
    def snapshotService

    def index() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate: startDate, endDate: endDate]
    }

    def ajaxGetSnapshots() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        DateTime startDate = DateTime.parse(params.startDate, dateFormatter)
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter)

        render (template: "snapshotViewerResults", model: [snapshots: snapshotService.getSnapshots(startDate, endDate)])
    }

    def ajaxGetSafe() {
        Snapshot safeSnapshot = snapshotService.getSafeSnapshot()

        if (safeSnapshot) {
            render(template: "snapshotModal", model: [snapshot: safeSnapshot])
        } else {
            render "Unable to retrieve safe."
        }
    }

    def ajaxGetSnapshot(int snapshotId) {
        Snapshot snapshot = snapshotService.getSnapshot(snapshotId)

        if (snapshot) {
            render(template: "snapshotSummaryModal", model: [ snapshot: snapshot])
        } else {
            render "Unable to retrieve snapshot"
        }
    }

    def ajaxSaveSafeCount(SaveSafeCommand safeCommand) {
        def snapshot = snapshotService.getSafeSnapshot()

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
}

class SaveSafeCommand {
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
