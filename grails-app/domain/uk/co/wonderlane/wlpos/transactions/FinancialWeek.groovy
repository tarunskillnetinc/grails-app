package uk.co.wonderlane.wlpos.transactions

import grails.gorm.annotation.Entity

import javax.persistence.Id
import java.sql.Date

@Entity
class FinancialWeek {

    @Id
    int id
    int retailerId
    Date startDate
    String financialYear
    Integer weekNumber

    static constraints = {
    }

    static mapping = {
        datasources(["transactions"])

        table "financialweek"
        version false

        id column: "id", sqlType: "tinyint"
        retailerId column: "retailerId", sqlType: "tinyint"
        startDate column: "startDate", sqlType: "DateTime"
        financialYear column: "`financialYear`", sqlType: "char"
        weekNumber column: "weekNumber", sqlType: "int"

    }
}
