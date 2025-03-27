package uk.co.wonderlane.wlpos

import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class PhysicalInventoryCountExclusionControllerSpec extends Specification implements ControllerUnitTest<PhysicalInventoryCountExclusionController> {

    def setup() {
    }

    def cleanup() {
    }

    void "ajaxCSVCountExclusionUpload handles valid UTF-8 CSV file correctly"() {
        given:
        controller.physicalInventoryExclusionService = Stub(PhysicalInventoryExclusionService)

        MockMultipartFile testFile = new MockMultipartFile(
            'file', 
            'test.csv', 
            'text/csv', 
            createValidTestFile() as byte[]
        )

        when:
        request.addFile(testFile)
        controller.ajaxCSVCountExclusionUpload()

        then:
        response.status == 200
        response.text.contains("123123")
        response.text.contains("456456")
    }

    void "ajaxCSVCountExclusionUpload rejects UTF-8 BOM encoded file"() {
        given:
        MockMultipartFile testFile = new MockMultipartFile(
                'file',
                'test.csv',
                'text/csv',
                createUTF8BOMTestFile()
        )

        when:
        request.addFile(testFile)
        controller.ajaxCSVCountExclusionUpload()

        then:
        response.status == 200
        response.text.contains("Could not import file please ensure the file is using UTF-8 without BOM for encoding")
    }

    void "confirmImport processes SKUs correctly"() {
        given:
        controller.physicalInventoryExclusionService = Stub(PhysicalInventoryExclusionService) {
            processExclusionList(_) >> [success: true]
        }
        session.skus = ["123123", "456456"]

        when:
        controller.confirmImport()

        then:
        response.status == 200
        response.contentType == "text/html;charset=UTF-8" // Verify content type
        model.successful == true // Verify the model data
        model.results.success == true // Verify the results in the model
        model.preview == false // Verify the preview flag
    }

    void "exportResults generates correct CSV output"() {
        given:
        session.results = [invalidSkus: ["789789", "101101"]]

        when:
        controller.exportResults()

        then:
        response.status == 200
        response.contentType == "text/csv"
        response.getHeader("Content-Disposition").contains("ExclusionExport-")
        response.text == "789789,101101"
    }

    private String createValidTestFile() {
        return "123123\n456456\n"
    }

    private byte[] createNonUTF8TestFile() {
        return "123123\n456456\n".getBytes("ISO-8859-1")
    }

    private byte[] createUTF8BOMTestFile() {
        def bom = [0xEF, 0xBB, 0xBF] as byte[] // UTF-8 BOM
        def content = "SKU123\nSKU456\n".getBytes("UTF-8") // CSV content
        def outputStream = new ByteArrayOutputStream()
        outputStream.write(bom) // Write BOM
        outputStream.write(content) // Write content
        return outputStream.toByteArray() // Return combined byte array
    }
}