package uk.co.wonderlane.wlpos

import groovy.json.JsonOutput
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import java.time.LocalDate
import java.time.ZoneId

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class FinancialWeekCSVController extends BaseController {
    def springSecurityService
    def financialWeekService


    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        [financialWeeks: financialWeekService.getAllFinancialWeeks()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    @Override
    def getColumns() {
        return null
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxCSVFinancialWeekImport() {
        def retailerId = Retailer.get(springSecurityService.principal.retailerId).id
        def file = request.getFile('file')
        List<String> responseErrors = []
        try {
            List<FinancialWeek> financialWeeks = []
            List<String> errors = []
            int maxErrors = 10

            //Read imported csv and return all rows (max = 53)
            List<String[]> rows = financialWeekService.readCsvFile(file)

            //This method will validate each row
            // 1 -> Do row level validation
            // 2 -> If no error prepare Grom entity
            // 3 -> If any errors then put them into list
            financialWeeks =  financialWeekService.processCsvDataRows(rows, errors)

            //Once processing all rows validate return financial week list
            financialWeekService.validateFinancialWeekList(financialWeeks, errors);

            if (errors.isEmpty()) {  // If no validation errors, save to database as batch
                //Persist all successful entries as batch insert
                financialWeekService.saveFinancialWeeksInBatches(financialWeeks)
                flash.message = "File processed and data saved successfully!"
                log.info("Successfully process financial week csv file..... ")
                render(action: "index")
            } else { // Show all errors and rollback
                //Handle failures
                log.error("Errors found processing financial week csv file  ")
                if (errors.size() > maxErrors) { // Limit errors to maxErrors and add a message if there are more
                    errors = errors.take(maxErrors)
                    errors << "More errors found, please validate the CSV file again."
                }
                responseErrors << "Import failed with the following errors:\n" + errors.join("\n")
                response.setStatus(500)
                render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: responseErrors])
            }

        } catch (Exception e) {
            log.error("Errors found processing financial week csv file  ")
            responseErrors << "Unexpected error occurred during file processing."
            response.setStatus(500)
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([error: responseErrors])
        }
    }

    def confirmImport() {
        render(action: "index")
    }

    def downloadCsv() {
        def financialWeeks = financialWeekService.getAllFinancialWeeksByFinancialYear(params.yearSelect)
        // Replace with your domain class and query
        def csvContent = generateCsvContent(financialWeeks)

        // Send the file directly to the response
        response.setHeader("Content-disposition", "attachment; filename=financialWeeks.csv")
        response.contentType = "text/csv"
        response.outputStream << csvContent.bytes
        response.outputStream.flush()
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
