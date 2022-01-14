package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.monitoring.RabbitQueue

import javax.xml.bind.DatatypeConverter
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class BackOfficeRabbitService extends RabbitService {

    def springSecurityService

    def gson

    private String apiUrl
    private String apiAuthorization

    BackOfficeRabbitService(String host, int port, int apiPort, String username, String password) {
        super(host, port, username, password, null, null, new BackOfficeLogger()) // TODO Implement an actual BackOfficeLogger?

        apiUrl = "http://${host}:${apiPort}/api/queues"
        apiAuthorization = DatatypeConverter.printBase64Binary("${username}:${password}".getBytes())

        def dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
                    @Override
                    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new DateTime(dateTimeFormat.parse(json.getAsString()).getTime())
                    }
                }).create()

        init()
    }

    List<RabbitQueue> getStoreQueues() {
        def allRabbitQueues = getQueues()

        def rabbitQueues = []

        // Only return the queues for our retailer.
        allRabbitQueues?.each {
            if (it.name?.startsWith("R2_S") && it.name?.count("_") == 2) {
                it.retailerId = Integer.parseInt(it.name.substring(1, it.name.indexOf("_")))
                it.storeId = Integer.parseInt(it.name.substring(it.name.indexOf("_") + 2, it.name.lastIndexOf("_")))
                it.tillId = Integer.parseInt(it.name.substring(it.name.lastIndexOf("_") + 2))

                rabbitQueues.add(it)
            }
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

    private List<RabbitQueue> getQueues() {
        // Open a connection to the RabbitMQ REST API.
        def url = apiUrl.toURL()

        def urlConnection = url.openConnection()
        urlConnection.addRequestProperty("Authorization", "Basic ${apiAuthorization}")

        def responseJson = urlConnection.inputStream.text

        // Convert the response JSON into a list of RabbitQueue objects.
        Type listType = new TypeToken<ArrayList<RabbitQueue>>(){}.getType();

        return gson.fromJson(responseJson, listType)
    }

    void declareExchange(String exchange) {
        this.channel.exchangeDeclare(exchange, "fanout", true);
    }

    void declareQueue(String queue, String exchange) {
        this.channel.queueBind(queue, exchange, "")
    }
}