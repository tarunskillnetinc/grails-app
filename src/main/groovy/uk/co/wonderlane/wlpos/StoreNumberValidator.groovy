package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface StoreNumberValidator {

    boolean isValidStoreNumber(int retailerId, int storeId)
}