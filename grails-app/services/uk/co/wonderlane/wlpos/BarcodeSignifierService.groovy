package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DuplicateKeyException
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

import java.sql.SQLException

@Transactional
class BarcodeSignifierService extends MySqlDal {

    def springSecurityService
    def sessionFactory
    def messageSource

    protected BarcodeSignifierService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

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

            session.save(barcodeSignifier)
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

    private void handleException(Exception e, def result) {
        def errorMessages = [:]
        if (e instanceof ConstraintViolationException && e.getSQLException().getMessage().toLowerCase().contains("unique_retailer_pattern_length")) {
            errorMessages.general = "The combination of pattern and length cannot be duplicated."
        } else {
            errorMessages.general = "An unexpected error occurred while saving the data."
        }
        result.success = false
        result.errorMessages = errorMessages
    }
}
