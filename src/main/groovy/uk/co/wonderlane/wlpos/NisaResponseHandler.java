package uk.co.wonderlane.wlpos;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.wonderlane.wlpos.responses.wlim.NisaBrowserResponse;

public class NisaResponseHandler extends DefaultHandler {

    private static final String urlResponse = "urlResponse";
    private static final String orderId = "orderId";

    private NisaBrowserResponse nisaBrowserResponse;
    private StringBuilder elementValue;

    protected String currentElement = "";
    protected StringBuilder currentValue = new StringBuilder();



    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentValue == null) {
            currentValue = new StringBuilder();
        } else {
            currentValue.append(ch, start, length);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        nisaBrowserResponse = new NisaBrowserResponse();
    }

    public void startElement(String uri, String localName, String elementName) throws SAXException {
        currentElement = elementName;

        switch (currentElement) {
            case urlResponse:
                elementValue = new StringBuilder();
                break;
            case orderId:
                elementValue = new StringBuilder();
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String elementName) throws SAXException {

        switch (elementName) {
            case urlResponse:
                nisaBrowserResponse.setUrlResponse(currentValue.toString().trim());
                break;
            case orderId:
                nisaBrowserResponse.setOrderId(Integer.valueOf(currentValue.toString().trim()));
                break;
        }
        currentElement = "";
        currentValue.setLength(0);
    }

    public NisaBrowserResponse getResponse(String responseXmlBody) {
        nisaBrowserResponse.setResponseXMLString(responseXmlBody);
        return nisaBrowserResponse;
    }
}