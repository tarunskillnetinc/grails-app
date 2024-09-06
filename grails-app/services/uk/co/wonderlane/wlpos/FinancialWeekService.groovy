package uk.co.wonderlane.wlpos

import com.opencsv.CSVReader
import grails.gorm.transactions.Transactional
import org.hibernate.Session
import org.hibernate.Transaction
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
    boolean saveFinancialWeek(DateTime startDate, String financialYear, String weekNumber, Integer retailerId) {

        Session session = sessionFactory.openSession()
        Transaction transaction = session.beginTransaction()

        FinancialWeek existingFinancialWeek = FinancialWeek.findByStartDate(startDate)
        if (existingFinancialWeek) {
            return false
        }
        def financialWeek = new FinancialWeek(retailerId: retailerId, startDate: startDate, financialYear: financialYear, weekNumber: weekNumber)
        if (financialWeek.save(flush: true)) {
            println "Person saved successfully."
            return true
        } else {
            println "Failed to save person."
            return false
        }
        transaction.commit()
        session.close()
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

    List<FinancialWeek> processCsvDataRows(List<String[]> rows, List<String> errors) {
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
                        financialWeeks << new FinancialWeek(startDate: dateTime, financialYear: financialYear, weekNumber: weekNumber)
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

    void validateFinancialWeekList(List<FinancialWeek> financialWeeks, List<String> errors){
        if (!financialWeeks.isEmpty() && financialWeeks.size() > 0){
            errors.addAll(validateFinancialWeekSequence(financialWeeks))
        }
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
                throw new IllegalArgumentException("Invalid date format: " + dateStr);  // If both formats fail, throw an exception
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Date parsing error: " + dateStr);
        }
    }

    private List<String> validateFinancialWeekSequence(List<FinancialWeek> financialWeeks) {
        List<String> errors = []
        // Sort weeks by start date
        financialWeeks.sort { it.startDate }

        // Check for sequence gaps
        financialWeeks.eachWithIndex { week, index ->
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
        } catch (ParseException e) {
            errors << "Line $lineNumber: Invalid date format in '$dateStr'"
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
