package uk.co.wonderlane.wlpos.helpers

import uk.co.wonderlane.wlpos.BackOfficeRabbitService
import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

import java.util.concurrent.TimeoutException

class BackOfficeRabbitServiceHelper extends BackOfficeRabbitService {

    List<RabbitQueue> rabbitMQList
    boolean isMockQueueList

    BackOfficeRabbitServiceHelper(String host, int port, int apiPort, String username, String password, boolean useSsl, List<RabbitQueue> rabbitMQList) {
        super(host, port, apiPort, username, password, useSsl)
        this.rabbitMQList = rabbitMQList
    }

    BackOfficeRabbitServiceHelper(String host, int port, int apiPort, String username, String password, boolean useSsl) {
        super(host, port, apiPort, username, password, useSsl)
    }

    protected void init() throws IOException, TimeoutException {
        //Override to do nothing this
    }

    protected List<RabbitQueue> getQueues() {
        if (isMockQueueList){
            return rabbitMQList
        } else {
            return super.getQueues()
        }

    }

}
