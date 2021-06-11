package uk.co.wonderlane.wlpos

class MonitoringController {

    def rabbitService

    def index() {

    }

    def ajaxGetQueues() {
        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        def rabbitQueues = rabbitService.getQueues()

        rabbitQueues.sort { a, b ->
            a.storeId <=> b.storeId ?: a.tillId <=> b.tillId
        }

        render (template: "connectivity", model: [rabbitQueues: rabbitQueues])
    }
}