package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.CharitySale
import uk.co.wonderlane.wlpos.reporting.PayPointSale
import uk.co.wonderlane.wlpos.reporting.PromotionSale
import uk.co.wonderlane.wlpos.reporting.PromotionSaleProduct
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.reporting.Sale
import uk.co.wonderlane.wlpos.reporting.SaleCategory
import uk.co.wonderlane.wlpos.reporting.TillControlEvent

class ReportingServiceSpec extends Specification implements ServiceUnitTest<ReportingService>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        [Sale, SaleCategory, PromotionSale, TillControlEvent, PayPointSale, ReportColumns, CharitySale] as Class<?>[]
    }

    def setup() {
        service.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", 1)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }
    }

    def "Should get sales successfully"() {
        given:
        List<Sale> salesResultList = new TestPagedResultList(List.of(
                getMockSale(1, 1, 1),
                getMockSale(2, 1, 2))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(salesResultList)
        BuildableCriteria defaultCriteria = Sale.createCriteria()
        Sale.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getSales(new DateTime().minusMonths(1), new DateTime(), storeId)
        Sale.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse

        where:
        storeId | _
        1       | _
        null    | _
    }

    def "Should get charity sales successfully"() {
        given:
        List<CharitySale> charitySalesResults = new TestPagedResultList(List.of(
            getMockCharitySale(2, 105, 2, 7, 375, 3.99, 0.09, new DateTime()),
            getMockCharitySale(2, 105, 2, 7, 376, 9.99, 0.50, new DateTime()))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(charitySalesResults)
        BuildableCriteria defaultCriteria = CharitySale.createCriteria()
        CharitySale.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getCharityDonations(new DateTime(), new DateTime(), 2, Integer.MAX_VALUE, 0, "storeNumber", "desc")
        CharitySale.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse
        serviceResponse[0].size() == 2
    }

//     TODO: [YH] executeQuery are currently not supported in this implementation of GORM
//    def "Should get Sales For Category"() {
//        given:
//
//        Sale sale1 = getMockSale(1, 1, 1)
//        Sale sale2 = getMockSale(1, 1, 1)
//
//        List<Sale> salesResultList = new TestPagedResultList(List.of(
//                sale1,
//                sale2)
//        )
//
//        Sale.metaClass.static.executeQuery = { return salesResultList }
//
//        when:
//        def serviceResponse = service.getSalesForCategory(1, new DateTime().minusMonths(1), new DateTime(), 1)
//
//        then:
//        serviceResponse
//    }

    def "Should get promotion sales by description filter successfully"() {
        given:
        List<PromotionSale> promotionSalesResultList = new TestPagedResultList(List.of(
                getMockPromotionSale(1, 1, 1),
                getMockPromotionSale(2, 1, 1))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(promotionSalesResultList)
        BuildableCriteria defaultCriteria = PromotionSale.createCriteria()
        PromotionSale.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getPromotionSales(new DateTime().minusMonths(1), new DateTime(), "description_filter", PromotionType.BOGOF, storeId)
        PromotionSale.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse

        where:
        storeId | _
        1       | _
        null    | _
    }

    def "Should get promotion sales filter successfully"() {
        given:
        List<PromotionSale> promotionSalesResultList = new TestPagedResultList(List.of(
                getMockPromotionSale(1, 1, 1),
                getMockPromotionSale(2, 1, 1))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(promotionSalesResultList)
        BuildableCriteria defaultCriteria = PromotionSale.createCriteria()
        PromotionSale.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getPromotionSales(
                new DateTime().minusMonths(1), new DateTime(), 1, 10, 0, "id", "desc", storeId
        )
        PromotionSale.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse
        serviceResponse.totalCount > 0

        where:
        storeId | _
        1       | _
        null    | _
    }

    def "Should get promotion sale products successfully"() {
        given:
        PromotionSale promotionSale = getMockPromotionSale(1, 1, 1)
        PromotionSaleProduct promotionSaleProduct = getMockPromotionSaleProduct(promotionSale, 1)

        promotionSale.save(flush: true)
        promotionSaleProduct.save(flush: true)

        when:
        def response = service.getPromotionSaleProducts(1, null, 10, 0, "id", "desc", 1)

        then:
        response
        response.totalCount == 1
    }

    def "Should get promotion sale by promotion sale id successfully"() {
        given:
        PromotionSale promotionSale = getMockPromotionSale(1, 1, 1)
        promotionSale.save(flush: true, failOnError: true)

        when:
        def response = service.getPromotionSale(1)

        then:
        response
        response.id == 1
    }

    def "Should get Till Control Events by date range successfully"() {
        given:
        List<TillControlEvent> tillControlEventList = new TestPagedResultList(List.of(
                getMockTillControlEvent(1, 1, 1, 17),
                getMockTillControlEvent(2, 1, 1, 17))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(tillControlEventList)
        BuildableCriteria defaultCriteria = TillControlEvent.createCriteria()
        TillControlEvent.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getTillControlEvents(new DateTime().minusMonths(1), new DateTime())
        TillControlEvent.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse
        serviceResponse[0]
        serviceResponse[0].totalCount == 2
    }

    def "Should get Till Control Events successfully"() {
        given:
        List<TillControlEvent> tillControlEventList = new TestPagedResultList(List.of(
                getMockTillControlEvent(1, 1, 1, 17),
                getMockTillControlEvent(2, 1, 1, 17))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(tillControlEventList)
        BuildableCriteria defaultCriteria = TillControlEvent.createCriteria()
        TillControlEvent.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getTillControlEvents(
                new DateTime().minusMonths(1), new DateTime(), TillControlEventType.CUSTOMER_REFUSAL, 10, 0, "id", "desc"
        )

        TillControlEvent.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse
        serviceResponse[0]
        serviceResponse[0].totalCount == 2
    }

    def "Should get Pay point sales successfully"() {
        given:
        List<PayPointSale> payPointSalesList = new TestPagedResultList(List.of(
                getMockPayPointSale(1, 1, 1),
                getMockPayPointSale(2, 1, 1))
        )

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(payPointSalesList)
        BuildableCriteria defaultCriteria = PayPointSale.createCriteria()
        PayPointSale.metaClass.static.createCriteria = { return mockCriteria }

        when:
        def serviceResponse = service.getPayPointSales(
                new DateTime().minusMonths(1), new DateTime(), 1, "TEST_STATUS", "TEST_DESC", 10, 0, "id", "desc"
        )

        TillControlEvent.metaClass.static.createCriteria = { return defaultCriteria }

        then:
        serviceResponse
        serviceResponse[0]
        serviceResponse[0].totalCount == 2
    }

    def "Should get Report Columns"() {
        given:
        service.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("id", 1)
            map.put("retailerId", 1)
            map.put("storeId", 1)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        ReportColumns reportColumns = getMockReportColumns(1, 1, ReportType.DELIVERY)
        mockDomain(ReportColumns, [reportColumns])

        when:
        def serviceResponse = service.getReportColumns(ReportType.DELIVERY)

        then:
        serviceResponse
        serviceResponse.id == 1
    }

    void "should save report columns successfully"() {
        given:
        ReportColumns reportColumns = getMockReportColumns(100, 101, ReportType.DELIVERY)

        when:
        ReportColumns serviceResponse = service.saveReportColumns(reportColumns)

        then:
        serviceResponse
        serviceResponse.getId() == 100
        serviceResponse.getUserId() == 101
    }

    private Sale getMockSale(int id, int retailerId, int storeId) {
        Sale sale = new Sale()

        sale.setId(id)
        sale.setRetailerId(retailerId)
        sale.setStoreId(storeId)
        sale.setDateCreated(new DateTime().minusDays(5))

        return sale
    }

    private SaleCategory getMockSaleCategory(Sale sale, int id, int categoryId) {
        SaleCategory saleCategory = new SaleCategory()

        saleCategory.setSales(sale)
        saleCategory.setId(id)
        saleCategory.setId(categoryId)

        return saleCategory
    }

    private PromotionSale getMockPromotionSale(int id, int retailerId, int storeId) {
        PromotionSale promotionSale = new PromotionSale()

        promotionSale.setId(id)
        promotionSale.setRetailerId(retailerId)
        promotionSale.setStoreId(storeId)
        promotionSale.setTillId(1)
        promotionSale.setPromotionId(1)
        promotionSale.setFullPrice(BigDecimal.valueOf(100))
        promotionSale.setDiscount(BigDecimal.valueOf(10))
        promotionSale.setMargin(BigDecimal.valueOf(10))
        promotionSale.setProfit(BigDecimal.valueOf(10))
        promotionSale.setVat(BigDecimal.valueOf(10))
        promotionSale.setDateCreated(new DateTime())
        promotionSale.setType(PromotionType.BOGOF)
        promotionSale.setDescription("description_" + id)

        return promotionSale
    }

    private PromotionSaleProduct getMockPromotionSaleProduct(PromotionSale promotionSale, int id) {
        PromotionSaleProduct promotionSaleProduct = new PromotionSaleProduct()

        promotionSaleProduct.setPromotion(promotionSale)
        promotionSaleProduct.setId(id)
        promotionSaleProduct.setProductId(1)
        promotionSaleProduct.setItemCode("ITEM_CODE_" + id)
        promotionSaleProduct.setDescription("DESC_" + id)
        promotionSaleProduct.setCostPrice(BigDecimal.valueOf(100))
        promotionSaleProduct.setFullPrice(BigDecimal.valueOf(100))
        promotionSaleProduct.setFullPriceMargin(BigDecimal.valueOf(10))
        promotionSaleProduct.setFullPriceProfit(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscount(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscountedMargin(BigDecimal.valueOf(10))
        promotionSaleProduct.setDiscountedProfit(BigDecimal.valueOf(10))
        promotionSaleProduct.setVat(BigDecimal.valueOf(10))

        return promotionSaleProduct
    }

    private TillControlEvent getMockTillControlEvent(int id, int retailerId, int storeId, int tillId) {
        TillControlEvent tillControlEvent = new TillControlEvent()

        tillControlEvent.setId(id)
        tillControlEvent.setRetailerId(retailerId)
        tillControlEvent.setStoreId(storeId)
        tillControlEvent.setTillId(tillId)
        tillControlEvent.setType(TillControlEventType.CUSTOMER_REFUSAL)
        tillControlEvent.setReason("REASON_" + id)

        return tillControlEvent
    }

    private PayPointSale getMockPayPointSale(int id, int retailerId, int storeId) {
        PayPointSale payPointSale = new PayPointSale()

        payPointSale.setId(id)
        payPointSale.setRetailerId(retailerId)
        payPointSale.setStoreId(storeId)
        payPointSale.setStatus("TEST_STATUS")
        payPointSale.setTransactionDate(new DateTime())

        return payPointSale
    }

    private CharitySale getMockCharitySale(int storeId, int storeNumber, int retailerId, int tillId, int transactionId, BigDecimal basketTotal, BigDecimal donationTotal, DateTime dateCreated) {
        CharitySale charitySale = new CharitySale()

        charitySale.setStoreId(storeId)
        charitySale.setStoreNumber(storeNumber)
        charitySale.setRetailerId(retailerId)
        charitySale.setTillId(tillId)
        charitySale.setTransactionId(transactionId)
        charitySale.setBasketTotal(basketTotal)
        charitySale.setDonationTotal(donationTotal)
        charitySale.setDateCreated(dateCreated)

        return charitySale
    }

    private ReportColumns getMockReportColumns(int id, int userId, ReportType reportType) {
        ReportColumns reportColumns = new ReportColumns()

        Set<ReportColumn> reportColumnSet = new HashSet<>();
        reportColumnSet.add(getReportColumn(1, "storeId"))

        reportColumns.setId(id)
        reportColumns.setUserId(userId)
        reportColumns.setReportType(reportType)
        reportColumns.setColumns(reportColumnSet)

        return reportColumns
    }

    private ReportColumn getReportColumn(int id, String column) {
        ReportColumn reportColumn = new ReportColumn("id": id, "column": column)
        reportColumn.setEnabled(true)

        return reportColumn
    }
}
