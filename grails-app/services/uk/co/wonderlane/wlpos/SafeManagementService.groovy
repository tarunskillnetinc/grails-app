package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.enums.SafeSessionStatus

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@Transactional
class SafeManagementService extends MySqlPoolDal {

    def springSecurityService
    def gsonProvider

    protected SafeManagementService(DatabaseCredentials databaseCredentials) throws SQLException {
        super(databaseCredentials)
    }

    def serviceMethod() {}

    SafeSession getOpenSafeSession(Integer safeId){
        List<SafeSession> safeSessions = getActiveSafeSession(safeId)
        return safeSessions.stream()
                .filter(session -> SafeSessionStatus.OPEN.equals(session.getSessionStatus()))
                .findFirst()
                .orElse(null)
    }

    def saveSafeSession(SafeSession safeSession) {
        Connection conn = getConnection()
        CallableStatement saveShiftStatement = conn.prepareCall("{ call saveSafeSession(?, ?, ?) }")
        try {
            if (safeSession.id > 0) {
                saveShiftStatement.setInt(1, safeSession.id)
            } else {
                saveShiftStatement.setNull(1, Types.INTEGER)
            }
            saveShiftStatement.setString(2, gsonProvider.gson.toJson(safeSession, SafeSession.class))
            saveShiftStatement.executeUpdate()
            int shiftId = saveShiftStatement.getInt(3)
            safeSession.setId(shiftId) // Update the shift object with the new ID
            return shiftId
        } catch (Exception ex) {
            log.error(String.format("Error persisting safe session for retailer: %d store: %d safeId: %d error: %s", safeSession.getRetailerId(), safeSession.getStoreId(), safeSession.getSafeId(), ex.getMessage()), ex)
            throw new RuntimeException(String.format("Error persisting safe session for retailer: %d store: %d safeId: %d error: %s", safeSession.getRetailerId(), safeSession.getStoreId(), safeSession.getSafeId(), ex.getMessage()), ex)
        } finally{
            saveShiftStatement.close()
            conn.close()
        }
    }


    def getActiveSafeSession(Integer safeId) {
        List<SafeSession> safeSessions = new ArrayList<>()
        Connection conn = getConnection()
        CallableStatement getSafeSessionStatement = conn.prepareCall("{ call getActiveSafeSession(?, ?, ?) }")
        try {
            getSafeSessionStatement.setInt(1, springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                getSafeSessionStatement.setInt(2, springSecurityService.principal.storeId)
            } else {
                getSafeSessionStatement.setNull(2, Types.INTEGER)
            }
            if (safeId != null) {
                getSafeSessionStatement.setInt(3, safeId)
            } else {
                getSafeSessionStatement.setNull(3, Types.INTEGER)
            }
            ResultSet rs = getSafeSessionStatement.executeQuery()
            try {
                while (rs.next()) {
                    String sessionJson = rs.getString("session")
                    safeSessions.add(gsonProvider.gson.fromJson(sessionJson, SafeSession.class))
                }
            } finally {
                rs.close()
            }
        }catch (Exception ex) {
            log.error(String.format("Error loading active safe session for retailer: %d safeId: %d error: %s", springSecurityService.principal.retailerId, safeId, ex.getMessage()), ex)
            throw new RuntimeException(String.format("Error loading active safe session for retailer: %d safeId: %d error: %s", springSecurityService.principal.retailerId, safeId, ex.getMessage()), ex)
        } finally {
            getSafeSessionStatement.close()
            conn.close();
        }
        return safeSessions
    }
}
