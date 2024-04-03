package uk.co.wonderlane.wlpos

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import grails.gorm.transactions.Transactional
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.wlim.ShelfEdgeLabel
import uk.co.wonderlane.wlpos.enums.wlim.LabelTemplateFieldType
import uk.co.wonderlane.wlpos.enums.wlim.PrintProcess
import uk.co.wonderlane.wlpos.enums.wlim.PrintType
import uk.co.wonderlane.wlpos.labelling.LabelTemplate
import uk.co.wonderlane.wlpos.labelling.LabelTemplateField
import uk.co.wonderlane.wlpos.labelling.LabelTemplateMapping

import java.math.RoundingMode
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional
class ShelfEdgeLabelService extends MySqlDal {

    def springSecurityService

    private final float HELVETICA_HEIGHT_ADJUSTMENT = 0.855f
    private final float HELVETICA_BOLD_HEIGHT_ADJUSTMENT = 0.65f

    ShelfEdgeLabelService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getLabelTemplateMappings(PrintProcess printProcess, PrintType printType) {
        return LabelTemplateMapping.findAllByRetailerIdAndPrintProcessAndPrintType(springSecurityService.principal.retailerId, printProcess, printType)
    }

    def getLabelTemplates(PrintProcess printProcess, PrintType printType) {
        def criteria = LabelTemplate.createCriteria()

        return criteria.list {
            eq ("retailerId", springSecurityService.principal.retailerId)
            labelTemplateMappings {
                eq ("printProcess", printProcess)
                eq ("printType", printType)
            }
        }
    }

    def getLabelTemplate(int labelTemplateId) {
        return LabelTemplate.findByIdAndRetailerId(labelTemplateId, springSecurityService.principal.retailerId)
    }

    def generatePdf(ProductList productList, LabelTemplate labelTemplate, PrintProcess printProcess, PrintType printType, Store storeSettings) {
        // Find our labels.
        List<ShelfEdgeLabel> shelfEdgeLabels = new ArrayList<>()

        def now = DateTime.now(DateTimeZone.UTC)

        productList?.productListItems?.each { ProductListItem productListItem ->
            ShelfEdgeLabel shelfEdgeLabel = new ShelfEdgeLabel()

            shelfEdgeLabel.setProductId(productListItem.productVariant?.product?.id)
            shelfEdgeLabel.setItemCode(productListItem.productVariant?.product?.itemCode)
            shelfEdgeLabel.setDescription(productListItem.productVariant?.product?.description)
            shelfEdgeLabel.setUnitSize(productListItem.productVariant?.product?.unitSize)

            def barcodes = productListItem.productVariant?.getBarcodes()
            shelfEdgeLabel.setEanCode(barcodes?.size() > 0 ? barcodes?.first()?.barcode : null)

            def prices = productListItem.productVariant?.getAllPrices()?.findAll { it.priceBand.id == springSecurityService.principal.priceBand.id }

            def barcodeEffective = barcodes?.size() > 0 ? barcodes?.first()?.effectiveDate : null
            def price = prices?.find { it.effectiveDate.isBefore(now) }

            def effectiveDateToUse = productListItem.productVariant?.effectiveDate
            if (barcodeEffective && barcodeEffective.isAfter(effectiveDateToUse)) {
                effectiveDateToUse = barcodeEffective
            }
            if (!productListItem.productVariant?.product?.zeroPrice && price && !productListItem.productVariant?.retailPrice && price.effectiveDate.isAfter(effectiveDateToUse)) {
                effectiveDateToUse = price.effectiveDate
            }

            def priceToUse = productListItem.productVariant?.product?.zeroPrice ? BigDecimal.ZERO.setScale(2) : (productListItem.productVariant?.retailPrice ?: (price?.price ?: BigDecimal.ZERO.setScale(2)))

            shelfEdgeLabel.setPrice(priceToUse)
            shelfEdgeLabel.setWasPrice(null) // TODO How are we getting previous price?
            shelfEdgeLabel.setUnitPrice(priceToUse) // TODO Figure out what this is.
            shelfEdgeLabel.setEmbeddedBarcode(false) // TODO How are we handling this?
            shelfEdgeLabel.setWeightedItem(productListItem.productVariant?.product?.weightedItem)
            shelfEdgeLabel.setPricePerKg(productListItem.productVariant?.product?.pricePerKg)
            shelfEdgeLabel.setEffectiveDate(effectiveDateToUse)
            shelfEdgeLabel.setPromotionEndDate(null)
            shelfEdgeLabel.setWasPriceEffectiveDate(null)
            shelfEdgeLabel.setQuantity(productListItem.quantity)
            shelfEdgeLabel.setPrintProcess(printProcess)

            shelfEdgeLabels.add(shelfEdgeLabel)
        }

        PDDocument doc = generatePdfDocument(shelfEdgeLabels, labelTemplate, printProcess, printType, storeSettings)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        doc.save(out)
        doc.close()

        return out.toByteArray()
    }

    def generatePdf(DateTime effectiveDate, LabelTemplate labelTemplate, PrintProcess printProcess, PrintType printType, Store storeSettings) {
        def labels = getShelfEdgeLabelsForDate(effectiveDate, printProcess)

        PDDocument doc = generatePdfDocument(labels, labelTemplate, printProcess, printType, storeSettings)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        doc.save(out)
        doc.close()

        return out.toByteArray()
    }

    private PDDocument generatePdfDocument(List<ShelfEdgeLabel> shelfEdgeLabels, LabelTemplate labelTemplate, PrintProcess printProcess, PrintType printType, Store storeSettings) throws Exception {
        PDDocument doc = new PDDocument()

        // Since the original logic was provided an expanded list of products (ie, the same product multiple times to account for multiple labels), I'm doing
        // the same thing here so that I don't have to change that logic further down as we know it works.
        List<ShelfEdgeLabel> expandedShelfEdgeLabels = new ArrayList<>();

        for (ShelfEdgeLabel shelfEdgeLabel : shelfEdgeLabels) {
            for (int i = 0 ; i < shelfEdgeLabel.getQuantity() ; i++) {
                expandedShelfEdgeLabels.add(shelfEdgeLabel);
            }
        }

        int labelsPerPage = labelTemplate.columns * labelTemplate.rows
        int pagesRequired = (int)Math.ceil((float)expandedShelfEdgeLabels.size() / (float)labelsPerPage)

        PDRectangle rec

        // work out the page size
        if (labelsPerPage == 1) { // If the page only contains a single label we assume it a continuous label fed printer so set the page size to be the label size
            rec = new PDRectangle((float)pt((float)labelTemplate.labelWidth), (float)pt((float)labelTemplate.labelHeight))
        } else { // Assume its an a4 page
            rec = PDRectangle.A4
        }

        // Just add a blank page so that the document doesn't error.
        if (pagesRequired == 0) {
            doc.addPage(new PDPage(rec))
        }

        if (labelTemplate.rows > 0 && labelTemplate.columns > 0) {
            // If Columns and Rows are not set to a sensible number then a blank PDF is returned

            for (int pageNumber = 1 ; pageNumber <= pagesRequired ; pageNumber++) {
                PDPage page = new PDPage(rec)

                doc.addPage(page)
                PDPageContentStream contentStream = new PDPageContentStream(doc, page)

                // Create this page of the document.
                generatePdfPage(doc, contentStream, page, labelTemplate, expandedShelfEdgeLabels, storeSettings, pageNumber, labelsPerPage, printProcess);

                // Rotate the label if its for the BT printer or if specified.
                if (printType == PrintType.BLUETOOTH_PRINTER || labelTemplate.rotateOrientation) {
                    page.setRotation(90);
                }

                contentStream.close();
            }
        }

        return doc
    }

    private void generatePdfPage(PDDocument doc, PDPageContentStream contentStream, PDPage page, LabelTemplate labelTemplate, List<ShelfEdgeLabel> shelfEdgeLabels,
                                 Store storeSettings, int pageNumber, int labelsPerPage, PrintProcess printProcess) throws Exception {

        // Loop this page of labels by row and column.
        for (int row = 0 ; row < labelTemplate.rows ; row++) {
            for (int col = 0 ; col < labelTemplate.columns ; col++) {
                // Calculate the index of the product we are drawing.
                int productIndex = ((pageNumber - 1) * labelsPerPage) + (row * labelTemplate.columns) + col;

                if (productIndex < (labelsPerPage * pageNumber) && productIndex < shelfEdgeLabels.size()) {
                    ShelfEdgeLabel shelfEdgeLabel = shelfEdgeLabels.get(productIndex);

                    // Loop through and draw the fields on the label.
                    for (LabelTemplateField field : labelTemplate.labelTemplateFields) {
                        double fieldX = storeSettings.config["selMarginLeft"] + labelTemplate.marginLeft + (col * labelTemplate.labelWidth) + (col * labelTemplate.marginBetweenColumns) + field.x
                        double fieldY = storeSettings.config["selMarginTop"] + labelTemplate.marginTop + (row * labelTemplate.labelHeight) + (row * labelTemplate.marginBetweenRows) + field.y
                        double lineFieldX2 = storeSettings.config["selMarginLeft"] + labelTemplate.marginLeft + (col * labelTemplate.labelWidth) + (col * labelTemplate.marginBetweenColumns) + field.width
                        double lineFieldY2 = storeSettings.config["selMarginTop"] + labelTemplate.marginTop + (row * labelTemplate.labelHeight) + (row * labelTemplate.marginBetweenRows) + field.height

                        switch (field.type) {
                            case LabelTemplateFieldType.PRODUCT_DESCRIPTION:
                                String productDescription = shelfEdgeLabel.getDescription();

                                if (field.maxCharacters != 0) {
                                    if (field.maxCharacters < productDescription.length()) {
                                        addProductDescriptionField(contentStream, page, field, productDescription.substring(0, field.getMaxCharacters()), fieldX, fieldY);
                                    } else {
                                        addProductDescriptionField(contentStream, page, field, productDescription, fieldX, fieldY);
                                    }
                                } else {
                                    addProductDescriptionField(contentStream, page, field, productDescription, fieldX, fieldY);
                                }

                                break;
                            case LabelTemplateFieldType.PRODUCT_PRICE:
                                String price = "";
                                boolean zeroPrice = false;
                                boolean penceOnlyPrice = false;
                                boolean isGramPrice = false;
                                BigDecimal productPrice;

                                if (shelfEdgeLabel.isWeightedItem()) {
                                    if (shelfEdgeLabel.isPricePerKg()) {
                                        productPrice = shelfEdgeLabel.getPrice();
                                    } else {
                                        productPrice = shelfEdgeLabel.getPrice().divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
                                        isGramPrice = true;
                                    }
                                } else {
                                    productPrice = shelfEdgeLabel.getPrice();
                                }

                                if (productPrice == null) {
                                    continue
                                }

                                if (productPrice.compareTo(BigDecimal.ZERO) == 0) {
                                    penceOnlyPrice = false;
                                } else if (productPrice.compareTo(BigDecimal.ONE) < 0) {
                                    penceOnlyPrice = true;
                                }

                                if (penceOnlyPrice) {
                                    if (shelfEdgeLabel.getPrice().compareTo(BigDecimal.ONE) < 0) {
                                        price = shelfEdgeLabel.getPrice().toString();
                                        price = price.substring(2, 4);
                                        if (price.startsWith("0")) {
                                            price = price.substring(1, 2);
                                        } else {
                                            price = price.substring(0, 2);
                                        }

                                        price = price + "p";
                                    }

                                    if (shelfEdgeLabel.isWeightedItem()) {
                                        // Price is always stored in Kg so if this is a /100g item then we need to divide this by 10 to display /100g.
                                        if (shelfEdgeLabel.isPricePerKg()) {
                                            price = price + "/kg";
                                        } else {
                                            price = String.valueOf(shelfEdgeLabel.getPrice().divide(new BigDecimal(10), 2, RoundingMode.HALF_EVEN));
                                            price = price.substring(2, 4);
                                            if (price.startsWith("0")) {
                                                price = price.substring(1, 2);
                                            } else {
                                                price = price.substring(0, 2);
                                            }
                                            price = price + "p/100g";
                                        }
                                    } else if (shelfEdgeLabel.isEmbeddedBarcode()) {
                                        price = "As Marked";
                                    }

                                    addProductPriceField(contentStream, page, labelTemplate, field, price, fieldX, fieldY, false, isGramPrice);
                                } else {

                                    // Price is over one pound
                                    if (shelfEdgeLabel.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                                        price = "${Character.toChars(163)}0.00";
                                        zeroPrice = true;
                                    } else {
                                        price = "${Character.toChars(163)}" + String.valueOf(shelfEdgeLabel.getPrice());
                                    }

                                    if (shelfEdgeLabel.isWeightedItem()) {
                                        // Price is always stored in Kg so if this is a /100g item then we need to divide this by 10 to display /100g.
                                        if (shelfEdgeLabel.isPricePerKg()) {
                                            if (!zeroPrice) {
                                                price = price + "/kg";
                                            } else {
                                                price = "${Character.toChars(163)}0/kg";
                                            }
                                        } else {
                                            if (!zeroPrice) {
                                                price = "${Character.toChars(163)}" + String.valueOf(shelfEdgeLabel.getPrice().divide(new BigDecimal(10))) + "/100g";
                                            } else {
                                                price = "${Character.toChars(163)}0/100g";
                                            }
                                        }
                                    } else if (shelfEdgeLabel.isEmbeddedBarcode()) {
                                        price = "As Marked";
                                    }

                                    addProductPriceField(contentStream, page, labelTemplate, field, price, fieldX, fieldY, false, isGramPrice);
                                }

                                break;
                            case LabelTemplateFieldType.BARCODE:
                                // Ensure the product has at an EAN assigned to it.
                                if ((shelfEdgeLabel.getEanCode() != null && !shelfEdgeLabel.getEanCode().isEmpty())) {
                                    addBarcodeField(doc, contentStream, page, field, shelfEdgeLabel.getEanCode(), fieldX, fieldY);
                                }

                                break;
                            case LabelTemplateFieldType.STORE_NAME:
                                if (storeSettings.storeName != null) { // TODO In the old version this was actually replaced with an SEL free text, confirm if we want this.
                                    addGenericTextField(contentStream, page, labelTemplate, field, storeSettings.storeName, fieldX, fieldY);
                                }

                                break;
                            case LabelTemplateFieldType.EFFECTIVE_DATE:
                                addGenericTextField(contentStream, page, labelTemplate, field, (field.getDefaultValue() != null ? field.getDefaultValue() + shelfEdgeLabel.getEffectiveDate().toString("dd/MM/yyyy") : shelfEdgeLabel.getEffectiveDate().toString("dd/MM/yyyy")), fieldX, fieldY);

                                break;
                            case LabelTemplateFieldType.WAS_PRICE:
                                // If its a promotional SEL, RTC or if all the conditions are just right, then assign was price from the SEL object.
//                                if (shelfEdgeLabel.getStockListType() == ProductListType.ReduceToClear) {
//                                    String wasPrice;
//
//                                    if (shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ONE) >= 0 || shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ZERO) == 0 ) {
//                                        wasPrice = (char)163 + String.valueOf(shelfEdgeLabel.getWasPrice());
//                                    } else {
//                                        wasPrice = String.valueOf(shelfEdgeLabel.getWasPrice());
//                                        wasPrice = wasPrice.substring(2, 4);
//                                        wasPrice = wasPrice + "p";
//                                    }
//
//                                    addProductPriceField(contentStream, page, labelTemplate, field, wasPrice, fieldX, fieldY, true, false);
//                                } else if (printProcess == PrintProcess.PROMOTIONAL_LABEL_BATCH || printProcess == PrintProcess.PROMOTIONAL_BARKER) {
//                                    if (shelfEdgeLabel.getPromotionType() == PromotionDisplayType.PercentageDiscount ||
//                                            shelfEdgeLabel.getPromotionType() == PromotionDisplayType.FixedAmountDiscount) {
//
//                                        String wasPrice;
//                                        if (shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ONE) >= 0 ||
//                                                shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ZERO) == 0 ) {
//
//                                            wasPrice = (char)163 + String.valueOf(shelfEdgeLabel.getWasPrice());
//                                        } else {
//                                            wasPrice = String.valueOf(shelfEdgeLabel.getWasPrice());
//                                            wasPrice = wasPrice.substring(2, 4);
//                                            wasPrice = wasPrice + "p";
//                                        }
//                                        addProductPriceField(contentStream, page, labelTemplate, field, wasPrice, fieldX, fieldY, true, false);
//                                    }
//                                } else if (shelfEdgeLabel.getWasPrice() != null) {
                                    // Only show the was price if the setting is switched on in till settings.
                                    // Ensure the was price is higher than the current price.
//                                    if (shelfEdgeLabel.getWasPrice().compareTo(shelfEdgeLabel.getPrice()) > 0) {
                                        // Ensure the was price was effective for at least 28 days.
//                                        if (Math.abs(shelfEdgeLabel.getEffectiveDate().getTime() - shelfEdgeLabel.getWasPriceEffectiveDate().getTime()) / (24 * 60 * 60 * 1000) >= 28) {
                                            // Ensure the percentage difference is at least equal to our threshold.
//                                            BigDecimal decrease = shelfEdgeLabel.getWasPrice().subtract(shelfEdgeLabel.getPrice());
//                                            BigDecimal percentageDifference = decrease.divide(shelfEdgeLabel.getWasPrice(), 2, RoundingMode.ROUND_HALF_UP).multiply(new BigDecimal(100));
//
//                                            if (percentageDifference.compareTo(tillSettings.getWasNowThreshold()) >= 0) {
//
//                                                String wasPrice;
//                                                if (shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ONE) >= 0 ||
//                                                        shelfEdgeLabel.getWasPrice().compareTo(BigDecimal.ZERO) == 0 ) {
//                                                    wasPrice = (char)163 + String.valueOf(shelfEdgeLabel.getWasPrice());
//                                                } else {
//                                                    wasPrice = String.valueOf(shelfEdgeLabel.getWasPrice());
//                                                    wasPrice = wasPrice.substring(2, 4);
//                                                    wasPrice = wasPrice + "p";
//                                                }
//                                                addProductPriceField(contentStream, page, labelTemplate, field, wasPrice, fieldX, fieldY, true, false);
//                                            }
//                                        }
//                                    }
//                                }

                                break;

                            case LabelTemplateFieldType.PROMOTION_END_DATE:
//                                if (shelfEdgeLabel.getPromotionEndDate() != null) {
//                                    String endDate = shelfEdgeLabel.getPromotionEndDate().toString("dd/MM/yyyy")
//                                    if (!endDate.matches("31/12/9999")) {
//                                        addGenericTextField(contentStream, page, labelTemplate, field, ("End Date: " + endDate), fieldX, fieldY);
//                                    }
//                                }

                                break;
                            case LabelTemplateFieldType.RTC_TITLE:
                                addGenericTextField(contentStream, page, labelTemplate, field, ("Reduced to Clear"), fieldX, fieldY);

                                break;
                            case LabelTemplateFieldType.UNIT_SIZE:
                                if (shelfEdgeLabel.getUnitSize() != null) {
                                    addGenericTextField(contentStream, page, labelTemplate, field, shelfEdgeLabel.getUnitSize(), fieldX, fieldY);
                                }

                                break;
                            case LabelTemplateFieldType.STRAPLINE:
//                                if (shelfEdgeLabel.getPromotionStrapline() != null) {
//                                    addWordWrapField(contentStream, page, labelTemplate, field, shelfEdgeLabel.getPromotionStrapline(), fieldX, fieldY);
//                                }
//
                                break;
                            case LabelTemplateFieldType.LINE:
                                addLineField(contentStream, page, field, fieldX, fieldY, lineFieldX2, lineFieldY2);

                                break;
                            case LabelTemplateFieldType.BARKER_TITLE:
                                if (field.getDefaultValue() != null) {
                                    addGenericTextField(contentStream, page, labelTemplate, field, field.getDefaultValue(), fieldX, fieldY);
                                } else {
                                    addGenericTextField(contentStream, page, labelTemplate, field, "Products included in this Promotion:", fieldX, fieldY);
                                }

                                break;
                            case LabelTemplateFieldType.BARKER_TEXT:
//                                if (shelfEdgeLabel.getBarkerTextLines() != null) {
//                                    String products = "";
//                                    for (String line: shelfEdgeLabel.getBarkerTextLines()) {
//                                        products += line.toString();
//                                    }
//
//                                    if (field.getMaxCharacters() != 0 && field.getMaxCharacters() < products.length() - 1) {
//                                        addProductDescriptionField(contentStream, page, labelTemplate, field, products.substring(0, field.getMaxCharacters()), fieldX, fieldY);
//                                    } else {
//                                        addProductDescriptionField(contentStream, page, labelTemplate, field, products, fieldX, fieldY);
//                                    }
//                                }
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    // No more labels to draw!
                    return;
                }
            }
        }
    }

//    private byte[] generatePdfImage(PDDocument doc) throws Exception {
//        // PDFRenderer can be used to draw a PDF page as an image.
//        PDFRenderer pdfRenderer = new PDFRenderer(doc);
//
//        BufferedImage combinedImage = null;
//        Graphics g = null;
//
//        // For each page, draw the image onto a larger image so that we get all pages in a single image.
//        for (int pageNumber = 0; pageNumber < doc.getNumberOfPages(); pageNumber++) {
//            BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageNumber, 203, ImageType.RGB);
//
//            // Initialising these here as I need to know the page heights and widths to create the combined image.
//            if (pageNumber == 0) {
//                combinedImage = new BufferedImage(pageImage.getWidth(), pageImage.getHeight() * doc.getNumberOfPages(), BufferedImage.TYPE_INT_ARGB);
//                g = combinedImage.getGraphics();
//            }
//
//            g.drawImage(pageImage, 0, pageImage.getHeight() * pageNumber, null);
//        }
//
//        doc.close();
//
//        // If there were some pages, return the bytes of the combined image.
//        if (combinedImage != null) {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ImageIO.write(combinedImage, "png", outputStream);
//            outputStream.flush();
//
//            byte[] imageBytes = outputStream.toByteArray();
//            outputStream.close();
//
//            return imageBytes;
//        }
//
//        return null;
//    }

    private void addProductDescriptionField(PDPageContentStream contentStream, PDPage page, LabelTemplateField field, String productDescription, double fieldX, double fieldY) throws Exception {
        if (productDescription == null || productDescription.isEmpty()) {
            return
        }

        PDFont font = PDType1Font.HELVETICA;
        int fontSize = field.getPreferredTextSize();

        float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * HELVETICA_HEIGHT_ADJUSTMENT;

        // Start working out how many lines we need to fit the text into the width provided.
        List<String> textLines = new ArrayList<>();

        String[] descriptionParts = productDescription.split(" ")

        boolean moreWords = true

        while (moreWords) {
            for (int i = descriptionParts.size(); i > 0; i--) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int j = 0; j < i; j++) {
                    stringBuilder.append(descriptionParts[j])
                    stringBuilder.append(" ")
                }

                String descriptionAttempt = stringBuilder.toString().trim()

                float textWidth = mm(font.getStringWidth(descriptionAttempt) / 1000 * fontSize);

                if (textWidth > field.getWidth()) {
                    if (i == 0) {
                        // Unable to fit this word in the space at all.
                        moreWords = false
                    }

                    continue
                } else {
                    // All fits on the line.
                    textLines.add(descriptionAttempt);

                    if (i == descriptionParts.size()) {
                        moreWords = false
                    } else {
                        stringBuilder = new StringBuilder()

                        for (int j = i ; j < descriptionParts.size() ; j++) {
                            stringBuilder.append(descriptionParts[j])
                            stringBuilder.append(" ")
                        }

                        descriptionParts = stringBuilder.toString().trim().split(" ")
                    }

                    break
                }
            }
        }

        float y = (float)y(page.getMediaBox().getHeight(), pt(fieldY)) - fontHeight;

        int maxLines = (int)(pt(field.getHeight()) / fontHeight);

        contentStream.beginText();
        contentStream.setFont(font, fontSize);

        for (int i = 0 ; i < textLines.size() && i < maxLines ; i++) {
            String textLine = textLines.get(i);

            // Calculate the actual X position of this line of text if it is set to be centrally aligned.
            float textWidth = mm(font.getStringWidth(textLine) / 1000 * fontSize);
            float x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);

            // Move the text to the correct location using a matrix.
            Matrix matrix = new Matrix();
            matrix.translate(x, y);
            contentStream.setTextMatrix(matrix);

            contentStream.showText(textLine);
            contentStream.newLine();

            y -= (fontHeight + 2);
        }

        contentStream.endText();
    }

    private void addProductPriceField(PDPageContentStream contentStream, PDPage page, LabelTemplate labelTemplate, LabelTemplateField field, String unitPrice, double fieldX, double fieldY, boolean strikeThrough, boolean isGramPrice) throws Exception {
        PDFont font = PDType1Font.HELVETICA_BOLD;

        float fontSize = field.getPreferredTextSize();
        float smallFontSize = field.getPreferredSmallTextSize();
        float gramPriceSizeOffset = 7.5f;
        // Calculate the font size which means the price fits in the space provided.
        float textWidth = calculateStringWidth(unitPrice, font, fontSize, smallFontSize);

        // Keep making the font smaller until the price fits into the space provided.
        // Gram prices are large due to the amount of text on screen so they need a bit of squeezing.
        if (isGramPrice) {
            while (textWidth + gramPriceSizeOffset > field.getWidth() && fontSize > 4) {
                fontSize -= 1f;
                smallFontSize -= 0.5f;
                textWidth = calculateStringWidth(unitPrice, font, fontSize, smallFontSize);
            }

        } else {
            while (textWidth > field.getWidth() && fontSize > 4) {
                fontSize -= 1f;
                smallFontSize -= 0.5f;
                textWidth = calculateStringWidth(unitPrice, font, fontSize, smallFontSize);
            }
        }

        float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * HELVETICA_BOLD_HEIGHT_ADJUSTMENT;

        // Calculate the actual X position if the text is set to be centrally aligned.
        float x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);
        float y = (float)y(page.getMediaBox().getHeight(), pt(fieldY)) - fontHeight;

        for (char c : unitPrice.toCharArray()) {
            contentStream.beginText();
            if (Character.isDigit(c)) {
                contentStream.setFont(font, fontSize);
            } else {
                contentStream.setFont(font, smallFontSize);
            }

            // Move the text to the correct location using a matrix.
            Matrix matrix = new Matrix();
            matrix.translate(x, y);
            contentStream.setTextMatrix(matrix);

            contentStream.showText(String.valueOf(c));
            contentStream.endText();

            x += pt(calculateStringWidth(String.valueOf(c), font, fontSize, smallFontSize));
        }

        // Now strike through if required.
        if (strikeThrough) {
            float finalX = x;
            x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);

            contentStream.setLineWidth(1.0f);
            contentStream.moveTo(x + 1, y + 1);
            contentStream.lineTo(finalX - 1, y + fontHeight - 1);
            contentStream.stroke();
        }
    }

    private float calculateStringWidth(String value, PDFont font, float fontSize, float smallFontSize) throws IOException {
        float totalWidth = 0;
        boolean afterSlash = false;
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c) && !afterSlash) {
                totalWidth += mm(font.getStringWidth(String.valueOf(c)) / 1000 * fontSize);
            } else {
                totalWidth += mm(font.getStringWidth(String.valueOf(c)) / 1000 * smallFontSize);
            }
            if (!afterSlash) {
                if (c == '/') {
                    afterSlash = true;
                }
            }
        }
        return totalWidth;
    }

    private void addBarcodeField(PDDocument doc, PDPageContentStream contentStream, PDPage page, LabelTemplateField field, String barcode, double fieldX, double fieldY) throws Exception {

        try {
            PDFont font = PDType1Font.HELVETICA;
            int fontSize = field.getPreferredTextSize();

            BarcodeFormat format = BarcodeFormat.CODE_128;

            // 29 is treated as an RTC barcode and has to use CODE128 as it doesn't have any checksums.
            if (barcode.startsWith("29")) {
                format = BarcodeFormat.CODE_128;
            } else {
                switch (barcode.length()) {
                    case 13:
                        format = BarcodeFormat.EAN_13
                        break
                    case 8:
                        format = BarcodeFormat.EAN_8
                        break
                    case 12:
                        format = BarcodeFormat.UPC_A
                        break
                    default:
                        format = BarcodeFormat.CODE_128
                }
            }

            // Work out the font height for the text below the barcode as we need to subtract this height from the barcode so that the whole lot fits into the space provided.
            float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * HELVETICA_HEIGHT_ADJUSTMENT;

            BitMatrix bm

            try {
                bm = new MultiFormatWriter().encode(barcode, format, field.getWidth(), field.getHeight() - (int)mm(fontHeight));
            } catch (Exception e) {
                // Any errors, try again just using Code 128.
                format = BarcodeFormat.CODE_128
                bm = new MultiFormatWriter().encode(barcode, format, field.getWidth(), field.getHeight() - (int)mm(fontHeight));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bm, "jpeg", outputStream);

            PDImageXObject img = JPEGFactory.createFromStream(doc, new ByteArrayInputStream(outputStream.toByteArray()));

            // Calculate the actual X position if the barcode is set to be centrally aligned.
            float x = (float)pt(fieldX);
            float y = (float)y(page.getMediaBox().getHeight(), pt(fieldY + field.getHeight()) - (int)fontHeight);

            contentStream.drawImage(img, (Float)x, (Float)y, (Float)pt(field.getWidth()), (Float)((float)pt(field.getHeight()) - (int)fontHeight));

            // Now draw the actual digits below the barcode.
            float textWidth = mm(font.getStringWidth(barcode) / 1000 * fontSize);

            x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);

            // Move the barcode text to the correct location using a matrix.
            Matrix matrix = new Matrix();
            matrix.translate((Float)x, (Float)(y - fontHeight));
            contentStream.setTextMatrix(matrix);

            contentStream.showText(barcode);
            contentStream.endText();
        } catch(Exception e) {
            e.printStackTrace()
            // this is blank as we don't care if this errors, we just don't want the barcode added to the label
        }
    }

//    public static String create128Barcode(String barcode, boolean typeB) {
//        int checkSum = 104;
//        String newBarcode = "";
//        for (int i = 0 ; i < barcode.length() ; i++) {
//            int ch = barcode.toCharArray()[i] - 32;
//
//            checkSum += ch * (i + 1);
//        }
//
//        checkSum = checkSum % 103;
//        if (checkSum > 94) {
//            checkSum += 100;
//        } else {
//            checkSum += 32;
//        }
//
//        // The Ì (Code128 value 104) is the Type B start flag. Then add the barcode, then the checksum character, ending with the end flag.
//        if (typeB){
//            newBarcode = "Ì" + barcode + new Character((char)checkSum).toString() + "Î";
//        }else{
//            newBarcode = barcode + new Character((char)checkSum).toString();
//        }
//
//        return newBarcode;
//    }

    private void addGenericTextField(PDPageContentStream contentStream, PDPage page, LabelTemplate labelTemplate, LabelTemplateField field, String text, double fieldX, double fieldY) throws Exception {
        PDFont font = PDType1Font.HELVETICA;

        int fontSize = field.getPreferredTextSize();

        float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * HELVETICA_HEIGHT_ADJUSTMENT;

        // Calculate how many characters we can fit into the space provided.
        int textChars = text.length();
        float textWidth = mm(font.getStringWidth(text.substring(0, textChars)) / 1000 * fontSize);

        // Keep removing characters until the description fits into the space provided.
        while (textWidth > field.getWidth() && textChars > 1) {
            textChars--;
            textWidth = mm(font.getStringWidth(text.substring(0, textChars)) / 1000 * fontSize);
        }

        // Calculate the actual X position if the text is set to be centrally aligned.
        float x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);
        float y = (float)y(page.getMediaBox().getHeight(), pt(fieldY)) - fontHeight;

        contentStream.moveTo(x, y);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);

        // Move the text to the correct location using a matrix.
        Matrix matrix = new Matrix();
        matrix.translate(x, y);
        contentStream.setTextMatrix(matrix);

        contentStream.showText(text.substring(0, textChars));
        contentStream.endText();
    }

    private double y(float pageHeight, double y) {
        return (pageHeight - y);
    }

    private float mm(float point) {
        return point * 0.3527777f;
    }

    private float mm(double point) {
        return point * 0.3527777f;
    }

    private double pt(double mm) {
        return mm / 0.3527777f;
    }

//    List<LabelTemplate> getLabelTemplatesByDataSource(int dataSourceId) {
//        return labelTemplateDal.getLabelTemplatesByDataSource(dataSourceId);
//    }

    private void addLineField(PDPageContentStream contentStream, PDPage page, LabelTemplateField field, double fieldX, double fieldY, double lineFieldX2, double lineFieldY2) throws Exception {
        float xStart = (float)pt(fieldX);
        float xEnd = (float)pt(lineFieldX2);

        float yStart = (float)y(page.getMediaBox().getHeight(), pt(fieldY));
        float yEnd = (float)y(page.getMediaBox().getHeight(), pt(lineFieldY2));

        contentStream.setLineWidth(2.0f);
        contentStream.moveTo(xStart, yStart);
        contentStream.lineTo(xEnd, yEnd);
        contentStream.stroke();
    }

//    private void addWordWrapField(PDPageContentStream contentStream, PDPage page, LabelTemplate labelTemplate, LabelTemplateField field, String inputString, double fieldX, double fieldY) throws Exception {
//        List <String> subWords = new ArrayList<String>();
//        String manipulatedString = inputString;
//        int wordsRemoved = 0;
//        boolean moveToNextLine = false;
//        boolean stringComplete = false;
//
//        PDFont font = PDType1Font.HELVETICA;
//        int fontSize = field.getPreferredTextSize();
//        float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * HELVETICA_HEIGHT_ADJUSTMENT;
//
//        // Start working out how many lines we need to fit the text into the width provided.
//        List<String> textLines = new ArrayList<>();
//
//        //Individual words included in the strapline
//        List<String> straplineWords = new ArrayList<String>();
//
//        for(String word : inputString.split(" ")) {
//            straplineWords.add(word);
//        }
//        while (!stringComplete) {
//
//            //Check to see if there are lines to add, if there are, add them to the string to be checked this time.
//            if (moveToNextLine) {
//                int wordsRemoveCount = wordsRemoved;
//                moveToNextLine = false;
//                manipulatedString = "";
//                while (wordsRemoveCount > 0) {
//                    manipulatedString += straplineWords.get(straplineWords.size() - wordsRemoveCount) + " ";
//                    wordsRemoveCount --;
//                }
//                wordsRemoved = 0;
//            }
//
//            //Calculate the width of the new string.
//            float textWidth = mm(font.getStringWidth(manipulatedString) / 1000 * fontSize);
//
//            //Check to see if its too long.
//            if (textWidth > field.getWidth()) {
//
//                if (!StringUtils.containsWhitespace(manipulatedString.substring(0, manipulatedString.length() - 1 ))) {
//                    //Generate sub words from the full words.
//                    subWords = removeLettersToFitWidth(manipulatedString, field.getWidth(), font, fontSize);
//
//                    for (String subWord : subWords) {
//                        textLines.add(subWord);
//                    }
//
//                    moveToNextLine = true;
//                } else {
//
//                    manipulatedString = manipulatedString.substring(0, manipulatedString.length() - straplineWords.get((straplineWords.size() - wordsRemoved) - 1).length() - 1);
//                    wordsRemoved ++;
//                }
//            } else {
//                // Check to see if the new string will fit on an existing line.
//                if (textLines.size() > 0) {
//                    String testLine = textLines.get(textLines.size() - 1) + " " + manipulatedString;
//
//                    float testTextWidth = mm(font.getStringWidth(testLine) / 1000 * fontSize);
//
//                    if (testTextWidth > field.getWidth()) {
//                        textLines.add(manipulatedString);
//                        moveToNextLine = true;
//                    } else {
//                        textLines.remove(textLines.size() - 1);
//                        textLines.add(testLine);
//                        moveToNextLine = true;
//                    }
//                } else {
//                    //First word/sub word, add it with no manipulation
//                    textLines.add(manipulatedString);
//                    moveToNextLine = true;
//                }
//            }
//
//            // If there have been no words removed this loop then all lines have been added
//            if (wordsRemoved == 0) {
//                stringComplete = true;
//            }
//        }
//
//        contentStream.beginText();
//        for (String word : textLines) {
//            float textWidth = mm(font.getStringWidth(word) / 1000 * fontSize);
//            float y = (float)y(page.getMediaBox().getHeight(), pt(fieldY)) - fontHeight;
//
//            contentStream.setFont(font, fontSize);
//
//            for (int i = 0 ; i < textLines.size() ; i++) {
//
//                String textLine = textLines.get(i);
//
//                // Calculate the actual X position of this line of text if it is set to be centrally aligned.
//                textWidth = mm(font.getStringWidth(textLine) / 1000 * fontSize);
//                float x = field.isCentrallyAligned() ? (float)pt(fieldX + (field.getWidth() - textWidth) / 2) : (float)pt(fieldX);
//
//                // Move the text to the correct location using a matrix.
//                Matrix matrix = new Matrix();
//                matrix.translate(x, y);
//                contentStream.setTextMatrix(matrix);
//
//                contentStream.showText(textLine);
//                contentStream.newLine();
//
//                y -= (fontHeight + 2);
//            }
//        }
//        contentStream.endText();
//    }

//    List<String> removeLettersToFitWidth(String inputText, float fieldwidth, PDFont font, int fontSize) throws IOException {
//        boolean wordComplete = false;
//        boolean subWordComplete = false;
//        String removedCharacters = "";
//        List<String> subWords = new ArrayList<String>();
//
//        while (!wordComplete) {
//
//            while (!subWordComplete) {
//
//                float textWidth = mm(font.getStringWidth(inputText) / 1000 * fontSize);
//
//                //Remove characters until the string fits in the space.
//                if (textWidth > fieldwidth) {
//                    removedCharacters += inputText.substring(inputText.length() - 1, inputText.length());
//                    inputText = inputText.substring(0, inputText.length() - 1);
//                } else {
//                    subWordComplete = true;
//                }
//            }
//
//            subWords.add(inputText);
//
//            if (subWordComplete && removedCharacters != "") {
//                int count = removedCharacters.length() - 1;
//                inputText = "";
//                subWordComplete = false;
//
//                //Push the removed letters back to be checked and generated.
//                while (count > -1) {
//                    inputText += removedCharacters.charAt(count --);
//                }
//                removedCharacters = "";
//            } else {
//                wordComplete = true;
//            }
//        }
//
//        return subWords;
//    }

    def getShelfEdgeLabelsForDate(DateTime effectiveDate, PrintProcess printProcess) {
        def results = []

        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call getShelfEdgeLabelsForDate(?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, springSecurityService.principal.storeId)
            cstmt.setNull(3, Types.TINYINT)
            cstmt.setString(4, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
            cstmt.setString(5, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))

            ResultSet rs = cstmt.executeQuery()

            try {
                def products = [:]
                def variants = [:]

                while (rs.next()) {
                    def product = [:]
                    product.id = rs.getInt("id")
                    product.itemCode = rs.getString("itemCode")
                    product.description = rs.getString("description")
                    product.unitSize = rs.getString("unitSize")
                    product.weightedItem = rs.getBoolean("weightedItem")
                    product.pricePerKg = rs.getBoolean("pricePerKg")
                    product.variants = []

                    products.put(product.id, product)
                }

                cstmt.getMoreResults()

                rs = cstmt.getResultSet()

                while (rs.next()) {
                    def variant = [:]
                    variant.id = rs.getInt("id")
                    variant.sku = rs.getLong("sku")
                    variant.productId = rs.getInt("productId")
                    variant.retailPrice = rs.getBigDecimal("retailPrice")
                    variant.unitPrice = rs.getBigDecimal("unitPrice")
                    variant.barcodes = []

                    variants.put(variant.sku, variant)

                    products.get(variant.productId)?.variants?.add(variant)
                }

                cstmt.getMoreResults()

                rs = cstmt.getResultSet()

                while (rs.next()) {
                    def sku = rs.getLong("sku")
                    def barcode = rs.getString("barcode")

                    variants.get(sku)?.barcodes?.add(barcode)
                }

                for (Object product : products.values()) {
                    ShelfEdgeLabel shelfEdgeLabel = new ShelfEdgeLabel()
                    shelfEdgeLabel.productId = product.id
                    shelfEdgeLabel.itemCode = product.itemCode
                    shelfEdgeLabel.description = product.description
                    shelfEdgeLabel.unitSize = product.unitSize
                    shelfEdgeLabel.isWeightedItem = product.weightedItem
                    shelfEdgeLabel.isPricePerKg = product.pricePerKg

                    if (product.variants.size() > 0) {
                        shelfEdgeLabel.price = product.variants[0].retailPrice
                        shelfEdgeLabel.unitPrice = product.variants[0].retailPrice

                        if (product.variants[0].barcodes.size() > 0) {
                            shelfEdgeLabel.eanCode = product.variants[0].barcodes[0]
                        }
                    } else {
                        continue
                    }

                    shelfEdgeLabel.isEmbeddedBarcode = false
                    shelfEdgeLabel.effectiveDate = effectiveDate
                    shelfEdgeLabel.promotionEndDate = null
                    shelfEdgeLabel.wasPrice = null
                    shelfEdgeLabel.wasPriceEffectiveDate = null
                    shelfEdgeLabel.quantity = 1
                    shelfEdgeLabel.printProcess = printProcess

                    results.add(shelfEdgeLabel)
                }
            } finally {
                rs.close()
            }
        } finally {
            cstmt.close()
            conn.close()
        }

        return results
    }

    def setProductHistoryPrintStatus(DateTime effectiveDate, int printStatus) {
        Connection conn = getConnection()
        CallableStatement cstmt = conn.prepareCall("{ call saveProductHistoryPrintStatus(?, ?, ?, ?, ?) }")

        try {
            cstmt.setInt(1, springSecurityService.principal.retailerId)
            cstmt.setInt(2, springSecurityService.principal.storeId)
            cstmt.setString(3, effectiveDate.withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
            cstmt.setString(4, effectiveDate.plusDays(1).withTimeAtStartOfDay().toString(DATE_TIME_FORMAT))
            cstmt.setInt(5, printStatus)

            cstmt.executeUpdate()
        } finally {
            cstmt.close()
            conn.close()
        }
    }
}