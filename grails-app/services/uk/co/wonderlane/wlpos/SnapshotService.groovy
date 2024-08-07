package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.Snapshot
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional
class SnapshotService extends MySqlDal {

    def springSecurityService
    def gsonProvider
    def locationService

    protected static final String DATE_FORMAT = "yyyy-MM-dd";

    SnapshotService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSnapshots(DateTime fromDate, DateTime toDate) {
        List<Snapshot> snapshots = new ArrayList<>()

        Connection conn = getConnection()
        CallableStatement getSnapshotsStatement = conn.prepareCall("{ call getSnapshots(?, ?, ?, ?) }")

        try {
            getSnapshotsStatement.setInt(1, springSecurityService.principal.retailerId)
            getSnapshotsStatement.setInt(2, springSecurityService.principal.storeId)
            getSnapshotsStatement.setString(3, fromDate.toString(DATE_FORMAT))
            getSnapshotsStatement.setString(4, toDate.toString(DATE_FORMAT))

            ResultSet rs = getSnapshotsStatement.executeQuery()

            try {
                while (rs.next()) {
                    String snapshotJson = rs.getString("snapshot")
                    snapshots.add(gsonProvider.gson.fromJson(snapshotJson, Snapshot.class))
                }
            } finally {
                rs.close()
            }

        } finally {
            getSnapshotsStatement.close()
            conn.close()
        }

        return snapshots
    }

    def getSnapshot(int snapshotId) {
        Connection conn = getConnection()
        CallableStatement getSnapshotStatement = conn.prepareCall("{ call getSnapshot(?, ?, ?) }")

        try {
            getSnapshotStatement.setInt(1, springSecurityService.principal.retailerId)
            getSnapshotStatement.setInt(2, springSecurityService.principal.storeId)
            getSnapshotStatement.setInt(3, snapshotId)

            ResultSet rs = getSnapshotStatement.executeQuery()

            try {
                if (rs.next()) {
                    String snapshotJson = rs.getString("snapshot")

                    return gsonProvider.gson.fromJson(snapshotJson, Snapshot.class)
                }
            } finally {
                rs.close()
            }
        } finally {
            getSnapshotStatement.close()
            conn.close()
        }

        return null
    }

    def getSnapshotForLocation(Integer locationId) {
        Connection conn = getConnection()
        CallableStatement getSnapshotStatement = conn.prepareCall("{ call getLatestSnapshotForLocation(?, ?, ?) }")

        try {
            getSnapshotStatement.setInt(1, springSecurityService.principal.retailerId)
            getSnapshotStatement.setInt(2, springSecurityService.principal.storeId)
            if (locationId) {
                getSnapshotStatement.setInt(3, locationId)
            } else {
                getSnapshotStatement.setInt(3, getDefaultSafeLocation(springSecurityService.principal.retailerId, springSecurityService.principal.storeId).id)
            }

            ResultSet rs = getSnapshotStatement.executeQuery()

            try {
                if (rs.next()) {
                    String snapshotJson = rs.getString("snapshot")

                    return gsonProvider.gson.fromJson(snapshotJson, Snapshot.class)
                }
            } finally {
                rs.close()
            }
        } finally {
            getSnapshotStatement.close()
            conn.close()
        }

        Snapshot latest = new Snapshot(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, locationId)
        int latestId = saveSnapshot(latest)
        latest.setId(latestId)
        return latest
    }

    def saveSafeSnapshot(Snapshot snapshot) {
        Connection conn = getConnection()
        conn.setAutoCommit(false)
        CallableStatement saveSnapshotStatement = conn.prepareCall("{ call saveSnapshot(?, ?) }")

        try {
            saveSnapshotStatement.setInt(1, snapshot.getId())
            saveSnapshotStatement.setString(2, gsonProvider.gson.toJson(snapshot, Snapshot.class))
            saveSnapshotStatement.execute()

            Snapshot newSafe = new Snapshot(snapshot.retailerId, snapshot.storeId, snapshot.locationId)
            for (ReconciliationTotal total : snapshot.totals) {
                TenderTotal newTotal = new TenderTotal(total.tenderType)
                newTotal.value = total.value
                newSafe.expectedTotals.add(newTotal)
            }

            saveSnapshotStatement.setNull(1, Types.INTEGER)
            saveSnapshotStatement.setString(2, gsonProvider.gson.toJson(newSafe, Snapshot.class))
            saveSnapshotStatement.execute()
        } finally {
            saveSnapshotStatement.close()
            conn.commit()
            conn.close()
        }
    }

    def saveSnapshot(Snapshot snapshot) {
        Connection conn = getConnection()
        CallableStatement saveSnapshotStatement = conn.prepareCall("{ call saveSnapshot(?, ?) }")

        try {
            if (snapshot.id > 0) {
                saveSnapshotStatement.setInt(1, snapshot.getId())
            } else {
                saveSnapshotStatement.setNull(1, Types.INTEGER)
            }

            saveSnapshotStatement.setString(2, gsonProvider.gson.toJson(snapshot, Snapshot.class))
            ResultSet rs = saveSnapshotStatement.executeQuery()

            try {
                if (rs.next()) {
                    return rs.getInt("id")
                }
            } finally {
                rs.close()
            }
        } finally {
            saveSnapshotStatement.close()
            conn.close()
        }

        return 0
    }
}
