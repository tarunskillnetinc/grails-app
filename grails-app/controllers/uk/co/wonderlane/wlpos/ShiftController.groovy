package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType

class ShiftController {

    def springSecurityService
    def shiftService
    def snapshotService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect (uri: "/")
            return
        }

        DateTime startDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)
        DateTime endDate = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

        [startDate: startDate, endDate: endDate]
    }

    def ajaxGetShifts() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");

        DateTime startDate = DateTime.parse(params.startDate, dateFormatter)
        DateTime endDate = DateTime.parse(params.endDate, dateFormatter)
        Integer tillId = null
        if (params.tillId) {
            try {
                tillId = Integer.parseInt(params.tillId)
            } catch (Exception e) {
                // Non-numeric input added, do nothing.
            }
        }

        render (template: "shiftViewerResults", model: [shifts: shiftService.getShifts(startDate, endDate, tillId)])
    }

    def ajaxGetCashDetails(int shiftId) {
        def shift = shiftService.getShift(shiftId)

        if (shift?.reconciledDate == null) {
            render (template: "cashUpModal", model: [shift: shift])
        } else if (shift?.reconciledDate != null) {
            render (template: "cashUpSummaryModal", model: [shift: shift])
        } else {
            render ""
        }
    }

    def ajaxChangeCashUpType(CashUpCommand cashUpCommand) {
        def template = ""

        if (cashUpCommand.type == "VALUE") {
            template = "cashUpByValue"

            // Switching from denomination to value.
            if (cashUpCommand.cashUpBy == "DENOMINATION") {
                cashUpCommand.fiftyPounds *= 50
                cashUpCommand.twentyPounds *= 20
                cashUpCommand.tenPounds *= 10
                cashUpCommand.fivePounds *= 5
                cashUpCommand.twoPounds *= 2
                cashUpCommand.onePounds *= 1
                cashUpCommand.fiftyPences *= 0.50
                cashUpCommand.twentyPences *= 0.20
                cashUpCommand.tenPences *= 0.10
                cashUpCommand.fivePences *= 0.05
                cashUpCommand.twoPences *= 0.02
                cashUpCommand.onePences *= 0.01
            }
        } else if (cashUpCommand.type == "DENOMINATION") {
            template = "cashUpByDenomination"

            // Switching from value to denomination.
            if (cashUpCommand.cashUpBy == "VALUE") {
                cashUpCommand.fiftyPounds /= 50
                cashUpCommand.twentyPounds /= 20
                cashUpCommand.tenPounds /= 10
                cashUpCommand.fivePounds /= 5
                cashUpCommand.twoPounds /= 2
                cashUpCommand.onePounds /= 1
                cashUpCommand.fiftyPences /= 0.50
                cashUpCommand.twentyPences /= 0.20
                cashUpCommand.tenPences /= 0.10
                cashUpCommand.fivePences /= 0.05
                cashUpCommand.twoPences /= 0.02
                cashUpCommand.onePences /= 0.01
            }
        } else if (cashUpCommand.type == "TOTALS") {
            template = "cashUpByTotals"

            // Switching from value to totals.
            if (cashUpCommand.cashUpBy == "VALUE") {
                cashUpCommand.cashTotal = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds + cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences + cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences + cashUpCommand.onePences
            } else if (cashUpCommand.cashUpBy == "DENOMINATION") {
                // Switching from denomination to totals.
                cashUpCommand.cashTotal = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 + cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 + cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 + cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 + cashUpCommand.onePences * 0.01
            }
        }

        render (template: template, model: [ values: cashUpCommand ])
    }

    def ajaxSaveCash(CashUpCommand cashUpCommand) {
        def shift = shiftService.getShift(cashUpCommand.shiftId)

        ReconciliationTotal cashTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.CASH } ?: null

        if (cashTotal == null) {
            cashTotal = new ReconciliationTotal(TenderType.CASH)
            shift.reconciliationTotals.add(cashTotal)
        }

        if (cashUpCommand.cashUpBy == "VALUE") {
            cashTotal.value = cashUpCommand.fiftyPounds + cashUpCommand.twentyPounds + cashUpCommand.tenPounds + cashUpCommand.fivePounds + cashUpCommand.twoPounds + cashUpCommand.onePounds + cashUpCommand.fiftyPences + cashUpCommand.twentyPences + cashUpCommand.tenPences + cashUpCommand.fivePences + cashUpCommand.twoPences + cashUpCommand.onePences
        } else if (cashUpCommand.cashUpBy == "DENOMINATION") {
            cashTotal.value = cashUpCommand.fiftyPounds * 50 + cashUpCommand.twentyPounds * 20 + cashUpCommand.tenPounds * 10 + cashUpCommand.fivePounds * 5 + cashUpCommand.twoPounds * 2 + cashUpCommand.onePounds * 1 + cashUpCommand.fiftyPences * 0.50 + cashUpCommand.twentyPences * 0.20 + cashUpCommand.tenPences * 0.10 + cashUpCommand.fivePences * 0.05 + cashUpCommand.twoPences * 0.02 + cashUpCommand.onePences * 0.01
        } else {
            cashTotal.value = cashUpCommand.cashTotal
        }

        cashTotal.variance = (cashTotal.value ?: BigDecimal.ZERO) - (shift.tenderTotals.findAll { it.tenderType == TenderType.CASH }?.sum { it.value } ?: BigDecimal.ZERO)

        ReconciliationTotal vouchersTotal = shift.reconciliationTotals?.find { it.tenderType == TenderType.VOUCHER }

        if (vouchersTotal == null) {
            vouchersTotal = new ReconciliationTotal(TenderType.VOUCHER)
            shift.reconciliationTotals.add(vouchersTotal)
        }

        vouchersTotal.value = cashUpCommand.vouchersTotal
        vouchersTotal.variance = (vouchersTotal.value ?: BigDecimal.ZERO) - (shift.tenderTotals.findAll {it.tenderType == TenderType.VOUCHER }?.sum{ it.value } ?: BigDecimal.ZERO)

        shiftService.saveShift(shift)

        render(template: "cashUpSummaryModal", model: [ shift: shift, varianceReasons: TenderReconciliationVarianceReason.values() ])
    }

    def ajaxSaveShift(SaveShiftCommand saveShiftCommand) {
        def shift = shiftService.getShift(saveShiftCommand.shiftId)

        if (saveShiftCommand.tenderReconciliationVarianceReason != null) {
            shift.reconciliationTotals.findAll { it.variance != BigDecimal.ZERO }?.each {
                it.varianceReason = saveShiftCommand.tenderReconciliationVarianceReason
                it.varianceReasonText = saveShiftCommand.tenderReconciliationVarianceReasonText
            }
        }

        if (shift.reconciledDate == null) {
            shift.reconciledDate = DateTime.now()
            shift.reconciledByUserId = springSecurityService.principal.id
            shift.reconciledByUsersName = springSecurityService.principal.usersName
        } else {
            shift.reReconciledDate = DateTime.now()
            shift.reReconciledByUserId = springSecurityService.principal.id
            shift.reReconciledByUsersName = springSecurityService.principal.usersName
        }

        shiftService.saveShift(shift)

        Snapshot latestSnapshot = snapshotService.getSafeSnapshot()
        ReconciliationTotal cashTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.CASH } ?: null
        if (cashTotal != null) {
            TenderTotal cashExpected = latestSnapshot.expectedTotals.find{it.tenderType == TenderType.CASH} ?: null
            if (cashExpected == null) {
                cashExpected = new TenderTotal(TenderType.CASH)
                latestSnapshot.expectedTotals.add(cashExpected)
            }

            cashExpected.value = cashExpected.value.add(cashTotal.value)
        }

        ReconciliationTotal voucherTotal = shift.reconciliationTotals.find { it.tenderType == TenderType.VOUCHER } ?: null
        if (voucherTotal != null) {
            TenderTotal voucherExpected = latestSnapshot.expectedTotals.find{it.tenderType == TenderType.VOUCHER} ?: null
            if (voucherExpected == null) {
                voucherExpected = new TenderTotal(TenderType.VOUCHER)
                latestSnapshot.expectedTotals.add(voucherExpected)
            }

            voucherExpected.value = voucherExpected.value.add(voucherTotal.value)
        }

        snapshotService.saveSnapshot(latestSnapshot)

        render(template: "cashUpSummaryModal", model: [ shift: shift ])
    }
}

class CashUpCommand {

    int shiftId
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

class SaveShiftCommand {

    int shiftId
    TenderReconciliationVarianceReason tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}