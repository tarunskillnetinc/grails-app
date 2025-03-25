<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.BasketItemType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.TenderType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.BasketItemType" %>

<html>
<head>
    <meta name="layout" content="main"/>

    <title>Transaction Details - Receipt ${params.receiptId}</title>

    <asset:stylesheet src="receipt.css"/>
    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="validators/input-validator.js"/>

    <style>
@media (min-width: 1200px) {
    .container {
        max-width: 1363px;
    }
}

@media (min-width: 1400px) {
    .container {
        max-width: 1500px;
    }
}

#transaction-header-details .col-md-6 {
    max-width: 49%
}

    #transaction-details-table {
        max-width: 100%;
        overflow-x: auto;
    }

#transaction-details-table .col-1 { /* Make it 16 columns wide */
        font-size: 14px;
        max-width: 6.66666%
    }
    </style>
</head>

<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link
                            action="index">Transaction Search</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Transaction Details</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">Transaction Details</h2>
        </div>

        <div class="col-3 text-right">
            <button id="close-btn" class="btn btn-wl mt-1" onclick='history.back();'>Close</button>
        </div>
    </div>
</section>

<section id="errors-container" class="container-fluid">
</section>

<section id="transactions-container" class="container-fluid">
    <div class="container" id="transaction-header-details">
        <div class="row mt-3">
            <div class="col-md-6 card p-2 mr-auto">
                <h4 class="mx-auto">Transaction Reference</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Number:</div>

                        <div class="col-6">${receipt.transactionId}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Date &amp; Time:</div>

                        <div class="col-6"><g:formatDate format="dd/MM/yyyy HH:mm:ss"
                                                         date="${receipt.dateGenerated.toDate()}"
                                                         timeZone="Europe/London"/></div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Number:</div>

                        <div class="col-6">${receipt.transactionId}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Store:</div>

                        <div class="col-6">${store.config.storeName} - ${store.config.storeNumber}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Till:</div>

                        <div class="col-6">${receipt.tillId}</div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 card p-2">
                <h4 class="mx-auto">Operator Details</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Operator:</div>

                        <div class="col-6">${user.retailerUserId}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Operator Name:</div>

                        <div class="col-6">${user.name}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Operator Role:</div>

                        <div class="col-6">${user.role}</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-3">
            <div class="col-md-6 card p-2 mr-auto">
                <h4 class="mx-auto">Transaction Additional Details</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Loyalty Number:</div>

                        <div class="col-6">${basket.loyaltyMemberDetails?.memberId ?: "N/A"}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Discount Card:</div>

                        <div class="col-6">${basket.loyaltyMemberDetails?.guId ?: "N/A"}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">&nbsp;</div>

                        <div class="col-6">&nbsp;</div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 card p-2">
                <h4 class="mx-auto">Transaction</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Type:</div>

                        <div class="col-6">${receipt.paymentMethod}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Status:</div>

                        <div class="col-6">${receipt.voided ? "Voided" : "Complete"}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Total:</div>

                        <div class="col-6"><g:formatNumber
                                number="${receipt.transactionAmount ?: BigDecimal.ZERO}" type="currency"/></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="pt-2 pb-2 w-100">
                <h4>Transaction Details:</h4>

                <div id="transaction-details-table" class="table-responsive">
                    <div class="row mt-2 pb-2 ml-0 mr-0 table-wl bottom-border">
                        <div class="col-1 font-weight-bold">Sequence</div>

                        <div class="col-1 font-weight-bold">Type</div>

                        <div class="col-1 font-weight-bold">Entry<br/>Method</div>

                        <div class="col-1 font-weight-bold">Product<br/>Code</div>

                        <div class="col-1 font-weight-bold">Product<br/>Description</div>

                        <div class="col-1 font-weight-bold">Barcode</div>

                        <div class="col-1 font-weight-bold">Total<br/>Quantity</div>

                        <div class="col-1 font-weight-bold">Unit<br/>Price</div>

                        <div class="col-1 font-weight-bold">Total<br/>Price</div>

                        <div class="col-1 font-weight-bold">VAT</div>

                        <div class="col-1 font-weight-bold">Age<br/>Verification</div>

                        <div class="col-1 font-weight-bold">Return<br/>Reason</div>

                        <div class="col-1 font-weight-bold">Price<br/>Change</div>

                        <div class="col-1 font-weight-bold">RTC</div>

                        <div class="col-1 font-weight-bold">Promotions<br/>Type</div>
                    </div>

                <!--
                PaypointBasketItem.
                PaidInBasketItem :tick
                PaidOutBasketItem :tick

            -->
                    <g:set var="line" value="${0}"/>
                    <g:each in="${basketItems}" var="basketItem" status="seqNum">
                        <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.ProductBasketItem}">
                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                                <div id="sequence-id-${line + 1}" class="col-1 my-auto">${seqNum}</div>

                                <div id="type-id-${line + 1}" class="col-1 my-auto"><g:message
                                        code="BasketItemType.${basketItem.type}"/></div>

                                <div id="entrymethod-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.scanned ? "Scanned" : "Key-in"}</div>

                                <div id="productcode-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.product?.itemCode}</div>

                                <div id="productdescription-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.product?.description}</div>

                                <div id="barcode-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.barcodeScanned ?: "N/A"}</div>

                                <div id="totalquantity-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.qty ?: "N/A"}</div>

                                <g:if test="${basketItem.qty}">
                                    <div id="unitprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                            number="${basketItem.total / basketItem.qty}" type="currency"/></div>
                                </g:if>
                                <g:else>
                                    <div id="unitprice-id-${line + 1}" class="col-1 my-auto">-</div>
                                </g:else>

                                <div id="totalprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                        number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                                <div id="vat-id-${line + 1}" class="col-1 my-auto">
                                    <g:if test="${basketItem.priceDetails && basketItem.priceDetails.size() > 0}">
                                        <g:set var="vat_individual_total" value="${0}"/>
                                        <g:each in="${basketItem.priceDetails}" var="detail">
                                            <g:set var="vat_individual_total"
                                                   value="${vat_individual_total + detail.vatAmount}"/>
                                        </g:each>
                                        <g:formatNumber number="${vat_individual_total}" type="currency"/>
                                    </g:if>
                                    <g:else>
                                        N/A
                                    </g:else>
                                </div>

                                <div id="ageverification-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.ageRestricted ? "&#10003;" : "-"}</div>

                                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.RefundBasketItem}">
                                    <div id="returnreason-id-${line + 1}"
                                         class="col-1 my-auto">${basketItem.refundReasonOther + " " + basketItem.refundReason?.description + " " + basketItem.refundReason?.type}</div>
                                </g:if>
                                <g:else>
                                    <div id="returnreason-id-${line + 1}" class="col-1 my-auto">N/A</div>
                                </g:else>

                                <g:if test="${basketItem.markdownAmount}">
                                    <div id="pricechange-id-${seqNum + 1}" class="col-1 my-auto">
                                        <g:formatNumber number="${basketItem.markdownAmount}" type="currency"/>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div id="pricechange-id-${line + 1}" class="col-1 my-auto">
                                        N/A
                                    </div>
                                </g:else>

                                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.ReduceToClearBasketItem}">
                                    <div id="rtc-id-${line + 1}" class="col-1 my-auto">-</div>
                                </g:if>
                                <g:else>
                                    <div id="rtc-id-${line + 1}" class="col-1 my-auto">&#10003;</div>
                                </g:else>

                                <div id="promotionstype-id-${line + 1}" class="col-1 my-auto">-</div>
                            </div>
                            <g:set var="line" value="${line + 1}"/>
                        </g:if>

                        <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.PromotionBasketItem}">
                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                                <div id="sequence-id-${line + 1}" class="col-1 my-auto">${seqNum}</div>

                                <div id="type-id-${line + 1}" class="col-1 my-auto"><g:message
                                        code="BasketItemType.${basketItem.type}"/></div>

                                <div id="entrymethod-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.scanned ?: "N/A"}</div>

                                <div id="productcode-id-${line + 1}" class="col-1 my-auto">N/A</div>

                                <div id="productdescription-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.promotion?.description}</div>

                                <div id="barcode-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.barcodeScanned ?: "N/A"}</div>

                                <div id="totalquantity-id-${line + 1}" class="col-1 my-auto">N/A</div>

                                <div id="unitprice-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="totalprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                        number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                                <div id="vat-id-${line + 1}" class="col-1 my-auto">N/A</div>

                                <div id="ageverification-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="returnreason-id-${line + 1}" class="col-1 my-auto">N/A</div>

                                <div id="pricechange-id-${line + 1}" class="col-1 my-auto">N/A</div>

                                <div id="rtc-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="promotionstype-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.promotion?.type?.friendlyName ?: "N/A"}</div>
                            </div>
                            <g:set var="line" value="${line + 1}"/>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="card mt-3 mb-3 p-2 col-md-4">
                <h4 class="mx-auto">Transaction Discount Totals</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Staff Discount:</div>

                        <div class="col-6">1.23 TODO</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Promotion Savings:</div>

                        <div class="col-6">4.56 TODO</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="pt-2 pb-2 w-75">
                <h4>Tender Details</h4>

                <div id="tender-details-table">
                    <div class="row mt-2 pb-2 ml-0 mr-0 table-wl bottom-border">
                        <div class="col-2 font-weight-bold">Sequence</div>

                        <div class="col-2 font-weight-bold">Tender</div>

                        <div class="col-2 font-weight-bold">Tender Value</div>

                        <div class="col-4 font-weight-bold">Card Number</div>

                        <div class="col-2 font-weight-bold">Tender Status</div>
                    </div>
                </div>

                <g:set var="line" value="${0}"/>
                <g:each in="${basketItems}" var="basketItem" status="seqNum">
                    <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.TenderBasketItem}">
                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                            <div id="sequence-id-${line + 1}" class="col-2 my-auto">${seqNum}</div>

                            <div id="tender-id-${line + 1}" class="col-2 my-auto">
                                <g:message code="TenderType.${basketItem.tenderType}"/>
                            </div>

                            <div id="tendervalue-id-${line + 1}" class="col-2 my-auto"><g:formatNumber
                                    number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                            <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.CardTenderBasketItem}">
                                <div id="pan-id-${line + 1}" class="col-4 my-auto">${basketItem.pan}</div>
                            </g:if>
                            <g:else>
                                <div id="pan-id-${line + 1}" class="col-4 my-auto">-</div>
                            </g:else>

                            <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.CardTenderBasketItem}">
                                <div id="tenderstatus-id-${line + 1}"
                                     class="col-2 my-auto">${basketItem.authCode ?: ""}</div>
                            </g:if>
                            <g:else>
                                <div id="tenderstatus-id-${line + 1}" class="col-2 my-auto">-</div>
                            </g:else>
                        </div>
                        <g:set var="line" value="${line + 1}"/>
                    </g:if>
                </g:each>
            </div>
        </div>
    </div>
</section>

<section id="receipt-modal" class="container-fluid">
    <!-- Receipt modal -->
    <div class="modal fade" id="receiptModal" tabindex="-1" role="dialog" aria-labelledby="receiptModalLabel"
         aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Receipt Viewer</h2>
                </div>

                <div id="receiptModalContent"></div>

                <div class="modal-footer">
                    <button type="button" id="printReceiptButton" class="btn btn-info mr-auto"
                            onclick="printReceipt();">Print</button>
                    <button type="button" id="closeReceiptModalButton" class="btn btn-secondary"
                            data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</section>

<section id="receipt-details-modal" class="container-fluid">
    <!-- Receipt modal -->
    <div class="modal fade" id="receiptDetailsModal" tabindex="-1" role="dialog" aria-labelledby="receiptModalLabel"
         aria-hidden="true">
        <div class="superlarge-modal-dialog modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Transaction Details</h2>
                </div>

                <div id="receiptDetailsModalContent"></div>

                <div class="modal-footer">
                    <button type="button" id="closeReceiptDetailsModalButton" class="btn btn-secondary"
                            data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</section>
</body>
</html>