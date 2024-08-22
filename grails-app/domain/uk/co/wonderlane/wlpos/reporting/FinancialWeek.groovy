package uk.co.wonderlane.wlpos.reporting

import grails.gorm.annotation.Entity
import uk.co.wonderlane.wlpos.PriceBand

import javax.persistence.Id

@Entity
class FinancialWeek {

    @Id
    int id
    int retailerId
    String startDate
    String financialYear
    Integer weekNumber

    static constraints = {
    }

    static mapping = {
        datasources(["reporting"])

        table "financialweek"
        version false

        id column: "id", sqlType: "tinyint"
        retailerId column: "retailerId", sqlType: "tinyint"
        startDate column: "startDate", sqlType: "DateTime"
        financialYear column: "`financialYear`", sqlType: "char"
        weekNumber column: "weekNumber", sqlType: "int"

    }

    public FinancialWeek getFinancialWeek(int retailerId, char startDate, char financialYear, int weekNumber) {
        FinancialWeek financialWeek = new FinancialWeek()

        financialWeek.setId(id)
        financialWeek.setRetailerId(retailerId)
        financialWeek.setStartDate(startDate)
        financialWeek.setFinancialYear(financialYear)
        financialWeek.setWeekNumber(weekNumber)

        return financialWeek
    }
}
