package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface RetailerProvider {

    Retailer getRetailer(String username)

}