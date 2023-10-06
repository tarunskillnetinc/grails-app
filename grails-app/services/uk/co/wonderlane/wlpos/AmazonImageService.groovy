package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.nio.ByteBuffer

@Transactional
class AmazonImageService implements IImageService {

    def springSecurityService

    private final String customerDisplayImagesBucket
    private final String receiptImagesBucket
    private final String buttonImagesBucket

    private final S3Client s3Client

    AmazonImageService(String customerDisplayImagesBucket, String receiptImagesBucket, String buttonImagesBucket) {
        this.customerDisplayImagesBucket = customerDisplayImagesBucket
        this.receiptImagesBucket = receiptImagesBucket
        this.buttonImagesBucket = buttonImagesBucket

        s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
    }

    @Override
    def getButtonImage(int buttonId) throws Exception {
        String key = "${springSecurityService.principal.retailerId}/${buttonId}.png"

        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(buttonImagesBucket).key(key).build()

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray()
    }

    @Override
    def saveButtonImage(int buttonId, byte[] imageBytes) throws Exception {
        String key = "${springSecurityService.principal.retailerId}/${buttonId}.png"

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(buttonImagesBucket).key(key).build()

        s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(imageBytes)))
    }

    @Override
    def deleteButtonImage(int buttonId) throws Exception {
        String key = "${springSecurityService.principal.retailerId}/${buttonId}.png"

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(buttonImagesBucket).key(key).build()

        s3Client.deleteObject(deleteObjectRequest)
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