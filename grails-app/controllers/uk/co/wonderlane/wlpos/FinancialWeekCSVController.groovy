package uk.co.wonderlane.wlpos


import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class FinancialWeekCSVController {
    def springSecurityService
    def financialWeekService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        try {
            List<String> financialYears = financialWeekService.loadFinancialYears()
            boolean enableCsvDownload = financialYears != null && !financialYears.isEmpty()
            render(view: 'index', model: [financialYears: financialYears, success: true, enableCsvDownload : enableCsvDownload])
        } catch (Exception ex){
            log.error("Error loading financial weeks: $ex.message", ex)
            render(view: 'index', model: [financialWeeks: [], success: false, errorMessage: "Failed to load financial weeks. Please try again."])
        }

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
                financialWeekService.saveFinancialWeeksInBatches(financialWeeks, errors)
                List<String> financialYears = financialWeekService.loadFinancialYears()
                log.info("Successfully process financial week csv file..... ")
                render status: 200, contentType: 'application/json', text: JsonOutput.toJson([financialYears: financialYears])
            } else { // Show all errors and rollback
                //Handle failures
                log.error("Validation errors found processing financial week csv file  ")
                throw new RuntimeException("Validation errors found processing financial week csv file  ")
            }
        } catch (Exception ex) {
            log.error("Errors found processing financial week csv file, exception $ex ")
            List<String> errorResponseMessages = financialWeekService.prepareErrorResponse(errors, maxErrors)
            response.setStatus(500)
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([errorsList: errorResponseMessages])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def downloadCsv() {
        try {
            def financialWeeks = financialWeekService.getAllFinancialWeeksByFinancialYear(params.yearSelect)
            response.setHeader("Content-disposition", "attachment; filename=financialWeeks.csv")
            response.contentType = "text/csv"
            financialWeekService.populateCsvDownloadFile(financialWeeks, response.outputStream)
            response.outputStream.flush()
        } catch (Exception ex) {
            log.error("Errors donwloading financial weekly report, exception $ex ")
            List<String> errorResponseMessages = ["Financial week csv file generation failed"]
            render status: 500, contentType: 'application/json', text: JsonOutput.toJson([response: errorResponseMessages])
        }
    }

}
