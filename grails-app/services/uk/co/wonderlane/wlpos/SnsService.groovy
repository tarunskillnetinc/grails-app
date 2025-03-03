package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sns.model.CreateTopicResponse
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SnsException
import uk.co.wonderlane.wlpos.entities.supplier.Supplier

@Transactional
class SnsService {
    private final SnsClient snsClient
    private final String supplierUpdateTopic
    private final Gson gson

    SnsService(
            SnsClient snsClient,
            String supplierUpdateTopic,
            GsonProvider gsonProvider
    ) {
        this.snsClient = snsClient
        this.supplierUpdateTopic = supplierUpdateTopic
        gson = gsonProvider.gson
    }

    def publishSupplierAdd(Supplier supplier) {
        def supplierRequest = new SupplierRequest(
                type: MessageType.SUPPLIER_ADD,
                supplier: supplier
        )
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierUpdateTopic))
                .message(gson.toJson(supplierRequest))
                .build()

        snsClient.publish(request)
    }

    def publishSupplierDelete(Supplier supplier) {
        def supplierRequest = new SupplierRequest(
                type: MessageType.SUPPLIER_DELETE,
                supplier: supplier
        )
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierUpdateTopic))
                .message(gson.toJson(supplierRequest))
                .build()

        snsClient.publish(request)
    }

    private String generateTopicArn(String topicName) {
        CreateTopicResponse result = null
        try {
            CreateTopicRequest request = CreateTopicRequest.builder()
                    .name(topicName)
                    .build()

            result = snsClient.createTopic(request)
            return result.topicArn()
        } catch (SnsException e) {
            logger.logException("Error creating SNS topic: ${topicName}", TAG, e)
        }
        return null
    }

    class SupplierRequest {
        MessageType type
        Supplier supplier
    }

    enum MessageType {
        SUPPLIER_ADD, SUPPLIER_DELETE
    }
}
