package uk.co.wonderlane.wlpos

class MonitoringController {

    def springSecurityService

    def index() {

    }

    def ajaxGetQueues() {
        BackOfficeRabbitService rabbitService = new BackOfficeRabbitService(grailsApplication.config.getProperty('rabbitmq.host'), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')), Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.apiPort')), grailsApplication.config.getProperty('rabbitmq.username'), grailsApplication.config.getProperty('rabbitmq.password'))
        rabbitService.init()

        if (!rabbitService.isOpen()) {
            throw new Exception("Rabbit MQ not available")
        }

        def rabbitQueues = rabbitService.getQueues(springSecurityService.principal.retailerId)

        rabbitQueues.sort { a, b ->
            a.storeId <=> b.storeId ?: a.tillId <=> b.tillId
        }

        render (template: "connectivity", model: [rabbitQueues: rabbitQueues])
    }
}