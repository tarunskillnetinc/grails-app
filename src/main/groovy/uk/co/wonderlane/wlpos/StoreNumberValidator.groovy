package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface StoreNumberValidator {

    Store getStore(int retailerId, Integer storeNumber)
}