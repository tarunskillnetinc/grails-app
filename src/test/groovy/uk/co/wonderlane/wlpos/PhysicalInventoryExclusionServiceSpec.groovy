package uk.co.wonderlane.wlpos

import grails.testing.services.ServiceUnitTest
import grails.testing.gorm.DataTest
import org.joda.time.DateTime
import spock.lang.Specification

class PhysicalInventoryExclusionServiceSpec extends Specification implements ServiceUnitTest<PhysicalInventoryExclusionService>, DataTest {

    def setup() {
        mockDomain(ProductVariant)
    }

    def cleanup() {
    }

    void "Test processExclusionList handles invalid SKUs"() {
        given:
        def invalidSku = "999999"
        service.productService = Stub(ProductService) {
            getProductVariant(invalidSku.toLong()) >> null
        }

        when:
        def results = service.processExclusionList([invalidSku])

        then:
        results.invalidSkus.size() == 1
        results.invalidSkus.contains(invalidSku)
    }

    void "Test processExclusionList handles non-numeric SKUs"() {
        given:
        def nonNumericSku = "ABC123"
        service.productService = Stub(ProductService) {
            getProductVariant(_) >> { throw new NumberFormatException() }
        }

        when:
        def results = service.processExclusionList([nonNumericSku])

        then:
        results.invalidSkus.size() == 1
        results.invalidSkus.contains(nonNumericSku)
    }
}