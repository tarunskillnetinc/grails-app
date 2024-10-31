package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.LocationType
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.reporting.Location

class ShiftControllerSpec extends Specification implements ControllerUnitTest<ShiftController> {

    def 'Test the init action'() {
        given:
        Integer principalStoreId = storeId
        Integer principalRetailerId = retailerId

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = authority1

            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", principalRetailerId);
                    put("storeId", principalStoreId);
                    put("storeNumber", 100)
                    put("authorities", List.of(simpleGrantedAuthority1))
                }
            }
        }

        when: 'The index action is executed'
        HashMap model = controller.index()

        then: 'The model index render'

        if (storeId == null) {
            flash.error
            response.redirectedUrl == '/'
        } else {
            view == '/shift/index.gsp'
            model.endDate
            model.startDate
        }

        where: 'Pass following input parameters'
        retailerId || storeId || authority1
        9          || 234     || new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        9          || null    || new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
    }

    def 'Test get shifts'() {
        given:

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['tillId'] = tillId

        controller.shiftService = Stub(ShiftService) {
            getShifts(_, _, _) >> List.of(
                    getNewShiftObject(1),
                    getNewShiftObject(2)
            )
        }

        when:
        def mockView = '<div class="_shiftViewerResults"> </div>'
        views['/shift/_shiftViewerResults.gsp'] = mockView
        controller.ajaxGetShifts()

        then:
        model.shifts

        where:
        tillId || startDate    || endDate
        9      || "11/10/2013" || "11/11/2014"
        null   || "11/10/2013" || "11/11/2014"
    }

    def 'Test get cash details'() {
        given:

        controller.shiftService = Stub(ShiftService) {
            Shift _shift = getNewShiftObject(shiftId)
            _shift.setReconciledDate(reconciledDate)

            getShift(shiftId, -1, -1) >> _shift
        }

        when:
        def mockView1 = '<div class="_shiftReport"> </div>'
        def mockView2 = '<div class="_cashUpSummaryModal"> </div>'
        views['/shift/_shiftReport.gsp'] = mockView1
        views['/shift/_cashUpSummaryModal.gsp'] = mockView2

        controller.ajaxGetCashDetails(shiftId)

        then:
        model.shift
        model.shift.id
        model.shift.retailerId

        where:
        shiftId || reconciledDate
        1       || null
        2       || new DateTime()
    }

    def 'Test get cash details when the shift is null'() {
        given:

        controller.shiftService = Stub(ShiftService) {
            getShift(1, -1, -1) >> null
        }

        when:
        controller.ajaxGetCashDetails(1)

        then:
        view == null
    }

    def 'Test change CashUp type'() {
        given:
        CashUpCommand cashUpCommand = getCashUpCommand(shiftId, fromType, toType)

        when:
        controller.ajaxChangeCashUpType(cashUpCommand)

        then:
        // Switching from denomination to value.
        if (cashUpCommand.cashUpBy == "DENOMINATION" && cashUpCommand.type == "VALUE") {
            view == "/shift/cashUpByValue.gsp"

            cashUpCommand.getFiftyPounds() == BigDecimal.valueOf(50000)
            cashUpCommand.getTwentyPounds() == BigDecimal.valueOf(20000)
            cashUpCommand.getTenPounds() == BigDecimal.valueOf(10000)
            cashUpCommand.getFivePounds() == BigDecimal.valueOf(5000)
            cashUpCommand.getTwoPounds() == BigDecimal.valueOf(2000)
            cashUpCommand.getOnePounds() == BigDecimal.valueOf(1000)
            cashUpCommand.getFiftyPences() == BigDecimal.valueOf(500)
            cashUpCommand.getTwentyPences() == BigDecimal.valueOf(200)
            cashUpCommand.getTenPences() == BigDecimal.valueOf(100)
            cashUpCommand.getFivePences() == BigDecimal.valueOf(50)
            cashUpCommand.getTwoPences() == BigDecimal.valueOf(20)
            cashUpCommand.getOnePences() == BigDecimal.valueOf(10)
        } else if (cashUpCommand.cashUpBy == "VALUE" && cashUpCommand.type == "DENOMINATION") {
            view == "/shift/cashUpByDenomination.gsp"

            cashUpCommand.getFiftyPounds() == BigDecimal.valueOf(20)
            cashUpCommand.getTwentyPounds() == BigDecimal.valueOf(50)
            cashUpCommand.getTenPounds() == BigDecimal.valueOf(100)
            cashUpCommand.getFivePounds() == BigDecimal.valueOf(200)
            cashUpCommand.getTwoPounds() == BigDecimal.valueOf(500)
            cashUpCommand.getOnePounds() == BigDecimal.valueOf(1000)
            cashUpCommand.getFiftyPences() == BigDecimal.valueOf(2000)
            cashUpCommand.getTwentyPences() == BigDecimal.valueOf(5000)
            cashUpCommand.getTenPences() == BigDecimal.valueOf(10000)
            cashUpCommand.getFivePences() == BigDecimal.valueOf(20000)
            cashUpCommand.getTwoPences() == BigDecimal.valueOf(50000)
            cashUpCommand.getOnePences() == BigDecimal.valueOf(100000)
        } else if (cashUpCommand.type == "TOTALS" && cashUpCommand.cashUpBy == "VALUE") {
            view == "/shift/cashUpByTotals.gsp"

            cashUpCommand.getCashTotal() == BigDecimal.valueOf(12000)
        } else if (cashUpCommand.type == "TOTALS" && cashUpCommand.cashUpBy == "DENOMINATION") {
            view == "/shift/cashUpByTotals.gsp"

            cashUpCommand.getCashTotal() == BigDecimal.valueOf(88880)
        }


        where:
        shiftId || fromType       || toType
        1       || "DENOMINATION" || "VALUE"
        2       || "VALUE"        || "DENOMINATION"
        3       || "VALUE"        || "TOTALS"
        4       || "DENOMINATION" || "TOTALS"
    }

    def 'should save cash successfully'() {
        given:
        CashUpCommand cashUpCommand = getCashUpCommand(shiftId, fromType, toType)
        Shift _shift = getNewShiftObject(shiftId)

        controller.shiftService = Stub(ShiftService) {
            List<ReconciliationTotal> reconciliationTotalList = new ArrayList<>()
            List<TenderTotal> tenderTotalList = new ArrayList<>()

            if (reconCashTotal > 0) {
                reconciliationTotalList.add(getReconciliationTotal(TenderType.CASH, reconCashTotal, 0))
            }

            if (reconVoucherTotal > 0) {
                reconciliationTotalList.add(getReconciliationTotal(TenderType.VOUCHER, reconVoucherTotal, 0))
            }

            if (tenderCashTotal > 0) {
                tenderTotalList.add(getTenderTotal(TenderType.CASH, tenderCashTotal))
            }

            if (tenderVoucherTotal > 0) {
                tenderTotalList.add(getTenderTotal(TenderType.VOUCHER, tenderVoucherTotal))
            }

            _shift.setReconciliationTotals(reconciliationTotalList)
            _shift.setTenderTotals(tenderTotalList)

            getShift(cashUpCommand.getShiftId(), -1, -1) >> _shift
        }

        controller.locationService = Stub(LocationService) {}

        when:
        def mockView = '<div class="cashUpSummaryModal"> </div>'
        views['/shift/_cashUpSummaryModal.gsp'] = mockView

        controller.ajaxSaveCash(cashUpCommand)

        then:
        BigDecimal cashTotalVal = _shift.reconciliationTotals.find { it.tenderType == TenderType.CASH }.value
        BigDecimal cashTotalVariance = _shift.reconciliationTotals.find { it.tenderType == TenderType.CASH }.variance
        BigDecimal voucherTotalVal = _shift.reconciliationTotals.find { it.tenderType == TenderType.VOUCHER }.value
        BigDecimal voucherTotalVariance = _shift.reconciliationTotals.find { it.tenderType == TenderType.VOUCHER }.variance

        switch (shiftId) {
            case 1:
                cashTotalVal == BigDecimal.valueOf(88880)
                cashTotalVariance == BigDecimal.valueOf(88880)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break
            case 2:
                cashTotalVal == BigDecimal.valueOf(12000)
                cashTotalVariance == BigDecimal.valueOf(12000)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break

            case 3:
                cashTotalVal == BigDecimal.valueOf(1000)
                cashTotalVariance == BigDecimal.valueOf(1000)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break

            case 4:
                cashTotalVal == BigDecimal.valueOf(88880)
                cashTotalVariance == BigDecimal.valueOf(88880)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break

            case 5:
                cashTotalVal == BigDecimal.valueOf(12000)
                cashTotalVariance == BigDecimal.valueOf(12000)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break

            case 6:
                cashTotalVal == BigDecimal.valueOf(1000)
                cashTotalVariance == BigDecimal.valueOf(1000)
                voucherTotalVal == BigDecimal.valueOf(1000)
                voucherTotalVariance == BigDecimal.valueOf(1000)

                break
        }

        where:
        shiftId | fromType       | toType         | reconCashTotal | reconVoucherTotal | tenderCashTotal | tenderVoucherTotal
        1       | "DENOMINATION" | "VALUE"        | 10             | 20                | 0               | 0
        2       | "VALUE"        | "DENOMINATION" | 10             | 20                | 0               | 0
        3       | "TOTALS"       | null           | 10             | 20                | 0               | 0
        4       | "DENOMINATION" | "VALUE"        | 0              | 20                | 0               | 0
        5       | "VALUE"        | "DENOMINATION" | 10             | 0                 | 0               | 0
        6       | "TOTALS"       | null           | 0              | 0                 | 0               | 0
    }


    def 'Test save Shift'() {
        given:

        SaveShiftCommand saveShiftCommand = getSaveShiftCommand(
                1, TenderReconciliationVarianceReason.TILL_OVERS_UNDERS, "TILL_OVERS_UNDERS", 1)

        Shift _shift = getNewShiftObject(1)
        Location _safeLocation = getLocation(1, 1, 1, null, 1)
        Location _tillLocation = getLocation(2, 1, 1, _shift.tillId, null)
        Snapshot _latestSafeSnapshot = getSafeSnapshot(1, 1, 1, _safeLocation.id)

        controller.shiftService = Stub(ShiftService) {
            _shift.setReconciledDate(reconciledDate)
            _shift.setReconciliationTotals(List.of(
                    getReconciliationTotal(TenderType.CASH, 500, 10),
                    getReconciliationTotal(TenderType.VOUCHER, 100, 0)))
            _shift.setTenderTotals(List.of(getTenderTotal(TenderType.CASH, 10)))

            getShift(saveShiftCommand.getShiftId(), -1, -1) >> _shift
        }

        controller.locationService = Stub(LocationService) {
            getTillLocation(_shift.tillId) >> _tillLocation
            getLocation(_latestSafeSnapshot.locationId) >> _safeLocation
        }

        controller.snapshotService = Stub(SnapshotService) {
            List<TenderTotal> tenderTotals = new ArrayList<>();

            if (snapshotCashTotal != null) {
                tenderTotals.add(getTenderTotal(TenderType.CASH, BigDecimal.valueOf(snapshotCashTotal)))
            }

            if (snapshotVoucherTotal != null) {
                tenderTotals.add(getTenderTotal(TenderType.VOUCHER, BigDecimal.valueOf(snapshotVoucherTotal)))
            }

            _latestSafeSnapshot.setExpectedTotals(tenderTotals)
            getSnapshotForSafe(1) >> _latestSafeSnapshot
        }

        controller.reportingService = Stub(ReportingService) {}

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("id", 1)
                    put("retailerId", 1)
                    put("storeId", 1)
                    put("usersName", "TEST_USER")
                }
            }
        }

        when:
        def mockView = '<div class="cashUpSummaryModal"> </div>'
        views['/shift/_cashUpSummaryModal.gsp'] = mockView

        controller.ajaxSaveShift(saveShiftCommand)

        then:
        _shift
        _shift.reconciledDate
        if (reconciledDate == null) {
            _shift.getReconciledByUserId() == 1
            _shift.getReconciledByUsersName() == "TEST_USER"
        } else {
            _shift.getReReconciledByUserId() == 1
            _shift.getReReconciledByUsersName() == "TEST_USER"
        }

        _latestSafeSnapshot
        if (snapshotCashTotal != null && snapshotVoucherTotal != null) {
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.CASH }.getValue() == BigDecimal.valueOf(1500)
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.VOUCHER }.getValue() == BigDecimal.valueOf(600)
        } else if (snapshotCashTotal != null) {
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.CASH }.getValue() == BigDecimal.valueOf(1500)
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.VOUCHER }.getValue() == BigDecimal.valueOf(100)
        } else if (snapshotVoucherTotal != null) {
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.CASH }.getValue() == BigDecimal.valueOf(500)
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.VOUCHER }.getValue() == BigDecimal.valueOf(600)
        } else {
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.CASH }.getValue() == BigDecimal.valueOf(500)
            _latestSafeSnapshot.expectedTotals.find { it.tenderType == TenderType.VOUCHER }.getValue() == BigDecimal.valueOf(100)
        }


        where:
        reconciledDate || snapshotCashTotal || snapshotVoucherTotal
        null           || 1000              || 500
        new DateTime() || 1000              || 500
        new DateTime() || 1000              || null
        new DateTime() || null              || 500
        new DateTime() || null              || null
    }

    private Shift getNewShiftObject(int id) {
        return getNewShiftObject(id, 1, 1, 1)
    }

    private Shift getNewShiftObject(int id, int retailerId, int storeId, int tillId) {
        Shift shift = new Shift()

        shift.setId(id)
        shift.setRetailerId(retailerId)
        shift.setStoreId(storeId)
        shift.setTillId(tillId)
        shift.setFirstTransactionId(1)
        shift.setLastTransactionId(1)
        shift.setFirstTransactionDate(new DateTime())
        shift.setLastTransactionDate(new DateTime())
        shift.setShiftNumber(1)
        shift.setCustomerCount(1)
        shift.setCashInDrawer(1)

        return shift
    }

    private ReconciliationTotal getReconciliationTotal(TenderType tenderType, BigDecimal value, BigDecimal variance) {
        ReconciliationTotal reconciliationTotal = new ReconciliationTotal(tenderType)
        reconciliationTotal.setValue(value)
        reconciliationTotal.setVariance(variance)

        return reconciliationTotal;
    }

    private TenderTotal getTenderTotal(TenderType tenderType, BigDecimal value) {
        TenderTotal tenderTotal = new TenderTotal(tenderType)
        tenderTotal.setValue(value)

        return tenderTotal
    }

    private CashUpCommand getCashUpCommand(int shiftId, String cashUpBy, String type) {
        CashUpCommand cashUpCommand = new CashUpCommand()

        cashUpCommand.setShiftId(shiftId)
        cashUpCommand.setType(type)
        cashUpCommand.setCashUpBy(cashUpBy)
        cashUpCommand.setFiftyPounds(BigDecimal.valueOf(1000))
        cashUpCommand.setTwentyPounds(BigDecimal.valueOf(1000))
        cashUpCommand.setTenPounds(BigDecimal.valueOf(1000))
        cashUpCommand.setFivePounds(BigDecimal.valueOf(1000))
        cashUpCommand.setTwoPounds(BigDecimal.valueOf(1000))
        cashUpCommand.setOnePounds(BigDecimal.valueOf(1000))
        cashUpCommand.setFiftyPences(BigDecimal.valueOf(1000))
        cashUpCommand.setTwentyPences(BigDecimal.valueOf(1000))
        cashUpCommand.setTenPences(BigDecimal.valueOf(1000))
        cashUpCommand.setFivePences(BigDecimal.valueOf(1000))
        cashUpCommand.setTwoPences(BigDecimal.valueOf(1000))
        cashUpCommand.setOnePences(BigDecimal.valueOf(1000))

        cashUpCommand.setCashTotal(BigDecimal.valueOf(1000))
        cashUpCommand.setChequesTotal(BigDecimal.valueOf(1000))
        cashUpCommand.setVouchersTotal(BigDecimal.valueOf(1000))

        return cashUpCommand
    }

    private SaveShiftCommand getSaveShiftCommand(int shiftId, TenderReconciliationVarianceReason reason, String reasonText, int safeLocationId) {
        SaveShiftCommand saveShiftCommand = new SaveShiftCommand()

        saveShiftCommand.setShiftId(shiftId)
        saveShiftCommand.setTenderReconciliationVarianceReason(reason)
        saveShiftCommand.setTenderReconciliationVarianceReasonText(reasonText)
        saveShiftCommand.setSafeLocationId(safeLocationId)

        return saveShiftCommand
    }

    private Snapshot getSafeSnapshot(int id, int retailerId, int storeId, int locationId) {
        Snapshot snapshot = new Snapshot()

        snapshot.setId(id)
        snapshot.setRetailerId(retailerId)
        snapshot.setStoreId(storeId)
        snapshot.setLocationId(locationId)

        return snapshot
    }

    private Location getLocation(int id, int retailerId, int storeId, Integer tillId, Integer safeId) {
        Location location = new Location()

        location.setId(id)
        location.setRetailerId(retailerId)
        location.setStoreId(storeId)
        location.setTillId(tillId)
        location.setSafeId(safeId)
        location.setType(safeId != null ? LocationType.SAFE : LocationType.TILL)

        return location
    }
}
