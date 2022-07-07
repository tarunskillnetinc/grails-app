package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface StoreNumberValidator {

    StoreSettings getStore(int retailerId, Integer storeNumber)
}