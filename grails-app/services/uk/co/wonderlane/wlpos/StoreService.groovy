package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.StoreAdditionalDetail

import java.lang.reflect.Type
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Types
import java.util.stream.Collectors

@Transactional
class StoreService extends MySqlDal {

    def springSecurityService
    def gsonProvider

    StoreService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getStore(int retailerId, int storeId) {
        return Store.findByRetailerIdAndId(retailerId, storeId)
    }

    def getStores(Collection<Integer> storeIds) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND id IN (:storeIds) ORDER BY s.id DESC", [retailerId: springSecurityService.principal.retailerId, storeIds: storeIds])
    }

    def getStoreByStoreNumber(int retailerId, Integer storeNumber) {
        return Store.find("FROM Store s WHERE s.retailerId = :retailerId AND (JSON_EXTRACT(config, '\$.storeNumber') = :storeNumber OR (:storeNumber IS NULL AND JSON_EXTRACT(config, '\$.storeType') = 'HEAD_OFFICE')) ORDER BY s.id DESC", [retailerId: retailerId, storeNumber: storeNumber])
    }

    def getStoreIdByStoreNumber(Integer storeNumber) {
        def store = getStoreByStoreNumber(springSecurityService.principal.retailerId, storeNumber)
        return store?.id
    }

    def getStores(int retailerId) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId])
    }

    def getActiveStores(int retailerId) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.deleted = 0 AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId])
    }

    def getStoresByType(int retailerId, StoreType storeType) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND JSON_EXTRACT(config, '\$.storeType') = :storeType ORDER BY s.id DESC", [retailerId: retailerId, storeType: storeType.name()])
    }

    def getStoresByRange(int retailerId, Range range) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.range = :range AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, range: range])
    }

    def getStoresByPriceBand(int retailerId, PriceBand priceBand) {
        return Store.findAll("FROM Store s WHERE s.retailerId = :retailerId AND s.priceBand = :priceBand AND JSON_EXTRACT(config, '\$.storeType') != 'HEAD_OFFICE' ORDER BY s.id DESC", [retailerId: retailerId, priceBand: priceBand])
    }

    def searchStores(int retailerId, Integer storeNumber, String storeName, boolean showDeleted, params) {
        String sortColumn = "JSON_EXTRACT(s.config, '\$.storeNumber')"

        if (params.sort == "storeName") {
            sortColumn = "JSON_EXTRACT(s.config, '\$.storeName')"
        } else if (params.sort == "storeType") {
            sortColumn = "JSON_EXTRACT(s.config, '\$.storeType')"
        }

        def queryParams = [retailerId: retailerId]
        def sortParams = [max: params.max, offset: params.offset, sort: sortColumn, order: params.order]

        if (storeNumber) {
            queryParams.storeNumber = storeNumber
        }
        if (storeName) {
            queryParams.storeName = "%$storeName%"
        }

        String queryString = """FROM Store s 
                                WHERE s.retailerId = :retailerId
                                AND JSON_EXTRACT(s.config, '\$.storeType') != 'HEAD_OFFICE' 
                                ${(showDeleted ? "" : " AND s.deleted = false")}
                                ${(storeNumber ? " AND JSON_EXTRACT(s.config, '\$.storeNumber') = :storeNumber" : "")} 
                                ${(storeName ? " AND JSON_EXTRACT(s.config, '\$.storeName') LIKE :storeName" : "")}
                                ORDER BY ${sortParams.sort} ${sortParams.order}"""

        def stores = Store.findAll(queryString, queryParams, sortParams)
        def storeCount = Store.executeQuery("SELECT COUNT(s) " +queryString, queryParams)

        return [stores, storeCount.first()]
    }

    def saveStore(StoreCommand store, String configString, String storeAdditionalDetail) {
        return doSaveStore(store, configString, storeAdditionalDetail)
    }

    // Needs to not be transactional otherwise Hibernate tries to save the store object rather than allowing the stored procedure to do it (well, it does both).
    @Transactional (readOnly = true)
    def saveStore(Store store) {
        return doSaveStore(store, store.configString, store.additionalDetails)
    }

    private void doSaveStore(def store, String configString, String storeAdditionalDetail) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveStore(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
            if (store.id && store.id > 0) {
                cstmt.setInt(1, store.id)
            } else {
                cstmt.setNull(1, Types.INTEGER)
            }

            cstmt.setInt(2, springSecurityService.principal.retailerId)
            if (store.parentStoreId) {
                cstmt.setInt(3, store.parentStoreId)
            } else {
                cstmt.setNull(3, Types.INTEGER)
            }
            cstmt.setInt(4, springSecurityService.principal.id)
            cstmt.setInt(5, store.priceBand.id)
            cstmt.setInt(6, store.range.id)
            cstmt.setString(7, store.retailerStoreId)
            cstmt.setBoolean(8, store.deleted)
            cstmt.setString(9, configString)
            cstmt.setString(10, storeAdditionalDetail)
            cstmt.executeUpdate()

            def resultSet = cstmt.getResultSet()
            if (resultSet.next()) {
                store.id = resultSet.getInt("storeId")
            }
        } finally {
            cstmt.close()
            conn.close()
        }
    }

    String getAdditionalDetailsJsonString(List<StoreAdditionalDetailCommand> storeAdditionalDetails){
        List<StoreAdditionalDetail> storeAdditionalDetailList  = convertToStoreAdditionalDetailList(storeAdditionalDetails)
        Type listType = new TypeToken<List<StoreAdditionalDetail>>(){}.getType()
        return gsonProvider.gson.toJson(storeAdditionalDetailList, listType)
    }

    private List<StoreAdditionalDetail> convertToStoreAdditionalDetailList(List<StoreAdditionalDetailCommand> storeAdditionalDetails) {
        return storeAdditionalDetails.stream()
                .map(command -> {
                    if (command != null) {
                        StoreAdditionalDetail detail = new StoreAdditionalDetail();
                        detail.setDescription(command.getDescription());
                        detail.setValue(command.getValue());
                        return detail;
                    }
                })
                .collect(Collectors.toList());
    }
}