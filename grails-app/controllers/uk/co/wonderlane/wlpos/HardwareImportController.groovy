package uk.co.wonderlane.wlpos


import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBeanBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class HardwareImportController {

    def hardwareService

    def index() { }

    def ajaxCSVHardwareUpload() {
        def file = request.getFile('file')
        def inputStream = file.inputStream
        String importError
        def validSerialNumbersInFile = []

        try {
            List<CSVUploadHardware> rows = new CsvToBeanBuilder(inputStream.newReader())
                .withType(CSVUploadHardware)
                .build()
                .parse()

            importError = validateImport(file, rows)

            // No validation errors, can continue with the import preparation
            if (!importError) {
                def serialsInStock = hardwareService.getSerialsInStock()

                rows.forEach({ CSVUploadHardware row ->
                    if (validSerialNumbersInFile.contains(row.serialNumber)) { // Check that this serial number has not successfully been added before this in the same import
                        row.validRow = false
                        row.errorRow = "Invalid - Duplicate serial number in file"
                    } else if (row.serialNumber.length() > 50 && row.model.length() > 50) {
                        row.validRow = false
                        row.errorRow = "Invalid - Serial number and model must be less than 50 characters"
                    } else if (row.serialNumber.length() > 50) {
                        row.validRow = false
                        row.errorRow = "Invalid - Serial number must be less than 50 characters"
                    } else if (row.model.length() > 50) {
                        row.validRow = false
                        row.errorRow = "Invalid - Model must be less than 50 characters"
                    } else if (serialsInStock.contains(row.serialNumber?.trim())) {
                        row.validRow = false
                        row.errorRow = "Invalid - Serial number already exists"
                    } else {
                        validSerialNumbersInFile.add(row.serialNumber?.trim())
                    }
                })
            }

            session.ROWS = rows
        } catch (Exception e) {
            e.printStackTrace()
            importError = "Error occurred during processing of file"
        }

        render(template: "importResults", model: [successful: !importError, importError: importError, rows: session.ROWS])
    }

    def exportResults() {
        def fileName = "HardwareExport-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
        response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
        response.setHeader("Content-Type", "text/csv;")

        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("SerialNumber, Model, Valid Row\n")
        var rows = session.ROWS
        rows?.each {
            stringBuilder.append(it.serialNumber)
            stringBuilder.append(",")
            stringBuilder.append(it.model)
            stringBuilder.append(",")
            stringBuilder.append(it.validRow)
            stringBuilder.append("\n")
        }

        render stringBuilder.toString()
    }

    def confirmImport() {
        List<String> errors = new ArrayList<>()
        try {
            var rows = session.ROWS
            rows.forEach({ CSVUploadHardware row ->
                if (row.validRow) {
                    TillStock hardware = new TillStock()
                    hardware.setSerialNumber(row.serialNumber)
                    hardware.setModel(row.model)
                    hardware.setDateUpdated(DateTime.now(DateTimeZone.UTC))

                    hardwareService.saveHardware(hardware)
                }
            })
        } catch (Exception e) {
            e.printStackTrace()
            errors.add("Error occurred during completion of hardware import")
        }

        render(model: [successful: errors.isEmpty(), errors: errors, rows: session.ROWS])
    }

    private String validateImport(file, List<CSVUploadHardware> rows) {
        def anyEmptyRows = rows.any { !it.serialNumber?.trim() || !it.model?.trim() }

        if (anyEmptyRows) {
            log.error("Could not import hardware file due to failing validation - $file.filename")
            return "Could not import file as one or more rows did not have a serial number or model. Please verify the data and try again."
        }

        return null
    }
}

class CSVUploadHardware implements Serializable {
    @CsvBindByPosition(position = 0)
    String serialNumber;

    @CsvBindByPosition(position = 1)
    String model

    boolean validRow = true

    String errorRow = ""
}

class TillStockCommand {
    List<TillStock> tillStocks
}