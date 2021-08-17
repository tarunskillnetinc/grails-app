import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

import java.text.NumberFormat

class ReceiptTagLib {

    def BASKET_ITEM_LENGTH = 23
    def RECEIPT_BARCODE_WIDTH = 450
    def RECEIPT_BARCODE_HEIGHT = 75

    def currencyFormatter = NumberFormat.getCurrencyInstance()

    def receiptLine = { attrs, body ->
        def receiptLine = attrs.receiptLine

        switch (receiptLine.type) {
            case ReceiptLineType.IMAGE:
                out << """<div style="text-align: center;">${asset.image(src: "receipt_logo.png", class: "logo")}</div>"""

                break
            case ReceiptLineType.HEADER:
            case ReceiptLineType.MODIFIER:
                out << """<p class="header">${receiptLine.text}</p>"""
                break
            case ReceiptLineType.HEADINGS:
                if (receiptLine.text == " ") {
                    out << """<br />"""
                } else if (receiptLine.text.contains("-")) {
                    out << """<hr class="dotted" />"""
                } else {
                    out << """<div><span class="qty">QTY</span><span class="desc">DESC</span><span class="total">TOTAL</span></div>"""
                }
                break
            case ReceiptLineType.BASKET_ITEM:
                out << """<div><span class="qty">"""

                int spaces = 3 - receiptLine.quantity.toString().length()
                while (spaces > 0) {
                    out << """&nbsp;"""
                    spaces--
                }

                out << """${receiptLine.quantity.toString()}</span>"""
                out << """<span class="desc">${receiptLine.text.substring(0, Math.min(BASKET_ITEM_LENGTH - currencyFormatter.format(receiptLine.getTotal()).length(), receiptLine.getText().length()))}</span>"""
                out << """<span class="total"${receiptLine.total ? currencyFormatter.format(receiptLine.total) : ""}</span></div>"""

                break
            case ReceiptLineType.TENDER_ITEM:
                out << """<div><span class="qty">&nbsp;&nbsp;&nbsp;</span></span><span class="desc">${receiptLine.text}</span><span class="total">${receiptLine.total ? currencyFormatter.format(receiptLine.total) : ""}</span></div>"""

                break
            case ReceiptLineType.TENDER_HEADING:
            case ReceiptLineType.TOTAL:
                out << """<div class="tenderHeading"><spanclass="qty">&nbsp;&nbsp;&nbsp;</span><span class="desc">${receiptLine.text}</span><span class="total">${currencyFormatter.format(receiptLine.total ?: BigDecimal.ZERO)}</span></div>"""

                break
            case ReceiptLineType.MESSAGE:
            case ReceiptLineType.HARDCODED_MESSAGE:
                out << """<div class="message">${receiptLine.text}</div>"""

                break
            case ReceiptLineType.H_LINE:
                out << """<hr />"""


                // If this is a duplicate receipt, we use the first H_LINE as the place where we add the "DUPLICATE" message.
                if (attrs.duplicate && attrs.firstHorizontalLine) {
                    out << """<div class="header">** DUPLICATE **</div>"""

                    // If there are no other modifier lines to add, then we add another H_LINE.
                    if (!attrs.containsModifiers) {
                        out << """<hr />"""
                    }
                }

                break
            case ReceiptLineType.BARCODE_CODE_128_A:
            case ReceiptLineType.BARCODE_CODE_128_B:
            case ReceiptLineType.BARCODE_CODE_128_C:
            case ReceiptLineType.BARCODE_EAN_13:
            case ReceiptLineType.QR_CODE:
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()

                try {
                    BitMatrix bitMatrix = new Code128Writer().encode(receiptLine.getText(), BarcodeFormat.CODE_128, RECEIPT_BARCODE_WIDTH, RECEIPT_BARCODE_HEIGHT)

                    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream)

                    String dataUrl = "data:image/png;base64," + byteArrayOutputStream.toByteArray().encodeBase64().toString()

                    out << """<div style="text-align: center;"><img src="${dataUrl}" /></div>"""
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    byteArrayOutputStream.close()
                }

                break
            case ReceiptLineType.BARCODE_TEXT:
                out << """<div class="message">${receiptLine.text}</div>"""

                break
            case ReceiptLineType.VAT_ITEM:
                out << """<div><span class="qty">${receiptLine.text}</span><span class="total">${currencyFormatter.format(receiptLine.quantity)}</span></div><br />"""

                break
            case ReceiptLineType.VAT_HEADINGS:
                out << """<div><span class="qty">DESCRIPTION</span><span class="total">TOTAL&nbsp;&nbsp;&nbsp;&nbsp;VAT</span></div> """

                break
            default:
                out << """<div>DEFAULT - ${receiptLine.text}</div>"""

                break
        }
    }
}