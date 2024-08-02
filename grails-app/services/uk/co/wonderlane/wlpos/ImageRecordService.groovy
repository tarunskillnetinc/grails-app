package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ImageType
/**
 * This service is responsible for fetching all entities related to ImageRecord
 * from the WLPOS database. It retrieves these entities directly by utilizing
 * existing stored procedures or custom queries, depending on the specific
 * requirements. The service ensures that all data handling operations related
 * to ImageRecords are performed efficiently and securely.
 */
@Transactional
class ImageRecordService {

    def springSecurityService
    def sessionFactory

    def getImageRecordByImageId(ImageType imageType, int imageId) {
        return ImageRecord.findByRetailerIdAndTypeAndImageId(springSecurityService.principal.retailerId,
                imageType.name(), imageId)
    }

    def saveImageRecord(ImageRecord imageRecord) {
        try {
            return imageRecord.save()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def deleteImageRecord(ImageRecord imageRecord) {
        imageRecord.delete()
    }
}
