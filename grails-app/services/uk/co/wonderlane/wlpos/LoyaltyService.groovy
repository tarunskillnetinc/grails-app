package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal

@Transactional
class LoyaltyService extends MySqlDal {

    def springSecurityService

    protected LoyaltyService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSegment(String searchTerm, String searchBy, int max, int offset, String sortColumn, String sortOrder) {
        def totalCount = Segment.createCriteria().get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    like("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
            projections {
                countDistinct("id")
            }
        }

        def segments = Segment.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    ilike("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
        }

        return [totalCount: totalCount, segments: segments]
    }


    def getLoyaltyOffers(String searchTerm, String searchBy, int max, int offset, String sortColumn, String sortOrder){
        def totalCount = LoyaltyOffer.createCriteria().get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    like("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
            projections {
                countDistinct("id")
            }
        }

        def offers = LoyaltyOffer.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    ilike("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
        }

        return [totalCount: totalCount, offers: offers]
    }

    List<Segment> getLoyaltySegmentForRetailer(int retailerId){
        List<Segment> segmentList = new ArrayList<>();
        segmentList = Segment.withCriteria {
            eq ("retailerId", retailerId)
        }
        return segmentList;
    }
}
