package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.helpers.SnapshotServiceHelperService

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class SnapshotServiceSpec  extends Specification  implements ServiceUnitTest<SnapshotServiceHelperService>, DataTest{


    SnapshotServiceHelperService snapshotServiceHelperService
    Connection mockConnection
    DatabaseCredentials databaseCredentials

    def setup() {
        databaseCredentials = Mock(DatabaseCredentials)
        mockConnection = Mock(Connection)
        snapshotServiceHelperService = new SnapshotServiceHelperService(databaseCredentials, mockConnection);
    }

    def cleanup() {}

    def 'Test the load snapshots successfully'() {

        given:

        Gson gson = new GsonBuilder().create()

        snapshotServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        snapshotServiceHelperService.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
            gson.fromJson(_) >> new Snapshot()
        }

        DateTime startTime = DateTime.now(DateTimeZone.UTC)

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getString("snapshot") >> getSnapshotDummyJson()


        when: 'The get snapshot action is executed'
        List<Snapshot> snapshotsReturned = snapshotServiceHelperService.getSnapshots(startTime, startTime)

        then: 'successfully get snapshot'
        snapshotsReturned.size() == returnListSize
        if (!emptySet){
            snapshotsReturned.get(0).id == 1
            snapshotsReturned.get(0).retailerId == 9
            snapshotsReturned.get(0).storeId == 234
        }

        where: 'Pass following input parameters'
        emptySet   || resultSetNextResult || returnListSize
        false      || [true, false]       || 1
        true       || [false]             || 0

    }

    def 'Test the load snapshots by id successfully'() {

        given:

        Gson gson = new GsonBuilder().create()

        snapshotServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        snapshotServiceHelperService.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
            gson.fromJson(_) >> new Snapshot()
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getString("snapshot") >> getSnapshotDummyJson()


        when: 'The get snapshot action is executed'
        Snapshot snapshotsReturned = snapshotServiceHelperService.getSnapshot(1)

        then: 'successfully get snapshot'
        if (!emptySet){
            snapshotsReturned
            snapshotsReturned.id == 1
            snapshotsReturned.retailerId == 9
            snapshotsReturned.storeId == 234
        } else {
            snapshotsReturned == null
        }

        where: 'Pass following input parameters'
        emptySet   || resultSetNextResult
        false      || [true, false]
        true       || [false]

    }

    def 'Test the load safe snapshots successfully'() {

        given:

        Gson gson = new GsonBuilder().create()

        snapshotServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        snapshotServiceHelperService.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
            gson.fromJson(_) >> new Snapshot()
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getString("snapshot") >> getSnapshotDummyJson()


        when: 'The get snapshot action is executed'
        Snapshot snapshotsReturned = snapshotServiceHelperService.getSafeSnapshot()

        then: 'successfully get snapshot'
        if (!emptySet){
            snapshotsReturned
            snapshotsReturned.id == expectedReturnedId
            snapshotsReturned.retailerId == 9
            snapshotsReturned.storeId == 234
        } else {
            snapshotsReturned == null
        }

        where: 'Pass following input parameters'
        emptySet   || resultSetNextResult  || expectedReturnedId
        false      || [true, false]        || 1
        false      || [false, false]       || 0

    }

    def 'Test save snapshots successfully'() {

        given:

        Gson gson = new GsonBuilder().create()

        snapshotServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        snapshotServiceHelperService.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
            gson.fromJson(_) >> new Snapshot()
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getInt("id") >> 100

        Snapshot snapshot = getDummySnapshot(snapshotId)


        when: 'The get snapshot action is executed'
        int returnedId = snapshotServiceHelperService.saveSnapshot(snapshot)

        then: 'successfully get snapshot'
        returnedId == responseId

        where: 'Pass following input parameters'
        snapshotId || resultSetNextResult || responseId
        100        || [true, false]       || 100
        0          || [true, false]       || 100
        100        || [false, false]      || 0

    }


    def 'Test save safe snapshots successfully'() {

        given:

        Gson gson = new GsonBuilder().create()

        snapshotServiceHelperService.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        snapshotServiceHelperService.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
            gson.fromJson(_) >> new Snapshot()
        }

        CallableStatement callableStatementMock =  Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.execute() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getInt("id") >> 100

        Snapshot snapshot = getDummySnapshot(snapshotId)


        when: 'The get snapshot action is executed'
        snapshotServiceHelperService.saveSafeSnapshot(snapshot)

        then: 'successfully get snapshot'
        /* This will assert method has been called without throwing any errors */

        where: 'Pass following input parameters'
        snapshotId || resultSetNextResult || responseId
        100        || [true, false]       || 100
        0          || [true, false]       || 100
        100        || [false, false]      || 0

    }


    String getSnapshotDummyJson(){
        return "{\n" +
                "  \"id\" : 1,\n" +
                "  \"retailerId\" : 9,\n" +
                "  \"storeId\" : 234\n" +
                "}"
    }

    Snapshot getDummySnapshot(int snapshotId){
        Snapshot snapshot = new Snapshot();
        snapshot.setId(snapshotId)
        snapshot.setRetailerId(9)
        snapshot.setStoreId(234)
        snapshot.setTotals(getReconciliationTotal())
        return snapshot;
    }

    List<ReconciliationTotal> getReconciliationTotal(){
        ReconciliationTotal reconciliationTotal1 = new ReconciliationTotal(TenderType.CARD)
        reconciliationTotal1.setValue(new BigDecimal(1))

        ReconciliationTotal reconciliationTotal2 = new ReconciliationTotal(TenderType.CASH)
        reconciliationTotal2.setValue(new BigDecimal(2))

        return Arrays.asList(reconciliationTotal1, reconciliationTotal2)
    }


}
