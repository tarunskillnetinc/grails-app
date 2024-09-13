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
import java.text.SimpleDateFormat
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

   @Transactional('transactions')
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
            log.error("Financial week - Unexpected database persistence error : ${ex.message}", ex)
            errors.add("Unexpected database persistence error, please verify content and check the logs for more details")
            throw ex
        }
    }

    @Transactional('transactions')
    List<FinancialWeek> getAllFinancialWeeks() {
        def financialWeeks = FinancialWeek.list()
        return financialWeeks.unique { it.financialYear }
    }

    @Transactional('transactions')
    List<FinancialWeek> getAllFinancialWeeksByFinancialYear(String financialYear) {
        def criteria = FinancialWeek.createCriteria()
        return criteria.list {
            eq("financialYear", financialYear)
            order("startDate", "asc")
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
                List<String[]> allRows = csvReader.readAll()

                return allRows.findAll { row -> // Filter out empty rows
                    // Check if the row contains at least one non-empty value
                    row.any { cell -> cell?.trim() } // trims the cell and checks if it's not empty/null
                }
            }
        } catch (Exception ex) {
            log.error("Financial week - Error reading financial week csv file: ${ex.message} ", ex)
            throw new RuntimeException("Financial week - Error reading financial week csv file: ${ex.message} ", ex)
        }
    }

    List<FinancialWeek> processCsvDataRows(List<String[]> rows, List<String> errors, int retailerId) {
        List<FinancialWeek> financialWeeks = []
        try{
            if (rows == null || rows.isEmpty()) { // Check if there was an error during file read
                errors << "Csv file read error. Please check the file format and try again."
            }
            rows?.eachWithIndex { row, index ->
                int lineNumber = index + 1
                try {
                    def startDate = row[0]?.trim() // e.g., 2024/08/03
                    def financialYear = row[1]?.trim() // e.g., 2024/25
                    def weekNumberStr = row[2]?.trim() // e.g., 18

                    // Collect errors for this line
                    // This will validate row data for any validation failures
                    List<String> lineErrors = validateCsvDataRow(lineNumber, startDate, weekNumberStr, financialYear)

                    if (lineErrors.isEmpty()) {
                        LocalDate date = parseDate(startDate)
                        Date convertedDate = Date.valueOf(date)
                        int weekNumber = weekNumberStr as int
                        financialWeeks << new FinancialWeek(startDate: convertedDate, financialYear: financialYear, weekNumber: weekNumber, retailerId: retailerId)
                    } else{
                        errors.addAll(lineErrors)
                    }
                } catch (Exception ex) {
                    log.error("Financial week - Unexpected processing error for line number $lineNumber : ${ex.message} ", ex)
                    errors << "Line $lineNumber: Unexpected processing error"
                }
            }
            return (errors != null && errors.isEmpty()) ? financialWeeks : []
        } catch (Exception ex) {
            errors << "Unexpected error processing csv data rows"
            log.error("Financial week - Unexpected error processing csv data rows: ${ex.message} " , ex)
            throw new RuntimeException("Financial week - Unexpected error processing csv data rows: ${ex.message} " , ex)
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
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy")

                // Write CSV rows
                financialWeeks.each { week ->
                    String[] row = [dateFormat.format(week.startDate), week.financialYear.toString(), week.weekNumber.toString()]
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
            Set<String> financialYears = rows?.collect { it[1] } as Set // Extract the financial years
            if (financialYears.size() > 1) {
                errors << "Financial year should be unique within for the csv file. Found more than one : $financialYears"
                throw new IllegalArgumentException("Financial week - Financial year should be unique within the csv file. Found: $financialYears")
            }

            if (financialYears.size() < 0){
                errors << "Financial year should be provided in the csv file."
                throw new IllegalArgumentException("Financial week - Financial year should be provided in the csv file. Found: $financialYears")
            }

            String financialYear = financialYears.first()
            if (isFinancialYearExists(financialYear)){
                errors << "Financial year already exists. Found: $financialYears in records."
                throw new IllegalArgumentException("Financial week - Financial year already existed. Found: $financialYears")
            }
        } catch (Exception ex) {
            log.error("Financial week - Financial year pre validation error detected ,Exception ${ex.message}" , ex)
            throw new RuntimeException("Financial week - Financial year validation exception.")
        }

    }

    void csvFinancialStartDatePostValidation(List<FinancialWeek> financialWeeks, List<String> errors){
        if (financialWeeks != null && !financialWeeks.isEmpty()){
            financialWeeks.eachWithIndex { financialWeek, index ->
                Date startDate = financialWeek.startDate // Convert Date to String if needed
                int lineNumber = index + 1 // Line number, assuming index starts from 0
                errors.addAll(validateDuplicateStartDate(startDate, financialWeeks, lineNumber, index))
                errors.addAll(validateStartDateOverlap(startDate, financialWeeks, lineNumber, index))
            }
        }
    }

    void csvFinancialWeekPostValidation(List<FinancialWeek> financialWeeks, List<String> errors){
        if (financialWeeks != null && !financialWeeks.isEmpty()){
            financialWeeks.eachWithIndex { financialWeek, index ->
                String weekNumber = financialWeek.weekNumber // Convert Date to String if needed
                int lineNumber = index + 1 // Line number, assuming index starts from 0
                errors.addAll(validateDuplicateWeekNumber(weekNumber, financialWeeks, lineNumber, index))
            }
        }
    }

    List<String> prepareErrorResponse(List<String> errors, int maxErrors){
        List<String> errorResponseMessages = []
        if (errors!= null && !errors.isEmpty()){
            if (errors.size() > maxErrors) { // Limit errors to maxErrors and add a message if there are more
                errors = errors.take(maxErrors)
                errors << "More errors found, please check the logs for more details and validate the csv file again."
            }
            errorResponseMessages.addAll(errors)
        } else {
            errorResponseMessages.add("Unexpected error while file processing, please check the logs for more details")
        }
        return errorResponseMessages;
    }

    private List<String> validateCsvDataRow(int lineNumber, String startDate, String weekNumberStr, String financialYear){
        List<String> lineErrors = []
        lineErrors.addAll(processCsvStartDateValidations(lineNumber, startDate))// Validate start date
        lineErrors.addAll(processCsvWeekValidations(lineNumber, weekNumberStr))// Validate week number
        lineErrors.addAll(processCsvFinancialYearValidations(lineNumber, financialYear))// Validate financial year
        return lineErrors
    }

    private List<String> processCsvStartDateValidations(int lineNumber, String startDate){
        List<String> errors = []

        // Step 1: Validate if the start date is blank
        errors.addAll(validateStartDateBlank(startDate, lineNumber))

        // Step 2: If the start date is not blank, validate the date format
        if (errors.isEmpty()) {
            errors.addAll(validateDate(startDate, lineNumber))
        }

        return errors
    }

    private List<String> processCsvWeekValidations(int lineNumber,String weekNumberStr){
        List<String> errors = []

        // Step 1: Validate if the week is blank
        errors.addAll(validateWeekNumberBlank(weekNumberStr, lineNumber))

        // Step 2: If the week is not blank, validate the week number
        if (errors.isEmpty()) {
            errors.addAll(validateWeekNumber(weekNumberStr, lineNumber))
        }

        // Step 2: If the week number validation pass, validate the week number sequence order
        if (errors.isEmpty()) {
            errors.addAll(validateFinancialWeekSequence(weekNumberStr, lineNumber))
        }

        return errors
    }

    private List<String> processCsvFinancialYearValidations(int lineNumber,String financialYear){
        List<String> errors = []
        // Step 1: Validate financial year format
        errors.addAll(validateFinancialYear(financialYear, lineNumber))
        return errors
    }

    private List<String> validateStartDateBlank(String startDate, int lineNumber) {
        List<String> errors = []
        try {
            if (!startDate?.trim()) {
                errors << "Line $lineNumber: Start date is blank, please correct before upload"
            }
        } catch (Exception ex) {
            log.error("Financial week - unexpected error while validating start date, $ex" , ex)
            errors << "Line $lineNumber: Unexpected error while validating start date, please check the data and logs."
        }
        return errors
    }

    private List<String> validateDate(String dateStr, int lineNumber) {
        List<String> errors = []
        try {
            parseDate(dateStr)
        } catch (DateTimeParseException ex) {
            log.error("Financial week - date parsing error detected , exception ${ex.message}" , ex)
            errors << "Line $lineNumber: Incorrect financial week date format, please check date format"
        } catch (Exception ex){
            log.error("Financial week - date parsing unexpected error detected , exception ${ex.message}" , ex)
            errors << "Line $lineNumber: Unexpcted error while parsing start date, please check date format and logs."
        }
        return errors
    }

    private List<String> validateDuplicateStartDate(Date startDate, List<FinancialWeek> financialWeeks, int lineNumber, int index) {
        List<String> errors = []
        try {
            if (financialWeeks.take(index).any { it.startDate == startDate }) {
                errors << "Line $lineNumber: Duplicate start date, please correct before upload"
            }
        } catch (Exception ex) {
            log.error("Financial week - duplicate start date detected , exception $ex" , ex)
            errors << "Line $lineNumber: Unexpected error while validating duplicate start date, please check the data and logs"
        }
        return errors
    }

    private List<String> validateStartDateOverlap(Date startDate, List<FinancialWeek> financialWeeks, int lineNumber, int index){
        List<String> errors = []
        try {
            if (index > 0) { // Skip validation for the first item
                FinancialWeek financialWeekFileStart = financialWeeks[0] as FinancialWeek
                Date financialWeekStartDate = financialWeekFileStart.startDate
                LocalDate currentStartDate = toLocalDate(startDate)
                LocalDate previousStartDate = toLocalDate(financialWeekStartDate)
                if (!currentStartDate.isEqual(previousStartDate.plusWeeks(index))) {
                    log.error("Financial week - The start date $startDate must be ${index} weeks after the weekly file start date ${financialWeekStartDate}")
                    errors << "Line $lineNumber: Invalid start week date, please correct before upload."
                }
            }
        } catch (Exception ex) {
            log.error("Financial week - Unexpected error while validating start date overlapping , exception ${ex.message}" , ex)
            errors << "Line $lineNumber: Unexpected error while validating start week date, please check date format"
        }
        return errors;
    }

    private List<String> validateWeekNumberBlank(String weekNumberStr, int lineNumber) {
        List<String> errors = []
        try {
            if (!weekNumberStr?.trim()) {
                errors << "Line $lineNumber: Financial week number is blank,please correct before upload."
            }
        } catch (Exception ex) {
            log.error("Financial week - week number blank validation error detected , exception $ex" , ex)
            errors << "Line $lineNumber: Unexpcted error while validating week number, please correct before upload"
        }
        return errors
    }

    private List<String> validateWeekNumber(String weekNumberStr, int lineNumber) {
        List<String> errors = []
        try {

            int weekNumber = weekNumberStr as int

            if (weekNumber < 1) {
                errors << "Line $lineNumber: Min financial weeks shouldn't be less than 1 weeks."
            } else if (weekNumber > 53) {
                errors << "Line $lineNumber: Max financial weeks shouldn't be greater than 53 weeks."
            }
        } catch (NumberFormatException e) {
            errors << "Line $lineNumber: Week number is not an integer in '$weekNumberStr'"
        } catch (Exception ex) {
            log.error("Financial week - Unexpected error while validating week number: ${ex.message}" , ex)
            errors << "Line $lineNumber: Unexpcted error while validating week number, please correct before upload"
        }
        return errors
    }

    private List<String> validateFinancialWeekSequence(String weekNumberStr, int lineNumber){
        List<String> errors = []
        try {
            int currentWeekNumber = weekNumberStr as int
            int expectedWeekNumber = lineNumber
            if (currentWeekNumber != expectedWeekNumber) {
                log.error("Financial week - Week number $weekNumberStr is not in the expected sequential order. Expected $expectedWeekNumber.")
                errors << "Line $lineNumber: Invalid financial week sequence, please correct before upload."
            }
        } catch (Exception ex) {
            log.error("Financial week - Unexpected error while validating week number sequential order ${ex.message}" , ex)
            errors << "Line $lineNumber: Unexpcted error while validating week number sequential order, please correct before upload"
        }
        return errors
    }

    private List<String> validateDuplicateWeekNumber(String weekNumberStr, List<FinancialWeek> financialWeeks, int lineNumber, int index) {
        List<String> errors = []
        try {
            int weekNumber = weekNumberStr as int
            if (financialWeeks.take(index).any { it.weekNumber == weekNumber }) {
                errors << "Line $lineNumber: Duplicate financial week number, please correct before upload."
            }
        } catch (Exception ex) {
            log.error("Financial week - duplicate week number detected , exception $ex" , ex)
            errors << "Line $lineNumber: Unexpcted error while validating duplicate week number, please correct before upload"
        }
        return errors
    }

    private List<String> validateFinancialYear(String financialYear, int lineNumber) {
        List<String> errors = []
        try {
            if (!financialYear.matches("\\d{4}/\\d{2}")) { // Check that the financial year matches the format YYYY/YY
                errors << "Line $lineNumber: Incorrect financial year format. Please use YYYY/YY format."
            }
        } catch (Exception ex) {
            log.error("Financial week - financial year format validation error detected , exception $ex" , ex)
            errors << "Line $lineNumber: Unexpcted error while validating financial year format, please correct before upload"
        }
        return errors
    }

    private LocalDate parseDate(String dateStr) throws ParseException {
        DateTimeFormatter formatter_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter formatter_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try { // Try to parse the date with the "dd/MM/yyyy" format
            return LocalDate.parse(dateStr, formatter_YYYY_MM_DD)
        } catch (DateTimeParseException e) {
            try { // If parsing fails, try the second format
                return LocalDate.parse(dateStr, formatter_DD_MM_YYYY)
            } catch (DateTimeParseException ex) {
                throw new DateTimeParseException("Financial week - Invalid date format: " + dateStr, dateStr, 0)  // If both formats fail, throw an exception
            }
        } catch (DateTimeParseException ex) {
            throw new DateTimeParseException("Financial week - Date parsing error: " + dateStr, dateStr, 0)
        } catch (Exception ex) {
            throw new IllegalArgumentException("Financial week - date parsing unexpected error: " + dateStr)
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toLocalDate()
    }

}
