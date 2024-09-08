package uk.co.wonderlane.wlpos

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import javax.validation.ConstraintViolationException
import java.sql.Date
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.text.ParseException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Transactional
class FinancialWeekService extends MySqlDal {

    def springSecurityService
    def sessionFactory

    protected FinancialWeekService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

   @Transactional('reporting')
   saveFinancialWeeksInBatches(List<FinancialWeek> financialWeeks, List<String> errors) {
        try {
            FinancialWeek.saveAll(financialWeeks)// Save all financial weeks in this batch
            // Flush the session to write changes to the database
            FinancialWeek.withSession { session ->
                session.flush()
                session.clear()
            }
        } catch (ConstraintViolationException ex){
            log.error("Financial week - Entries persisting unique key violation occurred: ${ex.message}", ex)
            errors.add("Data conflicts, please verify content and check the logs for more details")
            throw ex
        } catch (SQLIntegrityConstraintViolationException ex){
            log.error("Financial week - Entries persisting sql integrity constraint violation: ${ex.message}", ex)
            errors.add("Data conflicts, please verify content and check the logs for more details")
            throw ex
        } catch (Exception ex) {
            log.error("Financial week - Entries persisting unexpected errors : ${ex.message}", ex)
            errors.add("Unexpected database persistence error, please verify content and check the logs for more details")
            throw ex
        }
    }

    @Transactional('reporting')
    List<FinancialWeek> getAllFinancialWeeks() {
        def financialWeeks = FinancialWeek.list()
        return financialWeeks.unique { it.financialYear }
    }

    @Transactional('reporting')
    List<FinancialWeek> getAllFinancialWeeksByFinancialYear(String financialYear) {
        def criteria = FinancialWeek.createCriteria()
        return criteria.list {
            eq("financialYear", financialYear)
        }
    }

    boolean isFinancialYearExists(String financialYear){
        try {
            List<FinancialWeek> existingWeeksForFinancialYear = getAllFinancialWeeksByFinancialYear(financialYear)
            if (existingWeeksForFinancialYear!= null && !existingWeeksForFinancialYear.isEmpty()){
                return true
            }
        } catch (Exception ex) {
            log.error("Financial week - Error loading existing financial years : ${ex.message} ", ex)
            throw new RuntimeException("Financial week - Error loading existing financial years : ${ex.message} ", ex)
        }
    }

    List<String[]> readCsvFile(MultipartFile file){
        try {
            file.inputStream.withReader('UTF-8') { reader ->
                CSVReader csvReader = new CSVReader(reader)
                // Reads all rows at once --? Max row count is 53 so keep in memory should not impact on application
                return csvReader.readAll()
            }
        } catch (Exception ex) {
            log.error("Financial week - Error reading financial week csv file, ${ex.message} ", ex)
            throw new RuntimeException("Financial week - Error reading financial week csv file, ${ex.message} ", ex)
        }
    }

    List<FinancialWeek> processCsvDataRows(List<String[]> rows, List<String> errors, int retailerId) {
        List<FinancialWeek> financialWeeks = []
        try{
            if (rows == null || rows.isEmpty()) { // Check if there was an error during file read
                errors << "CSV file read error. Please check the file format and try again."
            }
            rows?.eachWithIndex { row, lineNumber ->
                try {
                    if (lineNumber == 0 && rows[0][0].startsWith("header_column_name")) return

                    def startDate = row[0]?.trim() // e.g., 2024/08/03
                    def financialYear = row[1]?.trim() // e.g., 2024/25
                    def weekNumberStr = row[2]?.trim() // e.g., 18

                    // Collect errors for this line
                    // This will validate row data for any validation failures
                    List<String> lineErrors = validateCsvDataRow(lineNumber, startDate, weekNumberStr, financialYear, financialWeeks)

                    if (lineErrors.isEmpty()) {
                        LocalDate date = parseDate(startDate)
                        Date convertedDate = Date.valueOf(date)
                        int weekNumber = weekNumberStr as int
                        financialWeeks << new FinancialWeek(startDate: convertedDate, financialYear: financialYear, weekNumber: weekNumber, retailerId: retailerId)
                    } else{
                        errors.addAll(lineErrors)
                    }
                } catch (Exception ex) {
                    log.error("Financial week - Error attempting to process line number $lineNumber : ${ex.message} ", ex)
                    errors << "Unexpected error processing line number $lineNumber "
                }
            }
            return financialWeeks
        } catch (Exception ex) {
            errors << "Unexpected error processing csv data row"
            log.error("Financial week - Unexpected error processing CSV file : ${ex.message} " , ex)
            throw new RuntimeException("Financial week - Unexpected error processing CSV file : ${ex.message} " , ex)
        }
    }

    List<String> loadFinancialYears(){
        try {
            def financialWeeks = getAllFinancialWeeks()
            return financialWeeks?.collect { it.financialYear }?.unique()
        } catch (Exception ex) {
            log.error("Financial week - Error loading financial years : ${ex.message} " , ex)
            throw new RuntimeException("Financial week - Error loading financial years : ${ex.message} " , ex)
        }
    }

    void populateCsvDownloadFile(List<FinancialWeek> financialWeeks, OutputStream outputStream){
        try {
            outputStream.withWriter('UTF-8') { writer ->
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
        } catch (Exception ex) {
            log.error("Financial week - CSV generation error : ${ex.message} " , ex)
            throw new RuntimeException("Financial week - CSV generation error : ${ex.message} " , ex)
        }
    }

    void financialYearPreValidation(List<String[]> rows, List<String> errors){
        try {
            Set<String> financialYears = rows.collect { it[1] } as Set // Extract the financial years
            if (financialYears.size() > 1) {
                errors << "Financial year should be unique within the CSV file. Found: $financialYears"
                throw new IllegalArgumentException("Financial week - Financial year should be unique within the CSV file. Found: $financialYears")
            }

            if (financialYears.size() < 0){
                errors << "Financial year should be provided in the CSV file. Found: $financialYears"
                throw new IllegalArgumentException("Financial week - Financial year should be provided in the CSV file. Found: $financialYears")
            }

            String financialYear = financialYears.first()
            if (isFinancialYearExists(financialYear)){
                errors << "Financial year already exists. Found: $financialYears"
                throw new IllegalArgumentException("Financial week - Financial year already existed. Found: $financialYears")
            }
        } catch (Exception ex) {
            log.error("Financial week - Financial year pre validation error detected ,Exception $ex" , ex)
            throw new RuntimeException("Financial week - Financial year validation exception.")
        }

    }

    List<String> prepareErrorResponse(List<String> errors, int maxErrors){
        List<String> errorResponseMessages = []
        if (errors!= null && !errors.isEmpty()){
            if (errors.size() > maxErrors) { // Limit errors to maxErrors and add a message if there are more
                errors = errors.take(maxErrors)
                errors << "More errors found, please validate the CSV file again."
            }
            errorResponseMessages.addAll(errors)
        } else {
            errorResponseMessages.add("Unexpected error occurred during file processing.Please check the logs for more details")
        }
        return errorResponseMessages;
    }

    private List<String> validateCsvDataRow(int lineNumber, String startDate, String weekNumberStr, String financialYear, List<FinancialWeek> financialWeeks){
        List<String> lineErrors = []
        lineErrors.addAll(validateStartDateBlank(startDate, lineNumber))
        lineErrors.addAll(validateDate(startDate, lineNumber))
        lineErrors.addAll(validateDuplicateStartDate(startDate, financialWeeks, lineNumber))
        lineErrors.addAll(validateWeekNumberBlank(startDate, lineNumber))
        lineErrors.addAll(validateWeekNumber(weekNumberStr, lineNumber, financialWeeks))
        lineErrors.addAll(validateDuplicateWeekNumber(weekNumberStr, financialWeeks, lineNumber))
        lineErrors.addAll(validateFinancialYear(financialYear, lineNumber))
        return lineErrors
    }

    private LocalDate parseDate(String dateStr) throws ParseException {
        DateTimeFormatter formatterYYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter formatterMM_DD_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try { // Try to parse the date with the "dd/MM/yyyy" format
            return LocalDate.parse(dateStr, formatterYYYY_MM_DD)
        } catch (DateTimeParseException e) {
            try { // If parsing fails, try the second format
                return LocalDate.parse(dateStr, formatterMM_DD_YYYY)
            } catch (DateTimeParseException ex) {
                throw new DateTimeParseException("Financial week - Invalid date format: " + dateStr, dateStr, 0);  // If both formats fail, throw an exception
            }
        } catch (DateTimeParseException ex) {
            throw new DateTimeParseException("Financial week - Date parsing error: " + dateStr, dateStr, 0);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Financial week - date parsing unexpected error: " + dateStr);
        }
    }

    private List<String> validateStartDateBlank(String startDate, int lineNumber) {
        try {
            List<String> errors = []
            if (!startDate?.trim()) {
                errors << "Line $lineNumber: Start date is blank, please correct before upload"
            }
            return errors
        } catch (Exception ex) {
            log.error("Financial week - start date blank validation error detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - start date blank validation error detected, exception $ex" , ex)
        }
    }

    private List<String> validateWeekNumberBlank(String weekNumberStr, int lineNumber) {
        try {
            List<String> errors = []
            if (!weekNumberStr?.trim()) {
                errors << "Line $lineNumber: Financial week number is blank, please correct before upload."
            }
            return errors
        } catch (Exception ex) {
            log.error("Financial week - week number blank validation error detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - week number blank validation error detected, exception $ex" , ex)
        }
    }

    private List<String> validateDate(String dateStr, int lineNumber) {
        List<String> errors = []
        try {
            parseDate(dateStr)
        } catch (DateTimeParseException ex) {
            log.error("Financial week - date parsing error detected , exception $ex" , ex)
            errors << "Line $lineNumber: Incorrect financial week date format."
        } catch (Exception ex){
            log.error("Financial week - date parsing unexpected error detected , exception $ex" , ex)
            errors << "Line $lineNumber: Unexpcted financial week date parsing error. Please check format"
        }
        return errors
    }

    private List<String> validateFinancialYear(String financialYear, int lineNumber) {
        try {
            List<String> errors = []
            // Check that the financial year matches the format YYYY/YY
            if (!financialYear.matches("\\d{4}/\\d{2}")) {
                errors << "Line $lineNumber: Incorrect financial year format. Please use YYYY/YY format."
            }
            return errors
        } catch (Exception ex) {
            log.error("Financial week - financial year format validation error detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - financial year format validation error detected, exception $ex" , ex)
        }
    }

    private List<String> validateWeekNumber(String weekNumberStr, int lineNumber, List<FinancialWeek> financialWeeks) {
        List<String> errors = []
        try {

            int weekNumber = weekNumberStr as int
            if (weekNumber < 1) {
                errors << "Line $lineNumber: Min financial weeks shouldn't be less than 1 weeks."
            } else if (weekNumber > 53) {
                errors << "Line $lineNumber: Max financial weeks shouldn't be greater than 53 weeks."
            } else {
                int expectedNextWeekNumber = (financialWeeks ? financialWeeks[-1]?.weekNumber : 0) + 1
                if (weekNumber != expectedNextWeekNumber) {
                    errors << "Line $lineNumber: Week number $weekNumber is not in the expected sequential order. Expected $expectedNextWeekNumber."
                }
            }
        } catch (NumberFormatException e) {
            errors << "Line $lineNumber: Week number is not an integer in '$weekNumberStr'"
        } catch (Exception ex) {
            log.error("Financial week - week number validation unexpected error detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - week number validation unexpected error detected, exception $ex" , ex)
        }
        return errors
    }

    private List<String> validateDuplicateWeekNumber(String weekNumberStr, List<FinancialWeek> financialWeeks, int lineNumber) {
        try {
            List<String> errors = []
            int weekNumber = weekNumberStr as int
            if (financialWeeks.any { it.weekNumber == weekNumber }) {
                errors << "Line $lineNumber: Duplicate financial week number, please correct before upload."
            }
            return errors
        } catch (Exception ex) {
            log.error("Financial week - duplicate week number detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - duplicate week number detected, exception $ex" , ex)
        }
    }

    private List<String> validateDuplicateStartDate(String startDate, List<FinancialWeek> financialWeeks, int lineNumber) {
        try {
            List<String> errors = []
            if (financialWeeks.any { it.startDate == startDate }) {
                errors << "Line $lineNumber: Duplicate start date, please correct before upload"
            }
            return errors
        } catch (Exception ex) {
            log.error("Financial week - duplicate start date detected , exception $ex" , ex)
            throw new RuntimeException("Financial week - duplicate start date detected, exception $ex" , ex)
        }
    }


}
