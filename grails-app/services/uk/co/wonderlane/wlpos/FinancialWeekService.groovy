package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.labelling.LabelTemplate
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

    @Transactional('reporting')
    boolean saveFinancialWeek(DateTime startDate, String financialYear, String weekNumber, Integer retailerId) {

        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        FinancialWeek existingFinancialWeek = FinancialWeek.findByStartDate(startDate)
        if (existingFinancialWeek) {
            return false
        }
        def financialWeek = new FinancialWeek(retailerId: retailerId, startDate: startDate, financialYear: financialYear, weekNumber: weekNumber)
        if (financialWeek.save(flush: true)) {
            println "Person saved successfully."
            return true
        } else {
            println "Failed to save person."
            return false
        }
        transaction.commit()
        session.close()
    }

    @Transactional('reporting')
    List<FinancialWeek> getAllFinancialWeeks() {
        def financialWeeks = FinancialWeek.list()
        return financialWeeks.unique { it.financialYear }
    }

    @Transactional('reporting')
    List<FinancialWeek> getAllFinancialWeeksByFinancialYear(String financialYear) {
        def criteria = FinancialWeek.createCriteria()

        return criteria.list {
            eq("financialYear", financialYear)
        }
    }
}
