package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.apache.pdfbox.pdmodel.PDDocument
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.PrintReceiptOption
import uk.co.wonderlane.wlpos.enums.ProductHistoryType
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType
import uk.co.wonderlane.wlpos.labelling.LabelTemplate

class ShelfEdgeLabelControllerSpec extends Specification implements ControllerUnitTest<ShelfEdgeLabelController>, DataTest{

    def setup() {}

    def cleanup() {}

    Class<?>[] getDomainClassesToMock(){
        return [ButtonGrid, Button, ProductList] as Class[]
    }

    //------------------Calling Index Action -------------//

    def 'Test calling index action successfully '() {

        given:

        when: 'The index action is executed'
        controller.index()

        then: 'Successfully render index model'
        view == "/shelfEdgeLabel/index.gsp"

    }

    //------------------Calling Get AdHoc Batches Action -------------//

    def 'Test calling Get AdHoc Batches action successfully '() {

        given:

        ProductList mockProductList = getMockProductList()
        List<LabelTemplate> mockLabelTemplateList = getMockLabelTemplateList()

        controller.productListService = Stub(ProductListService){
            getAdHocBatches() >> mockProductList
        }

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            getLabelTemplates(_) >> mockLabelTemplateList
        }

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        views['/shelfEdgeLabel/_adHocResults.gsp'] = "test"

        when: 'The ajaxGetAdHocBatches action is executed'
        controller.ajaxGetAdHocBatches()

        then: 'Successfully render model with response objects'
        assert controller.response.text == 'test'
        assert model.productLists
        assert model.labelTemplates
        assert model.deletableStatuses == [ProductListStatus.PARTIALLY_COMPLETE]

    }

    //------------------Calling Get Scheduled Batches Action -------------//

    def 'Test get scheduled batches action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        views['/shelfEdgeLabel/_scheduledResults.gsp'] = "test"

        List<LabelTemplate> mockLabelTemplateList = getMockLabelTemplateList()

        controller.productListService = Stub(ProductListService){
            getScheduledBatches(_) >> getScheduledBatchedMap()
        }

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            getLabelTemplates(_, _) >> mockLabelTemplateList
        }

        when: 'The ajaxGetScheduledBatches action is executed'
        controller.ajaxGetScheduledBatches()

        then: 'Successfully render model with response objects'
        assert controller.response.text == 'test'
        assert model.batchesToBePrinted
        assert model.batchesToBeConfirmed
        assert model.labelTemplates
        assert model.batchesToBePrinted.size() == 1
        assert model.batchesToBeConfirmed.size() == 1
        assert model.labelTemplates.size() == 1
    }

    //------------------Calling Generate Ad Hoc Pdf Action -------------//

    def 'Test calling generate ad hoc Pdf action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String productListId = "1"
        String labelTemplateId = "1"
        String printProcess = "SHELF_EDGE_LABEL_SINGLE"
        String printType = "PDF"

        ProductList mockProductList = getMockProductList()

        LabelTemplate mockLabelTemplate = getLabelTemplate()

        StoreSettings mockStoreSettings = getStoreSetting("Store dummy Name")
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockDomain(StoreSettings, [mockStoreSettings])
        mockStoreSettings.save(flush: true, failOnError: true)

        controller.productListService = Stub(ProductListService){
            getProductList(1) >> mockProductList
        }

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            getLabelTemplate(1) >> mockLabelTemplate
            generatePdf(mockProductList, mockLabelTemplate, _, _, mockStoreSettings) >> getDummyDocumentByte()
        }

        when: 'The ajaxGenerateAdHocPdf action is executed'
        params["productListId"] = productListId
        params["labelTemplateId"] = labelTemplateId
        params["printProcess"] = printProcess
        params["printType"] = printType
        controller.ajaxGenerateAdHocPdf()

        then: 'Successfully assert response'
        assert response.contentLength ==  getDummyDocumentByte().size()
        assert response.characterEncoding ==  "UTF-8"
        assert response.contentType ==  "application/pdf"

    }

    //------------------Calling Generate Scheduled Pdf Action -------------//

    def 'Test calling generate scheduled Pdf action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String productListId = "1"
        String labelTemplateId = "1"
        String printProcess = passingPrintProcess
        String printType = passingPrintType
        String effectiveDate = passingEffectiveDate

        ProductList mockProductList = getMockProductList()

        LabelTemplate mockLabelTemplate = passingLabelTemplate

        StoreSettings mockStoreSettings = getStoreSetting("Store dummy Name")
        if (mockStoreSettings != null){
            mockStoreSettings.springSecurityService = controller.springSecurityService
            mockDomain(StoreSettings, [mockStoreSettings])
            mockStoreSettings.save(flush: true, failOnError: true)
        }

        controller.productListService = Stub(ProductListService){
            getProductList(1) >> mockProductList
        }

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            getLabelTemplate(1) >> mockLabelTemplate
            generatePdf(_, _, _, _, _) >> getDummyDocumentByte()
        }

        when: 'The ajaxGenerateScheduledPdf action is executed'
        params["productListId"] = productListId
        params["labelTemplateId"] = labelTemplateId
        params["printProcess"] = printProcess
        params["printType"] = printType
        params["effectiveDate"] = effectiveDate
        controller.ajaxGenerateScheduledPdf()

        then: 'Successfully assert successful and failures'
        if (!passingEffectiveDate || !passingLabelTemplate || !passingPrintType || !passingPrintProcess) {
            assert response.status == 400
        }else {
            assert response.contentLength ==  getDummyDocumentByte().size()
            assert response.characterEncoding ==  "UTF-8"
            assert response.contentType ==  "application/pdf"
        }

        where: 'Pass following input parameters'
        passingLabelTemplate || passingPrintProcess      || passingPrintType || passingEffectiveDate
        null                 || null                     || null             || null
        getLabelTemplate()   || "SHELF_EDGE_LABEL_SINGLE"|| "PDF"            || null
        getLabelTemplate()   || "SHELF_EDGE_LABEL_SINGLE"|| null             || "02/12/2022"
        getLabelTemplate()   || null                     || "PDF"            || "02/12/2022"
        null                 || "SHELF_EDGE_LABEL_SINGLE"|| "PDF"            || "02/12/2022"
        getLabelTemplate()   || "SHELF_EDGE_LABEL_SINGLE"|| "PDF"            || "02/12/2022"
        getLabelTemplate()   || "SHELF_EDGE_LABEL_SINGLE"|| "PDF"            || "02/12/2022"

    }

    //------------------Calling Confirm Ad Hoc Batch Print Successful Action -------------//

    def 'Test confirm ad hoc batch Print action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String productListId = "1"

        ProductList mockProductList = getMockProductList()
        mockProductList.setId(Integer.parseInt(productListId))
        mockProductList.setRetailerId(9)
        mockProductList.setStore(getStoreSetting("Some Store Name"))
        mockProductList.setStatus(ProductListStatus.IN_PROGRESS)

        mockProductList.save(flush: true, failOnError: true)

        controller.productListService = Stub(ProductListService){
            getProductList(1) >> mockProductList
        }

        when: 'The ajaxConfirmAdHocBatchPrintSuccessful action is executed'
        params["productListId"] =  productListId
        controller.ajaxConfirmAdHocBatchPrintSuccessful()

        then: 'Successfully assert response status'
        assert response.status == 204
    }

    //------------------Calling Confirm Scheduled Batch Print Successful Action -------------//

    def 'Test confirm scheduled batch print action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String effectiveDate = passingEffectiveDate

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            setProductHistoryPrintStatus(_,_) >> null
        }

        when: 'The ajaxConfirmScheduledBatchPrintSuccessful action is executed'
        params["effectiveDate"] =  effectiveDate
        controller.ajaxConfirmScheduledBatchPrintSuccessful()

        then: 'Successfully assert for response status'
        assert response.status == 204

        where: 'Pass following input parameters'
        passingEffectiveDate ||_
        "02/12/2022"         ||_
        null                 ||_
    }

    //------------------Calling Delete Product List Action -------------//

    def 'Test delete product list action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String productListId = "1"

        ProductList mockProductList = getMockProductList()
        mockProductList.setId(Integer.parseInt(productListId))
        mockProductList.setRetailerId(9)
        mockProductList.setStore(getStoreSetting("Some Store Name"))
        mockProductList.setStatus(ProductListStatus.IN_PROGRESS)

        mockProductList.save(flush: true, failOnError: true)

        ProductList productListBeforeDelete = ProductList.findById(1)

        controller.productListService = Stub(ProductListService){
            getProductList(1) >> mockProductList
            deleteProductList(mockProductList) >> mockProductList.delete()
        }

        when: 'The ajaxDeleteProductList action is executed'
        params["productListId"] =  productListId
        controller.ajaxDeleteProductList()

        ProductList productListAfterDelete = ProductList.findById(1)

        then: 'Successfully assert for response status and before delete and after delete object'
        assert response.status == 204
        assert productListBeforeDelete != null
        assert productListAfterDelete == null
    }

    //------------------Calling Confirm Apply Changes To Batch Action -------------//

    def 'Test confirm apply changes to batch action successfully '() {

        given:

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9)
                put("storeId", 234)
                put("storeNumber", 100)
            }}
        }

        String effectiveDate = "02/12/2022"
        String batchType = "PDF"

        controller.productListService = Stub(ProductListService){
            getScheduledBatches(_) >> getScheduledBatchedMap()
        }

        controller.shelfEdgeLabelService = Stub(ShelfEdgeLabelService){
            setProductHistoryPrintStatus(_, _) >> null
        }

        controller.productService = Stub(ProductService){
            syncProductUpdatesToSingleStore(_, _) >> null
        }

        when: 'The ajaxConfirmApplyChangesToBatch action is executed'
        params["effectiveDate"] =  effectiveDate
        params["batchType"] =  batchType
        controller.ajaxConfirmApplyChangesToBatch()

        then: 'Successfully assert response status'
        assert response.status == 204
    }

    def getScheduledBatchedMap(){
        def results = [toBePrinted: [], toBeConfirmed: [], totalCount: 0]
        results.toBeConfirmed.add(getToBePrintedHashMap())
        results.toBePrinted.add(getToBePrintedHashMap())
        return results
    }


    def getToBePrintedHashMap(){
        def result = [:]
        result.id =  1
        result.productId = 1
        result.storeId = 234
        result.updateDate = DateTime.now(DateTimeZone.UTC)
        result.effectiveDate = DateTime.now(DateTimeZone.UTC)
        result.type = ProductHistoryType.PRICE
        result.priceBandId = 1
        result.field = "Price"
        result.fromValue = "1"
        result.toValue = "100"
        result.userId = 1
        result.usersName = "Wonderlane"
        result.productVariantId =  1
        return result
    }


    byte[] getDummyDocumentByte(){
        return "DummyDoc".bytes
    }

    ProductList getMockProductList(){
        ProductList productList = new ProductList();
        productList.setUserId("1")
        productList.setType(ProductListType.ORDER)
        productList.setStatus(ProductListStatus.COMPLETE)
        productList.setStockAdjustedOnCompletion(true)
        return productList
    }

    List<LabelTemplate> getMockLabelTemplateList(){
        List<LabelTemplate> labelTemplateList = new ArrayList<>();
        LabelTemplate labelTemplate = getLabelTemplate()
        labelTemplate.setStationeryCode("ABC")
        labelTemplate.setName("Dummy label template")
        labelTemplateList.add(labelTemplateList)
        return labelTemplateList;
    }

    LabelTemplate getLabelTemplate(){
        LabelTemplate labelTemplate = new LabelTemplate()
        labelTemplate.setId(1)
        labelTemplate.setRetailerId(9)
        labelTemplate.setMarginLeft(new BigDecimal(1))
        labelTemplate.setMarginTop(new BigDecimal(1))
        labelTemplate.setLabelHeight(new BigDecimal(1))
        labelTemplate.setLabelWidth(new BigDecimal(1))
        labelTemplate.setMarginBetweenColumns(new BigDecimal(1))
        labelTemplate.setMarginBetweenRows(new BigDecimal(1))
        labelTemplate.setRows(1)
        labelTemplate.setColumns(1)
        return labelTemplate
    }

    StoreSettings getStoreSetting(String storeName){
        StoreSettings storeSettings = new StoreSettings()
        storeSettings.setId(234)
        storeSettings.setRetailerId(9)
        storeSettings.setPrintReceiptOption(PrintReceiptOption.ALWAYS_PRINT)
        storeSettings.setPriceBand(new PriceBand())
        storeSettings.setRange(new Range())
        storeSettings.setCountIncrement(new BigDecimal(1))
        storeSettings.setSelMarginLeft(new BigDecimal(1))
        storeSettings.setSelMarginTop(new BigDecimal(1))
        storeSettings.setStoreName(storeName)
        storeSettings.setType("Dummy store setting type")
        return storeSettings
    }

    byte[] getDocumentByteArray(){
        PDDocument doc = new PDDocument()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        doc.save(out)
        doc.close()
        return out.toByteArray()
    }
}
