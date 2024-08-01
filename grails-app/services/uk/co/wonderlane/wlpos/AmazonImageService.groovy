package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.co.wonderlane.wlpos.entities.ImageRecord
import uk.co.wonderlane.wlpos.enums.ImageType

import java.nio.ByteBuffer

@Transactional
class AmazonImageService implements IImageService {

    def imageRecordService
    def s3Client

    private final String customerDisplayImagesBucket
    private final String receiptImagesBucket
    private final String buttonImagesBucket

    AmazonImageService(S3Client s3Client, String customerDisplayImagesBucket, String receiptImagesBucket, String buttonImagesBucket) {
        this.customerDisplayImagesBucket = customerDisplayImagesBucket
        this.receiptImagesBucket = receiptImagesBucket
        this.buttonImagesBucket = buttonImagesBucket
    }

    @Override
    def getButtonImage(int buttonId) throws Exception {
        ImageRecord imageRecord = imageRecordService.getImageRecordByImageId(ImageType.BUTTON, buttonId)
        if (imageRecord != null) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(buttonImagesBucket).key(imageRecord.getStorageKey()).build()

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray()
        }
        return new byte[]{}
    }

    @Override
    def saveButtonImage(int buttonId, byte[] imageBytes) throws Exception {
        ImageRecord imageRecord = imageRecordService.getImageRecordByImageId(ImageType.BUTTON, buttonId)
        if (imageRecord != null) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(buttonImagesBucket).key(imageRecord.getStorageKey()).build()

            s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(imageBytes)))
        }
    }

    @Override
    def deleteButtonImage(int buttonId) throws Exception {
        ImageRecord imageRecord = imageRecordService.getImageRecordByImageId(ImageType.BUTTON, buttonId)
        if (imageRecord != null) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(buttonImagesBucket).key(imageRecord.getStorageKey()).build()

            s3Client.deleteObject(deleteObjectRequest)
        }
    }

    @Override
    def getCustomerDisplayImages() throws Exception {
        return null
    }

    @Override
    def getReceiptImage() throws Exception {
        return null
    }
}