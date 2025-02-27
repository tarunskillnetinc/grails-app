package uk.co.wonderlane.wlpos

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
//import uk.co.wonderlane.wlpos.dataaccess.AmazonS3FileDal

import java.nio.ByteBuffer

class AmazonBrandAssetsService implements IBrandAssetsService {

    def springSecurityService
    def s3Client
    def config

//    private final AmazonS3FileDal fileDal
    private final String brandAssetsBucket

    AmazonBrandAssetsService(String brandAssetsBucket) {
//        fileDal = new AmazonS3FileDal(brandAssetsBucket, new BackOfficeLogger())
        this.brandAssetsBucket = brandAssetsBucket
    }

    @Override
    def getBrandLogo() {
//        return fileDal.getFile("${springSecurityService.principal.retailerId}/BrandLogo.png")

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(brandAssetsBucket).key("${springSecurityService.principal.retailerId}/BrandLogo.png").build()

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray()
        } catch (NoSuchBucketException ne) {
            ne.printStackTrace()
        } catch (NoSuchKeyException ke) {
            ke.printStackTrace()
        } catch (S3Exception ignored) {
            return null
        }

        return new byte[]{}
    }

    @Override
    def saveBrandLogo(byte[] brandLogo) throws Exception {
//        fileDal.writeFile("${springSecurityService.principal.retailerId}/BrandLogo.png", brandLogo)

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(brandAssetsBucket).key("${springSecurityService.principal.retailerId}/BrandLogo.png").build()

            s3Client.putObject(putObjectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(brandLogo)))
        } catch (NoSuchBucketException ne) {
            ne.printStackTrace()
        } catch (NoSuchKeyException ke) {
            ke.printStackTrace()
        }
    }

    @Override
    def resetBrandLogo() throws Exception {
//        fileDal.deleteFile("${springSecurityService.principal.retailerId}/BrandLogo.png")

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(brandAssetsBucket).key("${springSecurityService.principal.retailerId}/BrandLogo.png").build()

            s3Client.deleteObject(deleteObjectRequest)
        } catch (NoSuchBucketException ne) {
            ne.printStackTrace()
        } catch (NoSuchKeyException ke) {
            ke.printStackTrace()
        }
    }
}