package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.cash.Shift

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional
class ShiftService extends MySqlDal {

    def springSecurityService
    def gsonProvider

    protected static final String DATE_FORMAT = "yyyy-MM-dd";

    ShiftService(String host, int port, String database, String username, String password) {
        super(host, port, database, username, password)
    }

    def getShifts(DateTime fromDate, DateTime toDate, Integer tillId) {
        List<Shift> shifts = new ArrayList<>()

        Connection conn = getConnection()
        CallableStatement getShiftsStatement = conn.prepareCall("{ call getShifts(?, ?, ?, ?, ?) }")

        try {
            getShiftsStatement.setInt(1, springSecurityService.principal.retailerId)
            getShiftsStatement.setInt(2, springSecurityService.principal.storeId)

            if (tillId != null) {
                getShiftsStatement.setInt(3, tillId)
            } else {
                getShiftsStatement.setNull(3, Types.INTEGER)
            }

            getShiftsStatement.setString(4, fromDate.toString(DATE_FORMAT))
            getShiftsStatement.setString(5, toDate.toString(DATE_FORMAT))

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
        CallableStatement saveShiftStatement = conn.prepareCall("{ call saveShift(?, ?) }")

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
}