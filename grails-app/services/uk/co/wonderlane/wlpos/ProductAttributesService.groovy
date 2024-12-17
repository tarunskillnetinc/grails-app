package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

import javax.validation.constraints.NotNull
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.SQLException

@Transactional
class ProductAttributesService extends MySqlDal{
    def springSecurityService
    def messageSource
    def gsonProvider

    protected ProductAttributesService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def getProductAttributes(int max, int offset, String sort, String order, long retailerId) {
        def query = ProductAttributes.where {
            retailerId == retailerId
        }

        def totalCount = query.count()

        def results = query.list(max: max, offset: offset)

        if (sort) {
            results = results.sort { it[sort] }
            if (order?.equalsIgnoreCase('desc')) {
                results = results.reverse()
            }
        }

        return [list: results, count: totalCount]
    }

    def saveProductAttribute(@NotNull ProductAttributes productAttribute) {
        def result = [:]

        try {
            productAttribute.retailerId = springSecurityService.principal.retailerId
            if (!productAttribute.validate()) {
                result.success = false
                def errorMessages = productAttribute.errors.fieldErrors.collectEntries { error ->
                    [(error.field): messageSource.getMessage(error.code, error.arguments, Locale.default)]
                }
                result.errorMessages = errorMessages
                return result
            }
            productAttribute.save(flush: true)
            result.success = true
        } catch (Exception e) {
            log.error "Error saving product attribute: ${e.message}", e
            result.success = false
            result.errorMessages = [general: "An unexpected error occurred while saving the product attribute"]
        }

        return result
    }

    def updateListValues(int id, List<String> newValues) {
        def result = [success: false]

        try (Connection conn = getConnection();
             CallableStatement updateListValuesStatement = conn.prepareCall("{ call updateProductAttributeListValues(?, ?) }")) {

            updateListValuesStatement.setInt(1, id)
            updateListValuesStatement.setString(2, gsonProvider.gson.toJson(newValues))

            updateListValuesStatement.executeUpdate()

            result.success = true
        } catch (SQLException ex) {
            log.error("SQL error updating listValues for product attribute id: ${id}. Error: ${ex.getMessage()}", ex)
            result.errorMessages = [general: messageSource.getMessage("productAttribute.update.error.sql", [id] as Object[], Locale.default)]
            throw new RuntimeException("SQL error updating listValues for product attribute id: ${id}. Error: ${ex.getMessage()}", ex)
        } catch (Exception ex) {
            log.error("Unexpected error updating listValues for product attribute id: ${id}. Error: ${ex.getMessage()}", ex)
            result.errorMessages = [general: messageSource.getMessage("productAttribute.update.error.unexpected", [id] as Object[], Locale.default)]
            throw new RuntimeException("Unexpected error updating listValues for product attribute id: ${id}. Error: ${ex.getMessage()}", ex)
        }

        return result
    }

    List<String> getListValues(int id) {
        def productAttribute = ProductAttributes.get(id)
        if (!productAttribute) {
            log.warn "No product attribute found with id: $id"
            return []
        }

        try {
            String jsonString = productAttribute.listValues

            if (jsonString) {
                return gsonProvider.gson.fromJson(jsonString, new TypeToken<List<String>>(){}.getType())
            } else {
                return []
            }
        } catch (Exception e) {
            log.error "Error retrieving listValues: ${e.message}", e
            return []
        }
    }
}
