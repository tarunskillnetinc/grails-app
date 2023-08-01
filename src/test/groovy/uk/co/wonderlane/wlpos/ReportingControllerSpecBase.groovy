package uk.co.wonderlane.wlpos

import spock.lang.Ignore
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.supplier.Supplier

@Ignore
class ReportingControllerSpecBase extends Specification {
    Store getMockStoreSettings(int id, int retailerId, int storeNumber) {
        return getMockStoreSettings(id, retailerId, storeNumber, StoreType.STORE)
    }

    Store getMockStoreSettings(int id, int retailerId, int storeNumber, StoreType type) {
        Store storeSettings = new Store()

        storeSettings.setId(id)
        storeSettings.setRetailerId(retailerId)
        storeSettings.setStoreId(storeNumber)
        storeSettings.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings.setPriceBand(new PriceBand(retailerId: retailerId, description: "Dummy description"))
        storeSettings.setRange(new Range(retailerId: retailerId, description: "Dummy description"))
        storeSettings.setCountIncrement(BigDecimal.valueOf(0.5))
        storeSettings.setType(type.name())

        return storeSettings
    }

    Supplier getMockSupplier(int id, int retailerId, int storeId) {
        Supplier supplier = new Supplier()

        supplier.setId(id)
        supplier.setName("TEST_SUPPLIER")
        supplier.setRetailerId(retailerId)
        supplier.setStoreId(storeId)

        return supplier
    }
}
