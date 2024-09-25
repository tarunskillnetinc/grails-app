package uk.co.wonderlane.wlpos.loyalty

class OfferSegment implements Serializable {

    static belongsTo = [loyaltyOffer: Offer]

    int id
    int offerId
    int segmentId
    int count = 0
    boolean delete = false

    static transients = ['delete']

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

    public uk.co.wonderlane.wlpos.entities.LoyaltyOfferSegment getLoyaltyOfferSegments(){
        uk.co.wonderlane.wlpos.entities.LoyaltyOfferSegment loyaltyOfferSegment = new uk.co.wonderlane.wlpos.entities.LoyaltyOfferSegment();
        loyaltyOfferSegment.setId(id)
        loyaltyOfferSegment.setOfferId(offerId)
        loyaltyOfferSegment.setSegmentId(segmentId)
        loyaltyOfferSegment.setCount(count)
        return loyaltyOfferSegment
    }
}
