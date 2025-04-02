package uk.co.wonderlane.wlpos


import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sns.model.CreateTopicResponse
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SnsException
import uk.co.wonderlane.wlpos.entities.event.message.*
import uk.co.wonderlane.wlpos.enums.event.message.TaskEventMessageActionType
import uk.co.wonderlane.wlpos.entities.supplier.Supplier

@Transactional
class SnsService {
    def springSecurityService
    def snsClient
    def supplierTopic
    def gsonProvider

    def publishSupplierAdd(Supplier supplier) {
        def notification = new SupplierTaskEventMessage(springSecurityService.principal.retailerId, TaskEventMessageActionType.ADD, supplier)
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification, TaskEventMessage.class))
                .messageAttributes(Map.of("retailerId", MessageAttributeValue.builder().dataType("Number").stringValue((String) springSecurityService.principal.retailerId).build()))
                .build()

        snsClient.publish(request)
    }

    def publishSupplierDelete(Supplier supplier) {
        def notification = new SupplierTaskEventMessage(springSecurityService.principal.retailerId, TaskEventMessageActionType.DELETE, supplier)
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification, TaskEventMessage.class))
                .messageAttributes(Map.of("retailerId", MessageAttributeValue.builder().dataType("Number").stringValue((String) springSecurityService.principal.retailerId).build()))
                .build()

        snsClient.publish(request)
    }

    private String generateTopicArn(String topicName) {
        try {
            CreateTopicRequest request = CreateTopicRequest.builder()
                    .name(topicName)
                    .build()

            CreateTopicResponse result = snsClient.createTopic(request)
            return result.topicArn()
        } catch (SnsException e) {
            log.error("Error creating SNS topic: ${topicName}", e)
        }
        return null
    }
}
