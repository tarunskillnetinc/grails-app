package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.exceptions.RabbitServiceException
import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

import javax.xml.bind.DatatypeConverter
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class BackOfficeRabbitService extends RabbitService {

    def springSecurityService
    def gson

    private String apiUrl
    private String apiAuthorization
    private String senderExchange

    def rabbitMqDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

    BackOfficeRabbitService(String host, int port, String apiProtocol, int apiPort, String username, String password, boolean useSsl, String senderExchange) {
        super(host, port, username, password, useSsl, null, null, new BackOfficeLogger()) // TODO Implement an actual BackOfficeLogger?

        this.senderExchange = senderExchange
        apiUrl = "${apiProtocol}://${host}:${apiPort}/api/"
        apiAuthorization = DatatypeConverter.printBase64Binary("${username}:${password}".getBytes())

        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
                    @Override
                    JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json))
                    }
                })
                .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                    @Override
                    DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new DateTime(rabbitMqDateFormat.parse(json.getAsString()).getTime())
                    }
                })
                .create()

        init()
    }

    private void initVirtualHost(String virtualHost) {
        setVirtualHost(virtualHost)
        close() // close old connection before opening a new one (WAIT-585)
        init()

        if (channel == null || !channel.isOpen()) {
            throw new IOException("Rabbit MQ not available.")
        }
    }

    List<RabbitQueue> getStoreQueues() {
        def rabbitQueues = []

        try {
            def allRabbitQueues = getQueues()

            // Only return the queues for our retailer.
            allRabbitQueues?.each {
                if (it.name?.startsWith("R${springSecurityService.principal.retailerId}_S") && it.name?.count("_") == 2) {
                    it.retailerId = Integer.parseInt(it.name.substring(1, it.name.indexOf("_")))
                    it.storeId = Integer.parseInt(it.name.substring(it.name.indexOf("_") + 2, it.name.lastIndexOf("_")))
                    it.tillId = Integer.parseInt(it.name.substring(it.name.lastIndexOf("_") + 2))

                    rabbitQueues.add(it)
                }
            }
        } catch(Exception ex) {
            System.println("Error found when loading existing queues, Error " + ex)
            log.error("Error found when loading existing queues, Error " + ex)
        }

        return rabbitQueues
    }

    List<RabbitQueue> getServiceQueues(String... queueNames) {
        def allRabbitQueues = getQueues()

        def rabbitQueues = []

        // Only return the queues for our retailer.
        allRabbitQueues?.each {
            if (queueNames.contains(it.name)) {
                rabbitQueues.add(it)
            }
        }

        return rabbitQueues
    }

    protected List<RabbitQueue> getQueues() {
        try {
            // Open a connection to the RabbitMQ REST API.
            def url = (apiUrl + "queues").toURL()

            def urlConnection = url.openConnection()
            urlConnection.addRequestProperty("Authorization", "Basic ${apiAuthorization}")

            def responseJson = urlConnection.inputStream.text

            // Convert the response JSON into a list of RabbitQueue objects.
            Type listType = new TypeToken<ArrayList<RabbitQueue>>(){}.getType()

            return gson.fromJson(responseJson, listType)
        } catch(Exception ex) {
            System.println("Error found when loading existing queues, Error " + ex)
            log.error("Exception when creating till connection")
        }

        return []
    }

    void declareExchange(String exchange) {
        this.channel.exchangeDeclare(exchange, "fanout", true)
    }

    void declareQueue(String queue, String exchange) {
        this.channel.queueBind(queue, exchange, "")
    }

    def purgeQueue(int retailerId, int storeId, int tillId) {
        initVirtualHost(springSecurityService.principal.retailer.config.rabbitMqVirtualHost)

        channel.queuePurge(String.format("R%d_S%d_T%d", retailerId, storeId, tillId))
    }

    def deleteQueue(int retailerId, int storeId, int tillId) {
        initVirtualHost(springSecurityService.principal.retailer.config.rabbitMqVirtualHost)

        channel.queueDelete(String.format("R%d_S%d_T%d", retailerId, storeId, tillId))
    }

    void sendMessage(SyncMessage syncMessage) throws IOException {
        initVirtualHost(springSecurityService.principal.retailer.config.rabbitMqVirtualHost)

        if (syncMessage.getStoreNumber() > 0 && syncMessage.getTillId() > 0) {
            String exchangeName = String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber())
            String queueName = String.format("R%d_S%d_T%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber(), syncMessage.getTillId())

            declareExchange(exchangeName)
            declareQueue(queueName, exchangeName)

            sendQueueMessage(queueName, gson.toJson(syncMessage))
        } else if (syncMessage.getStoreNumber() > 0) {
            String exchangeName = String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber())

            // Note, declaring the exchange here means that we don't throw any errors, but there would be no queues attached to it so our message wouldn't go anywhere.
            declareExchange(exchangeName)

            sendExchangeMessage(exchangeName, gson.toJson(syncMessage))
        } else {
            String exchangeName = String.format("R%d", syncMessage.getRetailerId())

            // Note, declaring the exchange here means that we don't throw any errors, but there would be no queues attached to it so our message wouldn't go anywhere.
            declareExchange(exchangeName)

            sendExchangeMessage(exchangeName, gson.toJson(syncMessage))
        }
    }

    void sendSenderExchangeMessage(String json) throws IOException, RabbitServiceException {
        initVirtualHost(springSecurityService.principal.retailer.config.rabbitMqVirtualHost)

        if (channel.isOpen()) {
            sendExchangeMessage(senderExchange, json);
        } else {
            throw new RabbitServiceException(0, "Error sending rabbit message " + senderExchange + ": " + json);
        }
        logger.logInfo("Sending rabbit message " + senderExchange + ": ", json);
    }
}