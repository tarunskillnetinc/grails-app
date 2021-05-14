package uk.co.wonderlane.wlpos.supplier

import grails.databinding.BindingFormat
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus

class SymbolGroupSubscription {

    int id
    int retailerId
    int storeId
    SymbolGroup symbolGroup
    boolean active
    String storeIdentifier
    String organisationIdentifier
    String username
    String password
    @BindingFormat('dd/MM/yyyy HH:mm:ss')
    DateTime lastProductDownload
    @BindingFormat('dd/MM/yyyy HH:mm:ss')
    DateTime lastPromotionDownload
    String additionalPassword
    SymbolGroupSubscriptionStatus status
    String error
    @BindingFormat('dd/MM/yyyy HH:mm:ss')
    DateTime updateDate

    static mapping = {
        table "symbolgroupsubscription"
        version false

        retailerId column: "retailerId"
        storeId column: "storeId"
        symbolGroup column: "symbolGroupId"
        active column: "active"
        storeIdentifier column: "storeIdentifier"
        organisationIdentifier column: "organisationIdentifier"
        username column: "username"
        password column: "password"
        lastProductDownload column: "lastProductDownload", sqlType: "datetime"
        lastPromotionDownload column: "lastPromotionDownload", sqlType: "datetime"
        additionalPassword column: "additionalPassword"
        status column: "status"
        error column: "error"
        updateDate column: "updateDate", sqlType: "datetime"
    }

    static constraints = {
        storeIdentifier nullable: true, maxSize: 50
        organisationIdentifier nullable: true, maxSize: 50
        username nullable: true, maxSize: 50
        password nullable: true, maxSize: 50
        lastProductDownload nullable: true
        lastPromotionDownload nullable: true
        additionalPassword nullable: true, maxSize: 50
        error nullable: true, maxSize: 250
        updateDate nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.supplier.SymbolGroupSubscription getSymbolGroupSubscription() {
        uk.co.wonderlane.wlpos.entities.supplier.SymbolGroupSubscription symbolGroupSubscription = new uk.co.wonderlane.wlpos.entities.supplier.SymbolGroupSubscription()

        symbolGroupSubscription.setId(id)
        symbolGroupSubscription.setRetailerId(retailerId)
        symbolGroupSubscription.setStoreId(storeId)
        symbolGroupSubscription.setSymbolGroupId(symbolGroup?.getId())
        symbolGroupSubscription.setActive(active)
        symbolGroupSubscription.setStoreIdentifier(storeIdentifier)
        symbolGroupSubscription.setOrganisationIdentifier(organisationIdentifier)
        symbolGroupSubscription.setUsername(username)
        symbolGroupSubscription.setPassword(password)
        symbolGroupSubscription.setLastProductDownload(lastProductDownload)
        symbolGroupSubscription.setLastPromotionDownload(lastPromotionDownload)
        symbolGroupSubscription.setAdditionalPassword(additionalPassword)
        symbolGroupSubscription.setStatus(status)
        symbolGroupSubscription.setError(error)
        symbolGroupSubscription.setUpdateDate(updateDate)

        return symbolGroupSubscription
    }
}