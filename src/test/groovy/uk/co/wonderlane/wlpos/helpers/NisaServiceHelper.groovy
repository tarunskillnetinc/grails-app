package uk.co.wonderlane.wlpos.helpers


import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

import java.sql.Connection

class NisaServiceHelper extends NisaServiceNetworkTestHelper {


    NisaServiceHelper(DatabaseCredentials databaseCredentials, Connection connection) {
        super(databaseCredentials, connection)
    }

    protected String doRequest(String requestBody) throws IOException {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE ocs-b2b-document-response SYSTEM \"http://www.ntorder-uat.com/ocs-b2b-document-response.dtd\">\n" +
                "<ocs-b2b-document-response>\n" +
                "<header>\n" +
                "<document-info>\n" +
                "<document-uid>WL_NULL-NULL-2022-11-21T06:15:54.921Z12345</document-uid>\n" +
                "<document-date>2022-11-21 06:15:54.921</document-date>\n" +
                "</document-info>\n" +
                "</header>\n" +
                "<body>\n" +
                "<browser-response>\n" +
                "<urlResponse>http://www.ntorder-uat.com/tk/9e5d0d9640494954b4741f19acdddfb7/App_presentation/order_entry/ViewOrder.aspx?oip_id=90359096</urlResponse>\n" +
                "<orderId>3759</orderId>\n" +
                "</browser-response>\n" +
                "</body>\n" +
                "</ocs-b2b-document-response>"
    }
}
