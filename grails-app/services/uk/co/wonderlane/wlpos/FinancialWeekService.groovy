package uk.co.wonderlane.wlpos

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import java.sql.SQLException
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
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
   saveFinancialWeeksInBatches(List<FinancialWeek> financialWeeks) {
        try {
            FinancialWeek.saveAll(financialWeeks)// Save all financial weeks in this batch
            // Flush the session to write changes to the database
            FinancialWeek.withSession { session ->
                session.flush()
                session.clear()
            }
        } catch (Exception ex) {
            log.error("Error saving batch of FinancialWeeks, exception $ex")
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
        List<FinancialWeek> existingWeeksForFinancialYear = getAllFinancialWeeksByFinancialYear(financialYear)
        if (existingWeeksForFinancialYear!= null && !existingWeeksForFinancialYear.isEmpty()){
            return true;
        }
        return false;
    }

    List<String[]> readCsvFile(MultipartFile file){
        try {
            file.inputStream.withReader('UTF-8') { reader ->
                CSVReader csvReader = new CSVReader(reader)
                // Reads all rows at once --? Max row count is 53 so keep in memory should not impact on application
                return csvReader.readAll()
            }
        } catch (Exception ex) {
            log.error("Error when reading financial week csv file, Exception  $ex ")
            return null // Indicate a read failure by returning null
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
                    List<String> lineErrors = validateCsvDataRow(lineNumber, startDate, weekNumberStr, financialYear, financialWeeks)

                    if (lineErrors.isEmpty()) {
                        LocalDate date = parseDate(startDate)
                        Date convertedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        DateTime dateTime = new DateTime(convertedDate).withZone(DateTimeZone.UTC);
                        int weekNumber = weekNumberStr as int
                        financialWeeks << new FinancialWeek(startDate: dateTime, financialYear: financialYear, weekNumber: weekNumber, retailerId: retailerId)
                    } else{
                        errors.addAll(lineErrors)
                    }
                } catch (Exception ex) {
                    log.error("Error when attempting to process line number $lineNumber , Exception  $ex ")
                    errors << "Unexpected error processing line number $lineNumber "
                }
            }
            return financialWeeks
        } catch (Exception ex) {
            log.error("Unexpected error when processing CSV file rows , Exception " , ex)
            return []
        }
    }

    List<String> loadFinancialYears(){
        try {
            def financialWeeks = getAllFinancialWeeks()
            return financialWeeks?.collect { it.financialYear }?.unique()
        } catch (Exception ex) {
            log.error("Error loading weekly financial years , Exception " , ex)
            throw new RuntimeException("Error loading weekly financial years")
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
            log.error("Weekly financial csv generation error ,Exception $ex" , ex)
        }
    }

    void financialYearPreValidation(List<String[]> rows, List<String> errors){
        try {
            Set<String> financialYears = rows.collect { it[1] } as Set // Extract the financial years
            if (financialYears.size() > 1) {
                errors << "Financial year should be unique within the CSV file. Found: $financialYears"
                throw new IllegalArgumentException("Financial year should be unique within the CSV file. Found: $financialYears")
            }

            if (financialYears.size() < 0){
                errors << "Financial year should be provided in the CSV file. Found: $financialYears"
                throw new IllegalArgumentException("Financial year should be unique within the CSV file. Found: $financialYears")
            }

            String financialYear = financialYears.first()
            if (isFinancialYearExists(financialYear)){
                errors << "Financial year already exists. Found: $financialYears"
                throw new IllegalArgumentException("Financial year should be unique within the CSV file. Found: $financialYears")
            }
        } catch (Exception ex) {
            log.error("Weekly financial year validation error detected ,Exception $ex" , ex)
            throw new RuntimeException("Financial year validation exception.")
        }

    }

    void validateFinancialWeekList(List<FinancialWeek> financialWeeks, List<String> errors){
        if (!financialWeeks.isEmpty() && financialWeeks.size() > 0){
            errors.addAll(validateFinancialWeekSequence(financialWeeks))
        }
    }

    List<String> prepareErrorResponse(List<String> errors, int maxErrors){
        List<String> errorResponseMessages = []
        if (errors.size() > 0){
            if (errors.size() > maxErrors) { // Limit errors to maxErrors and add a message if there are more
                errors = errors.take(maxErrors)
                errors << "More errors found, please validate the CSV file again."
            }
            errorResponseMessages.addAll(errors)
        } else {
            errorResponseMessages.add("Unexpected error occurred during file processing.")
        }
        return errorResponseMessages;
    }

    private List<String> validateCsvDataRow(int lineNumber, String startDate, String weekNumberStr, String financialYear, List<FinancialWeek> financialWeeks){
        List<String> lineErrors = []
        lineErrors.addAll(validateStartDateBlank(startDate, lineNumber))
        lineErrors.addAll(validateWeekNumberBlank(startDate, lineNumber))
        lineErrors.addAll(validateDate(startDate, lineNumber))
        lineErrors.addAll(validateFinancialYear(financialYear, lineNumber))
        lineErrors.addAll(validateWeekNumber(weekNumberStr, lineNumber))
        lineErrors.addAll(validateDuplicateWeekNumber(weekNumberStr, financialWeeks, lineNumber))
        return lineErrors
    }

    List<String> checkDateRangeOverlap(List<FinancialWeek> financialWeeks, Long retailerId) {
        List<String> errors = []
        financialWeeks.each { week ->
            def existingWeeks = findOverlappingWeeks(week.startDate, retailerId)
            if (existingWeeks) {
                errors << "Date overlap found for week starting on ${week.date}."
            }
        }
        return errors
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
                throw new DateTimeParseException("Invalid date format: " + dateStr, dateStr, 0);  // If both formats fail, throw an exception
            }
        } catch (DateTimeParseException ex) {
            throw new DateTimeParseException("Financial week date parsing error: " + dateStr, dateStr, 0);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Financial week date parsing unexpected error: " + dateStr);
        }
    }

    private List<String> validateFinancialWeekSequence(List<FinancialWeek> financialWeeks) {
        List<String> errors = []
        financialWeeks.sort { it.startDate } // Sort weeks by start date
        financialWeeks.eachWithIndex { week, index ->  // Check for sequence gaps
            if (index > 0) { // Skip the first element
                def previousWeek = financialWeeks[index - 1]
                if (week.startDate.isAfter(previousWeek.startDate.plusDays(7))) {
                    errors << "Gap found between week starting on ${previousWeek.startDate} and week starting on ${week.startDate}."
                }
            }
        }
        return errors
    }


    private List<String> validateStartDateBlank(String startDate, int lineNumber) {
        List<String> errors = []
        if (!startDate?.trim()) {
            errors << "Line $lineNumber: Start date is blank. Please provide a valid start date."
        }
        return errors
    }

    private List<String> validateWeekNumberBlank(String weekNumberStr, int lineNumber) {
        List<String> errors = []
        if (!weekNumberStr?.trim()) {
            errors << "Line $lineNumber: Week number is blank. Please provide a valid week number."
        }
        return errors
    }

    private List<String> validateDate(String dateStr, int lineNumber) {
        List<String> errors = []
        try {
            parseDate(dateStr)
        } catch (DateTimeParseException ex) {
            errors << "Line $lineNumber: Invalid date format in '$dateStr'"
        } catch (Exception ex){
            errors << "Line $lineNumber: unexpcted date parsing error '$dateStr'"
        }
        return errors
    }

    private List<String> validateFinancialYear(String financialYear, int lineNumber) {
        List<String> errors = []
        // Check that the financial year matches the format YYYY/YY
        if (!financialYear.matches("\\d{4}/\\d{2}")) {
            errors << "Line $lineNumber: Invalid financial year format in '$financialYear'. Please use YYYY/YY format."
        }
        return errors
    }

    private List<String> validateWeekNumber(String weekNumberStr, int lineNumber) {
        List<String> errors = []
        try {
            int weekNumber = weekNumberStr as int
            if (weekNumber < 1 || weekNumber > 53) {
                errors << "Line $lineNumber: Week number must be between 1 and 53 in '$weekNumberStr'"
            }
        } catch (NumberFormatException e) {
            errors << "Line $lineNumber: Week number is not an integer in '$weekNumberStr'"
        }
        return errors
    }

    private List<String> validateDuplicateWeekNumber(String weekNumberStr, List<FinancialWeek> financialWeeks, int lineNumber) {
        List<String> errors = []
        int weekNumber = weekNumberStr as int
        if (financialWeeks.any { it.weekNumber == weekNumber }) {
            errors << "Line $lineNumber: Duplicate week number found in '$weekNumberStr'"
        }
        return errors
    }


}
