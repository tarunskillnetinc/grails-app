package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.transactions.SafeSession

import java.sql.SQLException

@Transactional
class SafeSessionService extends MySqlDal {

    def springSecurityService

    protected SafeSessionService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    @Transactional('transactions')
    SafeSession findSafeSessionById(int id) {
        return SafeSession.findById(id)
    }

    @Transactional('transactions')
    List<SafeSession> getSafeSessions(Integer storeId, DateTime startDate, DateTime endDate, String sortColumn, String sortOrder) {
        def safeSssions = SafeSession.createCriteria()

        def sessions = safeSssions.list() {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeId)

            eq("sessionStatus", "FINALISED")

            between("dateCreated", startDate, endDate)
            order(sortColumn ?: "safeId", sortOrder ?: "asc")
        }

        return sessions
    }
}