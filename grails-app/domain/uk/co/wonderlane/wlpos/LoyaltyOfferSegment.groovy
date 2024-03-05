package uk.co.wonderlane.wlpos

class LoyaltyOfferSegment implements Serializable {

    static belongsTo = [loyaltyOffer: LoyaltyOffer]

    int id
    int offerId
    int segmentId
    int count = 0
    boolean delete = false
    boolean isUpdated = false

    static transients = ['delete', 'isUpdated']

    static constraints = {}

    static mapping = {
        autowire true
        datasources (["loyalty"])

        table "offer_segment"
        version false

        loyaltyOffer column: "offer_id", insertable: false, updateable: false
        id column: "id", sqlType: "int"
        segmentId column: "segment_id"
        count column: "count"
    }
}
