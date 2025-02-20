package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.co.wonderlane.wlpos.entities.supplier.Supplier

@Transactional
class SnsService {
    private final SnsClient snsClient
    private final String accountId
    private final String supplierUpdateTopic
    private final String region
    private final Gson gson

    SnsService(
            SnsClient snsClient,
            String accountId,
            String supplierUpdateTopic,
            String region,
            GsonProvider gsonProvider
    ) {
        this.snsClient = snsClient
        this.accountId = accountId
        this.supplierUpdateTopic = supplierUpdateTopic
        this.region = region
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
        return "arn:aws:sns:${region}:${accountId}:${topicName}"
    }

    class SupplierRequest {
        MessageType type
        Supplier supplier
    }

    enum MessageType {
        SUPPLIER_ADD, SUPPLIER_DELETE
    }
}
