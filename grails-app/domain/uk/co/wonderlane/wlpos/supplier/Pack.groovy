package uk.co.wonderlane.wlpos.supplier

import org.grails.web.util.WebUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.Barcode
import uk.co.wonderlane.wlpos.ProductVariant
import uk.co.wonderlane.wlpos.entities.wlim.PackLine
import uk.co.wonderlane.wlpos.enums.PackStatus

import java.util.stream.Collectors

class Pack implements Serializable {
    def springSecurityService


    static belongsTo = [ productVariant: ProductVariant ]

    int id
    Supplier supplier
    BigDecimal quantity
    BigDecimal price
    String orderCode
    BigDecimal recommendedRetailPrice
    DateTime effectiveDate
    DateTime effectiveEndDate
    PackStatus status
    Integer maximumOrderQuantity
    boolean allowSubstitutes
    boolean priceMarked
    boolean primaryCase
    DateTime updateDatetime
    Collection<Barcode> barcodez = new ArrayList<>()

    static transients = ['barcodez']

    static mapping = {
        table "pack"
        version false

        productVariant column: "productVariantId"
        supplier column: "supplierId"
        quantity column: "quantity"
        price column: "price"
        orderCode column: "orderCode"
        recommendedRetailPrice column: "recommendedRetailPrice"
        effectiveDate column: "effectiveDate"
        effectiveEndDate column: "effectiveEndDate"
        status column: "status"
        maximumOrderQuantity column: "maximumOrderQuantity"
        allowSubstitutes column: "allowSubstitutes"
        priceMarked column: "priceMarked"
        updateDatetime column: "updateDatetime"
        primaryCase column: "primaryCase"
    }

    int getQuantity(List<PackLine> packLines){
        PackLine packLine = packLines?.find {it?.orderCode == this?.orderCode}
        if (packLine != null){
            return packLine.quantity
        }
        return 0;
    }

    // pack is active if the current datetime is after the pack effectiveDate and before the pack effectiveEndDate
    boolean isActive() {
        DateTime now = DateTime.now(DateTimeZone.UTC)
        return !supplier.deleted && (effectiveDate == null || now > effectiveDate) && (effectiveEndDate == null || now < effectiveEndDate)
    }

    static constraints = {
        productVariant nullable: true
        supplier nullable: false, blank: false
        quantity nullable: false, blank: false, min: 0.00 as BigDecimal, max: 2147483647.000 as BigDecimal
        price nullable: false, blank: false, min: 0.00 as BigDecimal, max: 9999.99 as BigDecimal, scale: 2
        orderCode nullable: true, size: 1..20
        recommendedRetailPrice nullable: true, max: 9999.99 as BigDecimal, scale: 2
        effectiveDate nullable: true
        effectiveEndDate nullable: true
        status nullable: false
        maximumOrderQuantity nullable: true, min: 0 as Integer, max: 99999 as Integer
        allowSubstitutes nullable: false
        priceMarked nullable: false
        primaryCase nullable: false
        updateDatetime nullable: false
        barcodez bindable: true
    }

    public uk.co.wonderlane.wlpos.entities.supplier.Pack getPack() {
        uk.co.wonderlane.wlpos.entities.supplier.Pack pack = new uk.co.wonderlane.wlpos.entities.supplier.Pack()

        pack.setId(id)
        pack.setProductVariantId(productVariant?.id)
        pack.setSupplierId(supplier?.id)
        pack.setQuantity(quantity)
        pack.setPrice(price)
        pack.setOrderCode(orderCode)
        pack.setRecommendedRetailPrice(recommendedRetailPrice)
        pack.setEffectiveDate(effectiveDate)
        pack.setEffectiveEndDate(effectiveEndDate)
        pack.setStatus(status)
        pack.setMaximumOrderQuantity(maximumOrderQuantity != null ? maximumOrderQuantity : 0)
        pack.setAllowSubstitutes(allowSubstitutes)
        pack.setPriceMarked(priceMarked)
        pack.setPrimaryCase(primaryCase)
        pack.setUpdateDate(updateDatetime)

        getBarcodes()?.each {
            pack.getBarcodes().add(it.barcode)
        }

        return pack
    }
    public DateTime getSessionEffectiveDate() {
        def sessionEffectiveDate = WebUtils.retrieveGrailsWebRequest().session.getAttribute("effectiveDate")

        return sessionEffectiveDate != null && sessionEffectiveDate.size() > 0 ? sessionEffectiveDate[1] : DateTime.now(DateTimeZone.UTC)
    }

    public List<Barcode> getBarcodes() {
        // Load all barcodes based on sku.
        def barcodesOnPackId = Barcode.findAllByPackIdAndRetailerIdAndEffectiveDateLessThanEquals(
                id, 3, getSessionEffectiveDate(), [sort: "effectiveDate", order: "desc"]) // TODO change this retailer ID just here for testing

        // Declare list to populate displaying barcodes.
        def barcodesToShow = new ArrayList<Barcode>()

        // Sort the list by id in descending order in case if barcode deleted in same date as created list might not be in
        def sortedBarcodes = barcodesOnPackId.stream()
                .sorted((b1, b2) -> {
                    int compareEffectiveDate = b2.getEffectiveDate().compareTo(b1.getEffectiveDate())
                    if (compareEffectiveDate != 0) {
                        return compareEffectiveDate
                    }
                    return b2.getId().compareTo(b1.getId())
                })
                .collect(Collectors.toList())

        // Group by barcode, ensuring each group is sorted by id in descending order
        // Collect into linkedHashMap to ensure the map maintains insertion order
        def groupedByBarcodeValue = sortedBarcodes.stream().collect(Collectors.groupingBy({it.barcode},
                {-> new LinkedHashMap<>()}, Collectors.toList()))

        // They're already sorted in effective date, so if the first is valid then display it, if not then it's deleted and shouldn't be displayed.
        groupedByBarcodeValue?.each {
            if (it.value?.first()?.recordStatus == ('C' as char)) {
                barcodesToShow.add(it.value?.first())
            }
        }

        return barcodesToShow
    }

    public List<Barcode> getAllBarcodes() {
        return Barcode.findAllByPackIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }
}