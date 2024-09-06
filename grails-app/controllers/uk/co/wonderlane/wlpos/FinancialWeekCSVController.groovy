package uk.co.wonderlane.wlpos

import com.opencsv.CSVWriter
import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class FinancialWeekCSVController {
    def springSecurityService
    def financialWeekService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        [financialWeeks: financialWeekService.getAllFinancialWeeks()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxCSVFinancialWeekImport() {
        def retailerId = Retailer.get(springSecurityService.principal.retailerId).id
        def file = request.getFile('file')
        List<String> errors = []
        int maxErrors = 10
        try {
            //Read imported csv and return all rows (max = 53)
            List<String[]> rows = financialWeekService.readCsvFile(file)

            //Validate existing financial years
            financialWeekService.financialYearPreValidation(rows, errors)

            //This method will validate each row
            // 1 -> Do row level validation
            // 2 -> If no error prepare Grom entity
            // 3 -> If any errors then put them into list
            List<FinancialWeek> financialWeeks =  financialWeekService.processCsvDataRows(rows, errors, retailerId)

            //Once processing all rows validate return financial week list
            financialWeekService.validateFinancialWeekList(financialWeeks, errors)

            if (errors.isEmpty()) {  // If no validation errors, save to database as batch
                //Persist all successful entries as batch insert
                financialWeekService.saveFinancialWeeksInBatches(financialWeeks)
                flash.message = "File processed and data saved successfully!"
                log.info("Successfully process financial week csv file..... ")
                redirect(action: "index")
            } else { // Show all errors and rollback
                //Handle failures
                log.error("Validation errors found processing financial week csv file  ")
                throw new RuntimeException("Validation errors found processing financial week csv file  ")
            }
        } catch (Exception ex) {
            log.error("Errors found processing financial week csv file, exception $ex ")
            List<String> errorResponseMessages = financialWeekService.prepareErrorResponse(errors, maxErrors)
            response.setStatus(500)
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([response: errorResponseMessages])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def downloadCsv() {
        try {

//        def financialWeeks = financialWeekService.getAllFinancialWeeksByFinancialYear(params.yearSelect)
//        // Replace with your domain class and query
//        def csvContent = generateCsvContent(financialWeeks)
//
//        // Send the file directly to the response
//        response.setHeader("Content-disposition", "attachment; filename=financialWeeks.csv")
//        response.contentType = "text/csv"
//        response.outputStream << csvContent.bytes
//        response.outputStream.flush()
//
            def financialWeeks = financialWeekService.getAllFinancialWeeksByFinancialYear(params.yearSelect)

            response.setHeader("Content-disposition", "attachment; filename=financialWeeks.csv")
            response.contentType = "text/csv"

            // Use OpenCSV to write CSV data to the response's output stream
            response.outputStream.withWriter('UTF-8') { writer ->
                CSVWriter csvWriter = new CSVWriter(writer)

                // Write CSV header
                String[] header = ["StartDate", "FinancialYear", "WeekNumber"]
                csvWriter.writeNext(header)

                // Write CSV rows (replace this with your actual financialWeeks data)
                financialWeeks.each { week ->
                    String[] row = [week.startDate.toString(), week.financialYear, week.weekNumber.toString()]
                    csvWriter.writeNext(row)
                }

                csvWriter.flush()
            }

            response.outputStream.flush()
        } catch (Exception ex) {
            log.error("Errors donwloading financial weekly report, exception $ex ")
        }
    }

    private String generateCsvContent(records) {
        StringBuilder sb = new StringBuilder()

        // Add header row // Replace with your actual column names

        // Add data rows
        records.each { record ->
            sb.append("${record.startDate},${record.financialYear},${record.weekNumber}\n")
        }
        return sb.toString()
    }

}
