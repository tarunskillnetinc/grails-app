package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception

import java.nio.ByteBuffer

@Transactional
class AmazonImageService implements IImageService {

    def s3Client
    def config

    @Override
    def getImage(ImageRecord imageRecord) throws Exception {
        if (imageRecord != null) {
            String bucketName = generateBucketName(imageRecord)
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(imageRecord.getStorageKey()).build()

                return s3Client.getObjectAsBytes(getObjectRequest).asByteArray()
            } catch (NoSuchBucketException ne) {
                ne.printStackTrace()
            } catch (NoSuchKeyException ke) {
                ke.printStackTrace()
            } catch (S3Exception ignored) {
                return null
            }
        }
        return new byte[]{}

    }

    @Override
    def saveImage(ImageRecord imageRecord, byte[] imageBytes) throws Exception {
        if (imageRecord != null) {
            String bucketName = generateBucketName(imageRecord)
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(imageRecord.getStorageKey()).build()

                s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(imageBytes)))
            } catch (NoSuchBucketException ne) {
                ne.printStackTrace()
            } catch (NoSuchKeyException ke) {
                ke.printStackTrace()
            }
        }
    }

    @Override
    def deleteImage(ImageRecord imageRecord) throws Exception {
        if (imageRecord != null) {
            String bucketName = generateBucketName(imageRecord)
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(imageRecord.getStorageKey()).build()

                s3Client.deleteObject(deleteObjectRequest)
            } catch (NoSuchBucketException ne) {
                ne.printStackTrace()
            } catch (NoSuchKeyException ke) {
                ke.printStackTrace()
            }
        }
    }

    def generateBucketName(ImageRecord imageRecord) {
        String bucketName = config.getProperty("wlpos.${imageRecord.getType().toLowerCase()}ImageBucket")
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = config.getProperty("wlpos.defaultImageBucket")
        }
        return bucketName;
    }
}