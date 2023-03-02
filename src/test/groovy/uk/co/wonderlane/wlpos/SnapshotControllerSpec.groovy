package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import spock.lang.Specification
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.TenderReconciliationVarianceReason
import uk.co.wonderlane.wlpos.enums.TenderType

class SnapshotControllerSpec extends Specification implements ControllerUnitTest<SnapshotController>, DataTest{

    def setup() {}

    def cleanup() {}

    Class<?>[] getDomainClassesToMock(){
        return [ButtonGrid, Button] as Class[]
    }

    //------------------- Calling Index Action ---------------------------------------------//

    def 'Test calling index action successfully render index page'() {

        given:

        when: 'The index action is executed'
        HashMap model = controller.index()

        then: 'The model index render'
        assert view == "/snapshot/index.gsp"
        assert model.startDate == DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)
        assert model.endDate == DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()
    }

    //------------------- Calling Ajax Get Snapshots Action ---------------------------------------------//

    def 'Test calling get snapshots action successfully'() {

        given:

        String startDate = "02/12/2022"
        String endDate = "10/12/2022"

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        DateTime parseStartDate = DateTime.parse(startDate, dateFormatter)
        DateTime parseEndDate = DateTime.parse(endDate, dateFormatter)

        controller.snapshotService = Stub(SnapshotService){
            getSnapshots(parseStartDate, parseEndDate) >> getDummySnapshotList(true, true)
        }

        views['/snapshot/_snapshotViewerResults.gsp'] = "test"

        when: 'The ajaxGetSnapshots action is executed'
        params["startDate"] = startDate
        params["endDate"] = endDate
        controller.ajaxGetSnapshots()

        then: 'The template response successful with returning snapshot list'
        assert controller.response.text == 'test'
        assert model.snapshots
        assert model.snapshots.size() == 1
        assert model.snapshots.get(0).id == 1
        assert model.snapshots.get(0).retailerId == 9
        assert model.snapshots.get(0).storeId == 234

    }

    //------------------- Calling Ajax Get Safe Action ---------------------------------------------//

    def 'Test calling get safe action successfully'() {

        given:

        Snapshot snapshotMock =  null
        if (existingSnapshot){
            snapshotMock = getDummySnapshot(isCashRequired, isVoucherRequired)
        }

        controller.snapshotService = Stub(SnapshotService){
            getSafeSnapshot() >> snapshotMock
        }

        views['/snapshot/_snapshotModal.gsp'] = "test"

        when: 'The ajaxGetSafe action is executed'
        controller.ajaxGetSafe()

        then: 'The response rendering base on existence of snapshot'
        if (existingSnapshot){
            assert controller.response.text == 'test'
            assert model.snapshot
            assert model.snapshot.id == 1
            assert model.snapshot.retailerId == 9
            assert model.snapshot.storeId == 234
        } else {
            assert controller.response.text == "Unable to retrieve safe."
        }

        where: 'Pass following input parameters'
        existingSnapshot || isCashRequired || isVoucherRequired
        true             || true           || true
        false            || true           || true

    }

    //------------------- Calling Ajax Get Snapshot Action ---------------------------------------------//

    def 'Test calling get snapshot action successfully'() {

        given:

        Snapshot snapshotMock =  null
        if (snapshotId > 0){
            snapshotMock = getDummySnapshot(isCashRequired, isVoucherRequired)
        }

        controller.snapshotService = Stub(SnapshotService){
            getSnapshot(snapshotId) >> snapshotMock
        }

        views['/snapshot/_snapshotSummaryModal.gsp'] = "test"

        when: 'The ajaxGetSnapshot action is executed'
        controller.ajaxGetSnapshot(snapshotId)

        then: 'The response rendering base on existence of snapshot'
        if (snapshotId > 0){
            assert controller.response.text == 'test'
            assert model.snapshot
            assert model.snapshot.id == 1
            assert model.snapshot.retailerId == 9
            assert model.snapshot.storeId == 234
        } else {
            assert controller.response.text == "Unable to retrieve snapshot"
        }

        where: 'Pass following input parameters'
        snapshotId   || isCashRequired || isVoucherRequired
        1            || true           || true
        0            || true           || true

    }

    //------------------- Calling Ajax Save Safe Count Action ---------------------------------------------//

    def 'Test calling save safe snapshot action successfully'() {

        given:

        SaveSafeCommand saveSafeCommand = new SaveSafeCommand();
        saveSafeCommand.cashUpBy = cashUpBy
        saveSafeCommand.cashTotal = cashInputTotal
        saveSafeCommand.vouchersTotal = vouchersInputTotal
        saveSafeCommand.fiftyPounds = BigDecimal.ONE
        saveSafeCommand.twentyPounds = BigDecimal.ONE
        saveSafeCommand.tenPounds = BigDecimal.ONE
        saveSafeCommand.fivePounds = BigDecimal.ONE
        saveSafeCommand.twoPounds = BigDecimal.ONE
        saveSafeCommand.onePounds = BigDecimal.ONE
        saveSafeCommand.fiftyPences = BigDecimal.ONE
        saveSafeCommand.twentyPences = BigDecimal.ONE
        saveSafeCommand.tenPences = BigDecimal.ONE
        saveSafeCommand.fivePences = BigDecimal.ONE
        saveSafeCommand.twoPences = BigDecimal.ONE
        saveSafeCommand.onePences = BigDecimal.ONE

        Snapshot snapshotMock =  getDummySnapshot(isCashRequired, isVoucherRequired)

        controller.snapshotService = Stub(SnapshotService){
            getSafeSnapshot() >> snapshotMock
            saveSnapshot(_) >> null
        }

        views['/snapshot/_snapshotSummaryModal.gsp'] = "test"

        when: 'The ajaxSaveSafeCount action is executed'
        controller.ajaxSaveSafeCount(saveSafeCommand)

        then: 'The template response successful with updating snapshot values'
        assert controller.response.text == "test"
        assert model.snapshot
        assert model.varianceReasons
        assert model.varianceReasons.size() == 3

        /* Assert For Cash Total Values */
        ReconciliationTotal cashTotal = model.snapshot.totals.find {it.tenderType == TenderType.CASH}
        assert cashTotal != null
        assert cashTotal.value == getExpectedCashTotal(saveSafeCommand)
        assert cashTotal.variance == getExpectedCashVariance(saveSafeCommand, snapshotMock)

        /* Assert For Voucher Total Values */
        ReconciliationTotal vouchersTotal = model.snapshot.totals?.find { it.tenderType == TenderType.VOUCHER }
        assert vouchersTotal != null
        assert vouchersTotal.value == getExpectedVoucherTotal(saveSafeCommand)
        assert vouchersTotal.variance == getExpectedVoucherVariance(saveSafeCommand, snapshotMock)

        /* Assert For Snapshot Variance */
        assert model.snapshot.variance == getExpectedSnapshotVariance(snapshotMock)

        where: 'Pass following input parameters'
        cashUpBy       || isCashRequired || isVoucherRequired ||  cashInputTotal     || vouchersInputTotal
        ""             || true           || true              ||  new BigDecimal(0)  || new BigDecimal(0)
        ""             || true           || false             ||  new BigDecimal(0)  || new BigDecimal(0)
        ""             || false          || true              ||  new BigDecimal(10) || new BigDecimal(10)
        ""             || false          || false             ||  new BigDecimal(10) || new BigDecimal(10)
        "VALUE"        || true           || true              ||  new BigDecimal(10) || new BigDecimal(10)
        "VALUE"        || true           || false             ||  new BigDecimal(10) || new BigDecimal(10)
        "VALUE"        || false          || true              ||  new BigDecimal(10) || new BigDecimal(10)
        "VALUE"        || false          || false             ||  new BigDecimal(10) || new BigDecimal(10)
        "DENOMINATION" || true           || true              ||  new BigDecimal(10) || new BigDecimal(10)
        "DENOMINATION" || true           || false             ||  new BigDecimal(10) || new BigDecimal(10)
        "DENOMINATION" || false          || true              ||  new BigDecimal(10) || new BigDecimal(10)
        "DENOMINATION" || false          || false             ||  new BigDecimal(10) || new BigDecimal(10)

    }

    //------------------- Calling Ajax Save Snapshot Action ---------------------------------------------//

    def 'Test calling save snapshot action successfully'() {

        given:

        SaveSnapshotCommand snapshotCommand = new SaveSnapshotCommand()
        snapshotCommand.snapshotId = 1
        snapshotCommand.varianceReason = varianceType
        snapshotCommand.varianceReasonText = "Dummy Variance text"

        Snapshot snapshotMock =  getDummySnapshot(isCashRequired, isVoucherRequired)

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("id", 9);
                put("retailerId", 9);
                put("storeId", 234);
                put("usersName", "WonderLane User")}}
        }

        controller.snapshotService = Stub(SnapshotService){
            getSnapshot(snapshotCommand.snapshotId) >> snapshotMock
        }
        views['/snapshot/_snapshotSummaryModal.gsp'] = "test"

        when: 'The ajaxSaveSnapshot action is executed'
        controller.ajaxSaveSnapshot(snapshotCommand)

        then: 'The response rendering successfully'
        assert controller.response.text == 'test'
        assert model.snapshot
        assert model.snapshot.id == 1
        assert model.snapshot.countedByUserId == 9
        assert model.snapshot.countedByUsersName == "WonderLane User"

        if (varianceType != null){
            assert model.snapshot.varianceReason == TenderReconciliationVarianceReason.OTHER
            assert model.snapshot.varianceReasonText == "Dummy Variance text"
        }

        where: 'Pass following input parameters'
        varianceType                             || isCashRequired || isVoucherRequired
        TenderReconciliationVarianceReason.OTHER || true           || true
        null                                     || true           || true

    }

    BigDecimal getExpectedCashTotal(SaveSafeCommand safeCommand){
        if (safeCommand.cashUpBy == "VALUE") {
            return (safeCommand.fiftyPounds + safeCommand.twentyPounds + safeCommand.tenPounds + safeCommand.fivePounds + safeCommand.twoPounds
            + safeCommand.onePounds + safeCommand.fiftyPences + safeCommand.twentyPences + safeCommand.tenPences + safeCommand.fivePences
            + safeCommand.twoPences + safeCommand.onePences)
        } else if (safeCommand.cashUpBy == "DENOMINATION") {
            return (safeCommand.fiftyPounds * 50 + safeCommand.twentyPounds * 20 + safeCommand.tenPounds * 10 + safeCommand.fivePounds * 5
            + safeCommand.twoPounds * 2 + safeCommand.onePounds * 1 + safeCommand.fiftyPences * 0.50 + safeCommand.twentyPences * 0.20
            + safeCommand.tenPences * 0.10 + safeCommand.fivePences * 0.05 + safeCommand.twoPences * 0.02 + safeCommand.onePences * 0.01)
        } else {
            return safeCommand.cashTotal
        }
    }

    BigDecimal getExpectedCashVariance(SaveSafeCommand safeCommand, Snapshot snapshot){
        BigDecimal getExpectedCashTotal = getExpectedCashTotal(safeCommand)

        return ((getExpectedCashTotal ?: BigDecimal.ZERO) -
                (snapshot.expectedTotals.findAll { it.tenderType == TenderType.CASH }?.sum { it.value } ?: BigDecimal.ZERO))
    }

    BigDecimal getExpectedVoucherTotal(SaveSafeCommand safeCommand){
        return safeCommand.vouchersTotal
    }

    BigDecimal getExpectedVoucherVariance(SaveSafeCommand safeCommand, Snapshot snapshot){
        BigDecimal getExpectedVoucherTotal = getExpectedVoucherTotal(safeCommand)

        return ((getExpectedVoucherTotal ?: BigDecimal.ZERO)
        - (snapshot.expectedTotals.findAll { it.tenderType == TenderType.VOUCHER }?.sum { it.value } ?: BigDecimal.ZERO))
    }

    BigDecimal getExpectedSnapshotVariance(Snapshot snapshot){
        return new BigDecimal(snapshot.totals.sum { it.variance.abs() })
    }

    List<Snapshot> getDummySnapshotList(boolean isCashRequired, boolean  isVoucherRequired){
        Snapshot snapshot = new Snapshot();
        snapshot.setId(1)
        snapshot.setRetailerId(9)
        snapshot.setStoreId(234)
        snapshot.getTotals().addAll(getReconciliationTotal(isCashRequired, isVoucherRequired))
        snapshot.getExpectedTotals().addAll(getExpectedTotal(isCashRequired, isVoucherRequired))
        return Arrays.asList(snapshot)
    }

    Snapshot getDummySnapshot(boolean isCashRequired, boolean  isVoucherRequired){
        Snapshot snapshot = new Snapshot();
        snapshot.setId(1)
        snapshot.setRetailerId(9)
        snapshot.setStoreId(234)
        snapshot.getTotals().addAll(getReconciliationTotal(isCashRequired, isVoucherRequired))
        snapshot.getExpectedTotals().addAll(getExpectedTotal(isCashRequired, isVoucherRequired))
        return snapshot
    }

    List<ReconciliationTotal> getReconciliationTotal(boolean isCashRequired, boolean  isVoucherRequired){
        List<ReconciliationTotal> reconciliationTotalList = new ArrayList<>();

        ReconciliationTotal reconciliationTotal1 = new ReconciliationTotal(TenderType.CARD)
        reconciliationTotal1.setValue(new BigDecimal(1))
        reconciliationTotalList.add(reconciliationTotal1)

        if (isCashRequired){
            ReconciliationTotal reconciliationTotal2 = new ReconciliationTotal(TenderType.CASH)
            reconciliationTotal2.setValue(new BigDecimal(2))
            reconciliationTotalList.add(reconciliationTotal2)
        }

        if (isVoucherRequired){
            ReconciliationTotal reconciliationTotal3 = new ReconciliationTotal(TenderType.VOUCHER)
            reconciliationTotal3.setValue(new BigDecimal(2))
            reconciliationTotalList.add(reconciliationTotal3)
        }

        return reconciliationTotalList
    }

    List<TenderTotal> getExpectedTotal(boolean isCashRequired, boolean  isVoucherRequired){
        List<TenderTotal> tenderTotalArrayList = new ArrayList<>()

        TenderTotal tenderTotal1 = new TenderTotal(TenderType.CARD)
        tenderTotal1.setValue(new BigDecimal(2))
        tenderTotalArrayList.add(tenderTotal1)

        if (isCashRequired){
            TenderTotal tenderTotal2 = new TenderTotal(TenderType.CASH)
            tenderTotal2.setValue(new BigDecimal(1))
            tenderTotalArrayList.add(tenderTotal2)
        }

        if (isVoucherRequired){
            TenderTotal tenderTotal3 = new TenderTotal(TenderType.VOUCHER)
            tenderTotal3.setValue(new BigDecimal(1))
            tenderTotalArrayList.add(tenderTotal3)
        }

        return tenderTotalArrayList
    }

}
