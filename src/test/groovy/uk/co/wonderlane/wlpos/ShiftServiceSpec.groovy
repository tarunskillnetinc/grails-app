package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.helpers.ShiftServiceHelper

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

class ShiftServiceSpec extends Specification implements ServiceUnitTest<ShiftServiceHelper>, DataTest {

    ShiftServiceHelper shiftServiceHelper
    Connection mockConnection

    def setup() {
        mockConnection = Mock(Connection)

        shiftServiceHelper = new ShiftServiceHelper(Mock(DatabaseCredentials), mockConnection)
        shiftServiceHelper.gsonProvider = Stub(GsonProvider) {
            getGson() >> new GsonBuilder().create()
        }
    }

    def 'Should get shifts successfully'() {
        given:

        def springSecService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()
            map.put("retailerId", 9)
            map.put("storeId", principalStoreId)

            getPrincipal() >> map
        }

        shiftServiceHelper.springSecurityService = springSecService

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getString("shift") >> getShiftDummyJson()

        when:
        List<Shift> shiftsReturned = shiftServiceHelper.getShifts(tillId)

        then: 'successfully get snapshot'
        shiftsReturned.size() == returnListSize


        where:
        returnListSize | principalStoreId | tillId | resultSetNextResult
        1              | 1                | 17     | [true, false]
        2              | 1                | 17     | [true, true, false]
        1              | null             | null   | [true, false]
        2              | null             | null   | [true, true, false]
        0              | null             | null   | [false]
    }

    def 'Should get shift by shift id successfully'() {
        given:

        shiftServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9)
                    put("storeId", 234)
                }
            }
        }

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        resultSetMock.next() >>> resultSetNextResult
        resultSetMock.getString("shift") >> getShiftDummyJson()

        when:
        Shift shiftReturned = shiftServiceHelper.getShift(1, -1, -1)

        then:
        (shiftReturned == null) == emptyResult

        where:
        emptyResult | resultSetNextResult
        false       | [true, false]
        true        | [false]
    }


    def 'Should save the shift successfully'() {
        given:
        Shift shitToBeSaved = new Shift()
        shitToBeSaved.setId(shiftId)

        CallableStatement callableStatementMock = Mock(CallableStatement)

        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeUpdate() >> 1

        when:
        shiftServiceHelper.saveShift(shitToBeSaved)

        then:
        noExceptionThrown()

        where:
        shiftId | _
        1       | _
        0       | _
    }

    private String getShiftDummyJson() {
        return "{\n" +
                "  \"id\" : 1,\n" +
                "  \"retailerId\" : 9,\n" +
                "  \"storeId\" : 234,\n" +
                "  \"tillId\" : 17\n" +
                "}"
    }
}



