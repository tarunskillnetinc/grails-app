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
 * @version 2.0
 */
class PhysicalInventoryCountExclusionController {

    def physicalInventoryExclusionService

    def index() { }

    def ajaxCSVCountExclusionUpload() {
        def file = request.getFile('file')
        String importError
        def skus = []

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
                        importError = 'Could not import file please ensure the file is using UTF-8 without BOM for encoding'
                    } else {
                        // Parse the CSV file
                        def inputStream = file.inputStream
                        inputStream.eachLine { line ->
                            line.split(',')*.trim().each { sku ->
                                if (sku) {
                                    skus << sku
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace()
                importError = "Error occurred during processing of file"
            }
        }
        session.skus = skus
        // Render the importResults template for preview
        render(template: "importResults", model: [successful: !importError, importError: importError, skus: skus, preview: true])
    }

    def confirmImport() {
        try {
            def skus = session.skus
            if (!skus || skus.isEmpty()) {
                render(template: "importResults", model: [successful: false, importError: "No SKUs available for import", preview: false])
                return
            }
            def results = physicalInventoryExclusionService.processExclusionList(skus)
            session.results = results

            render(template: "importResults", model: [successful: true, results: results, preview: false])
        } catch (Exception e) {
            render(status: 500, text: "An error occurred: ${e.message}")
        }
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