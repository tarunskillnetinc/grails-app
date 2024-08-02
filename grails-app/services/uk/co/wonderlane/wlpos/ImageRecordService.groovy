package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
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
class ImageRecordService {

    def springSecurityService
    def sessionFactory

    /**
     * Retrieves an ImageRecord based on the given image type and image ID.
     * @param imageType The type of the image.
     * @param imageId The ID of the image.
     * @return The ImageRecord matching the provided image type and ID.
     */
    def getImageRecordByImageId(ImageType imageType, int imageId) throws SQLException {
        return ImageRecord.findByRetailerIdAndTypeAndImageId(springSecurityService.principal.retailerId,
                imageType.name(), imageId)
    }

    /**
     * Saves the provided ImageRecord to the database.
     * @param imageRecord The ImageRecord to be saved.
     * @return The saved ImageRecord.
     */
    def saveImageRecord(ImageRecord imageRecord) {
        try {
            return imageRecord.save()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes the provided ImageRecord from the database.
     * @param imageRecord The ImageRecord to be deleted.
     */
    def deleteImageRecord(ImageRecord imageRecord) throws SQLException {
        imageRecord.delete()
    }

    /**
     * Recover and fetch the image record. This method provides backward compatibility for systems that need to recover ImageRecords for old images.
     * @param imageType The type of the image.
     * @param imageId The ID of the image.
     * @return An instance of ImageRecord.
     */
    def getImageRecordOrRecover(ImageType imageType, int imageId) throws SQLException {
        ImageRecord imageRecord = getImageRecordByImageId(imageType, imageId)
        //Make this backward compatible by saving image record if not exist
        if (imageRecord == null) {
            imageRecord = new ImageRecord(
                    retailerId: springSecurityService.principal.retailerId,
                    type: imageType.name(),
                    imageId: imageId,
                    storageKey: "${springSecurityService.principal.retailerId}/${imageId}.png",
                    guid: UUID.randomUUID().toString(),
                    name: "",
                    creationTime: DateTime.now(),
                    updatedTime: DateTime.now()
            )
            saveImageRecord(imageRecord)
        }
        return imageRecord;
    }
}
