package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sns.model.CreateTopicResponse
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SnsException
import uk.co.wonderlane.wlpos.entities.sns.SnsNotification
import uk.co.wonderlane.wlpos.entities.supplier.Supplier
import uk.co.wonderlane.wlpos.enums.sns.SnsActionType
import uk.co.wonderlane.wlpos.enums.sns.SnsNotificationType

import java.lang.reflect.Type

@Transactional
class SnsService {
    def springSecurityService
    def snsClient
    def supplierTopic
    def gsonProvider

    def publishSupplierAdd(Supplier supplier) {
        def notification = new SnsNotification<Supplier>(SnsNotificationType.SUPPLIER ,SnsActionType.ADD, supplier)
        Type typeToken = new TypeToken<SnsNotification<Supplier>>(){}.getType();
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification, typeToken))
                .messageAttributes(Map.of("retailerId", MessageAttributeValue.builder().dataType("Number").stringValue((String) springSecurityService.principal.retailerId).build()))
                .build()

        snsClient.publish(request)
    }

    def publishSupplierDelete(Supplier supplier) {
        def notification = new SnsNotification<Supplier>(SnsNotificationType.SUPPLIER, SnsActionType.DELETE, supplier)
        Type typeToken = new TypeToken<SnsNotification<Supplier>>(){}.getType();
        PublishRequest request = PublishRequest.builder()
                .topicArn( generateTopicArn(supplierTopic))
                .message(gsonProvider.getGson().toJson(notification, typeToken))
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
