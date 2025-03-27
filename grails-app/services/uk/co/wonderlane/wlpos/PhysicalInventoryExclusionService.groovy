package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

/**
 * Service for handling physical inventory count exclusion logic.
 * This service processes lists of SKUs to determine which products should be excluded from physical inventory counts.
 *
 * The service provides the following functionality:
 * - Validates SKUs from a list
 * - Checks if products exist for the SKUs
 * - Marks valid products to be excluded from inventory counts
 * - Tracks invalid SKUs that couldn't be processed
 *
 * @author Tarun Singh
 * @version 2.0
 */
@Transactional
class PhysicalInventoryExclusionService {

    def productService

    def processExclusionList(List<String> skus) {

        def results = [
                invalidSkus: []
        ]
        skus.each { sku ->
            try {
                long numericSku = sku.toLong()
                List<ProductVariant> variantList = productService.getAllProductVariantsBySkuForCurrentRetailer(numericSku)
                if (!variantList.isEmpty()) {
                    variantList.each { productVariant ->
                        productVariant.excludeFromInventoryCount = true
                        productVariant.save(flush: true, failOnError: true)
                    }
                } else {
                    results.invalidSkus << sku
                }
            } catch (NumberFormatException e) {
                log.error("Invalid SKU format: {}", sku, e)
                results.invalidSkus << sku
            } catch (Exception ex) {
                log.error("Error processing SKU: {}", sku, ex)
                results.invalidSkus << sku
            }
        }
        return results
    }
}