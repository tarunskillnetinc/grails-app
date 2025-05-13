package uk.co.wonderlane.wlpos


import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.transactions.FinancialWeek

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class FinancialWeekController {

    def springSecurityService
    def financialWeekService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        try {
            int retailerId = springSecurityService.principal.retailerId
            List<String> financialYears = financialWeekService.loadFinancialYears(retailerId)
            boolean enableCsvDownload = financialYears != null && !financialYears.isEmpty()
            def message = flash.error ?: flash.success
            def actionSuccess = flash.error ? false : true
            render(view: 'index', model: [financialYears: financialYears, actionSuccess: actionSuccess, enableCsvDownload : enableCsvDownload, message: message])
        } catch (Exception ex){
            log.error("Error loading financial weeks: $ex.message", ex)
            render(view: 'index', model: [financialWeeks: [], actionSuccess: false, message: "Failed to load financial weeks. Please try again."])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxCSVFinancialWeekImport() {
        int retailerId = springSecurityService.principal.retailerId
        def file = request.getFile('file')
        List<String> errors = []
        int maxErrors = 10
        try {
            //Read imported csv and return all rows (max = 53)
            List<String[]> rows = financialWeekService.readCsvFile(file)

            //Validate existing financial years --> check against database
            financialWeekService.financialYearPreValidation(rows, errors, retailerId)

            //This method will validate each row
            // 1 -> Do row level validation
            // 2 -> If no error prepare Grom entity
            // 3 -> If any errors then put them into error list
            List<FinancialWeek> financialWeeks =  financialWeekService.processCsvDataRows(rows, errors, retailerId)

            //This is financial week csv start date post validations (This include validations after basic date validations)
            // 1 -> Validate duplicate date
            // 2 -> Validate overlapping date
            financialWeekService.csvFinancialStartDatePostValidation(financialWeeks, errors)

            //This is financial week csv week post validations (This include validations after basic week validations)
            // 1 -> Validate duplicate week
            financialWeekService.csvFinancialWeekPostValidation(financialWeeks, errors)

            if (errors.isEmpty()) {  // If no validation errors, save to database as batch
                //Persist all successful entries as batch insert
                financialWeekService.saveFinancialWeeksInBatches(financialWeeks, errors)
                List<String> financialYears = financialWeekService.loadFinancialYears(retailerId)
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
            int retailerId = springSecurityService.principal.retailerId
            List<FinancialWeek> financialWeeks = financialWeekService.getAllFinancialWeeksByFinancialYear(params.yearSelect, retailerId)
            if (financialWeeks != null && !financialWeeks.isEmpty()) {
                String csvFileName = "financialWeeks${params.yearSelect}.csv"
                response.setHeader("Content-disposition", "attachment; filename=${csvFileName}")
                response.contentType = "text/csv"
                financialWeekService.populateCsvDownloadFile(financialWeeks, response.outputStream)
                response.outputStream.flush()
            } else {
                log.error("Financial week data not found for year ${params.yearSelect} and retailer id ${retailerId}")
                flash.error = "Financial week data not found for year ${params.yearSelect} and retailer id ${retailerId}"
                redirect(action: 'index')
            }
        } catch (Exception ex) {
            log.error("Errors donwloading financial weekly report for financial year ${params.yearSelect}, exception $ex ")
            flash.error = "Financial week CSV file generation failed ${params.yearSelect}"
            redirect(action: 'index') // Custom error page or action
        }
    }

}
