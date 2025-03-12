package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.springframework.security.core.context.SecurityContextHolder

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
 * @version 1.0
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
                def productVariant = productService.getProductVariant(sku.toLong())
                // Check if the product variant belongs to the current user's retailer
                if (productVariant) {
                    log.println("true")
                    productVariant.excludeFromInventoryCount = true
                    productVariant.save(flush: true)
                } else {
                    results.invalidSkus << sku
                }
            } catch (NumberFormatException e) {
                log.debug("Invalid SKU format: ${sku} - ${e.message}")
                results.invalidSkus << sku
            }
        }
        return results
    }
}