package uk.co.wonderlane.wlpos

import grails.testing.web.controllers.ControllerUnitTest
import io.micronaut.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class HardwareImportControllerSpec extends Specification implements ControllerUnitTest<HardwareImportController> {

    def setup() {
    }

    def cleanup() {
    }

    ArrayList<TillStock> hardwareList = new ArrayList<>()

    void 'hardwareUploadWorksCorrectly'() {
        when:
        controller.hardwareService = Stub(HardwareService) {
            getHardwareBySerialNumber("serial") >> hardwareList
        }

        MockMultipartFile testFile = new MockMultipartFile('file', 'test.csv', 'text/csv', createTestFile() as byte[])

        request.addFile(testFile)
        controller.ajaxCSVHardwareUpload()

        then:
        ArrayList<CSVUploadHardware> rows = controller.session.ROWS
        response.status == org.springframework.http.HttpStatus.OK.value()
        rows.size() == 1
        rows[0].serialNumber == "serial"
        rows[0].validRow
    }

    void 'exportResults correctly exports to CSV the import result'() {
        given:
        controller.hardwareService = Stub(HardwareService) {
            getHardwareBySerialNumber("serial") >> hardwareList
        }

        MockMultipartFile testFile = new MockMultipartFile('file', 'test.csv', 'text/csv', createTestFile() as byte[])

        request.addFile(testFile)
        controller.ajaxCSVHardwareUpload()
        getWebRequest().response

        when:

        response.reset()
        controller.exportResults()

        then:
        String result = response.contentAsString
        assert result != null
        assert result == "SerialNumber, Model, Valid Row\nserial,model,true\n"
    }


    String createTestFile() {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("serial")
        stringBuilder.append(",")
        stringBuilder.append("model")
        stringBuilder.append("\n")
        return stringBuilder.toString()
    }
}
