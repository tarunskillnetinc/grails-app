package uk.co.wonderlane.wlpos

import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.ProcessType
import uk.co.wonderlane.wlpos.enums.TenderType

class SaveButtonFormCommand implements Validateable {

    int buttonGridId
    int id
    ButtonType type
    int row
    int column
    String description
    BigDecimal amount
    Integer quantity
    Long sku
    Integer subPageId
    ProcessType process
    TenderType tenderType

    String bgColour
    String textColour
    boolean imageDisplay
    boolean textDisplay

    Integer retailerId
    Integer storeId
    Integer btnStoreId
    Integer overrideId

    boolean removeImage
    MultipartFile image
    boolean manual
    boolean exact

    static constraints = {
        // The following constraints are covered by Button.groovy, duplicating them here would duplicate the error message
        buttonGridId nullable: true
        id nullable: true
        type nullable: true
        row  nullable: true
        column nullable: true
        description nullable: true
        amount nullable: true
        quantity nullable: true
        sku nullable: true
        subPageId nullable: true
        process nullable: true
        tenderType nullable: true
        bgColour nullable: true
        textColour nullable: true
        imageDisplay nullable: true
        textDisplay nullable: true
        retailerId nullable: true
        storeId nullable: true
        btnStoreId nullable: true
        overrideId nullable: true
        removeImage nullable: true
        manual nullable: true
        exact nullable: true

        // image validator is used instead of maxSize because tested to not be functional
        image nullable: true, validator: { val, obj ->
            if (val == null) {
                return true
            }

            if (val.size > 1048576 /* 1MB */) { // max size should match number value in edit.gsp inside #image on change
                return ['button.error.fileSize.message']
            }
        }
    }
}