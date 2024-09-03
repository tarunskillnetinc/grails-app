package uk.co.wonderlane.wlpos

import com.opencsv.bean.CsvToBeanBuilder
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.io.input.XmlStreamReader
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import static uk.co.wonderlane.wlpos.saveFinancialWeekCommand.*

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class saveFinancialWeekCommand {

    String startDate
    String financialYear
    Integer weekNumber
    Integer retailerId

    static constraints = {
        startDate nullable: false
        financialYear nullable: false
        weekNumber nullable: false
        retailerId nullable: false
    }

    static mapping = {
        table 'financialweek '  // Define your actual table name
        id generator: 'id'
        startDate column: 'startDate'
        financialYear column: 'financialYear'
        weekNumber column: 'weekNumber'
        retailerId column: 'retailerId'
    }
}

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class FinancialWeekCSVController extends BaseController {
    def springSecurityService
    DatabaseCredentials databaseCredentials
    def csvService
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
    @Transactional
    def ajaxCSVFinancialWeekImport() {
        def retailerId = Retailer.get(springSecurityService.principal.retailerId).id
        def file = request.getFile('file')
        byte[] fileBytes = file.getBytes()
        String importError

        XmlStreamReader xmlStreamReader = new XmlStreamReader(new ByteArrayInputStream(fileBytes))
        String detectedEncoding = xmlStreamReader.getEncoding()

        // Check if the file is UTF-8 encoded

        // Check if the file is UTF-8 encoded with BOM

        // File is UTF-8 encoded correctly without BOM proceed with upload
        def inputStream = file.inputStream

        try {
            // No validation errors, can continue with the import preparation
            if (!importError) {

                inputStream.withReader('UTF-8') { reader ->
                    reader.eachLine { line, lineNumber ->
                        // Skip the header row if present
                        if (lineNumber == 1 && line.startsWith("header_column_name")) return

                        // Split the line by commas
                        def columns = line.split(",")

                        // Assign each split part to a variable
                        def startDate = columns[0]?.trim()   // e.g., 2024/08/03
                        def financialYear = columns[1]?.trim()   // e.g., 2024/25
                        def weekNumber = columns[2]?.trim()  // e.g., 18

                        String inputDate = new String(startDate);
                        boolean isDateAppended
                        SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        dateFormatWithTime.setLenient(false);

                        try {
                            dateFormatWithTime.parse(inputDate);
                            isDateAppended = true
                        } catch (ParseException e) {
                            isDateAppended = false
                        }


                        if (isDateAppended) {
                            DateTime dateTime = new DateTime(inputDate);
                            if (financialWeekService.saveFinancialWeek(dateTime, financialYear, weekNumber, retailerId)) {
                                flash.message = "File processed and data saved successfully!"
                            } else {
                                importError = "Error occurred during saving of file to database"
                                response.status = 409
                            }
                        } else {
                            inputDate = convertDateFormat(inputDate)
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                            LocalDate date = LocalDate.parse(inputDate, dateFormatter);
                            LocalDateTime dateTime = date.atStartOfDay();
                            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
                            long instantMillis = zonedDateTime.toInstant().toEpochMilli();
                            DateTime jodaDateTime = new DateTime(instantMillis, org.joda.time.DateTimeZone.forID(ZoneId.systemDefault().getId()));

                            if (financialWeekService.saveFinancialWeek(jodaDateTime, financialYear, weekNumber, retailerId)) {
                                flash.message = "File processed and data saved successfully!"
                            } else {
                                importError = "Error occurred during saving of file to database"
                                response.status = 409
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
            importError = "Error occurred during saving of file to database"
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

    private String convertDateFormat(String inputDate) {
        inputDate = inputDate.trim()
        inputDate = inputDate.replaceAll("[^\\x20-\\x7E]", "")
        // Regular expressions to check the date format
        def dashPattern = /^\d{4}-\d{2}-\d{2}$/
        def slashPattern = /^\d{4}\/\d{2}\/\d{2}$/

        // Check if the inputDate is in YYYY-MM-DD format
        if (inputDate ==~ dashPattern) {
            // Replace dashes with slashes
            return inputDate.replaceAll('-', '/')
        }
        // Check if the inputDate is in YYYY/MM/DD format
        else if (inputDate ==~ slashPattern) {
            // Return the date as is
            return inputDate
        }
        // If the date format is unknown
        else {
            return "Unknown format"
        }
    }
}