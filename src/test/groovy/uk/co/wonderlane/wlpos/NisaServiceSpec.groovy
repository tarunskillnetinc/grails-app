package uk.co.wonderlane.wlpos


import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.mockito.Mockito
import spock.lang.Specification
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.helpers.NisaServiceHelper
import uk.co.wonderlane.wlpos.helpers.NisaServiceNetworkTestHelper

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet

class NisaServiceSpec extends Specification implements ServiceUnitTest<NisaServiceHelper>, DataTest {

    Connection mockConnection

    def setup() {
        mockConnection = Mock(Connection)
    }

    //-------------------------------generateXMLForOrder function Unit tests----------------------------//

    void 'should generate Nisa product download response'() {
        given:

        NisaService nisaService = new NisaServiceHelper(Mock(DatabaseCredentials), mockConnection)

        uk.co.wonderlane.wlpos.entities.wlim.ProductList orderProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        orderProductList.getProductListItems().add(getMockProductListItem(100))
        orderProductList.setStoreId("100")
        orderProductList.setSupplierReference(supplierRef)

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        callableStatementMock.execute() >> resultSetMock
        callableStatementMock.getResultSet() >> resultSetMock
        resultSetMock.next() >>> [true, false, true, true, false, false]
        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("retailerId") >> 9
        resultSetMock.getInt("storeId") >> 100
        resultSetMock.getInt("symbolGroupId") >> 0
        resultSetMock.getBoolean("active") >> true
        resultSetMock.getString("storeIdentifier") >> "100"
        resultSetMock.getString("organisationIdentifier") >> "100"
        resultSetMock.getString("username") >> "username"
        resultSetMock.getString("password") >> "password"
        resultSetMock.getString("additionalPassword") >> "password"
        resultSetMock.getString("status") >> "ACTIVE"
        resultSetMock.getString("error") >> "N/A"
        resultSetMock.getDate(Mockito.isA(String)) >> new Date(System.currentTimeMillis())

        resultSetMock.getString("apiUrl") >> "http://fke.fakedomain.fke/nisa_fake_url"

        resultSetMock.getInt("packId") >> 101
        resultSetMock.getInt("quantity") >> 100
        resultSetMock.getString("orderCode") >> "100"

        nisaService.springSecurityService = getFakeSpringSecurityService()

        when: 'generateXMLForOrder action is executed'
        def response = nisaService.generateXMLForOrder(mockConnection, orderProductList)

        then: 'generateXMLForOrder action response is correct'
        response == 'http://www.ntorder-uat.com/tk/9e5d0d9640494954b4741f19acdddfb7/App_presentation/order_entry/ViewOrder.aspx?oip_id=90359096'

        where:
        ID | supplierRef
        1  | "Nisa Way"
        2  | "Nisa FAC"
        3  | "Other"
    }

    void 'should throw connection error if nisa service not reachable'() {
        given:
        NisaService nisaService = new NisaServiceNetworkTestHelper(Mock(DatabaseCredentials), mockConnection)

        uk.co.wonderlane.wlpos.entities.wlim.ProductList orderProductList = new uk.co.wonderlane.wlpos.entities.wlim.ProductList()
        orderProductList.getProductListItems().add(getMockProductListItem(100))
        orderProductList.setStoreId("100")

        CallableStatement callableStatementMock = Mock(CallableStatement)
        ResultSet resultSetMock = Mock(ResultSet)
        mockConnection.prepareCall(_) >> callableStatementMock
        callableStatementMock.executeQuery() >> resultSetMock
        callableStatementMock.execute() >> resultSetMock
        callableStatementMock.getResultSet() >> resultSetMock
        resultSetMock.next() >>> [true, false, true, true, false, false]
        resultSetMock.getInt("id") >> 100
        resultSetMock.getInt("retailerId") >> 9
        resultSetMock.getInt("storeId") >> 100
        resultSetMock.getInt("symbolGroupId") >> 0
        resultSetMock.getBoolean("active") >> true
        resultSetMock.getString("storeIdentifier") >> "100"
        resultSetMock.getString("organisationIdentifier") >> "100"
        resultSetMock.getString("username") >> "username"
        resultSetMock.getString("password") >> "password"
        resultSetMock.getString("additionalPassword") >> "password"
        resultSetMock.getString("status") >> "ACTIVE"
        resultSetMock.getString("error") >> "N/A"
        resultSetMock.getDate(Mockito.isA(String)) >> new Date(System.currentTimeMillis())

        resultSetMock.getString("apiUrl") >> "http://fke.fakedomain.fke/nisa_fake_url"

        resultSetMock.getInt("packId") >> 101
        resultSetMock.getInt("quantity") >> 100
        resultSetMock.getString("orderCode") >> "100"

        nisaService.springSecurityService = getFakeSpringSecurityService()

        when: 'generateXMLForOrder action is executed'
        def response = nisaService.generateXMLForOrder(mockConnection, orderProductList)

        then: 'generateXMLForOrder action response is correct'
        response == null
        Exception e = thrown()
        e != null
        e instanceof UnknownHostException
    }

    private uk.co.wonderlane.wlpos.entities.wlim.ProductListItem getMockProductListItem(int id) {
        uk.co.wonderlane.wlpos.entities.wlim.ProductListItem productListItem = new uk.co.wonderlane.wlpos.entities.wlim.ProductListItem()

        productListItem.setId(id)
        productListItem.setProductQuantityInStock(10)
        productListItem.setFillQuantity(10)

        return productListItem
    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 1)
                }
            }
        }
    }

}
