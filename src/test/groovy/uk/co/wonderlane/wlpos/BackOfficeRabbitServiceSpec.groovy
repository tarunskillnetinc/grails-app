package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.helpers.BackOfficeRabbitServiceHelper
import uk.co.wonderlane.wlpos.helpers.RabbitMQMockChannel

import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

class BackOfficeRabbitServiceSpec extends Specification implements ServiceUnitTest<BackOfficeRabbitServiceHelper>, DataTest{

    BackOfficeRabbitServiceHelper backOfficeRabbitServiceHelper
    List<RabbitQueue> rabbitMQList
    int retailerId = 9

    def setup() {
        rabbitMQList = getRabbitMqList()
        backOfficeRabbitServiceHelper = new BackOfficeRabbitServiceHelper("127.0.0.1", 80, 81, "wonderlane", "password", false, rabbitMQList)
    }

    def cleanup() {}

    //------------------- Calling Get Store Queue Action ---------------------------------------------//

    def 'Test the get store queue successfully'() {
        given:
        backOfficeRabbitServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'getCfdImagesFromFile action is executed'
        List<RabbitQueue> returnedList =  backOfficeRabbitServiceHelper.getStoreQueues()

        then: 'getCfdImagesFromFile result is correct'
        if (isOpenChannel && !isNullChannel){
            assert returnedList.size() == getFilteredRabbitMQQueueList(rabbitMQList).size()
        } else {
            assert returnedList.size() == 0
        }


        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false
        true          || false

    }

    def 'Test the get store queue handle successfully when connection not available'() {
        given:
        backOfficeRabbitServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        backOfficeRabbitServiceHelper.isMockQueueList = false
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'getCfdImagesFromFile action is executed'
        List<RabbitQueue> returnedList =  backOfficeRabbitServiceHelper.getStoreQueues()

        then: 'getStoreQueues result is correct'
        //Since connection not available empty list returned without throwing any exception
        assert returnedList.size() == 0

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }


    //------------------- Calling Get Service Queue Action ---------------------------------------------//

    def 'Test the get service queue successfully'() {
        given:
        backOfficeRabbitServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'getCfdImagesFromFile action is executed'
        List<RabbitQueue> returnedList =  backOfficeRabbitServiceHelper.getServiceQueues("DataSyncService", "DataSyncService", "ReceiptService")

        then: 'getCfdImagesFromFile result is correct'
        if (isOpenChannel && !isNullChannel){
            assert returnedList.size() == 3
        }

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }

    def 'Test the get service queue exception scenario'() {
        given:
        backOfficeRabbitServiceHelper.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)}}
        }
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'getCfdImagesFromFile action is executed'
        backOfficeRabbitServiceHelper.getServiceQueues("DataSyncService", "DataSyncService", "ReceiptService")

        then: 'getCfdImagesFromFile result is correct'
        Exception e = thrown()
        e.message == "Rabbit MQ not available."

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false

    }

    //------------------- Calling RabbitMQ Declare Exchange Action ---------------------------------------------//

    def 'Test the rabbitMq declare exchange for active channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.declareExchange("Nisa Exchange")

        then: 'declareExchange action execute without any error'
        //Execute method without any error

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }

    def 'Test the rabbitMq declare exchange for null channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.declareExchange("Nisa Exchange")

        then: 'declareExchange action throws rabbitMq error'
        Exception e = thrown()
        e.message == "Rabbit MQ not available."

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false

    }

    //------------------- Calling RabbitMQ Declare Queue Action ---------------------------------------------//

    def 'Test the rabbitMq declare queue for active channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.declareQueue("Nisa Queue","Nisa Exchange")

        then: 'declareExchange action execute without any error'
        //Execute method without any error

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }

    def 'Test the rabbitMq declare queue for null channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.declareQueue("Nisa Queue", "Nisa Exchange")

        then: 'declareQueue action execute by throwing rabbitMq error'
        Exception e = thrown()
        e.message == "Rabbit MQ not available."

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false

    }

    //------------------- Calling RabbitMQ Purge Queue Action ---------------------------------------------//

    def 'Test the rabbitMq purge queue for active channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.purgeQueue(9, 234, 33)

        then: 'purgeQueue action execute without any error'
        //Execute method without any error

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }

    def 'Test the rabbitMq purge queue for null channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.purgeQueue(9, 234, 33)

        then: 'purgeQueue action by throwing rabbitMq error'
        Exception e = thrown()
        e.message == "Rabbit MQ not available."

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false

    }

    //------------------- Calling RabbitMQ Delete Queue Action ---------------------------------------------//

    def 'Test the rabbitMq delete queue for active channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.deleteQueue(9, 234, 33)

        then: 'deleteQueue action execute without any error'
        //Execute method without any error

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || false

    }

    def 'Test the rabbitMq delete queue for null channel'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        backOfficeRabbitServiceHelper.channel = getMockChannel(isOpenChannel, isNullChannel)

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.deleteQueue(9, 234, 33)

        then: 'declareExchange action by throwing rabbitMq error'
        Exception e = thrown()
        e.message == "Rabbit MQ not available."

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel
        true          || true
        false         || false
        false         || false

    }

    //------------------- Calling Send Message Action ---------------------------------------------//

    def 'Test the calling send message action'() {

        given:
        backOfficeRabbitServiceHelper.isMockQueueList = true
        SyncMessage mockSyncMessage = getSyncMessage(storeNumber, tillId)

        RabbitMQMockChannel rabbitMQMockChannel = getMockChannel(isOpenChannel, isNullChannel)
        backOfficeRabbitServiceHelper.channel = rabbitMQMockChannel

        when: 'declareExchange action is executed'
        backOfficeRabbitServiceHelper.sendMessage(mockSyncMessage)

        then: 'declareExchange action execute without any error'
        if (isOpenChannel && !isNullChannel){
            if (mockSyncMessage.getStoreNumber() > 0 && mockSyncMessage.getTillId() > 0) {
                String queueName = getQueueName(mockSyncMessage)
                assert rabbitMQMockChannel.getQueueMessageMap().get(queueName) != null
            } else {
                String exchangeName = getExchangeName(mockSyncMessage)
                assert rabbitMQMockChannel.getExchangeMessageMap().get(exchangeName) != null
            }
        }

        where: 'Pass following input parameters'
        isOpenChannel || isNullChannel || storeNumber || tillId
        true          || false         || 234         || 33
        true          || false         || 0           || 33
        true          || false         || 0           || 0
        true          || false         || 234         || 0

    }

    String getExchangeName(SyncMessage mockSyncMessage){
        String exchangeName = null;
        if (mockSyncMessage.getStoreNumber() <= 0 || mockSyncMessage.getTillId() <= 0) {
            if (mockSyncMessage.getStoreNumber() > 0) {
                exchangeName = String.format("R%d_S%d", mockSyncMessage.getRetailerId(), mockSyncMessage.getStoreNumber())
            } else {
                exchangeName = String.format("R%d", mockSyncMessage.getRetailerId())
            }
        }
        return exchangeName
    }

    String getQueueName(SyncMessage mockSyncMessage){
        if (mockSyncMessage.getStoreNumber() > 0 && mockSyncMessage.getTillId() > 0) {
            return String.format("R%d_S%d_T%d", mockSyncMessage.getRetailerId(), mockSyncMessage.getStoreNumber(), mockSyncMessage.getTillId())
        }
        return null
    }

    SyncMessage getSyncMessage(int storeNumber, int tillId){
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.PRODUCT, 9)
        syncMessage.setStoreNumber(storeNumber)
        syncMessage.setTillId(tillId)
        syncMessage.setRetailerId(9)
        return syncMessage
    }


    List<RabbitQueue> getFilteredRabbitMQQueueList(List<RabbitQueue> rabbitMQList){
        List<RabbitQueue> rabbitMQListFiltered = new ArrayList<>();
        rabbitMQList?.each {
            if (it.name?.startsWith("R${retailerId}_S") && it.name?.count("_") == 2) {
                it.retailerId = Integer.parseInt(it.name.substring(1, it.name.indexOf("_")))
                it.storeId = Integer.parseInt(it.name.substring(it.name.indexOf("_") + 2, it.name.lastIndexOf("_")))
                it.tillId = Integer.parseInt(it.name.substring(it.name.lastIndexOf("_") + 2))

                rabbitMQListFiltered.add(it)
            }
        }
        return rabbitMQListFiltered;
    }

    List<RabbitQueue> getRabbitMqList(){
        List<RabbitQueue> rabbitQueueList = new ArrayList<>();

        List<String> rabbitQList = Arrays.asList("R9_S234_T33", "DataSyncService", "DataSyncService", "ReceiptService", "SnappyService" , "StockProcessor",
                "TransactionProcessor")

        for (String queueName : rabbitQList){
            RabbitQueue rabbitQueue = new RabbitQueue();
            rabbitQueue.name = queueName
            rabbitQueue.retailerId = 9
            rabbitQueue.storeId = 234
            rabbitQueue.tillId = 33
            rabbitQueueList.add(rabbitQueue)
        }

        return rabbitQueueList;

    }

    RabbitMQMockChannel getMockChannel(Boolean isOpenChannel, boolean isNullChannel){
        RabbitMQMockChannel mockChannel = null
        if (!isNullChannel){
            mockChannel = new RabbitMQMockChannel(isOpenChannel)
        }
        return mockChannel
    }



}
