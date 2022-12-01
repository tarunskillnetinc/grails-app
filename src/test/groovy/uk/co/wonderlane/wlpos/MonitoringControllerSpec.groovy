package uk.co.wonderlane.wlpos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

class MonitoringControllerSpec extends Specification implements ControllerUnitTest<MonitoringController>, DataTest {

    HashMap<String,String> queueHashMap = new HashMap<>();

    def setup() {

        populateQueueNameMap()

        grailsApplication.config.wlpos.transactionProcessorQueue = getQueueMapping("wlpos.transactionProcessorQueue")
        grailsApplication.config.wlpos.dataSyncServiceQueue = getQueueMapping("wlpos.dataSyncServiceQueue")
        grailsApplication.config.wlpos.kpiProcessorQueue = getQueueMapping("wlpos.kpiProcessorQueue")
        grailsApplication.config.wlpos.reportingProcessorQueue = getQueueMapping("wlpos.reportingProcessorQueue")
        grailsApplication.config.wlpos.shiftProcessorQueue = getQueueMapping("wlpos.shiftProcessorQueue")
        grailsApplication.config.wlpos.stockProcessorQueue = getQueueMapping("wlpos.stockProcessorQueue")
        grailsApplication.config.wlpos.nisaServiceQueue = getQueueMapping("wlpos.nisaServiceQueue")
        grailsApplication.config.wlpos.receiptServiceQueue = getQueueMapping("wlpos.receiptServiceQueue")
        grailsApplication.config.wlpos.rawTransactionWriterQueue = getQueueMapping("wlpos.rawTransactionWriterQueue")
    }

    def cleanup() {}

    //------------------- Calling Till Connectivity Action ---------------------------------------------//

    def 'Test till connectivity successful method'() {
        given:

        when: 'The tillConnectivity action is executed'
        controller.tillConnectivity()

        then: 'The model tillConnectivity render'
        view == '/monitoring/tillConnectivity.gsp'

    }

    //------------------- Calling Transaction Service Status Action ---------------------------------------------//

    def 'Test transaction service status successful method'() {
        given:

        when: 'The transactionServiceStatus action is executed'
        controller.transactionServiceStatus()

        then: 'The model transactionServiceStatus render'
        view == '/monitoring/transactionServiceStatus.gsp'

    }

    //------------------- Calling Ajax Get Queues Action ---------------------------------------------//

    def 'Test get queue action successful method'() {

        given:

        String storeIdFilter = passingStoreId
        String tillIdFilter = passingTillId
        String statusFilter = passingStatus

        List<RabbitQueue> queueList = getRabbitQueue()

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> rabitServiceStatus
            getStoreQueues() >> queueList
        }

        views['/monitoring/_connectivity.gsp'] = "test"

        when: 'The ajaxGetQueues action is executed'
        params["storeIdFilter"] = storeIdFilter
        params["tillIdFilter"] = tillIdFilter
        params["statusFilter"] = statusFilter
        controller.ajaxGetQueues()

        then: 'successfully get snapshot'
        if (rabitServiceStatus){
            controller.response.text == 'test'
            model.rabbitQueues
            assert model.rabbitQueues.size == getFilterRemainingQueueCount(queueList, passingStoreId, passingTillId, passingStatus)
        }

        where: 'Pass following input parameters'
        passingStoreId || passingTillId || passingStatus || rabitServiceStatus
        234            || 1             || "Online"      || true
        234            || 1             || "Offline"     || true
        null           || null          || null          || true


    }

    def 'Test get queue action for not open rabbit service method'() {

        given:

        String storeIdFilter = "234"
        String tillIdFilter = "1"
        String statusFilter = passingStatus

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> rabitServiceStatus
            getStoreQueues() >> getRabbitQueue()
        }

        views['/monitoring/_connectivity.gsp'] = "test"

        when: 'The ajaxGetQueues action is executed'
        params["storeIdFilter"] = storeIdFilter
        params["tillIdFilter"] = tillIdFilter
        params["statusFilter"] = statusFilter
        controller.ajaxGetQueues()

        then: 'successfully get snapshot'
        Exception e = thrown()
        e.message == "Rabbit MQ not available"

        where: 'Pass following input parameters'
        passingStatus || rabitServiceStatus
        "Online"      || false

    }

    //------------------- Calling Ajax Get Transaction Service Status Action ---------------------------------------------//

    def 'Test get transaction service status method'() {

        given:

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> true
            getServiceQueues(grailsApplication.config.getProperty('wlpos.transactionProcessorQueue'),
                    grailsApplication.config.getProperty('wlpos.dataSyncServiceQueue'),
                    grailsApplication.config.getProperty('wlpos.kpiProcessorQueue'),
                    grailsApplication.config.getProperty('wlpos.reportingProcessorQueue'),
                    grailsApplication.config.getProperty('wlpos.shiftProcessorQueue'),
                    grailsApplication.config.getProperty('wlpos.stockProcessorQueue'),
                    grailsApplication.config.getProperty('wlpos.nisaServiceQueue'),
                    grailsApplication.config.getProperty('wlpos.receiptServiceQueue'),
                    grailsApplication.config.getProperty('wlpos.rawTransactionWriterQueue')) >> getServiceQueue()
        }

        views['/monitoring/_transactionServiceStatus.gsp'] = "test"

        when: 'The ajaxGetTransactionServiceStatus action is executed'
        controller.ajaxGetTransactionServiceStatus()

        then: 'successfully get response text and queue list'
        controller.response.text == 'test'
        model.transactionProcessorQueue
        model.dataSyncServiceQueue
        model.kpiProcessorQueue
        model.reportingProcessorQueue
        model.shiftProcessorQueue
        model.stockProcessorQueue
        model.nisaServiceQueue
        model.receiptServiceQueue
        model.rawTransactionWriterQueue

    }

    def 'Test get transaction service status for close rabbit service method'() {

        given:

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> false
            getStoreQueues() >> getRabbitQueue()
        }

        views['/monitoring/_transactionServiceStatus.gsp'] = "test"

        when: 'The ajaxGetTransactionServiceStatus action is executed'
        controller.ajaxGetTransactionServiceStatus()

        then: 'Appropriate error message thrown'
        Exception e = thrown()
        e.message == "Rabbit MQ not available"

    }

    //------------------- Calling Ajax Purge Queue Action ---------------------------------------------//

    def 'Test for purge queue method'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> true
            purgeQueue(_) >> null
        }

        String successText = "The queue for till" + tillId + " in store " + storeId + " has been cleared."

        when: 'The ajaxPurgeQueue action is executed'
        controller.ajaxPurgeQueue(storeId, tillId)

        then: 'successfully response returned'
        controller.response.text == successText
        controller.response.status == 200

    }

    def 'Test for purge queue method for close rabbit service method'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> false
            purgeQueue(_) >> null
        }

        when: 'The gajaxPurgeQueue is executed'
        controller.ajaxPurgeQueue(storeId, tillId)

        then: 'Appropriate error message thrown'
        controller.response.status == 500
        controller.response.text == "Unable to open connection to RabbitMQ."

    }

    def 'Test for purge queue method for handle exception'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> {throw new Exception()}
            purgeQueue(_) >> null
        }

        when: 'The ajaxPurgeQueue action is executed'
        controller.ajaxPurgeQueue(storeId, tillId)

        then: 'Appropriate error message thrown'
        controller.response.status == 500
        controller.response.text == "Error connecting to RabbitMQ."

    }

    //------------------- Calling Ajax Delete Queue Action ---------------------------------------------//

    def 'Test for delete queue method'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> true
            deleteQueue(_) >> null
        }

        String successText = "The queue for till" + tillId + " in store " + storeId + " has been deleted."

        when: 'The ajaxDeleteQueue action is executed'
        controller.ajaxDeleteQueue(storeId, tillId)

        then: 'successfully response returned'
        controller.response.text == successText
        controller.response.status == 200

    }

    def 'Test for delete queue method for close rabbit service method'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> false
            deleteQueue(_) >> null
        }

        when: 'The ajaxDeleteQueue action is executed'
        controller.ajaxDeleteQueue(storeId, tillId)

        then: 'Appropriate error message thrown'
        controller.response.status == 500
        controller.response.text == "Unable to open connection to RabbitMQ."

    }

    def 'Test for delete queue method for handle exception'() {

        given:

        int storeId = 234
        int tillId = 1

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> {throw new Exception()}
            deleteQueue(_) >> null
        }

        when: 'The ajaxDeleteQueue action is executed'
        controller.ajaxDeleteQueue(storeId, tillId)

        then: 'Appropriate error message thrown'
        controller.response.status == 500
        controller.response.text == "Error connecting to RabbitMQ."

    }

    //------------------- Calling Ajax Force Sync Action ---------------------------------------------//

    def 'Test for force sync method'() {

        given:

        String storeId = "234"
        String tillId = "1"

        Gson gson = new GsonBuilder().create()

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> true
            sendQueueMessage(_) >> null
        }

        controller.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
        }

        when: 'The ajaxForceSync action is executed'
        params["tillId"] = tillId
        params["storeId"] = storeId
        controller.ajaxForceSync()

        String successText =  "Sync should begin shortly for Till " + params.tillId + " in Store " + params.storeId + "."

        then: 'successfully response returned'
        controller.response.text == successText
        controller.response.status == 200

    }

    def 'Test for force sync for close rabbit service method'() {

        given:

        String storeId = "234"
        String tillId = "1"

        Gson gson = new GsonBuilder().create()

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}
        }

        controller.rabbitService = Stub(BackOfficeRabbitService){
            isOpen() >> false
            sendQueueMessage(_) >> null
        }

        controller.gsonProvider = Stub(GsonProvider){
            getGson() >> gson
        }

        when: 'The ajaxForceSync action is executed'
        params["tillId"] = tillId
        params["storeId"] = storeId
        controller.ajaxForceSync()

        then: 'Appropriate error message thrown'
        controller.response.text == "Unable to open connection to RabbitMQ."
        controller.response.status == 500

    }



    int getFilterRemainingQueueCount(List<RabbitQueue> rabbitQueueList, Integer storeIdFilter, Integer tillIdFilter, String statusFilter){
        if (storeIdFilter) {
            rabbitQueueList.removeAll { it.storeId != storeIdFilter }
        }

        if (tillIdFilter) {
            rabbitQueueList.removeAll { it.tillId != tillIdFilter }
        }

        if (statusFilter) {
            rabbitQueueList.removeAll { statusFilter == "Online" ? it.consumers < 1 : it.consumers > 0 }
        }

        return rabbitQueueList.size();
    }

    List<RabbitQueue> getRabbitQueue(){

        List<RabbitQueue> rabbitQueueList = new ArrayList<>()

        RabbitQueue rabbitQueue1 = new RabbitQueue()
        rabbitQueue1.setConsumers(0)
        rabbitQueue1.setStoreId(234)
        rabbitQueue1.setTillId(1)

        RabbitQueue rabbitQueue2 = new RabbitQueue()
        rabbitQueue2.setConsumers(2)
        rabbitQueue2.setStoreId(234)
        rabbitQueue2.setTillId(1)

        RabbitQueue rabbitQueue3 = new RabbitQueue()
        rabbitQueue3.setConsumers(2)
        rabbitQueue3.setStoreId(234)
        rabbitQueue3.setTillId(2)

        RabbitQueue rabbitQueue4 = new RabbitQueue()
        rabbitQueue4.setConsumers(3)
        rabbitQueue4.setStoreId(234)
        rabbitQueue4.setTillId(3)

        RabbitQueue rabbitQueue5 = new RabbitQueue()
        rabbitQueue5.setConsumers(3)
        rabbitQueue5.setStoreId(235)
        rabbitQueue5.setTillId(3)

        rabbitQueueList.add(rabbitQueue1)
        rabbitQueueList.add(rabbitQueue2)
        rabbitQueueList.add(rabbitQueue3)
        rabbitQueueList.add(rabbitQueue4)
        rabbitQueueList.add(rabbitQueue5)

        return rabbitQueueList
    }

    List<RabbitQueue> getServiceQueue(){
        List<RabbitQueue> rabbitQueueList = new ArrayList<>()
        for (Map.Entry<String, String> set : queueHashMap.entrySet()){
            RabbitQueue rabbitQueue = new RabbitQueue()
            rabbitQueue.setName(set.getValue())
            rabbitQueueList.add(rabbitQueue)
        }
        return rabbitQueueList
    }


    HashMap<String, String> populateQueueNameMap(){
        queueHashMap.put("wlpos.transactionProcessorQueue", "transactionProcessorQueue")
        queueHashMap.put("wlpos.dataSyncServiceQueue", "dataSyncServiceQueue")
        queueHashMap.put("wlpos.kpiProcessorQueue", "kpiProcessorQueue")
        queueHashMap.put("wlpos.reportingProcessorQueue", "reportingProcessorQueue")
        queueHashMap.put("wlpos.shiftProcessorQueue", "shiftProcessorQueue")
        queueHashMap.put("wlpos.stockProcessorQueue", "stockProcessorQueue")
        queueHashMap.put("wlpos.nisaServiceQueue", "nisaServiceQueue")
        queueHashMap.put("wlpos.receiptServiceQueue", "receiptServiceQueue")
        queueHashMap.put("wlpos.rawTransactionWriterQueue", "rawTransactionWriterQueue")
    }

    String getQueueMapping(String key){
        return queueHashMap.get(key)
    }
}
