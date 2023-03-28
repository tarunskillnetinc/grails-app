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
import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

import javax.xml.bind.DatatypeConverter
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class BackOfficeRabbitService extends RabbitService {

    def springSecurityService
    def gson

    private String apiUrl
    private String apiAuthorization

    def dateTimeFormatUnix = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    def dateTimeFormatWindows = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

    BackOfficeRabbitService(String host, int port, int apiPort, String username, String password, boolean useSsl) {
        super(host, port, username, password, useSsl, null, null, new BackOfficeLogger()) // TODO Implement an actual BackOfficeLogger?

        apiUrl = "https://${host}:${apiPort}/api/"
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
                        return new DateTime(getDateFormat().parse(json.getAsString()).getTime())
                    }
                })
                .create()

        init()
    }

    private SimpleDateFormat getDateFormat() {
        String osName = System.getProperty("os.name")
        if (osName != null && osName.contains("Windows")) {
            return dateTimeFormatWindows
        } else {
            return dateTimeFormatUnix
        }
    }

    private void checkChannelAvailability() {
        if (channel == null || !channel.isOpen()) {
            init()

            if (channel == null || !channel.isOpen()) {
                throw new IOException("Rabbit MQ not available.")
            }
        }
    }

    List<RabbitQueue> getStoreQueues() {
        def rabbitQueues = []

        try {
            checkChannelAvailability()

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
        checkChannelAvailability()

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
        }catch(Exception ex){
            System.println("Error found when loading existing queues, Error " + ex)
            log.error("Exception when creating till connection")
        }
        return []

    }

    void declareExchange(String exchange) {
        checkChannelAvailability()

        this.channel.exchangeDeclare(exchange, "fanout", true)
    }

    void declareQueue(String queue, String exchange) {
        checkChannelAvailability()

        this.channel.queueBind(queue, exchange, "")
    }

    def purgeQueue(int retailerId, int storeId, int tillId) {
        checkChannelAvailability()

        channel.queuePurge(String.format("R%d_S%d_T%d", retailerId, storeId, tillId))
    }

    def deleteQueue(int retailerId, int storeId, int tillId) {
        checkChannelAvailability()

        channel.queueDelete(String.format("R%d_S%d_T%d", retailerId, storeId, tillId))
    }

    void sendMessage(SyncMessage syncMessage) throws IOException {
        if (syncMessage.getStoreNumber() > 0 && syncMessage.getTillId() > 0) {
            String exchangeName = String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber())
            String queueName = String.format("R%d_S%d_T%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber(), syncMessage.getTillId())

            declareExchange(exchangeName)
            declareQueue(queueName, exchangeName)

            sendQueueMessage(queueName, gson.toJson(syncMessage))
        } else {
            sendExchangeMessage(syncMessage)
        }
    }

    void sendExchangeMessage(SyncMessage syncMessage) throws IOException {
        checkChannelAvailability()

        String exchangeName

        if (syncMessage.getStoreNumber() > 0) {
            exchangeName = String.format("R%d_S%d", syncMessage.getRetailerId(), syncMessage.getStoreNumber())
        } else {
            exchangeName = String.format("R%d", syncMessage.getRetailerId())
        }

        // TODO Note that whilst we will declare the exchange if it is missing, we are not declaring any queues, which means that the message will still not go anywhere.
        declareExchange(exchangeName)
        sendExchangeMessage(exchangeName, gson.toJson(syncMessage))
    }
}