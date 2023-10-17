package uk.co.wonderlane.wlpos


import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvToBeanBuilder
import org.apache.commons.io.input.XmlStreamReader
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class HardwareImportController {

    def hardwareService

    def index() { }

    def ajaxCSVHardwareUpload() {
        def file = request.getFile('file')
        byte[] fileBytes = file.getBytes()
        String importError

        XmlStreamReader xmlStreamReader = new XmlStreamReader(new ByteArrayInputStream(fileBytes))
        String detectedEncoding = xmlStreamReader.getEncoding()

        // Check if the file is UTF-8 encoded
        if (!detectedEncoding.equals("UTF-8")) {
            importError = 'Could not import file please ensure the file is using UTF-8 for encoding'
        } else {
            // Check if the file is UTF-8 encoded with BOM
            if (fileBytes.length >= 3 &&
                    (fileBytes[0] & 0xFF) == 0xEF &&
                    (fileBytes[1] & 0xFF) == 0xBB &&
                    (fileBytes[2] & 0xFF) == 0xBF) {
                // Byte array is UTF-8 with BOM display error
                importError = 'Could not import file please ensure the file is using UTF-8 without BOM for encoding'
            } else {
                // File is UTF-8 encoded correctly without BOM proceed with upload
                def inputStream = file.inputStream
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
                            if (validSerialNumbersInFile.contains(row.serialNumber?.trim()) || validSerialNumbersInFile.contains(row.serialNumber)) {
                                // Check that this serial number has not successfully been added before this in the same import
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
                            } else if (serialsInStock.contains(row.serialNumber?.trim()) || serialsInStock.contains(row.serialNumber)) {
                                row.validRow = false
                                row.errorRow = "Invalid - Serial number already exists"
                            } else if (!validRegexSerial(row.getSerialNumber())) {
                                row.validRow = false
                                row.errorRow = "Invalid - Serial number cannot contain special characters or spaces"
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
            }
        }
        
        render(template: "importResults", model: [successful: !importError, importError: importError, rows: session.ROWS])
    }

    def exportResults() {
        def fileName = "HardwareExport-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
        response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
        response.setHeader("Content-Type", "text/csv;")

        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Serial Number,Model,Valid to Import\n")
        var rows = session.ROWS
        rows?.each {
            stringBuilder.append(it.serialNumber)
            stringBuilder.append(",")
            stringBuilder.append(it.model)
            stringBuilder.append(",")
            stringBuilder.append(
                    it.validRow ? "Valid"
                            : it.errorRow != null && it.errorRow != "" ? it.errorRow
                            : "Invalid"
            )
            stringBuilder.append("\n")
        }

        render stringBuilder.toString()
    }

    def confirmImport() {
        List<String> errors = new ArrayList<>()

        try {
            var rows = session.ROWS

            def now = DateTime.now(DateTimeZone.UTC)

            def validRows = []

            rows?.each { CSVUploadHardware row ->
                if (row.validRow) {
                    TillStock hardware = new TillStock()
                    hardware.setSerialNumber(row.serialNumber)
                    hardware.setModel(row.model)
                    hardware.setDateUpdated(now)

                    validRows.add(hardware)
                }
            }

            hardwareService.saveHardware(validRows)
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
            return "Could not import file. One or more rows was either empty or did not have a serial number or model. Please verify the data and try again."
        }

        return null
    }

    private static boolean validRegexSerial(String s) {
        return s ==~ /[a-zA-Z0-9\-\\/]+/
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
