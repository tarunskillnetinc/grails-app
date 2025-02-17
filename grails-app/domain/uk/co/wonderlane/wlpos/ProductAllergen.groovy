package uk.co.wonderlane.wlpos

import org.hibernate.Session
import org.hibernate.Transaction

public class ProductAllergen implements Serializable {

    int productId
    int allergenId

    static mapping = {
        autowire true
        table "productallergen"
        version false

        id composite: ['productId', 'allergenId']
        productId column: "productId"
        allergenId column: "allergenId"
    }

    static constraints = {

    }

    static List<Integer> getExistingProductAllergens(Integer productId) {
        List<Integer> allergenIds = new ArrayList<>()
        findAllByProductId(productId)?.forEach {allergenIds.add(it.allergenId) }
        return allergenIds
    }
}
