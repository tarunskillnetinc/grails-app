package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

import java.sql.SQLException

@Transactional
class BarcodeSignifierService extends MySqlDal{

    def springSecurityService

    protected BarcodeSignifierService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def getSignifiersByFilters(Integer retailerId, String type, String pattern, String description, String sortBy) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT bs FROM BarcodeSignifier bs LEFT JOIN bs.barcodeSignifierEmbeddedDatas be")
                .append(" WHERE bs.retailerId=:retailerId")

        def filters = [retailerId:retailerId]
        if (type != null) {
            queryBuilder.append(" AND bs.type=:type")
            filters << [type:type]
        }

        if (pattern != null) {
            queryBuilder.append(" AND bs.pattern=:pattern")
            filters << [pattern:pattern]
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
            queryBuilder.append(" ORDER BY bs.type asc , bs.pattern asc")
        }
        return BarcodeSignifier.findAll(queryBuilder.toString(), filters)
    }
}
