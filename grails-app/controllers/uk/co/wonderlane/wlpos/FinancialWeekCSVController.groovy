package uk.co.wonderlane.wlpos

import com.opencsv.bean.CsvToBeanBuilder
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.io.input.XmlStreamReader
import org.joda.time.DateTime
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.enums.ProductStatus
import uk.co.wonderlane.wlpos.reporting.FinancialWeek

import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import static uk.co.wonderlane.wlpos.saveFinancialWeekCommand.*

@Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
class saveFinancialWeekCommand {

    String startDate
    String financialYear
    Integer weekNumber
    Integer retailerId

    static constraints = {
        startDate nullable: false
        financialYear  nullable: false
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
            // Fetch categories from the service
            List<FinancialWeek> financialWeeks = financialWeekService.getAllFinancialWeeks()
            // Pass categories to the GSP view
            render(view: 'index', model: [financialWeeks: financialWeeks])

    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    @Override
    def getColumns() {
        return null
    }
    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    @Transactional
    def ajaxCSVFinancialWeekImport() {

        System.out.println "hit ajaxCSVFinancialWeekImport"

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
                        System.out.println "hit input area"

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
                                String result = startDate.replace('/', '-');
                                System.out.println 'test' + startDate


                                String inputDate = new String(startDate);

                                // Define the pattern of the input date string

                                // Define the pattern of the input date string (date only)
                                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

                                // Parse the string into a LocalDate
                                LocalDate date = LocalDate.parse(inputDate, dateFormatter);

                                // Convert LocalDate to LocalDateTime by appending a default time (00:00)
                                LocalDateTime dateTime = date.atStartOfDay();

                                // Save to database
                                financialWeekService.saveFinancialWeek(dateTime, financialYear, weekNumber, retailerId)
                            }
                        }
                    }
                    flash.message = "File processed and data saved successfully!"
                } catch (Exception e) {
                    e.printStackTrace()
                    importError = "Error occurred during saving of file to database"
                }


        redirect(action: "index")
    }
}
