package uk.co.wonderlane.wlpos


import org.apache.commons.io.input.XmlStreamReader

/**
 * Controller for handling physical inventory count exclusion functionality.
 * This controller manages the upload and processing of CSV files containing
 * SKUs that should be excluded from physical inventory counts.
 *
 * The controller provides the following functionality:
 * - File upload validation
 * - CSV file processing
 * - Integration with the PhysicalInventoryExclusionService for business logic
 * - Results display and export
 *
 * @author Tarun Singh
 * @version 1.0
 */
class PhysicalInventoryCountExclusionController {

    def physicalInventoryExclusionService

    def index() { }

    def ajaxCSVCountExclusionUpload() {
        def file = request.getFile('file')
        String importError
        def results = [:]

        // Validate file extension
        if (!file.originalFilename.toLowerCase().endsWith('.csv')) {
            importError = 'Please upload a valid CSV file'
        } else {
            byte[] fileBytes = file.getBytes()
            String detectedEncoding

            try {
                XmlStreamReader xmlStreamReader = new XmlStreamReader(new ByteArrayInputStream(fileBytes))
                detectedEncoding = xmlStreamReader.getEncoding()

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
                        // Process the CSV file
                        def inputStream = file.inputStream
                        importError = validateImport(file)
                        if (!importError) {
                            List<String> skus = []

                            inputStream.eachLine { line ->
                                line.split(',')*.trim().each { sku ->
                                    if (sku) {
                                        skus << sku
                                    }
                                }
                            }

                            results = physicalInventoryExclusionService.processExclusionList(skus)
                            session.results = results
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
                importError = "Error occurred during processing of file"
            }
        }

        render(template: "importResults", model: [successful: !importError, importError: importError, results: results])
    }

    private static String validateImport(file) {
        if (file == null || file.empty) {
            return "No file selected or file is empty"
        }
        if (file.size == 0) {
            return "File is empty"
        }
        return null
    }

    def exportResults() {
        def fileName = "ExclusionExport-" + new Date().format("yyyy_MM_dd_HH_mm_ss") + ".csv"
        response.setHeader("Content-Disposition", "attachment; filename=${fileName}")
        response.setHeader("Content-Type", "text/csv")
        def results = session.results

        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append(results.invalidSkus.join(','))

        render stringBuilder.toString()
    }
}