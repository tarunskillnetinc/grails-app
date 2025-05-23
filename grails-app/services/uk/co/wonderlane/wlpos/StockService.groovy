package uk.co.wonderlane.wlpos

//import uk.co.wonderlane.wlpos.entities.Job
import uk.co.wonderlane.wlpos.enums.JobType
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.enums.JobStatus
import org.joda.time.DateTime
import java.util.UUID

class StockService {

    def rabbitService
    def jobService

    def initiateStockResetJob(selectedStore, loggedInUsername, retailerId) {
        def jobInstance

        try {
            jobInstance = new Job()
            jobInstance.uuid = UUID.randomUUID() // GORM UUID id field is named "id", not "uuid"
            jobInstance.type = JobType.STOCK_RESET
            jobInstance.storeId= selectedStore.id as int
            jobInstance.retailerId = retailerId as int
            jobInstance.status = JobStatus.PENDING
            jobInstance.productListId = null // or null if valid
            jobInstance.dateCreated = DateTime.now()

            jobService.saveJob(jobInstance)
        } catch (Exception e) {
            log.error("Failed to create or save Job: ${e.message}", e)
            return null
        }

        try {
            def syncMessage = new SyncMessage(
                    SyncMessageType.STOCK_RESET,
                    retailerId,
                    null,
                    selectedStore.id,
                    null
            )
            syncMessage.setUuid(jobInstance.uuid)
            syncMessage.setStatus(JobStatus.PENDING)
            syncMessage.setProductListId(jobInstance.productListId)
            rabbitService.sendJobsMessage(syncMessage)
        } catch (Exception e) {
            log.error("Failed to send RabbitMQ message for job ${jobInstance?.getUuid()}: ${e.message}", e)
        }

        return jobInstance
    }
}
