package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class MonitoringController {

    def springSecurityService
    def rabbitService
    def gsonProvider

    def tillConnectivity() {

    }

    def transactionServiceStatus() {

    }

    def ajaxGetQueues() {
        Integer storeIdFilter = params.storeIdFilter ? Integer.parseInt(params.storeIdFilter) : null
        Integer tillIdFilter = params.tillIdFilter ? Integer.parseInt(params.tillIdFilter) : null
        String statusFilter = params.statusFilter ? String.valueOf(params.statusFilter) : null

        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        def rabbitQueues = rabbitService.getStoreQueues()

        if (storeIdFilter) {
            rabbitQueues.removeAll { it.storeId != storeIdFilter }
        }

        if (tillIdFilter) {
            rabbitQueues.removeAll { it.tillId != tillIdFilter }
        }

        if (statusFilter) {
            rabbitQueues.removeAll { statusFilter == "Online" ? it.consumers < 1 : it.consumers > 0 }
        }

        rabbitQueues.sort { a, b ->
            a.storeId <=> b.storeId ?: a.tillId <=> b.tillId
        }

        render (template: "connectivity", model: [rabbitQueues: rabbitQueues])
    }

    def ajaxGetTransactionServiceStatus() {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        def rabbitQueues = rabbitService.getServiceQueues(grailsApplication.config.getProperty('wlpos.transactionProcessorQueue'),
                                                          grailsApplication.config.getProperty('wlpos.dataSyncServiceQueue'),
                                                          grailsApplication.config.getProperty('wlpos.kpiProcessorQueue'),
                                                          grailsApplication.config.getProperty('wlpos.reportingProcessorQueue'),
                                                          grailsApplication.config.getProperty('wlpos.shiftProcessorQueue'),
                                                          grailsApplication.config.getProperty('wlpos.stockProcessorQueue'),
                                                          grailsApplication.config.getProperty('wlpos.nisaServiceQueue'),
                                                          grailsApplication.config.getProperty('wlpos.receiptServiceQueue'),
                                                          grailsApplication.config.getProperty('wlpos.rawTransactionWriterQueue'))

        render (template: "transactionServiceStatus", model: [transactionProcessorQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.transactionProcessorQueue') },
                                                              dataSyncServiceQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.dataSyncServiceQueue') },
                                                              kpiProcessorQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.kpiProcessorQueue') },
                                                              reportingProcessorQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.reportingProcessorQueue') },
                                                              shiftProcessorQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.shiftProcessorQueue') },
                                                              stockProcessorQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.stockProcessorQueue') },
                                                              nisaServiceQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.nisaServiceQueue') },
                                                              receiptServiceQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.receiptServiceQueue') },
                                                              rawTransactionWriterQueue: rabbitQueues.find { it.name == grailsApplication.config.getProperty('wlpos.rawTransactionWriterQueue') }])
    }

    def ajaxPurgeQueue(int storeId, int tillId) {
        try {
            if (!rabbitService.isOpen()) {
                render status: 500, text: "Unable to open connection to RabbitMQ."
                return
            }

            rabbitService.purgeQueue(springSecurityService.principal.retailerId, storeId, tillId)

            render status: 200, text: "The queue for till" + tillId + " in store " + storeId + " has been cleared."
        } catch (Exception e) {
            // Assume Rabbit not available.
            render status: 500, text: "Error connecting to RabbitMQ."
        }
    }

    def ajaxDeleteQueue(int storeId, int tillId) {
        try {
            if (!rabbitService.isOpen()) {
                render status: 500, text: "Unable to open connection to RabbitMQ."
                return
            }

            rabbitService.deleteQueue(springSecurityService.principal.retailerId, storeId, tillId)

            render status: 200, text: "The queue for till" + tillId + " in store " + storeId + " has been deleted."
        } catch (Exception e) {
            // Assume Rabbit not available.
            render status: 500, text: "Error connecting to RabbitMQ."
        }
    }

    def ajaxForceSync() {
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.FORCE_DATA_SYNC, springSecurityService.principal.retailerId, Integer.parseInt(params.storeId), null, Integer.parseInt(params.tillId))
        syncMessage.setInsert(false)

        try {
            rabbitService.sendMessage(syncMessage)
        } catch (Exception e) {
            render status: 500, text: "Unable to open connection to RabbitMQ."
            return
        }

        render status: 200, text: "Sync should begin shortly for Till " + params.tillId + " in Store " + params.storeId + "."
    }
}