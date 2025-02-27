package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ProductMessageType;

class ProductMessage implements Serializable {
    
    ProductMessageType type

    Product product
    Message message
    
    static belongsTo = [product: Product, message: Message]
    
    static mapping = {
        table "productmessage"
        version false

        id composite: ['product', 'message', 'type']

        product column: "productId"
        message column: "messageId"
        type column: "`type`", sqlType: "enum", enumType: "string"
    }

    uk.co.wonderlane.wlpos.entities.ProductMessage getProductMessage() {
        uk.co.wonderlane.wlpos.entities.ProductMessage productMessage = new uk.co.wonderlane.wlpos.entities.ProductMessage()

        productMessage.setProductId(productId)
        productMessage.setMessageId(messageId)
        productMessage.setType(type)

        return productMessage
    }
}