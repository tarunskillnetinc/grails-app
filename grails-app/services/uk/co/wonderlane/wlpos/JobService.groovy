package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class JobService extends MySqlDal {

    def springSecurityService
    def storeService
    def productService

    JobService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def saveJob(Job job) {
        job.save(flush: true,failOnError: true)
    }
}
