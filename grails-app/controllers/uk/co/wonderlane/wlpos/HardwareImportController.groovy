package uk.co.wonderlane.wlpos

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBeanBuilder
import grails.converters.JSON
import grails.validation.ValidationErrors
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError

class HardwareImportController {
    def hardwareService

    def index() { }

    def ajaxCSVHardwareUpload() {
        def file = request.getFile('file')
        def inputStream = file.inputStream
        List<String> errors = new ArrayList<>()

        try {
            List<CSVUploadHardware> rows = new CsvToBeanBuilder(inputStream.newReader())
                .withType(CSVUploadHardware)
                .build()
                .parse()

            validateImport(file, errors)

            // No validation errors, can continue with the import preparation
            if(errors.isEmpty()) {
                rows.forEach({CSVUploadHardware row ->
                    if (hardwareService.getHardwareBySerialNumber(row.serialNumber)) {
                        row.validRow = false;
                    }
                })
            }

            session.ROWS = rows

//            rows.forEach({CSVUploadHardware row ->
//                if(row.get)
////                Hardware hardware = new Hardware()
////                hardware.setSerialNumber(row.serialNumber)
////                hardware.setModel(row.model)
////                hardware.setDateUpdated(now)
////                hardwareService.saveHardware(hardware)
//            })
        } catch (Exception e) {
            e.printStackTrace()
            errors.add("Error occurred during processing of file")
        }

        render([status: errors.isEmpty() ? "SUCCESS" : "FAILED", errors: errors] as JSON)
    }

    private void validateImport(file, ArrayList<String> errors) {
        def anyEmptyRows = rows.any {  !it.serialNumber?.trim() || !it.model?.trim()  }

        if (anyEmptyRows) {
            log.error("Could not import hardware file due to failing validation - $file.filename")
            errors.add("Could not import file as one or more rows did not have a serial number or model. Please verify the data and try again.")
            return;
        }
    }
}

class CSVUploadHardware {
    @CsvBindByPosition(position = 0)
    String serialNumber;

    @CsvBindByPosition(position = 1)
    String model

    boolean validRow = true
}
