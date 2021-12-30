package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic

@CompileStatic
interface StoreNumberValidator {

    int getStoreId(int retailerId, int storeNumber)
}