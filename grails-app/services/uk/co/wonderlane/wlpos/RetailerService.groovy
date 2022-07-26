package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class RetailerService implements RetailerProvider {

    @Override
    Retailer getRetailer(int retailerId) {
        return Retailer.findById(retailerId)
    }
}