package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface StoreNumberValidator {

    Integer getStoreId(int retailerId, Integer storeNumber)
}