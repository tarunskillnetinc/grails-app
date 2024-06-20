package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DuplicateKeyException
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

import javax.persistence.PersistenceException
import java.sql.SQLException

@Transactional
class BarcodeSignifierService extends MySqlDal {

    def springSecurityService
    def sessionFactory
    def messageSource

    protected BarcodeSignifierService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    /**
     * Retrieves a list of BarcodeSignifiers based on the specified filters and sorting criteria.
     *
     * @param retailerId  the ID of the retailer to filter BarcodeSignifiers by
     * @param type        the type of BarcodeSignifier to filter by (optional)
     * @param pattern     the pattern of BarcodeSignifier to filter by (optional)
     * @param description the description to search for in BarcodeSignifier's description or receipt description (optional)
     * @param sortBy      the column to sort the results by, which must be one of the allowed columns to prevent SQL injection (optional)
     * @return a list of BarcodeSignifiers that match the specified filters and sorting criteria
     */
    def getSignifiersByFilters(Integer retailerId, String type, String pattern, String description, String sortBy) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT bs FROM BarcodeSignifier bs LEFT JOIN bs.barcodeSignifierEmbeddedDatas be")
                .append(" WHERE bs.retailerId=:retailerId")

        def filters = [retailerId: retailerId]
        if (type != null) {
            queryBuilder.append(" AND bs.type=:type")
            filters << [type: type]
        }

        if (pattern != null) {
            queryBuilder.append(" AND bs.pattern=:pattern")
            filters << [pattern: pattern]
        }

        if (description != null) {
            queryBuilder.append(" AND (bs.description LIKE :description OR bs.receiptDescription LIKE :description)")
            filters << [description: "%${description}%"]
        }

        queryBuilder.append(" GROUP BY bs.id, bs.retailerId, bs.pattern, bs.startIndex, bs.length, bs.type")

        def allowedSortColumns = [
                "type asc",
                "type desc",
                "pattern asc",
                "pattern desc",
                "description asc",
                "description desc",
                "receiptDescription asc",
                "receiptDescription desc",
                "length asc",
                "length desc"
        ] //This array is used to preventing sql injection

        if (sortBy != null && allowedSortColumns.contains(sortBy)) {
            queryBuilder.append(" ORDER BY bs." + sortBy)
        } else {
            queryBuilder.append(" ORDER BY bs.pattern asc")
        }
        return BarcodeSignifier.findAll(queryBuilder.toString(), filters)
    }

    /**
     * Validates and saves the BarcodeSignifier object.
     *
     * This method performs the following actions:
     * 1. Validates the BarcodeSignifier object.
     * 2. If validation fails, collects field-specific error messages and returns them.
     * 3. If validation passes, saves the BarcodeSignifier object to the database.
     *
     * @param barcodeSignifier The BarcodeSignifier object to be validated and saved.
     * @return A map containing the following keys:
     *         - "success" (boolean): Indicates whether the operation was successful.
     *         - "errorMessages" (Map<String, String>): Field-specific error messages if validation fails.
     *         - "savedObject" (BarcodeSignifier): The saved BarcodeSignifier object if the operation was successful.
     */
    def saveSignifier(BarcodeSignifier barcodeSignifier) {
        Session session = sessionFactory.openSession()
        Transaction transaction = null
        def result = [:]

        try {
            transaction = session.beginTransaction()

            if (!barcodeSignifier.validate()) {
                def errorMessages = barcodeSignifier.errors.fieldErrors.collectEntries { error ->
                    [(error.field): messageSource.getMessage(error.code, error.arguments, Locale.default)]
                }
                result.errorMessages = errorMessages
                result.success = false
                return result
            }

            if (barcodeSignifier.id) {
                // Fetch existing entity if id is present
                BarcodeSignifier existingSignifier = session.get(BarcodeSignifier, barcodeSignifier.id)
                if (existingSignifier) {
                    // Copy properties from the incoming entity to the existing one
                    existingSignifier.properties = barcodeSignifier.properties
                    session.saveOrUpdate(existingSignifier)
                    barcodeSignifier = existingSignifier
                } else {
                    // Handle case where the id does not match any existing entity
                    result.errorMessages = ["id": "Barcode Signifier with provided ID does not exist."]
                    result.success = false
                    return result
                }
            } else {
                session.saveOrUpdate(barcodeSignifier)
            }

            transaction.commit()

            result.success = true
            result.savedObject = barcodeSignifier
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback()
            handleException(e, result)
        } catch (Exception e) {
            if (transaction != null) transaction.rollback()
            handleException(e, result)
        } finally {
            session.clear()
            session.close()
        }

        return result
    }

    /**
     * Deletes a BarcodeSignifier and its associated embedded data based on the provided retailerId and signifierId.
     *
     * @param retailerId the ID of the retailer to which the BarcodeSignifier belongs
     * @param signifierId the ID of the BarcodeSignifier to be deleted
     * @return a result map indicating the success or failure of the deletion operation
     */
    def deleteSignifier(int retailerId, int signifierId) {
        Session session = sessionFactory.openSession()
        Transaction transaction = null
        def result = [:]

        try {
            transaction = session.beginTransaction()

            // Fetching the existing signifier by retailerId and signifierId
            String hql = "FROM BarcodeSignifier WHERE retailerId = :retailerId AND id = :signifierId"
            BarcodeSignifier existingSignifier = session.createQuery(hql, BarcodeSignifier.class)
                    .setParameter("retailerId", retailerId)
                    .setParameter("signifierId", signifierId)
                    .uniqueResult()

            if (existingSignifier != null) {
                // Deleting associated embedded data
                String deleteEmbeddedDataHql = "DELETE FROM BarcodeSignifierEmbeddedData WHERE barcodeSignifierId = :signifierId"
                session.createQuery(deleteEmbeddedDataHql)
                        .setParameter("signifierId", existingSignifier.id)
                        .executeUpdate()

                // Deleting the signifier
                session.delete(existingSignifier)
            }

            transaction.commit()
            result.deletedObject = existingSignifier
            result.success = true
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback()
            }
            result.success = false
        } finally {
            session.close()
        }

        return result
    }

    /**
     * Retrieves a BarcodeSignifier by its unique identifier.
     *
     * @param signifierId the unique identifier of the BarcodeSignifier
     * @return the BarcodeSignifier object corresponding to the provided identifier, or null if not found
     */
    def getBarcodeSignifierById(int signifierId) {
        return BarcodeSignifier.findById(signifierId)
    }

    /**
     * Saves or updates a BarcodeSignifierEmbeddedData entity in the database.
     * Validates the embedded data before persisting it. If the entity has an ID, it attempts to update an existing record.
     * If validation fails or an error occurs, it populates the result map with appropriate error messages.
     *
     * @param embeddedData the BarcodeSignifierEmbeddedData entity to be saved or updated
     * @return a result map indicating the success or failure of the operation, along with any error messages
     */
    def saveEmbeddedData(BarcodeSignifierEmbeddedData embeddedData) {
        Session session = sessionFactory.openSession()
        Transaction transaction = null
        def result = [:]

        try {
            transaction = session.beginTransaction()

            if (!embeddedData.validate()) {
                def errorMessages = embeddedData.errors.fieldErrors.collectEntries { error ->
                    [(error.field): messageSource.getMessage(error.code, error.arguments, Locale.default)]
                }
                result.errorMessages = errorMessages
                result.success = false
                return result
            }

            if (embeddedData.id) {
                // Fetch existing entity if id is present
                BarcodeSignifierEmbeddedData existingEmbeddedData = session.get(BarcodeSignifierEmbeddedData, embeddedData.id)
                if (existingEmbeddedData) {
                    // Copy properties from the incoming entity to the existing one
                    existingEmbeddedData.properties = embeddedData.properties
                    session.saveOrUpdate(existingEmbeddedData)
                } else {
                    // Handle case where the id does not match any existing entity
                    result.errorMessages = ["id": "Barcode Signifier with provided ID does not exist."]
                    result.success = false
                    return result
                }
            } else {
                session.saveOrUpdate(embeddedData)
            }

            transaction.commit()

            result.success = true
            result.savedObject = embeddedData
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback()
            handleException(e, result)
        } catch (Exception e) {
            if (transaction != null) transaction.rollback()
            handleException(e, result)
        } finally {
            session.clear()
            session.close()
        }

        return result
    }

    def getEmbeddedData(int signifierId) {
        BarcodeSignifier barcodeSignifier = new BarcodeSignifier()
        barcodeSignifier.setId(signifierId);
        return BarcodeSignifierEmbeddedData.findAllByBarcodeSignifier(barcodeSignifier)
    }

    def getEmbeddedDataFormats() {
        //Expecting this to be configurable through a db migration in future
        return new String[]{
            "jjjj"
        }
    }

    def getEmbeddedDataById(int embeddedDataId) {
        return BarcodeSignifierEmbeddedData.findById(embeddedDataId)
    }

    /**
     * Handles exceptions that occur during data persistence operations and populates the result map with appropriate error messages.
     *
     * @param e      the exception that was thrown during the operation
     * @param result the result map to be populated with success status and error messages
     */
    private void handleException(Exception e, def result) {
        def errorMessages = [:]
        String message = e instanceof ConstraintViolationException ? e.getSQLException().getMessage().toLowerCase() :
                e instanceof PersistenceException && e.getCause() instanceof ConstraintViolationException ?
                        ((ConstraintViolationException) e.getCause()).getSQLException().getMessage().toLowerCase() : ""

        if (message.contains("unique_retailer_pattern_length")) {
            errorMessages.general = "The combination of pattern and length cannot be duplicated."
        } else if (message.contains("unique_embeddeddata_type_barcodesignifierid")) {
            errorMessages.type = "The type cannot be duplicated for a Barcode Signifier."
        } else {
            errorMessages.general = "An unexpected error occurred while saving the data."
        }

        result.success = false
        result.errorMessages = errorMessages
    }

    def deleteEmbeddedData(int embeddedDataId) {
        def result = [:]
        try {
            BarcodeSignifierEmbeddedData embeddedData = BarcodeSignifierEmbeddedData.findById(embeddedDataId)
            if (embeddedData) {
                embeddedData.delete(flush: true)
                result.success = true
                result.deletedObject = embeddedData
            } else {
                result.success = false
                result.errorMessages = ["general": "Embedded data not found."]
            }
        } catch (Exception e) {
            handleException(e, result)
        }
        return result
    }

}
