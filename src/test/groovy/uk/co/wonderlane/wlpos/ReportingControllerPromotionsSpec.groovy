package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.*

class ReportingControllerPromotionsSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {
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

    def 'Should generate the promotions Grouped report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.promotionsGrouped()

        then:
        view == '/reporting/promotionsGrouped.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the promotion grouped report details'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionTypeFilter'] = promotionTypeFilter
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            PromotionSale promotionSale1 = getMockPromotionalSale(1, 1, 1)
            PromotionSale promotionSale2 = getMockPromotionalSale(2, 1, 1)

            getPromotionSales(_, _, _, _, _) >> new ArrayList(List.of(promotionSale1, promotionSale2))
        }

        when:
        def mockView = '<div class="_promotionsGroupedResults"> </div>'
        views['/reporting/_promotionsGroupedResults.gsp'] = mockView

        controller.ajaxPromotionsGrouped(sortParams)

        then:

        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.promotionSales
            assert model.totalResults
        }

        where:
        promotionTypeFilter | startDate    | endDate      | principalStoreId | descriptionFilter | storeFilter | csv     | sortColumn    | sortOrder
        "BOGOF"             | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
        "BOGOF"             | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "quantity"    | "desc"
        "BOGOF"             | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "true"  | "description" | "asc"
        "BOGOF"             | null         | "11/11/2021" | null             | null              | "1"         | "false" | "description" | "asc"
        "  "                | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
        "INVALID_TYPE"      | "11/10/2020" | "11/11/2021" | 1                | "TEST_1"          | null        | "false" | "description" | "asc"
    }

    def 'Should generate the promotions report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        Store mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        when:
        HashMap model = controller.promotions()

        then:
        view == '/reporting/promotions.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the promotions report successfully-ajaxPromotions'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionId'] = promotionId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            PromotionSale promotionSale1 = getMockPromotionalSale(1, 1, 1)
            PromotionSale promotionSale2 = getMockPromotionalSale(2, 1, 1)

            getPromotionSales(_, _, _, _, _, _, _, _) >> new TestPagedResultList(new ArrayList(List.of(promotionSale1, promotionSale2)))
        }

        when:
        def mockView = '<div class="_promotionsResults"> </div>'
        views['/reporting/_promotionsResults.gsp'] = mockView

        controller.ajaxPromotions(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.promotionSales
            assert model.totalResults
        }

        where:
        promotionId | startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn    | sortOrder
        "1"         | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "description" | "asc"
        "2"         | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity"    | "desc"
        "3"         | "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "description" | "asc"
        "4"         | null         | "11/11/2021" | null             | "1"         | "false" | "description" | "asc"
    }

    def 'Should generate the promotion report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.promotion()

        then:
        view == '/reporting/promotion.gsp'
        model
        model.startDate
        model.endDate

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the promotion report successfully - ajaxPromotion'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionId'] = promotionSaleId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            PromotionSaleProduct promotionSaleProduct1 = getMockPromotionSaleProduct(1, 1)
            PromotionSaleProduct promotionSaleProduct2 = getMockPromotionSaleProduct(2, 1)

            getPromotionSaleProducts(_, _, _, _, _, _, _) >> new TestPagedResultList(
                    new ArrayList(List.of(promotionSaleProduct1, promotionSaleProduct2))
            )
        }

        when:
        def mockView = '<div class="_promotionResults"> </div>'
        views['/reporting/_promotionResults.gsp'] = mockView

        controller.ajaxPromotion(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.promotionSaleProducts
            assert model.totalResults
        }

        where:
        promotionSaleId | startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn    | sortOrder | descriptionFilter
        "1"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "description" | "asc"     | "TEST"
        "2"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity"    | "desc"    | "TEST"
        "3"             | "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "description" | "asc"     | "TEST"
        "4"             | null         | "11/11/2021" | null             | "1"         | "false" | "description" | "asc"     | "TEST"
    }

    private PromotionSale getMockPromotionalSale(int id, int retailerId, int storeId) {
        PromotionSale promotionSale = new PromotionSale()

        promotionSale.setId(id)
        promotionSale.setRetailerId(retailerId)
        promotionSale.setStoreId(storeId)
        promotionSale.setFullPrice(BigDecimal.valueOf(100))
        promotionSale.setDiscount(BigDecimal.valueOf(10))
        promotionSale.setMargin(BigDecimal.valueOf(10))
        promotionSale.setProfit(BigDecimal.valueOf(10))
        promotionSale.setVat(BigDecimal.valueOf(10))

        return promotionSale
    }

    private PromotionSaleProduct getMockPromotionSaleProduct(int id, int productId) {
        PromotionSaleProduct promotionSaleProduct = new PromotionSaleProduct()

        promotionSaleProduct.setId(id)
        promotionSaleProduct.setProductId(productId)
        promotionSaleProduct.setCostPrice(BigDecimal.valueOf(100))
        promotionSaleProduct.setFullPrice(BigDecimal.valueOf(150))
        promotionSaleProduct.setFullPriceMargin(BigDecimal.valueOf(10))
        promotionSaleProduct.setFullPriceProfit(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscount(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscountedMargin(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscountedProfit(BigDecimal.valueOf(10))
        promotionSaleProduct.setVat(BigDecimal.valueOf(10))

        return promotionSaleProduct

    }
}
