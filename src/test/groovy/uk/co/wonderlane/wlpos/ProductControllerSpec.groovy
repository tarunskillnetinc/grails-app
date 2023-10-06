package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.multipart.MultipartFile
import spock.lang.Shared
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PackStatus
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.helpers.ProductVariantHelper
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType
import uk.co.wonderlane.wlpos.supplier.Pack
import uk.co.wonderlane.wlpos.supplier.Supplier

class ProductControllerSpec extends Specification implements ControllerUnitTest<ProductController>, DataTest {

    @Shared
    DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.UTC)

    Class<?>[] getDomainClassesToMock() {
        [VatCode, Retailer, PriceBand, Range, Supplier, Store,
         RangeProduct, Product, Barcode, ProductVariant, ProductPrice, ReportColumns] as Class<?>[]
    }

    //-------------------------------index function Unit tests----------------------------//

    void "should retrieve user columns on index page"() {
        given:
        controller.productService = Stub(ProductService) {
            getColumns() >> List.of("TestColumn1", "TestColumn2")
        }

        when: 'index action is executed'
        def controllerResponse = controller.index()

        then: 'index action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.userColumns != null
        controllerResponse.userColumns.size() == 2
        controllerResponse.userColumns.containsAll(["TestColumn1", "TestColumn2"])
    }

    //-------------------------------show function Unit tests----------------------------//

    void "should redirect to index page if product for the id not found on show function"() {
        given:
        controller.productService = Stub(ProductService) {
            getProduct(_) >> null
        }

        when: 'show action is executed'
        def controllerResponse = controller.show(100)

        then: 'show action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == "Product not found"
    }

    void "should return add product view with correct values for none head_office or engineer roles"() {
        given:
        Product testProduct = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        testProduct.setId(100)
        controller.productService = Stub(ProductService) {
            getProduct(_) >> testProduct
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        Retailer testRetailer = new Retailer(snappyShopperEnabled: false, twoStageSel: false)
        testRetailer.setId(9)
        testRetailer.save(flush: true, failOnError: true)

        session.effectiveDate = DateTime.now()

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        when: 'show action is executed'
        controller.show(100)

        then: 'show action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        model.product != null
        model.product.id == 100
        model.storeId == 100
        model.statusValues != null
        model.categoryValues != null
        model.productCategoryList != null
        model.vatValues != null
        model.vatValues.find({ it.id == 100 }) != null
        model.ranges != null
        model.ranges.isEmpty()
        model.priceBands != null
        model.priceBands.isEmpty()
        model.effectiveDateIndex != null
        model.now != null
        model.navlink == "details"
        model.snappyEnabled == testRetailer.isSnappyShopperEnabled()
    }

    void "should return add product view with correct values for head_office or engineer roles"() {
        given:
        Product testProduct = new Product(itemCode: "100", description: "Test", receiptDescription: "Test", retailerId: 9,
                sku: 100, unitSize: "10", vatCode: new VatCode(), status: ProductStatus.ACTIVE, category: new Category(),
                restrictions: new Restrictions())
        testProduct.setId(100)
        controller.productService = Stub(ProductService) {
            getProduct(_) >> testProduct
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = role
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        Retailer testRetailer = new Retailer(snappyShopperEnabled: false, twoStageSel: false)
        testRetailer.setId(9)
        testRetailer.save(flush: true, failOnError: true)

        session.effectiveDate = DateTime.now()

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        when: 'show action is executed'
        controller.show(100)

        then: 'show action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        model.product != null
        model.product.id == 100
        model.storeId == 100
        model.statusValues != null
        model.categoryValues != null
        model.productCategoryList != null
        model.vatValues != null
        model.vatValues.find({ it.id == 100 }) != null
        model.ranges != null
        !model.ranges.isEmpty()
        model.priceBands != null
        !model.priceBands.isEmpty()
        model.effectiveDateIndex != null
        model.now != null
        model.navlink == "details"
        model.snappyEnabled == testRetailer.isSnappyShopperEnabled()

        where:
        ID | role
        1  | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        2  | new SimpleGrantedAuthority("ROLE_ENGINEER")
    }

    //-------------------------------add function Unit tests----------------------------//

    void "should set effective date to session and retrieve product add view for non head_office or engineer role"() {
        given:
        params.effectiveDate = effectiveDate

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        when: 'setEffectiveDate action is executed'
        controller.add()

        then: 'setEffectiveDate action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        session.effectiveDate != null
        model.storeId == 100
        model.statusValues != null
        model.categoryValues != null
        model.vatValues != null
        model.ranges != null
        model.ranges.isEmpty()
        model.priceBands != null
        model.priceBands.isEmpty()
        model.isNewProduct

        where:
        ID | effectiveDate
        1  | "Current"
        2  | "12 December 2022"
        3  | null
    }

    void "should set effective date to session and retrieve product add view for head_office or engineer role"() {
        given:
        params.effectiveDate = effectiveDate

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = role
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        when: 'setEffectiveDate action is executed'
        controller.add()

        then: 'setEffectiveDate action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        session.effectiveDate != null
        model.storeId == 100
        model.statusValues != null
        model.categoryValues != null
        model.vatValues != null
        model.ranges != null
        !model.ranges.isEmpty()
        model.priceBands != null
        !model.priceBands.isEmpty()
        model.isNewProduct

        where:
        ID | effectiveDate      | role
        1  | "Current"          | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        1  | "Current"          | new SimpleGrantedAuthority("ROLE_ENGINEER")
        2  | "12 December 2022" | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        2  | "12 December 2022" | new SimpleGrantedAuthority("ROLE_ENGINEER")
        3  | null               | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
        3  | null               | new SimpleGrantedAuthority("ROLE_ENGINEER")
    }

    //-------------------------------search function Unit tests----------------------------//

    void "should retrieve product search results"() {
        given:
        params.searchTerm = "Test"
        controller.productService = Stub(ProductService) {
            searchProductsHql(_, _, _, _, _, _) >> [products: new ArrayList<>(), totalCount: 0]
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'search action is executed'
        controller.search()

        then: 'search action response is correct'
        response.status == HttpStatus.OK.value()
        model.products != null
        model.totalResults == 0
        model.storeId == 100
        session.PRODUCT_SEARCH_TERM == "Test"
        session.effectiveDate != null
    }

    //-------------------------------ajaxSearchProducts function Unit tests----------------------------//

    void "should retrieve ajax product search results"() {
        given:
        params.searchTerm = "Test"
        params.searchBy = "Test"
        params.max = max
        params.offset = offset
        controller.productService = Stub(ProductService) {
            searchProductsHql(_, _, _, _, _, _) >> [products: new ArrayList<>(), totalCount: 0]
            getColumns() >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSearchProducts action is executed'
        controller.ajaxSearchProducts()

        then: 'ajaxSearchProducts action response is correct'
        response.status == HttpStatus.OK.value()
        model.products != null
        model.totalResults == 0
        model.storeId == 100
        model.userColumns != null
        model.searchTerm == "Test"
        model.searchBy == "Test"
        model.max == params.max ?: 50
        model.offset == params.offset
        session.PRODUCT_SEARCH_TERM == "Test"
        session.effectiveDate != null

        where:
        ID | max   | offset
        1  | "10"  | "100"
        2  | "100" | "200"
        3  | null  | "0"
        4  | null  | null
    }

    //-------------------------------prices function Unit tests----------------------------//

    void "should retrieve price bands and categories"() {
        given:
        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        controller.tagService = Stub(TagService) {
            getTags() >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'prices action is executed'
        def controllerResponse = controller.prices()

        then: 'prices action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.categories != null
        controllerResponse.tags != null
        controllerResponse.priceBands != null
    }

    //-------------------------------pricesSearch function Unit tests----------------------------//

    void "should retrieve price search results"() {
        given:
        params.searchTerm = searchTerm
        params.category = categoryId
        params.tag = tagId

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        controller.productService = Stub(ProductService) {
            searchProductPrices(_, _, _) >> []
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'pricesSearch action is executed'
        controller.pricesSearch()

        then: 'pricesSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.productPrices != null
        model.priceBands != null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }

    //-------------------------------ranges function Unit tests----------------------------//

    void "should retrieve ranges, tags and categories"() {
        given:
        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        controller.tagService = Stub(TagService) {
            getTags() >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ranges action is executed'
        def controllerResponse = controller.ranges()

        then: 'ranges action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.categories != null
        controllerResponse.tags != null
        controllerResponse.ranges != null
    }

    //-------------------------------rangesSearch function Unit tests----------------------------//

    void "should retrieve range search results"() {
        given:
        params.searchTerm = searchTerm
        params.category = categoryId
        params.tag = tagId

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        controller.productService = Stub(ProductService) {
            searchRangeProducts(_, _, _) >> []
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'rangesSearch action is executed'
        controller.rangesSearch()

        then: 'rangesSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.rangeProducts != null
        model.ranges != null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }

    //-------------------------------supplierUpdates function Unit tests----------------------------//

    void "should retrieve supplier updates"() {
        given:
        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        when: 'supplierUpdates action is executed'
        def controllerResponse = controller.supplierUpdates()

        then: 'supplierUpdates action response is correct'
        response.status == HttpStatus.OK.value()
        controllerResponse.categories != null
        controllerResponse.suppliers != null
        controllerResponse.priceBands != null
    }

    //-------------------------------supplierUpdatesSearch function Unit tests----------------------------//

    void "should retrieve supplier update search results"() {
        given:
        params.supplierId = supplierId
        params.categoryId = categoryId
        params.priceBandId = priceBandId
        params.sinceDate = sinceDate
        params.offset = offset
        params.max = max

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        controller.supplierService = Stub(SupplierService) {
            getSupplierPriceUpdates(_, _, _, _, _, _) >> [results: [], totalCount: 0]
        }

        when: 'supplierUpdatesSearch action is executed'
        controller.supplierUpdatesSearch()

        then: 'supplierUpdatesSearch action response is correct'
        response.status == HttpStatus.OK.value()
        model.supplierPriceUpdates != null
        model.totalCount == 0

        where:
        ID | supplierId | categoryId | priceBandId | sinceDate    | offset | max
        1  | "1"        | "1"        | "1"         | "22/12/2022" | "0"    | "100"
        2  | "100"      | "100"      | "100"       | "11/10/2020" | "10"   | "2000"
        3  | null       | null       | null        | null         | null   | null
    }

    //-------------------------------ajaxSaveSupplierPriceUpdates function Unit tests----------------------------//

    void "should return http 400 status if price band not found when saving supplier price updates"() {
        given:
        params.supplierId = supplierId
        params.categoryId = categoryId
        params.priceBandId = priceBandId
        params.sinceDate = sinceDate
        params.offset = offset
        params.acceptRrps = acceptRrps

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        controller.supplierService = Stub(SupplierService) {
            getSupplierPriceUpdates(_, _, _, _, _, _) >> [results: [], totalCount: 0]
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSupplierPriceUpdates action is executed'
        controller.ajaxSaveSupplierPriceUpdates()

        then: 'ajaxSaveSupplierPriceUpdates action response is correct'
        response.status == HttpStatus.BAD_REQUEST.value()

        where:
        ID | supplierId | categoryId | priceBandId | sinceDate    | offset | acceptRrps
        1  | "1"        | "1"        | "1"         | "22/12/2022" | "0"    | "true"
        2  | "100"      | "100"      | "100"       | "11/10/2020" | "10"   | "false"
        3  | null       | null       | null        | null         | null   | null
    }

    void "should save supplier price updates"() {
        given:
        params.supplierId = supplierId
        params.categoryId = categoryId
        params.priceBandId = priceBandId
        params.sinceDate = sinceDate
        params.offset = offset
        params.acceptRrps = acceptRrps

        def updates = [[sku: 100, recommendedRetailPrice: 50],
                       [sku: 105, recommendedRetailPrice: null],
                       new PriceChangeCommand(sku: 110, price: BigDecimal.TEN, oldPrice: null),
                       new PriceChangeCommand(sku: 120, price: BigDecimal.TEN, oldPrice: BigDecimal.ZERO),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.TEN, oldPrice: BigDecimal.TEN),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.ZERO, oldPrice: BigDecimal.TEN),]

        params.priceChanges = priceChanges ? updates : []

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        def supplierUpdateResult = supplierUpdate ? [results: updates, totalCount: updates.size()] : [totalCount: 0]

        controller.supplierService = Stub(SupplierService) {
            getSupplierPriceUpdates(_, _, _, _, _, _) >> supplierUpdateResult
        }
        controller.productService = Stub(ProductService) {}

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveSupplierPriceUpdates action is executed'
        controller.ajaxSaveSupplierPriceUpdates()

        then: 'ajaxSaveSupplierPriceUpdates action response is correct'
        response.status == HttpStatus.NO_CONTENT.value()

        where:
        ID | supplierId | categoryId | priceBandId | sinceDate    | offset | acceptRrps | priceChanges | supplierUpdate
        1  | "1"        | "1"        | "100"       | "22/12/2022" | "0"    | "true"     | true         | true
        2  | "100"      | "100"      | "100"       | "11/10/2020" | "10"   | "false"    | true         | true
        3  | null       | null       | "100"       | null         | null   | null       | false        | false
        3  | null       | null       | "100"       | null         | null   | "true"     | false        | true
        3  | null       | null       | "100"       | null         | null   | "true"     | false        | false
    }

    //-------------------------------ajaxSavePriceChanges function Unit tests----------------------------//

    void "should save price changes"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.supplierService = Stub(SupplierService) {}
        controller.productService = Stub(ProductService) {}


        def updates = [new PriceChangeCommand(sku: 110, price: BigDecimal.TEN, oldPrice: null, priceBandId: 100),
                       new PriceChangeCommand(sku: 120, price: BigDecimal.TEN, oldPrice: BigDecimal.ZERO, priceBandId: 100),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.TEN, oldPrice: BigDecimal.TEN, priceBandId: 200),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.ZERO, oldPrice: BigDecimal.TEN, priceBandId: 200)]

        SavePriceChangesCommand command = sendCommand ? new SavePriceChangesCommand(priceChanges: sendUpdates ? updates : null) : null

        PriceBand testPriceBand1 = new PriceBand(retailerId: 9, description: "Test 1")
        testPriceBand1.setId(100)
        testPriceBand1.save(flush: true, failOnError: true)

        PriceBand testPriceBand2 = new PriceBand(retailerId: 9, description: "Test 2")
        testPriceBand2.setId(200)
        testPriceBand2.save(flush: true, failOnError: true)

        when: 'ajaxSavePriceChanges action is executed'
        controller.ajaxSavePriceChanges(command)

        then: 'ajaxSavePriceChanges action response is correct'
        response.status == HttpStatus.OK.value()
        response.text == "OK"

        where:
        ID | sendCommand | sendUpdates
        1  | true        | true
        2  | true        | false
        3  | false       | false
    }

    //-------------------------------ajaxSaveRangeProducts function Unit tests----------------------------//

    void "should save ranged products"() {
        given:
        controller.springSecurityService = getFakeSpringSecurityService()
        controller.supplierService = Stub(SupplierService) {}
        controller.productService = Stub(ProductService) {
            getProduct(_) >> new Product()
        }


        def updates = [new RangeProductCommand(productId: 100, rangeId: 120, ranged: false),
                       new RangeProductCommand(productId: 100, rangeId: 120, ranged: false),
                       new RangeProductCommand(productId: 200, rangeId: 220, ranged: true),
                       new RangeProductCommand(productId: 200, rangeId: 220, ranged: true),]

        SaveRangeProductsCommand command = new SaveRangeProductsCommand(rangeProducts: sendUpdates ? updates : null)

        Range testRange1 = new Range(retailerId: 9, description: "Test")
        testRange1.setId(120)
        testRange1.save(flush: true, failOnError: true)

        Range testRange2 = new Range(retailerId: 9, description: "Test")
        testRange2.setId(220)
        testRange2.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct = new RangeProduct(productId: 100, range: testRange1)
        testRangeProduct.setId(120)
        testRangeProduct.save(flush: true, failOnError: true)

        when: 'ajaxSaveRangeProducts action is executed'
        controller.ajaxSaveRangeProducts(command)

        then: 'ajaxSaveRangeProducts action response is correct'
        response.status == HttpStatus.OK.value()
        response.text == "OK"

        where:
        ID | sendUpdates
        1  | true
        2  | false
    }

    //-------------------------------save function Unit tests----------------------------//

    void "should show to add product page if save product has errors for non head_office or engineer user"() {
        given:
        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        params.category = new Category(retailerId: 9, description: "TestCategory",
                shortDescription: "TestCat", retailerCategoryCode: "a_a_B", restrictions: new Restrictions())

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        when: 'save action is executed'
        controller.save(productCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        model.product != null
        model.statusValues != null
        model.categoryValues != null
        model.productCategoryList != null
        model.ranges != null
        model.ranges.isEmpty()
        model.selectedRanges == productCommand.rangeId
        model.priceBands != null
        model.priceBands.isEmpty()
        model.editedPrices != null
        model.vatValues != null
        model.storeId == 100

        where:
        ID | productCommand
        1  | new ProductCommand(restrictions: new RestrictionsCommand(), priceChanges: [new SavePriceChangesCommand()])
        2  | new ProductCommand(restrictions: new RestrictionsCommand(), priceChanges: null, category: new Category())
    }

    void "should show to add product page if save product has errors for head_office or engineer user"() {
        given:
        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = role
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", 100);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        when: 'save action is executed'
        controller.save(productCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.OK.value()
        view == "/product/add"
        model.product != null
        model.statusValues != null
        model.categoryValues != null
        model.productCategoryList != null
        model.ranges != null
        !model.ranges.isEmpty()
        model.selectedRanges == productCommand.rangeId
        model.priceBands != null
        !model.priceBands.isEmpty()
        model.editedPrices != null
        model.vatValues != null
        model.storeId == 100

        where:
        ID | productCommand                                                                                                             | role
        1  | new ProductCommand(restrictions: new RestrictionsCommand(), priceChanges: [new SavePriceChangesCommand(priceChanges: [])]) | new SimpleGrantedAuthority("ROLE_ENGINEER")
        2  | new ProductCommand(restrictions: new RestrictionsCommand(), priceChanges: null, category: new Category())                  | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE")
    }

    void "should redirect to index page on successful save new product by non head_office or engineer user"() {
        given:
        params.itemCode = String.valueOf(System.nanoTime())
        params.description = "TestProduct"
        params.receiptDescription = "TestProduct"
        params.unitSize = "EACH"
        params.vatCode = new VatCode(retailerId: 9, code: 'A' as char, percentage: BigDecimal.TEN)
        params.status = ProductStatus.ACTIVE
        params.category = new Category(retailerId: 9, description: "TestCategory",
                shortDescription: "TestCat", retailerCategoryCode: "a_a_B", restrictions: new Restrictions())
        params.variants = [new ProductVariant(sku: 140, barcodez: [new Barcode(barcode: "", sku: 140,
                effectiveDate: DateTime.now(), recordStatus: 'A' as char)], packs: [new Pack()])]

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeId", storeId);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        def productCommand = new ProductCommand(restrictions: new RestrictionsCommand(),
                priceChanges: [new SavePriceChangesCommand()])

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct1 = new RangeProduct(productId: 100, range: testRange)
        testRangeProduct1.setId(120)
        testRangeProduct1.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct2 = new RangeProduct(productId: 0, range: testRange)
        testRangeProduct2.setId(200)
        testRangeProduct2.save(flush: true, failOnError: true)

        controller.restrictionsService = Stub(RestrictionsService) {}
        controller.productService = Stub(ProductService) {
            isSingleStageSel() >> isSingleSel
        }

        when: 'save action is executed'
        controller.save(productCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == "Product saved successfully"

        where:
        ID | isSingleSel | effectiveDate | storeId
        1  | true        | "20/12/2022"  | 100
        2  | false       | null          | null
    }

    void "should redirect to index page on successful save new product by head_office or engineer user"() {
        given:
        params.itemCode = String.valueOf(System.nanoTime())
        params.description = "TestProduct"
        params.receiptDescription = "TestProduct"
        params.unitSize = "EACH"
        params.vatCode = new VatCode(retailerId: 9, code: 'A' as char, percentage: BigDecimal.TEN)
        params.status = ProductStatus.ACTIVE
        params.effectiveDate = effectiveDate
        params.category = new Category(retailerId: 9, description: "TestCategory",
                shortDescription: "TestCat", retailerCategoryCode: "a_a_B", restrictions: new Restrictions())

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        PriceBand testPriceBand2 = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand2.setId(200)

        def price1 = new ProductPrice(price: 100)
        price1.setId(priceBandId)
        price1.setPriceBand(testPriceBand)
        params.variants = [new ProductVariantHelper(sku: 110, barcodez: [new Barcode(barcode: "", sku: 110,
                effectiveDate: DateTime.now(), recordStatus: 'A' as char)], packs: [new Pack()],
                productPrice: [price1])]

        def updates = [new PriceChangeCommand(sku: 110, price: BigDecimal.TEN, oldPrice: null, priceBandId: 100),
                       new PriceChangeCommand(sku: 120, price: BigDecimal.TEN, oldPrice: BigDecimal.ZERO, priceBandId: 100),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.TEN, oldPrice: BigDecimal.TEN, priceBandId: 200),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.ZERO, oldPrice: BigDecimal.TEN, priceBandId: 200)]
        SavePriceChangesCommand savePriceChangeCommand = new SavePriceChangesCommand(priceChanges: updates)
        params.priceChanges = sendCommand ? [savePriceChangeCommand] : null

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = role
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> [retailerId: 9, storeId: storeId, storeNumber: 100, authorities: authorities]
        }

        def productCommand = new ProductCommand(restrictions: new RestrictionsCommand())
        productCommand.priceChanges = sendCommand ? [savePriceChangeCommand] : null
        productCommand.rangeId = rangeIds

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct1 = new RangeProduct(productId: 100, range: testRange)
        testRangeProduct1.setId(120)
        testRangeProduct1.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct2 = new RangeProduct(productId: 0, range: testRange)
        testRangeProduct2.setId(200)
        testRangeProduct2.save(flush: true, failOnError: true)

        controller.restrictionsService = Stub(RestrictionsService) {}
        controller.productService = Stub(ProductService) {
            isSingleStageSel() >> isSingleSel
        }

        when: 'save action is executed'
        controller.save(productCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == "Product saved successfully"

        where:
        ID | isSingleSel | effectiveDate | storeId | role                                           | sendCommand | priceBandId | bandPrice | rangeIds
        1  | true        | "20/12/2022"  | 100     | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") | true        | 100         | 100       | [120]
        2  | true        | "20/12/2022"  | null    | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") | false       | 200         | 0         | null
        3  | false       | null          | 100     | new SimpleGrantedAuthority("ROLE_ENGINEER")    | true        | 200         | 100       | [220]
        4  | false       | null          | null    | new SimpleGrantedAuthority("ROLE_ENGINEER")    | false       | 100         | 0         | []
    }

    void "should redirect to index page on successful save existing product by head_office or engineer user"() {
        given:
        params.id = "100"

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        PriceBand testPriceBand2 = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand2.setId(200)

        def price1 = new ProductPrice(price: 100)
        price1.setId(priceBandId)
        price1.setPriceBand(testPriceBand)
        def barcode1 = new Barcode(barcode: "", sku: 110,
                effectiveDate: DateTime.now(), recordStatus: 'A' as char)
        barcode1.setId(110)
        barcode1.setBarcode("")
        def variant1 = new ProductVariantHelper(sku: 110, barcodes: [barcode1], packs: [new Pack()],
                productPrice: [price1], costPrice: BigDecimal.TEN, retailPrice: BigDecimal.TEN, product: new Product(),
                effectiveDate: DateTime.now())
        variant1.barcodez.add(barcode1)
        variant1.setId(110)
        def barcode2 = new Barcode(barcode: "", sku: 120,
                effectiveDate: DateTime.now(), recordStatus: 'B' as char)
        barcode2.setId(120)
        barcode2.setBarcode("")
        def variant2 = new ProductVariantHelper(sku: 120, barcodes: [barcode2], packs: [new Pack()],
                productPrice: [price1], costPrice: BigDecimal.TEN, retailPrice: BigDecimal.TEN, product: new Product(),
                effectiveDate: DateTime.now())
        variant2.barcodez.add(barcode2)
        variant2.setId(120)

        def updates = [new PriceChangeCommand(sku: 110, price: BigDecimal.TEN, oldPrice: null, priceBandId: 100),
                       new PriceChangeCommand(sku: 120, price: BigDecimal.TEN, oldPrice: BigDecimal.ZERO, priceBandId: 100),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.TEN, oldPrice: BigDecimal.TEN, priceBandId: 200),
                       new PriceChangeCommand(sku: 130, price: BigDecimal.ZERO, oldPrice: BigDecimal.TEN, priceBandId: 200)]
        SavePriceChangesCommand savePriceChangeCommand = new SavePriceChangesCommand(priceChanges: updates)

        controller.categoryService = Stub(CategoryService) {
            getTopLevelCategories() >> new ArrayList()
        }
        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = role
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> [retailerId: 9, storeId: storeId, storeNumber: 100, authorities: authorities]
        }

        def productCommand = new ProductCommand(restrictions: new RestrictionsCommand())
        productCommand.priceChanges = sendCommand ? [savePriceChangeCommand] : null
        productCommand.rangeId = rangeIds
        productCommand.itemCode = String.valueOf(System.nanoTime())
        productCommand.description = "TestProduct"
        productCommand.receiptDescription = "TestProduct"
        productCommand.unitSize = "EACH"
        productCommand.vatCode = new VatCode(retailerId: 9, code: 'A' as char, percentage: BigDecimal.TEN)
        productCommand.status = ProductStatus.ACTIVE
        productCommand.category = new Category(retailerId: 9, description: "TestCategory",
                shortDescription: "TestCat", retailerCategoryCode: "a_a_B", restrictions: new Restrictions())
        productCommand.priceChanges = sendCommand ? [savePriceChangeCommand] : null

        Range testRange = new Range(retailerId: 9, description: "Test")
        testRange.setId(120)
        testRange.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct1 = new RangeProduct(productId: 100, range: testRange)
        testRangeProduct1.setId(120)
        testRangeProduct1.save(flush: true, failOnError: true)

        RangeProduct testRangeProduct2 = new RangeProduct(productId: 0, range: testRange)
        testRangeProduct2.setId(200)
        testRangeProduct2.save(flush: true, failOnError: true)

        controller.restrictionsService = Stub(RestrictionsService) {}
        controller.productService = Stub(ProductService) {
            isSingleStageSel() >> isSingleSel
            def product = new Product(restrictions: new Restrictions(minOpenPrice: 10, maxOpenPrice: 100))
            product.getVariants().push(variant1)
            product.getVariants().push(variant2)
            getProduct(_) >> product
        }

        variant1.springSecurityService = controller.springSecurityService
        variant2.springSecurityService = controller.springSecurityService

        ProductVariantCommand variantCmd1 = new ProductVariantCommand()
        copyProductVariant(variant1, variantCmd1)
        ProductVariantCommand variantCmd2 = new ProductVariantCommand()
        copyProductVariant(variant2, variantCmd2)
        variantCmd2.delete = true
        variantCmd2.barcodez[0].id = 300 // change ID
        productCommand.variants = [variantCmd1, variantCmd2]

        ProductVariant.metaClass.getBarcodes = { return [] }

        when: 'save action is executed'
        controller.save(productCommand)

        then: 'save action response is correct'
        response.status == HttpStatus.FOUND.value()
        flash.message == "Product saved successfully"

        where:
        ID | isSingleSel | effectiveDate                          | storeId | role                                           | sendCommand | priceBandId | bandPrice | rangeIds
        1  | true        | "20/12/2022"                           | null    | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") | true        | 100         | 100       | [120]
        2  | true        | "20/12/2022"                           | 100     | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") | false       | 200         | 0         | null
        3  | false       | null                                   | 100     | new SimpleGrantedAuthority("ROLE_ENGINEER")    | true        | 200         | 100       | [220]
        4  | false       | null                                   | null    | new SimpleGrantedAuthority("ROLE_ENGINEER")    | false       | 100         | 0         | []
        1  | true        | DateTime.now().toString(dateFormatter) | null    | new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") | true        | 100         | 100       | [120]

    }

    private copyProductVariant(ProductVariant variant, ProductVariantCommand variantCmd) {
        variantCmd.id = variant.id
        variantCmd.storeId = variant.storeId ? variant.storeId : 0
        variantCmd.defaultSupplierId = variant.defaultSupplierId
        variantCmd.sku = variant.sku
        variantCmd.retailPrice = variant.retailPrice
        variantCmd.costPrice = variant.costPrice
        variantCmd.size = variant.size
        variantCmd.colour = variant.colour
        variantCmd.minimumStockLevel = variant.minimumStockLevel
        variantCmd.effectiveDate = variant.effectiveDate
        variantCmd.delete = variant.delete
        variantCmd.shelfLifeDays = variant.shelfLifeDays
        variantCmd.shelfCapacity = variant.shelfCapacity
        variantCmd.minimumDisplayQuantity = variant.minimumDisplayQuantity
        def packs = []
        variant.packs?.each {
            PackCommand packCommand = new PackCommand()
            packCommand.id = it.id
            packCommand.supplier = it.supplier
            packCommand.quantity = it.quantity ? it.quantity : 0
            packCommand.price = it.price
            packCommand.orderCode = it.orderCode
            packCommand.barcode = it.barcode
            packCommand.recommendedRetailPrice = it.recommendedRetailPrice
            packCommand.effectiveDate = it.effectiveDate
            packCommand.effectiveEndDate = it.effectiveEndDate
            packCommand.status = it.status
            packCommand.maximumOrderQuantity = it.maximumOrderQuantity
            packCommand.allowSubstitutes = it.allowSubstitutes
            packs.add(packCommand)
        }
        variantCmd.packs.addAll(packs)

        def barcodes = []
        variant.barcodez?.each {
            BarcodeCommand barcodeCommand = new BarcodeCommand()
            barcodeCommand.id = it.id
            barcodeCommand.sku = it.sku
            barcodeCommand.retailerId = it.retailerId
            barcodeCommand.barcode = it.barcode
            barcodeCommand.effectiveDate = it.effectiveDate
            barcodeCommand.recordStatus = it.recordStatus
            barcodes.add(barcodeCommand)
        }
        variantCmd.barcodez.addAll(barcodes)
    }

    //-------------------------------ajaxGetChildCategories function Unit tests----------------------------//

    void "should retrieve child categories for a category"() {
        given:
        controller.categoryService = Stub(CategoryService) {
            getCategory(_) >> new Category(childCategories: [new Category()])
        }

        when: 'ajaxGetChildCategories action is executed'
        controller.ajaxGetChildCategories(0, 0, 0)

        then: 'ajaxGetChildCategories action response is correct'
        response.status == HttpStatus.OK.value()
        model.categories != null
        model.level == 0
        model.selectedCategoryId == 0
    }

    //-------------------------------ajaxAddVariant function Unit tests----------------------------//

    void "should add variant via ajax"() {
        given:
        def mockView = '<div>DUMMY HTML</div>'
        views['/product/_addVariant.gsp'] = mockView

        when: 'ajaxAddVariant action is executed'
        controller.ajaxAddVariant(new AddVariantCommand(zeroPrice: true, operationMode: 2))

        then: 'ajaxAddVariant action response is correct'
        response.status == HttpStatus.OK.value()
        model.zeroPrice
        model.isEditMode
    }

    //-------------------------------ajaxAddBarcode function Unit tests----------------------------//

    void "should add barcode via ajax"() {
        given:

        when: 'ajaxAddBarcode action is executed'
        controller.ajaxAddBarcode(0)

        then: 'ajaxAddBarcode action response is correct'
        response.status == HttpStatus.OK.value()
        model.index == 0
    }

    //-------------------------------ajaxSaveVariant function Unit tests----------------------------//

    void "should save variant via ajax"() {
        given:
        def mockView = '<div>DUMMY HTML</div>'
        views['/product/_variant.gsp'] = mockView

        when: 'ajaxSaveVariant action is executed'
        controller.ajaxSaveVariant(new AddVariantCommand(zeroPrice: true, operationMode: 2, index: 0, barcodez: []))

        then: 'ajaxSaveVariant action response is correct'
        response.status == HttpStatus.OK.value()
        model.index != null
        model.variant != null
        model.barcodes != null
        model.barcodes.isEmpty()
    }

    //-------------------------------ajaxAddPrice function Unit tests----------------------------//

    void "should add price via ajax"() {
        given:
        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "Test")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxAddPrice action is executed'
        controller.ajaxAddPrice(0, 0, true)

        then: 'ajaxAddPrice action response is correct'
        response.status == HttpStatus.OK.value()
        model.skuIndex == 0
        model.sku == 0
        model.variant == null
        model.priceBands != null
        model.zeroPrice
    }

    //-------------------------------ajaxSuppliers function Unit tests----------------------------//

    void "should add suppliers via ajax"() {
        given:
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        params.defaultSupplier = 0

        when: 'ajaxSuppliers action is executed'
        controller.ajaxSuppliers(new SuppliersCommand(index: 0))

        then: 'ajaxSuppliers action response is correct'
        response.status == HttpStatus.OK.value()
        model.variantIndex == 0
        model.defaultSupplier == 0
        model.suppliers != null
        model.statuses != null
        model.variant != null
    }

    //-------------------------------ajaxAddPack function Unit tests----------------------------//

    void "should add pack via ajax"() {
        given:
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(100)
        testSupplier.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxAddPack action is executed'
        controller.ajaxAddPack(0, 0, 0)

        then: 'ajaxAddPack action response is correct'
        response.status == HttpStatus.OK.value()
        model.variantIndex == 0
        model.productVariantId == 0
        model.packIndex == 0
        model.suppliers != null
        model.statuses != null
        model.isNewPack
    }

    //-------------------------------ajaxSavePack function Unit tests----------------------------//

    void "should return error if saved pack has validation errors"() {
        given:
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(310)
        testSupplier.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        params.defaultSupplier = 0

        when: 'ajaxSavePack action is executed'
        controller.ajaxSavePack(new SuppliersCommand(packs: [new AddPackCommand()], index: 0))

        then: 'ajaxSavePack action response is correct'
        response.status == HttpStatus.BAD_REQUEST.value()
        model.variantIndex == 0
        model.defaultSupplier == 0
        model.suppliers != null
        model.statuses != null
        model.variant != null
    }

    //-------------------------------ajaxSavePack function Unit tests----------------------------//

    void "should save pack correctly if inputs are valid"() {
        given:
        Supplier testSupplier = new Supplier(name: "Test Supplier", retailerId: 9, storeId: 100)
        testSupplier.setId(310)
        testSupplier.save(flush: true, failOnError: true)

        controller.springSecurityService = getFakeSpringSecurityService()
        params.defaultSupplier = 0

        when: 'ajaxSavePack action is executed'
        SupplierCommand supplierCommand = new SupplierCommand()
        supplierCommand.setId(100)
        def command = new SuppliersCommand(packs: [new AddPackCommand(price: 100, quantity: 100,
                recommendedRetailPrice: 100, maximumOrderQuantity: 1000, status: PackStatus.ACTIVE,
                supplier: supplierCommand)], index: 0)
        controller.ajaxSavePack(command)

        then: 'ajaxSavePack action response is correct'
        response.status == HttpStatus.OK.value()
        model.variantIndex == 0
        model.defaultSupplier == 0
        model.packs != null
    }

    //-------------------------------ajaxGetRestrictions function Unit tests----------------------------//

    void "should retrieve restrictions via ajax"() {
        given:
        controller.categoryService = Stub(CategoryService) {
            getCategory(_) >> category
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxGetRestrictions action is executed'
        controller.ajaxGetRestrictions(0, true)

        then: 'ajaxGetRestrictions action response is correct'
        response.status == HttpStatus.OK.value()
        model.productOpenPrice
        !model.isNewProduct
        view == "/product/_restrictions"

        where:
        ID | category
        1  | new Category(restrictions: new Restrictions())
        2  | null
    }

    //-------------------------------ajaxGetProductHistory function Unit tests----------------------------//

    void "should retrieve product history via ajax"() {
        given:
        controller.productHistoryService = Stub(ProductHistoryService) {
            getProductHistory(_, _) >> []
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        session.effectiveDate = effectiveDate

        when: 'ajaxGetProductHistory action is executed'
        controller.ajaxGetProductHistory(productId)

        then: 'ajaxGetProductHistory action response is correct'
        response.status == HttpStatus.OK.value()
        model.productHistoryMap != null
        view == "/product/_productHistory"

        where:
        ID | productId | effectiveDate
        1  | 0         | [DateTime.now(), DateTime.now()]
        2  | 1         | [DateTime.now(), DateTime.now()]
        3  | 1         | null
    }

    //-------------------------------ajaxGetProductHistory function Unit tests----------------------------//

    void "should retrieve http 200 OK if parameters null when ajaxGetProductHistory"() {
        given:
        params.reportColumns = reportColumns
        params.reportType = reportType

        when: 'ajaxSaveColumns action is executed'
        controller.ajaxSaveColumns()

        then: 'ajaxSaveColumns action response is correct'
        response.status == HttpStatus.OK.value()

        where:
        ID | reportColumns | reportType
        1  | "Test"        | null
        2  | null          | "Test"
        3  | null          | null
    }

    void "should retrieve http 500 INTERNAL_SERVER_ERROR on exception when ajaxGetProductHistory"() {
        given:
        params.reportColumns = "Test"
        params.reportType = "Test"

        when: 'ajaxSaveColumns action is executed'
        controller.ajaxSaveColumns()

        then: 'ajaxSaveColumns action response is correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
        response.text == "An error occurred saving your report column preferences."
    }

    void "should retrieve http 200 OK on successful save columns"() {
        given:
        params.reportColumns = reportColumns
        params.reportType = reportType

        controller.productService = Stub(ProductService) {
            getColumns() >> reportServiceResponse
        }

        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'ajaxSaveColumns action is executed'
        controller.ajaxSaveColumns()

        then: 'ajaxSaveColumns action response is correct'
        response.status == HttpStatus.OK.value()

        where:
        ID | reportColumns   | reportType              | reportServiceResponse
        1  | '{"Test":true}' | ReportType.ORDER.name() | new ReportColumns(columns: [new ReportColumn()])
        2  | '{"Test":true}' | ReportType.ORDER.name() | new ReportColumns(columns: [new ReportColumn(column: "Test")])
        3  | '{"Test":true}' | ReportType.ORDER.name() | null
    }

    //-------------------------------ajaxCSVProductUpload function Unit tests----------------------------//

    void "should return errors when save products via csv upload if invalid csv file"() {
        given:
        String csvData = "id\nsfd\n\n\n\n\n" // guaranteed invalid csv data
        InputStream fakeInputStream = new ByteArrayInputStream(csvData.getBytes())
        request.addFile(Stub(MultipartFile) {
            getName() >> 'file'
            getInputStream() >> fakeInputStream
        })

        when: 'ajaxCSVProductUpload action is executed'
        controller.ajaxCSVProductUpload()

        then: 'ajaxCSVProductUpload action response is correct'
        response.status == HttpStatus.OK.value()
        response.text.contains("FAILED")
        response.text.contains("Error while processing the CSV file")
    }

    void "should save csv upload products"() {
        given:
        String csvData = "id,effective_date,product_description,receipt_description,plu_item_code,retailer_category_code,default_sku,variant_id,unit_size,price_bands,def_cost_price,def_barcode,shelf_life_days,vat_code,vat_override,discreet_message,status,weighted_item,weighted_pricing_type,snappy_item,deli_item,open_price,zero_price\n" +
                "0,31/10/2022,Test WLPOS-935 110,Test Description,100100110,5,100100110,0,EACH,BAND A=10.0,5,,100,100,,,ACTIVE,NO,,NO,NO,,"
        InputStream fakeInputStream = new ByteArrayInputStream(csvData.getBytes())
        request.addFile(Stub(MultipartFile) {
            getName() >> 'file'
            getInputStream() >> fakeInputStream
        })

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        Category category = new Category(restrictions: new Restrictions(),
                description: "Test", shortDescription: "Test", retailerCategoryCode: "5")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "BAND A")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        controller.restrictionsService = Stub(RestrictionsService) {}
        controller.productService = Stub(ProductService) {}

        when: 'ajaxCSVProductUpload action is executed'
        controller.ajaxCSVProductUpload()

        then: 'ajaxCSVProductUpload action response is correct'
        response.status == HttpStatus.OK.value()
        response.text.contains("SUCCESS")
    }

    void "should return validation errors in response if csv upload data contains invalid parameters"() {
        given:
        // description not set for triggering validation error
        String csvData = "id,effective_date,product_description,receipt_description,plu_item_code,retailer_category_code,default_sku,variant_id,unit_size,price_bands,def_cost_price,def_barcode,shelf_life_days,vat_code,vat_override,discreet_message,status,weighted_item,weighted_pricing_type,snappy_item,deli_item,open_price,zero_price\n" +
                "0,31/10/2022,Test WLPOS-935 110,,100100110,5,100100110,0,EACH,BAND A=10.0,5,,100,100,,,ACTIVE,NO,,NO,NO,,"
        InputStream fakeInputStream = new ByteArrayInputStream(csvData.getBytes())
        request.addFile(Stub(MultipartFile) {
            getName() >> 'file'
            getInputStream() >> fakeInputStream
        })

        controller.springSecurityService = Stub(SpringSecurityService) {
            SimpleGrantedAuthority simpleGrantedAuthority1 = new SimpleGrantedAuthority("ROLE_USER")
            SimpleGrantedAuthority simpleGrantedAuthority2 = new SimpleGrantedAuthority("ROLE_GUEST")
            Collection authorities = Arrays.asList(simpleGrantedAuthority1, simpleGrantedAuthority2)
            getPrincipal() >> new HashMap() {
                {
                    put("retailerId", 9);
                    put("storeNumber", 100)
                    put("authorities", authorities)

                }
            }
        }

        Category category = new Category(restrictions: new Restrictions(),
                description: "Test", shortDescription: "Test", retailerCategoryCode: "5")
        category.setId(100)
        category.save(flush: true, failOnError: true)

        VatCode testVatCode = new VatCode(code: 'A' as char, retailerId: 9, percentage: BigDecimal.TEN,
                description: "Test", retailerVatCode: "A")
        testVatCode.setId(100)
        testVatCode.save(flush: true, failOnError: true)

        PriceBand testPriceBand = new PriceBand(retailerId: 9, description: "BAND A")
        testPriceBand.setId(100)
        testPriceBand.save(flush: true, failOnError: true)

        controller.restrictionsService = Stub(RestrictionsService) {}
        controller.productService = Stub(ProductService) {}

        when: 'ajaxCSVProductUpload action is executed'
        controller.ajaxCSVProductUpload()

        then: 'ajaxCSVProductUpload action response is correct'
        response.status == HttpStatus.OK.value()
        response.text.contains("FAILED")
        response.text.contains("Validation errors for product code")
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
