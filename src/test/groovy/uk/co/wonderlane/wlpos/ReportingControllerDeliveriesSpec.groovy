package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.SortParams
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier

class ReportingControllerDeliveriesSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        return [Store, Supplier, ProductList, ProductListItem] as Class[]
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

    def 'Should generate the top level of the main deliveries report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        Supplier mockSupplier = getMockSupplier(1, 1, 100)
        mockSupplier.save(flush: true, failOnError: true)

        mockDomain(Store, [mockStoreSettings])
        mockDomain(Supplier, [mockSupplier])

        when:
        HashMap model = controller.deliveries()

        then:
        view == '/reporting/deliveries.gsp'
        model

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    // TODO:[YH] supplier is not a property of ProductList
    def 'Should generate the top level of the main deliveries report successfully- ajaxDeliveries'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['supplier'] = supplierId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.productListService = Stub(ProductListService) {
            getDeliveries(_, _, _, _) >> new TestPagedResultList(getMockDeliveryList())
        }

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        when:
        def mockView = '<div class="_deliveriesResults"> </div>'
        views['/reporting/_deliveriesResults.gsp'] = mockView

        controller.ajaxDeliveries(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.deliveries
        }

        where:
        supplierId | principalStoreId | storeFilter | sortColumn      | sortOrder | startDate    | endDate      | csv
        "1"        | 1                | null        | "deliveryId"    | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "storeId"       | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "status"        | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "deliveryDate"  | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "numberOfItems" | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "totalCost"     | "asc"     | "11/10/2020" | "11/10/2021" | "false"
        "1"        | 1                | null        | "deliveryId"    | "asc"     | "11/10/2020" | "11/10/2021" | "true"
        "1"        | null             | "1"         | "deliveryId"    | "asc"     | "11/10/2020" | "11/10/2021" | "true"
        "1"        | 1                | null        | "deliveryId"    | "desc"    | "11/10/2020" | "11/10/2021" | "true"
    }

    def 'Should generate the top level of the main delivery report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate
        params['productListId'] = "1"

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        Supplier mockSupplier = getMockSupplier(1, 1, 1)
        mockSupplier.save(flush: true, failOnError: true)

        ProductList productList = getMockDelivery(1, 1, 1)
        productList.save(flush: true, failOnError: true)

        mockDomain(Store, [mockStoreSettings])
        mockDomain(Supplier, [mockSupplier])
        mockDomain(ProductList, [productList])

        when:
        HashMap model = controller.delivery()

        then:
        view == '/reporting/delivery.gsp'
        model

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the top level of the main delivery report successfully- ajaxDelivery'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['supplier'] = supplierId
        params['csv'] = csv
        params['productListId'] = "1"

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.productListService = Stub(ProductListService) {
            getProductList(_) >> getMockDeliveryObject()
        }

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        when:
        def mockView = '<div class="_deliveryResults"> </div>'
        views['/reporting/_deliveryResults.gsp'] = mockView

        controller.ajaxDelivery(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.items
        }

        where:
        principalStoreId | storeFilter | sortColumn     | sortOrder | startDate    | endDate      | csv     | supplierId
        1                | null        | "sku"          | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "description"  | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "itemQuantity" | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "totalCost"    | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "sku"          | "asc"     | null         | "11/10/2021" | "false" | "1"
        1                | null        | "sku"          | "asc"     | "11/10/2020" | "11/10/2021" | "true"  | "1"
        1                | null        | "sku"          | "desc"    | "11/10/2020" | "11/10/2021" | "true"  | "1"
        null             | "1"         | "sku"          | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "sku"          | "asc"     | "11/10/2020" | "11/10/2021" | "false" | null
    }

    def 'Should accept delivery'() {
        given:
        params['productListId'] = "1"
        controller.productListService = Stub(ProductListService) {
            acceptDelivery(_) >> void
            getProductList(_) >> getMockDeliveryObject()
        }

        when:
        controller.ajaxAcceptDelivery()

        then:
        noExceptionThrown()
    }

    def 'Should generate the Delivery Pack Lines report successfully'() {
        given:
        params['productListId'] = "1"
        params['productListItemId'] = "1"
        params['startDate'] = startDate
        params['endDate'] = endDate
        params['supplierId'] = "1"
        params['storeId'] = "1"
        params['descriptionFilter'] = ""

        controller.productListService = Stub(ProductListService) {
            getProductListItem(_) >> getMockDeliveryObject().getProductListItems().get(0)
        }

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        Supplier mockSupplier = getMockSupplier(1, 1, 100)
        mockSupplier.save(flush: true, failOnError: true)

        mockDomain(Store, [mockStoreSettings])
        mockDomain(Supplier, [mockSupplier])

        when:
        HashMap model = controller.deliveryPackLines()

        then:
        view == '/reporting/deliveryPackLines.gsp'
        model

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }


    def 'Should generate the Delivery Pack Lines report successfully - ajaxDeliveryPackLines'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['productListId'] = "1"
        params['productListItemId'] = "1"
        params['supplier'] = supplierId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        ProductList productList = getMockDeliveryObject()

        controller.productListService = Stub(ProductListService) {
            getProductListItem(_) >> productList.getProductListItems().get(0)
        }

        when:
        def mockView = '<div class="_deliveryPackLineResults"> </div>'
        views['/reporting/_deliveryPackLineResults.gsp'] = mockView

        controller.ajaxDeliveryPackLines(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.packLines
        }

        where:
        principalStoreId | storeFilter | sortColumn         | sortOrder | startDate    | endDate      | csv     | supplierId
        1                | null        | "description"      | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "price"            | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "packCost"         | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "packSize"         | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "deliveryQuantity" | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "totalQuantity"    | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "totalSellValue"   | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "description"      | "asc"     | null         | "11/10/2021" | "false" | "1"
        1                | null        | "description"      | "asc"     | "11/10/2020" | "11/10/2021" | "true"  | "1"
        1                | null        | "description"      | "desc"    | "11/10/2020" | "11/10/2021" | "true"  | "1"
        null             | "1"         | "description"      | "asc"     | "11/10/2020" | "11/10/2021" | "false" | "1"
        1                | null        | "description"      | "asc"     | "11/10/2020" | "11/10/2021" | "false" | null
    }

    private ProductList getMockDeliveryObject() {
        Product mockProduct = getMockProduct(1, 1, "TP_001")
        ProductList mockProductList = getMockDelivery(1, 1, 1)

        ProductVariant mockVariant_1 = getMockProductVariant(1, 1)
        mockVariant_1.setProduct(mockProduct)

        ProductVariant mockVariant_2 = getMockProductVariant(2, 1)
        mockVariant_2.setProduct(mockProduct)

        ProductListItem mockProductListItem_1 = getMockProductListItem(1, mockVariant_1)
        ProductListItem mockProductListItem_2 = getMockProductListItem(2, mockVariant_2)

        Set<PackLine> packLineSet_1 = new HashSet<>()
        Set<PackLine> packLineSet_2 = new HashSet<>()

        PackLine packLine_1 = getMockPackLine(1, mockProductListItem_1)
        PackLine packLine_2 = getMockPackLine(2, mockProductListItem_1)

        PackLine packLine_3 = getMockPackLine(3, mockProductListItem_2)
        PackLine packLine_4 = getMockPackLine(4, mockProductListItem_2)

        packLineSet_1.add(packLine_1)
        packLineSet_1.add(packLine_2)

        packLineSet_2.add(packLine_3)
        packLineSet_2.add(packLine_4)

        mockProductListItem_1.setPackLines(packLineSet_1)
        packLine_1.setProductListItem(mockProductListItem_1)
        packLine_2.setProductListItem(mockProductListItem_1)

        mockProductListItem_2.setPackLines(packLineSet_2)
        packLine_3.setProductListItem(mockProductListItem_2)
        packLine_4.setProductListItem(mockProductListItem_2)

        mockProductList.setProductListItems(new ArrayList<ProductListItem>(List.of(
                mockProductListItem_1, mockProductListItem_2
        )))

        return mockProductList
    }

    private List<ProductList> getMockDeliveryList() {
        List orderList = new ArrayList()

        ProductList mockProductList1 = getMockDelivery(1, 1, 1)
        ProductList mockProductList2 = getMockDelivery(2, 1, 1)

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

    private ProductList getMockDelivery(int id, int retailerId, int storeId) {
        ProductList productList = new ProductList()

        productList.setId(id)
        productList.setStore(getMockStoreSettings(storeId))
        productList.setUserId("12345")
        productList.setStatus(ProductListStatus.PENDING)
        productList.setStockAdjustedOnCompletion(true)
        productList.setRetailerId(retailerId)
        productList.setType(ProductListType.DELIVERY)

        return productList
    }

    private ProductListItem getMockProductListItem(int id, ProductVariant productVariant) {
        ProductListItem productListItem = new ProductListItem()

        productListItem.setId(id)
        productListItem.setProductVariant(productVariant)
        productListItem.setProductQuantityInStock(10)
        productListItem.setFillQuantity(10)

        return productListItem
    }

    private ProductVariant getMockProductVariant(int id, int storeId) {
        ProductVariant productVariant = new ProductVariant()

        productVariant.setId(id)
        productVariant.setStoreId(storeId)
        productVariant.setSku(1111)
        productVariant.setEffectiveDate(new DateTime())
        productVariant.setRetailPrice(BigDecimal.valueOf(100))

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

    private Product getMockProduct(int id, int retailerId, String itemCode) {
        Product product = new Product()

        product.setId(id)
        product.setId(retailerId)
        product.setItemCode(itemCode)
        product.setDescription("DESC_" + id)

        return product
    }

    private ProductPrice getMockProductPrice(int id, long sku) {
        ProductPrice productPrice = new ProductPrice()

        productPrice.setId(id)
        productPrice.setSku(sku)
        productPrice.setEffectiveDate(new DateTime())
        productPrice.setPrice(BigDecimal.valueOf(1000))

        return productPrice
    }

    private Store getMockStoreSettings(int id) {
        Store storeSettings = new Store()

        storeSettings.id = id
        storeSettings.storeId = id
        storeSettings.countIncrement = BigDecimal.ONE
        storeSettings.range = getMockRange()
        storeSettings.priceBand = getMockPriceBand()
        storeSettings.type = StoreType.STORE
        storeSettings.printReceiptOption = PrintReceiptOption.ALWAYS_PRINT

        return storeSettings
    }

    private Range getMockRange() {
        return new Range(retailerId: 9, description: "Dummy Range")
    }

    private PriceBand getMockPriceBand() {
        return new PriceBand(retailerId: 9, description: "Dummy Price Band")
    }
}