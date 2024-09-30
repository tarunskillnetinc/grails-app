package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.transaction.FinancialWeek
import uk.co.wonderlane.wlpos.entities.transaction.Transaction
import uk.co.wonderlane.wlpos.enums.ShiftStatus

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@Transactional
class ShiftService extends MySqlDal {

    def springSecurityService
    def gsonProvider

    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    public static String DATE_PATTERN_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";

    ShiftService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getShifts(Integer tillId) {
        List<Shift> shifts = new ArrayList<>()

        Connection conn = getConnection()
        CallableStatement getShiftsStatement = conn.prepareCall("{ call getActiveShifts(?, ?, ?) }")

        try {
            getShiftsStatement.setInt(1, springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                getShiftsStatement.setInt(2, springSecurityService.principal.storeId)
            } else {
                getShiftsStatement.setNull(2, Types.INTEGER)
            }

            if (tillId != null) {
                getShiftsStatement.setInt(3, tillId)
            } else {
                getShiftsStatement.setNull(3, Types.INTEGER)
            }

            ResultSet rs = getShiftsStatement.executeQuery()

            try {
                while (rs.next()) {
                    String shiftJson = rs.getString("shift")

                    shifts.add(gsonProvider.gson.fromJson(shiftJson, Shift.class))
                }
            } finally {
                rs.close()
            }
        } finally {
            getShiftsStatement.close()
            conn.close();
        }

        return shifts
    }

    def getShift(int shiftId) {
        Connection conn = getConnection()
        CallableStatement getShiftStatement = conn.prepareCall("{ call getShift(?, ?, ?) }")

        try {
            getShiftStatement.setInt(1, springSecurityService.principal.retailerId)
            getShiftStatement.setInt(2, springSecurityService.principal.storeId)
            getShiftStatement.setInt(3, shiftId)

            ResultSet rs = getShiftStatement.executeQuery()

            try {
                if (rs.next()) {
                    String shiftJson = rs.getString("shift")

                    return gsonProvider.gson.fromJson(shiftJson, Shift.class)
                }
            } finally {
                rs.close()
            }
        } finally {
            getShiftStatement.close()
            conn.close()
        }

        return null
    }

    def saveShift(Shift shift) {
        Connection conn = getConnection()
        CallableStatement saveShiftStatement = conn.prepareCall("{ call saveShift(?, ?, ?) }")

        try {
            if (shift.id > 0) {
                saveShiftStatement.setInt(1, shift.id)
            } else {
                saveShiftStatement.setNull(1, Types.INTEGER)
            }

            saveShiftStatement.setString(2, gsonProvider.gson.toJson(shift, Shift.class))

            saveShiftStatement.executeUpdate()
        } finally {
            saveShiftStatement.close()
            conn.close()
        }
    }

    Shift createNewShift(int retailerId, int storeId, int tillId, boolean isTillControlEvent)  {
        try {
            def shift = getOpenShift(retailerId, storeId, tillId)
            if (shift == null || !(shift.getShiftStatus() == ShiftStatus.OPEN)) {
                //Check if shift is exists and shift is open if it is not create new shift
                shift = populateNewShift(retailerId, storeId, tillId, isTillControlEvent, false)
            } else if (!isTillControlEvent) {
                shift.setCustomerCount(shift.getCustomerCount() + 1)
            }
            return shift
        } catch (Exception ex) {
            log.error(String.format("Error creating shift for retailer id: %s store id: %s till id: %s error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
            throw new RuntimeException(String.format("Error creating shift for retailer id: %s store id: %s till id: %s error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
    }


    Shift getOpenShift(int retailerId, int storeId, int tillId) {
        Shift result = null;
        try (Connection conn = getConnection();
            CallableStatement getShiftStatement = conn.prepareCall("{ call getShiftForTill(?, ?, ?) }")) {
            getShiftStatement.setInt(1, retailerId)
            getShiftStatement.setInt(2, storeId)
            getShiftStatement.setInt(3, tillId)

            try (ResultSet rs = getShiftStatement.executeQuery()) {
                if (rs.next()) {
                    String shiftJson = rs.getString("shift")
                    result = gsonProvider.gson.fromJson(shiftJson, Shift.class)
                }
            }
        } catch (SQLException ex) {
            log.error(String.format("Sql error loading shift for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
            throw new RuntimeException(String.format("Sql error loading shift for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        } catch (Exception ex) {
            log.error(String.format("Unexpected error loading shift for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
            throw new RuntimeException(String.format("Unexpected error loading shift for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex)
        }
        return result
    }


    private Shift populateNewShift(int retailerId, int storeId, int tillId, boolean isTillControlEvent, boolean isAutoGenerated) throws Exception {
        int shiftNumber = getLastShiftNumber(retailerId, storeId, tillId) + 1
        FinancialWeek financialWeek = getFinancialWeek(retailerId);
        Shift shift = new Shift(retailerId, storeId, tillId, -1, new DateTime());
        shift.setCustomerCount(0);
        shift.setShiftNumber(shiftNumber);
        shift.setCashInDrawer(BigDecimal.ZERO);
        shift.setShiftStatus(ShiftStatus.OPEN);
        shift.setShiftOpenTime(convertDateTimeToString(new DateTime()));
        if (financialWeek != null){
            shift.setFinancialWeek(financialWeek);
        }
        saveShift(shift)
//        auditHandler.addAudit(shift, ShiftAction.OPEN, transaction, isAutoGenerated);
        return shift;
    }

    private Integer getLastShiftNumber(int retailerId, int storeId, int tillId) {
        int result = 0;
        try (Connection conn = getConnection();
             CallableStatement getShiftNumberStmt = conn.prepareCall("{ call getPreviousShiftNumberForTill(?, ?, ?) }")) {
            getShiftNumberStmt.setInt(1, retailerId);
            getShiftNumberStmt.setInt(2, storeId);
            getShiftNumberStmt.setInt(3, tillId);

            try (ResultSet rs = getShiftNumberStmt.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt("shiftNumber");
                }
            }
        } catch (SQLException ex) {
            log.error(String.format("Sql error loading last shift number for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex);
            throw new RuntimeException(String.format("Sql error loading last shift number for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex);
        } catch (Exception ex) {
            log.error(String.format("Unexpected error loading last shift number for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex);
            throw new RuntimeException(String.format("Unexpected error loading last shift number for from retailer: %d store: %d tillId: %d error: %s", retailerId, storeId, tillId, ex.getMessage()), ex);
        }
        return result;
    }

    List<Shift> getShiftsForNonExistingTills(List<Shift> shiftList){
        List<TillConfiguration> tillConfigList = getAllActiveTills()
        Set<Integer> shiftTillIds = new HashSet<>()

        for (Shift shift : shiftList) {
            shiftTillIds.add(shift.getTillId());
        }

        List<Shift> dummyShifts = new ArrayList<>();
        for (TillConfiguration tillConfig : tillConfigList) {
            if (!shiftTillIds.contains(tillConfig.getTillId())) {
                Shift dummyShift = new Shift();
                dummyShift.setId(0); // or some default value
                dummyShift.setRetailerId(tillConfig.getRetailerId())
                dummyShift.setStoreId(tillConfig.getStoreId())
                dummyShift.setTillId(tillConfig.getTillId())
                dummyShifts.add(dummyShift)
            }
        }
        return dummyShifts
    }



    private List<TillConfiguration> getAllActiveTills() {
        return TillConfiguration.createCriteria().list {
            isNotNull('serialNumber')
            eq('retailerId', springSecurityService.principal.retailerId)
//            if (springSecurityService.principal.storeId != null){
//                eq('storeId', springSecurityService.principal.storeId)
//            }
        } as List<TillConfiguration>
    }

    private FinancialWeek getFinancialWeek(int retailerId){
        FinancialWeek financialWeek =  null;
        Date currentDate = convertToSqlDate(new DateTime());
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall("{ call getFinancialWeek(?, ?) }")) {
            cstmt.setInt(1, retailerId);
            cstmt.setDate(2, currentDate);
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    financialWeek = new FinancialWeek();
                    financialWeek.setId(rs.getInt("id"));
                    financialWeek.setRetailerId(rs.getInt("retailerId"));
                    financialWeek.setStartDate(rs.getDate("startDate"));
                    financialWeek.setFinancialYear(rs.getString("financialYear"));
                    financialWeek.setWeekNumber(rs.getInt("weekNumber"));
                }
            }
        } catch (SQLException ex) {
            log.error(String.format("Sql error loading financial week from retailer: %d date: %s error: %s", retailerId, currentDate, ex.getMessage()), ex);
        } catch (Exception ex) {
            log.error(String.format("Unexpected error loading financial week from retailer: %d date: %s error: %s", retailerId, currentDate, ex.getMessage()), ex);
        }
        return financialWeek;
    }

    private  Date convertToSqlDate(DateTime date) {
        return new Date(date.withTimeAtStartOfDay().getMillis());
    }

    private String convertDateTimeToString(DateTime dateTime) {
        if(dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_PATTERN_YYYYMMDD_HHMMSS);
        return dateTime.toString(formatter);
    }

}