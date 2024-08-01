package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.entities.ImageRecord
import uk.co.wonderlane.wlpos.enums.ImageType

import java.sql.SQLException

/**
 * This service is responsible for fetching all entities related to ImageRecord
 * from the WLPOS database. It retrieves these entities directly by utilizing
 * existing stored procedures or custom queries, depending on the specific
 * requirements. The service ensures that all data handling operations related
 * to ImageRecords are performed efficiently and securely.
 */
@Transactional
class ImageRecordService extends MySqlPoolDal {

    def springSecurityService
    def sessionFactory

    protected ImageRecordService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def getImageRecordByImageId(ImageType imageType, int imageId) {
        ImageRecordDb imageRecord =  ImageRecordDb.findByRetailerIdAndTypeAndImageId(springSecurityService.principal.retailerId,
                imageType.name(), imageId)
        if (imageRecord != null) {
            return new ImageRecord(
                    id: imageRecord.id,
                    retailerId: imageRecord.retailerId,
                    guid: imageRecord.guid,
                    type: imageRecord.type,
                    imageId: imageRecord.imageId,
                    name: imageRecord.name,
                    storageKey: imageRecord.storageKey,
                    creationTime: imageRecord.creationTime,
                    updatedTime: imageRecord.updatedTime
            )
        }
        return null;
    }
}
