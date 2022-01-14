package uk.co.wonderlane.wlpos

class MonitoringController {

    def rabbitService

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
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        rabbitService.purgeQueue(storeId, tillId)

        render status: 200
    }

    def ajaxDeleteQueue(int storeId, int tillId) {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        rabbitService.deleteQueue(storeId, tillId)

        render status: 200
    }
}