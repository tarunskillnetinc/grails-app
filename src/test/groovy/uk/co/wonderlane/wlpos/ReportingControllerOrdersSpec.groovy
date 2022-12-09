package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.SortParams
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier

class ReportingControllerOrdersSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        return [StoreSettings, Supplier] as Class[]
    }

    def setup() {
        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", 1)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            getReportColumns(_) >> new ArrayList<>()
        }
    }

    def 'Should generate the top level of the main orders report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        StoreSettings mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        Supplier mockSupplier = getMockSupplier(1, 1, 100)
        mockSupplier.save(flush: true, failOnError: true)

        mockDomain(StoreSettings, [mockStoreSettings])
        mockDomain(Supplier, [mockSupplier])

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        when:
        HashMap model = controller.orders()

        then:
        view == '/reporting/orders.gsp'
        model

        where:
        startDate    | endDate      | principalStoreId
        "11/10/2020" | "11/11/2021" | 1
        "11/10/2020" | "11/11/2021" | null
        null         | "11/11/2021" | 1
    }

    def 'Should generate the top level of the main orders report successfully - ajaxOrders'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionId'] = productListId
        params['csv'] = csv
        params['supplier'] = supplier

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.productListService = Stub(ProductListService) {
            getOrders(_, _, _, _) >> new TestPagedResultList(getMockOrdersList())
        }

        StoreSettings mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        when:
        def mockView = '<div class="_ordersResults"> </div>'
        views['/reporting/_ordersResults.gsp'] = mockView

        controller.ajaxOrders(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.orders
        }

        where:
        productListId | supplier | startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn        | sortOrder
        "1"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "description"     | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "packQuantity"    | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "orderedQuantity" | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "lineValue"       | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "supplierName"    | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity"        | "asc"
        "3"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "description"     | "desc"
        "4"           | "1"      | null         | "11/11/2021" | null             | "1"         | "false" | "description"     | "desc"
    }

    def 'Should generate the top level of the main order report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate
        params['productListId'] = "1"

        StoreSettings mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        Supplier mockSupplier = getMockSupplier(1, 1, 100)
        mockSupplier.save(flush: true, failOnError: true)

        mockDomain(StoreSettings, [mockStoreSettings])
        mockDomain(Supplier, [mockSupplier])

        when:
        HashMap model = controller.order()

        then:
        view == '/reporting/order.gsp'
        model

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }


    def 'Should generate the top level of the main orders report successfully - ajaxOrder'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionId'] = productListId
        params['csv'] = csv
        params['supplier'] = supplier

        ProductList mockProductList1 = getMockOrderList(1, 1, 1)
        ProductList mockProductList2 = getMockOrderList(2, 1, 1)

        ProductVariant variant1 = getMockProductVariant(1, 1)
        ProductVariant variant2 = getMockProductVariant(2, 1)

        ProductListItem productListItem1 = getMockProductListItem(1, variant1)
        ProductListItem productListItem2 = getMockProductListItem(2, variant2)

        Set<PackLine> set1 = new HashSet<>()
        set1.add(getMockPackLine(1, productListItem1))
        productListItem1.setPackLines(set1)

        Set<PackLine> set2 = new HashSet<>()
        set2.add(getMockPackLine(2, productListItem2))
        productListItem1.setPackLines(set2)

        mockProductList1.setProductListItems(new ArrayList<ProductListItem>(List.of(
                productListItem1
        )))

        mockProductList2.setProductListItems(new ArrayList<ProductListItem>(List.of(
                productListItem2
        )))

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.productListService = Stub(ProductListService) {
            getOrder(_, _, _, _, _) >> new ArrayList<>(
                    List.of(
                            mockProductList1, mockProductList2
                    ))
        }

        when:
        def mockView = '<div class="_orderResults"> </div>'
        views['/reporting/_orderResults.gsp'] = mockView

        controller.ajaxOrder(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.orders
            assert model.totalResults
        }

        where:
        productListId | supplier | startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn        | sortOrder
        "1"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "description"     | "desc"
        "2"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "packQuantity"    | "desc"
        "3"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "orderedQuantity" | "desc"
        "4"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "lineValue"       | "desc"
        "5"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "supplierName"    | "desc"
        "6"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity"        | "asc"
        "7"           | "1"      | "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "description"     | "desc"
        "8"           | "1"      | null         | "11/11/2021" | null             | "1"         | "false" | "description"     | "desc"
    }

    private List<ProductList> getMockOrdersList() {
        List orderList = new ArrayList()

        ProductList mockProductList1 = getMockOrderList(1, 1, 1)
        ProductList mockProductList2 = getMockOrderList(2, 1, 1)

        ProductVariant variant1 = getMockProductVariant(1, 1)
        ProductVariant variant2 = getMockProductVariant(2, 1)

        ProductListItem productListItem1 = getMockProductListItem(1, variant1)
        ProductListItem productListItem2 = getMockProductListItem(2, variant2)

        Set<PackLine> set1 = new HashSet<>()
        set1.add(getMockPackLine(1, productListItem1))
        productListItem1.setPackLines(set1)

        Set<PackLine> set2 = new HashSet<>()
        set2.add(getMockPackLine(2, productListItem2))
        productListItem1.setPackLines(set2)

        mockProductList1.setProductListItems(new ArrayList<ProductListItem>(List.of(
                productListItem1
        )))

        mockProductList2.setProductListItems(new ArrayList<ProductListItem>(List.of(
                productListItem2
        )))

        orderList.add(mockProductList1)
        orderList.add(mockProductList2)

        return orderList
    }

    private ProductList getMockOrderList(int id, int retailerId, int storeId) {
        ProductList productList = new ProductList()

        productList.setId(id)
        productList.setStoreId(storeId)
        productList.setUserId("12345")
        productList.setStatus(ProductListStatus.PENDING)
        productList.setStockAdjustedOnCompletion(true)
        productList.setRetailerId(retailerId)
        productList.setType(ProductListType.ORDER)

        return productList
    }

    private ProductListItem getMockProductListItem(int id, ProductVariant productVariant) {
        ProductListItem productListItem = new ProductListItem()

        productListItem.setId(id)
        productListItem.setProductVariant(productVariant)

        return productListItem
    }

    private ProductVariant getMockProductVariant(int id, int storeId) {
        ProductVariant productVariant = new ProductVariant()

        productVariant.setId(id)
        productVariant.setStoreId(storeId)

        return productVariant
    }

    private PackLine getMockPackLine(int id, ProductListItem productListItem) {
        PackLine packLine = new PackLine()

        packLine.setProductListItem(productListItem)

        packLine.setId(id)
        packLine.setQuantity(BigDecimal.valueOf(10))
        packLine.setPack(getMockPack(1))

        return packLine
    }

    private Pack getMockPack(int id) {
        Pack pack = new Pack()

        pack.setId(id)
        pack.setQuantity(1)

        return pack
    }
}