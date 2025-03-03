package uk.co.wonderlane.wlpos


import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sns.model.CreateTopicResponse
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SnsException
import uk.co.wonderlane.wlpos.entities.SnsNotification
import uk.co.wonderlane.wlpos.entities.supplier.Supplier
import uk.co.wonderlane.wlpos.enums.sns.SnsActionType
import uk.co.wonderlane.wlpos.enums.sns.SnsNotificationType

@Transactional
class SnsService {
    def snsClient
    def supplierTopic
    def gsonProvider

    def publishSupplierAdd(Supplier supplier) {
        def notification = new SnsNotification(SnsNotificationType.SUPPLIER, SnsActionType.ADD, supplier)
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification))
                .build()

        snsClient.publish(request)
    }

    def publishSupplierDelete(Supplier supplier) {
        def notification = new SnsNotification(SnsNotificationType.SUPPLIER, SnsActionType.DELETE, supplier)
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification))
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
            log.error("Error creating SNS topic: ${topicName}", e)
        }
        return null
    }
}
