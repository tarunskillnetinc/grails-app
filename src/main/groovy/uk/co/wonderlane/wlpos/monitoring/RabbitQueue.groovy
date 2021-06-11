package uk.co.wonderlane.wlpos.monitoring

import org.joda.time.DateTime

class RabbitQueue {

    private int consumers
    private int messages
    private DateTime idle_since
    private String name

    // Helper fields made by splitting up the name.
    private int retailerId
    private int storeId
    private int tillId

    public RabbitQueue() {

    }

    int getConsumers() {
        return consumers
    }

    void setConsumers(int consumers) {
        this.consumers = consumers
    }

    int getMessages() {
        return messages
    }

    void setMessages(int messages) {
        this.messages = messages
    }

    DateTime getIdle_since() {
        return idle_since
    }

    void setIdle_since(DateTime idle_since) {
        this.idle_since = idle_since
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    int getRetailerId() {
        return retailerId
    }

    void setRetailerId(int retailerId) {
        this.retailerId = retailerId
    }

    int getStoreId() {
        return storeId
    }

    void setStoreId(int storeId) {
        this.storeId = storeId
    }

    int getTillId() {
        return tillId
    }

    void setTillId(int tillId) {
        this.tillId = tillId
    }
}
