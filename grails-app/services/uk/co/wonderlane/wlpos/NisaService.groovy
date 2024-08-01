package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.supplier.SymbolGroupSubscription
import uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus
import uk.co.wonderlane.wlpos.responses.wlim.NisaBrowserResponse

import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.nio.charset.StandardCharsets
import java.sql.*
import java.util.zip.GZIPInputStream

@Transactional
class NisaService extends MySqlDal {

    def springSecurityService
    private String orderApiUrl = "/ocs-nisa/B2B/"
    protected static final String TIMESTAMP_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    private URL url

    NisaService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def generateXMLForOrder(Connection connection, uk.co.wonderlane.wlpos.entities.wlim.ProductList orderProductList) throws SQLException, IOException, ParserConfigurationException, SAXException {
        String nisaXml = getNisaRequest(orderProductList);
        String responseBody = doRequest(nisaXml);
        NisaBrowserResponse response = saveResponseValues(connection, responseBody, orderProductList.getId())
        return !Objects.equals(response.getUrlResponse(), null) ? response.getUrlResponse() : (String.valueOf(url).replace(orderApiUrl, ""))
    }

    private String getNisaRequest(uk.co.wonderlane.wlpos.entities.wlim.ProductList productList) throws SQLException, MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder()
        DateTime now = DateTime.now(DateTimeZone.UTC)
        productList.getProductListItems()
        SymbolGroupSubscription symbolGroupSubscription = getSymbolGroupSubscriptionByRetailerIdAndStoreId(springSecurityService.principal.retailerId, Integer.parseInt(productList.getStoreId()))
        String documentUid = createDocumentUid(symbolGroupSubscription, now)
        stringBuilder.append(getNisaRequestDocumentHeader(symbolGroupSubscription, documentUid, now))
        stringBuilder.append(getNisaRequestOrderHeader(symbolGroupSubscription, productList))
        stringBuilder.append(getNisaRequestLines(productList))
        stringBuilder.append(getRequestFooter())
        return stringBuilder.toString()
    }

    protected String doRequest(String requestBody) throws IOException {
        HttpURLConnection connection
        OutputStream outputStream
        InputStream inputStream
        try{
            connection = getHttpConnection()
            outputStream = new BufferedOutputStream(connection.getOutputStream())
            outputStream.write(requestBody.getBytes())
            outputStream.flush()

            inputStream = "gzip".equals(connection.getContentEncoding()) ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream()
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)

        }catch(Exception ex){
            log.error("Order create exception (Symbol group orders) calling api request , Exception " + ex.getMessage())
            throw ex
        }
    }

    private NisaBrowserResponse saveResponseValues(Connection connection, String responseBody, int productListId) throws ParserConfigurationException, IOException, SAXException, SQLException {
        StringReader stringReader = new StringReader(responseBody)
        InputSource inputSource = new InputSource(stringReader)

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        SAXParser saxParser = factory.newSAXParser()

        inputSource.setEncoding("UTF-8")
        DefaultHandler defaultHandler = new NisaResponseHandler()

        saxParser.parse(inputSource, defaultHandler)
        NisaBrowserResponse nisaBrowserResponse = ((NisaResponseHandler) defaultHandler).getResponse(responseBody)
        saveOrderResponse(connection, nisaBrowserResponse, productListId)
        return nisaBrowserResponse
    }

    private HttpURLConnection getHttpConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setConnectTimeout(300000)
        connection.setReadTimeout(300000)
        connection.setRequestMethod("POST")
        connection.addRequestProperty("Content-Type", "application/xml")
        connection.addRequestProperty("Accept-Encoding", "gzip")
        connection.setDoInput(true)
        connection.setDoOutput(true)
        return connection;
    }

    private String createDocumentUid(SymbolGroupSubscription symbolGroupSubscription, DateTime documentDate) {
        String documentUid = String.format("WL_%s-%s-%s", symbolGroupSubscription.getStoreId(), symbolGroupSubscription.getStoreIdentifier(), documentDate);
        return documentUid;
    }

    private String getNisaRequestDocumentHeader(SymbolGroupSubscription symbolGroupSubscription, String documentUid, DateTime now) throws SQLException, MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder();
        url = new URL(getApiUrl(symbolGroupSubscription.getSymbolGroupId()).concat(orderApiUrl));

        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stringBuilder.append(String.format("<!DOCTYPE ocs-b2b-document-request SYSTEM \"%s\">", url));
        stringBuilder.append("<ocs-b2b-document-request>");
        stringBuilder.append("<header>");
        stringBuilder.append("<authentication-info>");
        stringBuilder.append(String.format("<username>%s</username>", symbolGroupSubscription.getUsername()));
        stringBuilder.append(String.format("<password>%s</password>", symbolGroupSubscription.getPassword()));
        stringBuilder.append("</authentication-info>");
        stringBuilder.append("<document-info>");
        stringBuilder.append(String.format("<document-uid>%s</document-uid>", documentUid));
        stringBuilder.append(String.format("<document-date>%s</document-date>", now.toString(TIMESTAMP_FORMAT_STRING)));
        stringBuilder.append("</document-info>");
        stringBuilder.append("</header>");

        return stringBuilder.toString();
    }

    private String getNisaRequestOrderHeader(SymbolGroupSubscription symbolGroupSubscription, uk.co.wonderlane.wlpos.entities.wlim.ProductList productList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<body>");
        stringBuilder.append("<order-entry>");
        stringBuilder.append("<orderHeader>");
        stringBuilder.append(String.format("<memberOrderNumber>%s</memberOrderNumber>", (productList.getId())));
        stringBuilder.append(String.format("<scheme>%s</scheme>", formatSupplierString(productList.getSupplierReference())));
        stringBuilder.append(String.format("<store>%s</store>", symbolGroupSubscription.getStoreIdentifier()));
        stringBuilder.append("</orderHeader>");
        return stringBuilder.toString();
    }

    private String getNisaRequestLines(uk.co.wonderlane.wlpos.entities.wlim.ProductList productList) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        for (uk.co.wonderlane.wlpos.entities.wlim.ProductListItem productListItem : productList.getProductListItems()) {
            List<uk.co.wonderlane.wlpos.entities.wlim.PackLine> orderPackLines = getPackLinesByProductListId(productList.getId(), productListItem.getId(), productList.getType().toString());

            for (uk.co.wonderlane.wlpos.entities.wlim.PackLine orderPack : orderPackLines) {
                stringBuilder.append("<orderLine>");
                stringBuilder.append(String.format("<itemCode>%s</itemCode>", orderPack.getOrderCode()));
                stringBuilder.append(String.format("<quantity>%s</quantity>", orderPack.getQuantity()));
                stringBuilder.append("</orderLine>");
            }
        }

        return stringBuilder.toString();
    }

    private String getRequestFooter() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("</order-entry>");
        stringBuilder.append("</body>");
        stringBuilder.append("</ocs-b2b-document-request>");
        return stringBuilder.toString();
    }

    private SymbolGroupSubscription getSymbolGroupSubscriptionByRetailerIdAndStoreId(int retailerId, int storeId) throws SQLException {
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getSymbolGroupSubscription(?, ?, ?, ?)}")
            cstmt.setNull(1, Types.INTEGER)
            cstmt.setInt(2, 1)
            cstmt.setInt(3, retailerId)
            cstmt.setInt(4, storeId)
            cstmt.execute()
            return mapToSymbolGroupSubscription(cstmt)
        }catch(Exception ex){
            log.error("Order create exception (Symbol group orders) found when loading symbol group subscription by retailer id and store id , Exception " + ex.getMessage())
            throw ex
        }finally{
            if (conn != null){
                conn.close()
            }
        }
    }

    private void saveOrderResponse(Connection connection, NisaBrowserResponse nisaBrowserResponse, int productListId) throws SQLException {
        CallableStatement cstmt
        try{
            cstmt = connection.prepareCall("{ call saveOrderResponse(?, ?, ?) }")
            cstmt.setInt(1, productListId)
            cstmt.setInt(2, nisaBrowserResponse.getOrderId())
            cstmt.setString(3, nisaBrowserResponse.getUrlResponse())
            cstmt.execute();
        }catch(Exception ex){
            log.error("Order create exception (Symbol group orders) found when saving order response , Exception " + ex.getMessage())
            throw ex
        }
    }

    private String getApiUrl(int symbolGroupId) throws SQLException {
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getSymbolGroupApiUrl(?) }")
            cstmt.setInt(1, symbolGroupId);
            cstmt.execute();
            ResultSet rs = cstmt.executeQuery()
            if (rs.next()) {
                return rs.getString("apiUrl");
            }
        }catch(Exception ex){
            log.error("Order create exception (Symbol group orders) found when loading api url , Exception " + ex.getMessage())
            throw ex
        }finally {
            if (conn != null){
                conn.close()
            }
        }
        return null
    }

    private List<uk.co.wonderlane.wlpos.entities.wlim.PackLine> getPackLinesByProductListId(int productListId, int productListItemId, String productListType) throws SQLException {
        List<uk.co.wonderlane.wlpos.entities.wlim.PackLine> orderPackLines = new ArrayList<>()
        Connection conn
        CallableStatement cstmt
        try {
            conn = getConnection()
            cstmt = conn.prepareCall("{ call getPackLinesByProductListId(?, ?, ?) }")
            cstmt.setInt(1, productListId);
            cstmt.setInt(2, productListItemId);
            cstmt.setString(3, productListType);
            cstmt.execute();
            ResultSet rs = cstmt.getResultSet();
            while (rs.next()) {
                uk.co.wonderlane.wlpos.entities.wlim.PackLine packLine = new uk.co.wonderlane.wlpos.entities.wlim.PackLine();
                packLine.setPackId(rs.getInt("packId"));
                packLine.setOrderCode(rs.getString("orderCode"));
                packLine.setQuantity(rs.getInt("quantity"));
                orderPackLines.add(packLine);
            }
            return orderPackLines;
        }catch(Exception ex){
            log.error("Order create exception (Symbol group orders) found when loading pack lines by product list id , Exception " + ex.getMessage())
            throw ex
        }finally{
            if (conn != null){
                conn.close()
            }
        }
    }

    private SymbolGroupSubscription mapToSymbolGroupSubscription(CallableStatement cstmt) throws SQLException {
        SymbolGroupSubscription symbolGroupSubscription = new SymbolGroupSubscription();
        ResultSet rs = cstmt.getResultSet()
        while (rs.next()) {
            symbolGroupSubscription.setId(rs.getInt("id"));
            symbolGroupSubscription.setRetailerId(rs.getInt("retailerId"));
            symbolGroupSubscription.setStoreId(rs.getInt("storeId"));
            symbolGroupSubscription.setSymbolGroupId(rs.getInt("symbolGroupId"));
            symbolGroupSubscription.setActive(rs.getBoolean("active"));
            symbolGroupSubscription.setStoreIdentifier(rs.getString("storeIdentifier"));
            symbolGroupSubscription.setOrganisationIdentifier(rs.getString("organisationIdentifier"));
            symbolGroupSubscription.setUsername(rs.getString("username"));
            symbolGroupSubscription.setPassword(rs.getString("password"));
            symbolGroupSubscription.setLastProductDownload(new DateTime(rs.getDate("lastProductDownload")));
            symbolGroupSubscription.setLastPromotionDownload(new DateTime(rs.getDate("lastPromotionDownload")));
            symbolGroupSubscription.setAdditionalPassword(rs.getString("additionalPassword"));
            symbolGroupSubscription.setStatus(SymbolGroupSubscriptionStatus.valueOf(rs.getString("status")));
            symbolGroupSubscription.setError(rs.getString("error"));
            symbolGroupSubscription.setUpdateDate(new DateTime(rs.getDate("updateDate")));
        }
        return symbolGroupSubscription;
    }

    private String formatSupplierString(String supplier) {
        switch (supplier) {
            case "Nisa Way":
                return "WAY";
            case "Nisa FAC":
                return "FAC";
            default:
                return null;
        }
    }


}
