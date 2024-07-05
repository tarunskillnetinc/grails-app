import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import uk.co.wonderlane.wlpos.enums.ReceiptLineType

import java.math.RoundingMode
import java.text.NumberFormat

class ReceiptTagLib {

    def brandAssetsService

    def BASKET_ITEM_LENGTH = 35
    def RECEIPT_BARCODE_WIDTH = 450
    def RECEIPT_BARCODE_HEIGHT = 75

    final static int QTY_WIDTH = 8
    final static String EMPTY_QTY_COL = "&nbsp;".repeat(QTY_WIDTH)
    final static String KG_UNIT = "/kg"
    final static String HUNDRED_GRAM_UNIT = "/100g"

    def currencyFormatter = NumberFormat.getCurrencyInstance(Locale.UK)

    def receiptLine = { attrs, body ->
        def receiptLine = attrs.receiptLine

        BigDecimal qty = receiptLine.quantity
        BigDecimal total =  receiptLine.total
        String text = receiptLine.text
        Integer maxLen = attrs.maxTotalLength as Integer ?: 0

        switch (receiptLine.type) {
            case ReceiptLineType.IMAGE:
//            case ReceiptLineType.IMAGE_FROM_FILE:
                if (receiptLine.text == "1") { //If we're the Logo receipt line, rather than the ReceiptImage receipt line
                    def brandLogo = brandAssetsService.getBrandLogo()

                    if (brandLogo) {
                        def brandLogoBase64 = new String(Base64.getEncoder().encode(brandLogo))

                        out << """<div style="text-align: center;"><img id="brand-logo" src="data:image/png;base64,${brandLogoBase64}" style="width: 100%;" /></div>"""
                    } else {
                        out << """<div style="text-align: center;">${asset.image(src: "receipt_logo.png", class: "logo")}</div>"""
                    }

                    break
                } else {
                    //If we're the ReceiptImage receipt Line we do not want to display anything at this time.
                    break
                }
//            case ReceiptLineType.PP_IMAGE:
//                byte[] imageBytes = hexStringToByteArray(receiptLine.getText())
//
//                String dataUrl = "data:image/png;base64," + imageBytes.encodeBase64().toString()
//
//                out << """<div><img src="${dataUrl}" /></div>"""
//
//                break
//            case ReceiptLineType.PP_IMAGE_ORIGINAL_ONLY:
//                if (!attrs.duplicate) {
//                    byte[] imageBytes = hexStringToByteArray(receiptLine.getText())
//
//                    String dataUrl = "data:image/png;base64," + imageBytes.encodeBase64().toString()
//
//                    out << """<div><img src="${dataUrl}" /></div>"""
//                }
//
//                break
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
                    out << """<div><span class="qty">QTY${"&nbsp;".repeat(5)}</span><span class="desc">DESC</span><span class="total">UNIT"""
                        << """${"&nbsp;".repeat(Math.max(attrs.maxTotalLength - 3, 1))}"""
                        << """TOTAL</span></div>"""
                }
                break
            case ReceiptLineType.BASKET_ITEM:
                makeBasketItemLine(qty, text, total, maxLen, null)?.genHtml()
                break
            case ReceiptLineType.BASKET_ITEM_KG:
                makeBasketItemLine(qty, text, total, maxLen, KG_UNIT)?.genHtml()
                break
            case ReceiptLineType.BASKET_ITEM_100G:
                makeBasketItemLine(qty, text, total, maxLen, HUNDRED_GRAM_UNIT)?.genHtml()
                break
            case ReceiptLineType.TENDER_ITEM:
                out << """<div><span class="qty">${EMPTY_QTY_COL}</span></span>"""
                out << """<span class="desc">${receiptLine.text.substring(0, Math.min(BASKET_ITEM_LENGTH - (receiptLine.total ? currencyFormatter.format(receiptLine.total).length() : 0), receiptLine.getText().length()))}</span>"""
                out << """<span class="total">${receiptLine.total ? currencyFormatter.format(receiptLine.total) : ""}</span></div>"""
                break
            case ReceiptLineType.TENDER_HEADING:
            case ReceiptLineType.TOTAL:
                out << """<div class="tenderHeading"><spanclass="qty">${EMPTY_QTY_COL}</span><span class="desc">${receiptLine.text}</span><span class="total">${currencyFormatter.format(receiptLine.total ?: BigDecimal.ZERO)}</span></div>"""

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
//            case ReceiptLineType.BARCODE_CODE_128_A_ORIGINAL_ONLY:
//            case ReceiptLineType.BARCODE_CODE_128_C_ORIGINAL_ONLY:
//            case ReceiptLineType.BARCODE_EAN_13_ORIGINAL_ONLY:
//                if (!attrs.duplicate) {
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
//
//                    try {
//                        BitMatrix bitMatrix = new Code128Writer().encode(receiptLine.getText(), BarcodeFormat.CODE_128, RECEIPT_BARCODE_WIDTH, RECEIPT_BARCODE_HEIGHT)
//
//                        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream)
//
//                        String dataUrl = "data:image/png;base64," + byteArrayOutputStream.toByteArray().encodeBase64().toString()
//
//                        out << """<div style="text-align: center;"><img src="${dataUrl}" /></div>"""
//                    } catch (Exception e) {
//                        e.printStackTrace()
//                    } finally {
//                        byteArrayOutputStream.close()
//                    }
//                }
//
//                break
            case ReceiptLineType.BARCODE_TEXT:
                out << """<div class="message">${receiptLine.text}</div>"""

                break
            case ReceiptLineType.VAT_ITEM:
                out << """<div><span class="qty">${receiptLine.text}</span><span class="total">${currencyFormatter.format(receiptLine.quantity.setScale(2, RoundingMode.HALF_UP))}"""

                for (int i = 0; i < Math.max(attrs.maxVatLength - receiptLine.total.toString().length() + 1, 1); i++) {
                    out << """&nbsp;"""
                }

                out << """${currencyFormatter.format(receiptLine.total)}</span></div>"""

                break
            case ReceiptLineType.VAT_HEADINGS:
                out << """<div><span class="qty">DESCRIPTION</span><span class="total">TOTAL"""

                for (int i = 0; i < Math.max(attrs.maxVatLength - 1, 1); i++) {
                    out << """&nbsp;"""
                }

                out << """VAT</span></div>"""

                break
//            case ReceiptLineType.PP_SINGLE:
//                out << """<div class="ppmessage">${receiptLine.text}</div>"""
//
//                break
//            case ReceiptLineType.PP_DOUBLE:
//                out << """<div class="ppdouble">${receiptLine.text}</div>"""
//
//                break
//            case ReceiptLineType.PP_SINGLE_ORIGINAL_ONLY:
//                if (!attrs.duplicate) {
//                    out << """<div class="ppmessage">${receiptLine.text}</div>"""
//                }
//
//                break
//            case ReceiptLineType.PP_DOUBLE_ORIGINAL_ONLY:
//                if (!attrs.duplicate) {
//                    out << """<div class="ppdouble">${receiptLine.text}</div>"""
//                }
//
//                break
            default:
                out << """<div>DEFAULT - ${receiptLine.text}</div>"""

                break
        }
    }

    private class ReceiptViewerLine {
        final String col1
        final String col2
        final String col3

        ReceiptViewerLine(String col1, String col2, String col3) {
            this.col1 = col1 ?: ""
            this.col2 = col2 ?: ""
            this.col3 = col3 ?: ""
        }

        void genHtml() {
            out << """<div>"""
                    << """<span class="qty">${col1}</span>"""
                    << """<span class="desc">${col2}</span>"""
                    << """<span class="total">${col3}</span>"""
                    << """</div>"""
        }
    }

    private ReceiptViewerLine makeBasketItemLine(BigDecimal quantityVal, String descVal, BigDecimal totalVal, int maxLen, String weightedSuffix) {
        boolean weighted = weightedSuffix != null
        String desc = descVal ?: ""

        String total = totalVal == null
                ? ""
                : currencyFormatter.format(totalVal)

        String quantity = quantityVal == null
                ? ""
                : quantityVal.setScale(weighted ? 3 : 0).toString() + (weighted ? "kg" : "")

        String unit = quantityVal == null || totalVal == null
                ? ""
                : currencyFormatter.format(totalVal.divide(quantityVal, 2, RoundingMode.HALF_UP)) + (weightedSuffix ?: "")

        return new ReceiptViewerLine(
                quantity + "&nbsp;".repeat(QTY_WIDTH - quantity.length()),
                desc.substring(0, Math.min(BASKET_ITEM_LENGTH - (unit + " " + total).length(), desc.length())),
                unit + (!unit.isEmpty() ? "&nbsp;".repeat(Math.max(maxLen - total.length() + 1, 1)) : "") + total
        )
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length()

        byte[] data = new byte[len / 2]

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16))
        }

        return data
    }
}