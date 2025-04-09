package uk.co.wonderlane.wlpos

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.dataaccess.MySqlPoolDal
import uk.co.wonderlane.wlpos.reporting.BasketTransaction
import uk.co.wonderlane.wlpos.reporting.BasketTransactionParameterContainer

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet

@Transactional("reporting")
class BasketTransactionService extends MySqlPoolDal {

    def gsonProvider
    def springSecurityService

    BasketTransactionService(databaseCredentials) {
        super(databaseCredentials)
    }

    class BasketTransactionTotal {
        Integer receiptId
        BigDecimal grandtotal

        BasketTransactionTotal(def inreceiptId, def ingrandtotal) {
            receiptId = inreceiptId
            grandtotal = ingrandtotal
        }
    }

    Map<Integer, BasketTransactionTotal> getBasketTransactionTotals(List<BasketTransactionParameterContainer> parameterList) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getBasketTransactionsAndGrandTotal(?, ?) }")

        Map<Integer, BasketTransactionTotal> totals = [:]

        if (parameterList.size == 0) {
            return totals;
        }

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setString(2, gsonProvider.gson.toJson(parameterList))

            ResultSet rs = cstmt.executeQuery()
            if (rs.next()) {
                def receiptId = rs.getInt("receiptId")
                def grandtotal = rs.getBigDecimal("grandtotal")

                totals.put(receiptId, new BasketTransactionTotal(receiptId, grandtotal))
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return totals
    }

    def getBasketTransactionByReceipt(Receipt receipt, Store store) {
        BasketTransaction basketTransactionDB = BasketTransaction.findByRetailerIdAndStoreIdAndTillIdAndTransactionId(springSecurityService.principal.retailerId, store.id, receipt.tillId, receipt.transactionId)

        uk.co.wonderlane.wlpos.entities.transactionv2.BasketTransaction basketTransaction = gsonProvider.getGson().fromJson(basketTransactionDB.transactionObject, uk.co.wonderlane.wlpos.entities.transactionv2.BasketTransaction.class);

        return basketTransaction
    }
}
