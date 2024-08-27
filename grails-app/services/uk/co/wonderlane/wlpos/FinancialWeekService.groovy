package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import java.sql.SQLException
import java.time.LocalDateTime

@Transactional
class FinancialWeekService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    protected FinancialWeekService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }


    def saveFinancialWeek(LocalDateTime startDate, String financialYear, String weekNumber, Integer retailerId) {
        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        FinancialWeek financialWeek = new FinancialWeek(retailerId: retailerId, startDate: startDate, financialYear: financialYear, weekNumber: weekNumber)
        session.save(financialWeek);
            // Clear the session for speed purposes.
            if (index.mod(500) == 0) {
                session.flush()
                session.clear()
            }

        transaction.commit()
        session.close()
    }

    @Transactional
    List<FinancialWeek> getAllFinancialWeeks() {

        Transaction transaction = session.beginTransaction()
        return FinancialWeek.list()
    }
}
