package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import uk.co.wonderlane.wlpos.reporting.PromotionSale
import uk.co.wonderlane.wlpos.reporting.Sale
import uk.co.wonderlane.wlpos.reporting.SaleCategory
import uk.co.wonderlane.wlpos.reporting.SortParams

class ReportingControllerSalesSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        return [Store, PromotionSale, Sale, SaleCategory] as Class[]
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

    def cleanup() {
    }

    // Sales report
    def 'Should generate the sales department details for the main sales report'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        controller.salesDepartment()

        then:
        view == '/reporting/salesDepartment.gsp'

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate sales department results successfully - ajaxSalesDepartment'() {
        given:
        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)
        sortParams.setOffset(offset)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['storeFilter'] = storeFilter
        params['descriptionFilter'] = descriptionFilter
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            Sale mockSale1 = getMockSale(1, 1, 1)
            Sale mockSale2 = getMockSale(2, 1, 1)

            SaleCategory mockSaleCategory = getMockSaleCategory(1, descriptionFilter)
            TreeSet<SaleCategory> salesCatSet = new TreeSet()
            salesCatSet.add(mockSaleCategory)

            mockSale1.setSalesCategories(salesCatSet)
            mockSale1.setCostPrice(BigDecimal.valueOf(100))
            mockSale1.setMargin(BigDecimal.valueOf(10))
            mockSale1.setQuantity(10)
            mockSale1.setProductItemCode("ITEM_CODE_1")

            mockSale2.setSalesCategories(salesCatSet)
            mockSale2.setCostPrice(BigDecimal.valueOf(200))

            getSales(_, _, _) >> new ArrayList(List.of(mockSale1, mockSale2))
        }

        when:
        def mockView = '<div class="_salesDepartmentResults"> </div>'
        views['/reporting/_salesDepartmentResults.gsp'] = mockView

        controller.ajaxSalesDepartment(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            model.sales != null
            model.totalResults != null
        }

        where:
        startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder | offset
        "11/10/2020" | "11/11/2021" | 1                | "TEST"            | null        | "false" | "description" | "asc"     | 0
        "11/10/2020" | "11/11/2021" | 1                | "TEST"            | null        | "false" | "quantity"    | "desc"    | 0
        "11/10/2020" | "11/11/2021" | 1                | "TEST"            | null        | "true"  | "description" | "asc"     | 0
        null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"     | 0
        "11/10/2020" | "11/11/2021" | null             | "TEST"            | null        | "false" | "description" | "asc"     | 0
        "11/10/2020" | "11/11/2021" | 1                | "TEST"            | null        | "false" | "description" | "asc"     | 4
    }

    def 'Should generate the sales categories successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.salesCategory()

        then:
        view == '/reporting/salesCategory.gsp'
        model != null
        model.startDate != null
        model.endDate != null
        model.categoryId

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the sales category for main sales report - ajaxSalesCategory'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['categoryId'] = categoryId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            Sale mockSale1 = getMockSale(1, 1, 1)
            Sale mockSale2 = getMockSale(2, 1, 1)

            SaleCategory mockSaleCategory1 = getMockSaleCategory(1, "TEST_1")
            mockSaleCategory1.setCategoryId(1)
            mockSaleCategory1.setCategoryLevel(1)

            SaleCategory mockSaleCategory2 = getMockSaleCategory(2, "TEST_2")
            mockSaleCategory2.setCategoryId(2)
            mockSaleCategory2.setCategoryLevel(mockSaleCategoryLevel)

            TreeSet<SaleCategory> salesCatSet = new TreeSet()
            salesCatSet.add(mockSaleCategory1)
            salesCatSet.add(mockSaleCategory2)

            mockSale1.setSalesCategories(salesCatSet)
            mockSale1.setCostPrice(BigDecimal.valueOf(100))

            mockSale2.setSalesCategories(salesCatSet)
            mockSale2.setCostPrice(BigDecimal.valueOf(200))

            getSalesForCategory(_, _, _, _) >> new ArrayList(List.of(mockSale1, mockSale2))
        }

        when:
        def mockView = '<div class="_salesCategoryResults"> </div>'
        views['/reporting/_salesCategoryResults.gsp'] = mockView

        controller.ajaxSalesCategory(sortParams)

        then:

        noExceptionThrown()

        if (csv == "false") {
            model.sales != null
            model.totalResults != null
        }

        where:
        categoryId | startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder | mockSaleCategoryLevel
        "1"        | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"     | 1
        "1"        | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "quantity"    | "desc"    | 1
        "1"        | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "true"  | "description" | "asc"     | 1
        "1"        | null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"     | 1
        "1"        | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"     | 2

    }

    def 'Should show the sales product details for the bottom level of the sales report'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        controller.salesProduct()

        then:
        view == '/reporting/salesProduct.gsp'

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the sales product results for main sales report - ajaxSalesProduct'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['productId'] = productId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            Sale mockSale1 = getMockSale(1, 1, 1)
            Sale mockSale2 = getMockSale(2, 1, 1)

            SaleCategory mockSaleCategory1 = getMockSaleCategory(1, "TEST_1")
            mockSaleCategory1.setCategoryId(1)
            mockSaleCategory1.setCategoryLevel(1)

            SaleCategory mockSaleCategory2 = getMockSaleCategory(2, "TEST_2")
            mockSaleCategory2.setCategoryId(2)
            mockSaleCategory2.setCategoryLevel(1)

            TreeSet<SaleCategory> salesCatSet = new TreeSet()
            salesCatSet.add(mockSaleCategory1)
            salesCatSet.add(mockSaleCategory2)

            mockSale1.setSalesCategories(salesCatSet)
            mockSale1.setCostPrice(BigDecimal.valueOf(100))

            mockSale2.setSalesCategories(salesCatSet)
            mockSale2.setCostPrice(BigDecimal.valueOf(200))

            getSalesForProduct(_, _, _, _, _, _, _, _, _) >> new ArrayList(List.of(mockSale1, mockSale2))
            countSalesForProduct(_, _, _, _, _) >> 100
        }

        when:
        def mockView = '<div class="_salesProductResults"> </div>'
        views['/reporting/_salesProductResults.gsp'] = mockView

        controller.ajaxSalesProduct(sortParams)

        then:

        noExceptionThrown()

        if (csv == "false") {
            model.sales != null
            model.totalResults != null
        }

        where:
        productId | startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder
        "1"       | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
        "1"       | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "quantity"    | "desc"
        "1"       | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "true"  | "description" | "asc"
        "1"       | null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"
    }

    def 'Should generate the standalone category sales report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.categorySales()

        then:
        view == '/reporting/categorySales.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the standalone product sales report details'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['maxCategoryLevel'] = maxCategoryLevel
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            Sale mockSale1 = getMockSale(1, 1, 1)
            Sale mockSale2 = getMockSale(2, 1, 1)

            SaleCategory mockSaleCategory1 = getMockSaleCategory(1, "TEST_1")
            mockSaleCategory1.setCategoryId(1)
            mockSaleCategory1.setCategoryLevel(1)

            SaleCategory mockSaleCategory2 = getMockSaleCategory(2, "TEST_2")
            mockSaleCategory2.setCategoryId(2)
            mockSaleCategory2.setCategoryLevel(1)

            SaleCategory mockSaleCategory3 = getMockSaleCategory(3, "TEST_3")
            mockSaleCategory3.setCategoryId(2)
            mockSaleCategory3.setCategoryLevel(1)

            TreeSet<SaleCategory> salesCatSet_1 = new TreeSet()
            TreeSet<SaleCategory> salesCatSet_2 = new TreeSet()
            salesCatSet_1.add(mockSaleCategory1)
            salesCatSet_1.add(mockSaleCategory2)

            salesCatSet_2.add(mockSaleCategory3)
            salesCatSet_2.add(mockSaleCategory1)
            salesCatSet_2.add(mockSaleCategory2)

            mockSale1.setSalesCategories(salesCatSet_1)
            mockSale1.setCostPrice(BigDecimal.valueOf(100))

            mockSale2.setSalesCategories(salesCatSet_2)
            mockSale2.setCostPrice(BigDecimal.valueOf(200))

            getSales(_, _, _) >> new ArrayList(List.of(mockSale1, mockSale2))
        }

        when:
        def mockView = '<div class="_categorySalesResults"> </div>'
        views['/reporting/_categorySalesResults.gsp'] = mockView

        controller.ajaxCategorySales(sortParams)

        then:

        noExceptionThrown()

        if (csv == "false") {
            model.sales != null
            model.totalResults != null
        }

        where:
        maxCategoryLevel | startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder
        "1"              | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
        "1"              | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "quantity"    | "desc"
        "1"              | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "true"  | "description" | "asc"
        "1"              | null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"
    }

    def 'Should generate the standalone product sales report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.sales()

        then:
        view == '/reporting/sales.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the standalone category sales report details - ajaxSales'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            Sale mockSale1 = getMockSale(1, 1, 1)
            Sale mockSale2 = getMockSale(2, 1, 1)

            SaleCategory mockSaleCategory1 = getMockSaleCategory(1, "TEST_1")
            mockSaleCategory1.setCategoryId(1)
            mockSaleCategory1.setCategoryLevel(1)

            SaleCategory mockSaleCategory2 = getMockSaleCategory(2, "TEST_2")
            mockSaleCategory2.setCategoryId(2)
            mockSaleCategory2.setCategoryLevel(1)

            TreeSet<SaleCategory> salesCatSet = new TreeSet()
            salesCatSet.add(mockSaleCategory1)
            salesCatSet.add(mockSaleCategory2)

            mockSale1.setSalesCategories(salesCatSet)
            mockSale1.setCostPrice(BigDecimal.valueOf(100))

            mockSale2.setSalesCategories(salesCatSet)
            mockSale2.setCostPrice(BigDecimal.valueOf(200))

            getSales(_, _, _) >> new ArrayList(List.of(mockSale1, mockSale2))
        }

        when:
        def mockView = '<div class="_salesResults"> </div>'
        views['/reporting/_salesResults.gsp'] = mockView

        controller.ajaxSales(sortParams)

        then:

        noExceptionThrown()

        if (csv == "false") {
            model.sales != null
            model.totalResults != null
        }

        where:
        startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder
        "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
        "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "quantity"    | "desc"
        "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "true"  | "description" | "asc"
        null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"
    }


    private Sale getMockSale(int id, int retailerId, int storeId) {
        Sale mockSale = new Sale()

        mockSale.setId(id)
        mockSale.setRetailerId(retailerId)
        mockSale.setStoreId(storeId)
        mockSale.setCostPrice(BigDecimal.valueOf(150))
        mockSale.setRetailPrice(BigDecimal.valueOf(200))
        mockSale.setVatAmount(BigDecimal.valueOf(10))

        return mockSale
    }

    private SaleCategory getMockSaleCategory(int id, String desc) {
        SaleCategory saleCategory = new SaleCategory()

        saleCategory.setId(id)
        saleCategory.setCategoryDescription(desc)

        return saleCategory
    }
}
