<%@ page import="uk.co.wonderlane.wlpos.entities.basketv2.PaidOutBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.PaidInBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.CardTenderBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.TenderBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.DiscountBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.SimpleDiscountBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.PayPointBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.PromotionBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.ReduceToClearBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.RefundBasketItem; uk.co.wonderlane.wlpos.entities.basketv2.ProductBasketItem" contentType="text/html;charset=UTF-8" %>

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
    max-width: 49.5%
}

#transaction-details-table {
    max-width: 100%;
    overflow-x: auto;
}

overridewrap {
    word-wrap: break-word;
}

#transaction-details-table .col-1 { /* Make it 15 columns wide */
    font-size: 14px;
    max-width: 6.66666%;
    flex: 0 0 6.66666%;
    overflow: hidden;
}

#transaction-details-table .col-2 { /* Make it 15 columns wide */
    font-size: 14px;
    max-width: 10.33332%;
    flex: 0 0 10.33332%;
}

#transaction-details-table .col-3 { /* Make it 15 columns wide */
    font-size: 14px;
    max-width: 13.33332%;
    flex: 0 0 13.33332%;
}

#transaction-details-table .col-05 { /* Half a column */
    font-size: 14px;
    max-width: 3.3333%;
    flex: 0 0 3.3333%;
    position: relative;
    width: 100%;
    padding-right: 15px;
    padding-left: 15px;
}

#transaction-details-table .col-07 { /* Half a column and a bit */
    font-size: 14px;
    max-width: 5.3333%;
    flex: 0 0 5.3333%;
    position: relative;
    width: 100%;
    padding-right: 15px;
    padding-left: 15px;
    overflow: hidden;
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
    <g:if test="${flash.error}">
        <div id="error-message" class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        ${exception}
    </g:if>
</section>

<g:if test="${!flash.error}">
<section id="transactions-container" class="container-fluid mb-5">
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
                    <g:if test="${user.username}">
                        <div class="row">
                            <div class="col-6 text-right font-weight-bold">Operator:</div>

                            <div class="col-6">${user.username}</div>
                        </div>
                    </g:if>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Operator Name:</div>

                        <div class="col-6">${user.name}</div>
                    </div>

                    <g:if test="${user.role}">
                        <div class="row">
                            <div class="col-6 text-right font-weight-bold">Operator Role:</div>

                            <div class="col-6"><g:message code="Role.${user.role}"/></div>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>

        <div class="row mt-3">
            <div class="col-md-6 card p-2 mr-auto">
                <h4 class="mx-auto">Transaction Additional Details</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Loyalty Number:</div>

                        <div class="col-6">${basket.loyaltyMemberDetails?.memberId ?: "-"}</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Discount Card:</div>

                        <div class="col-6">${discountCard}</div>
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

                        <div class="col-6">Retail</div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Transaction Status:</div>

                        <div class="col-6">${basketTransaction.voided ? "Voided" : "Complete"}</div>
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
            <div class="pt-4 pb-2 w-100">
                <h4>Product Details</h4>

                <div id="transaction-details-table" class="table-responsive">
                    <div class="row mt-2 pb-2 ml-0 mr-0 table-wl bottom-border">
                        <div class="col-05 font-weight-bold">Seq</div>

                        <div class="col-07 font-weight-bold">Type</div>

                        <div class="col-1 font-weight-bold">Entry<br/>Method</div>

                        <div class="col-2 font-weight-bold overridewrap">Product<br/>Code</div>

                        <div class="col-3 font-weight-bold">Product<br/>Description</div>

                        <div class="col-2 font-weight-bold overridewrap">Barcode</div>

                        <div class="col-05 font-weight-bold">Total<br/>Qty</div>

                        <div class="col-1 font-weight-bold">Unit<br/>Price</div>

                        <div class="col-1 font-weight-bold">Total<br/>Price</div>

                        <div class="col-1 font-weight-bold">VAT</div>

                        <div class="col-05 font-weight-bold ">Age<br/>Check</div>

                        <div class="col-1 font-weight-bold">Reason<br/>Code</div>

                        <div class="col-1 font-weight-bold">Price<br/>Change</div>

                        <div class="col-05 font-weight-bold">RTC</div>

                        <div class="col-1 font-weight-bold">Promo<br/>Type</div>
                    </div>

                <!--
                PaypointBasketItem.
                PaidInBasketItem :tick
                PaidOutBasketItem :tick

            -->
                    <g:set var="line" value="${0}"/>
                    <g:set var="pre_discount_total" value="${0}"/>
                    <g:set var="discount_total" value="${0}"/>
                    <g:set var="grand_total" value="${0}"/>
                    <g:set var="promotion_total" value="${0}"/>

                    <g:each in="${basketItems}" var="basketItem" status="seqNum">
                        <g:if test="${basketItem instanceof ProductBasketItem || basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem}">
                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                                <div id="sequence-id-${line + 1}" class="col-05 my-auto">${seqNum + 1}</div>

                                <div id="type-id-${line + 1}" class="col-07 my-auto">
                                    <g:if test="${basketItem instanceof PaidInBasketItem}">
                                        <g:message code="ReasonCodeType.${basketItem.paidInReason.type}"/>
                                    </g:if>
                                    <g:elseif test="${basketItem instanceof PaidOutBasketItem}">
                                        <g:message code="ReasonCodeType.${basketItem.paidOutReason.type}"/>
                                    </g:elseif>
                                    <g:else>
                                        <g:message code="BasketItemType.${basketItem.type}"/>
                                    </g:else>
                                </div>

                                <div id="entrymethod-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.scanned ? "Scanned" : "Key-in"}</div>

                                <div id="productcode-id-${line + 1}" class="col-2 my-auto p-1 overridewrap">
                                    <g:if test="${basketItem instanceof ProductBasketItem}">
                                        ${basketItem.product?.itemCode}
                                    </g:if>
                                    <g:else>
                                        -
                                    </g:else>
                                </div>

                                <div id="productdescription-id-${line + 1}" class="col-3 my-auto p-1">
                                    <g:if test="${basketItem instanceof ProductBasketItem}">
                                        ${basketItem.product?.description}
                                    </g:if>
                                    <g:elseif test="${basketItem instanceof PaidInBasketItem}">
                                        ${basketItem.paidInReason.description}
                                    </g:elseif>
                                    <g:elseif test="${basketItem instanceof PaidOutBasketItem}">
                                        ${basketItem.paidOutReason.description}
                                    </g:elseif>
                                    <g:else>
                                        -
                                    </g:else>
                                </div>

                                <div id="barcode-id-${line + 1}"
                                     class="col-2 my-auto p-1 overridewrap">
                                    <g:if test="${basketItem.barcodeScanned}"><!-- if this top level is set, use that -->
                                        ${basketItem.barcodeScanned}
                                    </g:if>
                                    <g:else>
                                        <g:if test="${basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem}">
                                            -
                                        </g:if>
                                        <g:else>
                                            ${basketItem.product.variants[0].barcodes[0]} <!-- Tell me we're not recording >1 barcode per basket item... -->
                                        </g:else>
                                    </g:else>
                                </div>

                                <div id="totalquantity-id-${line + 1}"
                                     class="col-05 my-auto">${basketItem.qty ?: "-"}</div>

                                <g:if test="${basketItem.qty}">
                                    <div id="unitprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                            number="${basketItem.product.variants[0].retailPrice}"
                                            type="currency"/></div>
                                </g:if>
                                <g:else>
                                    <div id="unitprice-id-${line + 1}" class="col-1 my-auto">-</div>
                                </g:else>

                                <div id="totalprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                        number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                                <div id="vat-id-${line + 1}" class="col-1 my-auto">
                                    <g:if test="${basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem}">
                                        -
                                    </g:if>
                                    <g:elseif test="${basketItem.priceDetails && basketItem.priceDetails.size() > 0}">
                                        <g:set var="vat_individual_total" value="${0}"/>
                                        <g:each in="${basketItem.priceDetails}" var="detail">
                                            <g:set var="vat_individual_total"
                                                   value="${vat_individual_total + detail.vatAmount}"/>
                                        </g:each>
                                        <g:formatNumber number="${vat_individual_total}" type="currency"/>
                                    </g:elseif>
                                    <g:else>
                                        -
                                    </g:else>
                                </div>

                                <div id="ageverification-id-${line + 1}" class="col-05 my-auto">
                                    <g:if test="${basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem}">
                                        -
                                    </g:if>
                                    <g:elseif test="${basketItem.ageRestricted}">
                                        &#10003;
                                    </g:elseif>
                                    <g:else>
                                        <g:set var="age_restriction_value"
                                               value="${(basketItem.product?.category?.restrictions?.buyerAgeRestriction ?: 0 > (basketItem.product?.restrictions?.buyerAgeRestriction ?: 0)
                                                       ? basketItem.product?.category?.restrictions?.buyerAgeRestriction : basketItem.product?.restrictions?.buyerAgeRestriction)}"/>
                                        ${age_restriction_value ?: "-"}
                                    </g:else>
                                </div>

                                <g:if test="${basketItem instanceof RefundBasketItem}">
                                    <div id="returnreason-id-${line + 1}"
                                         class="col-1 my-auto">${basketItem.refundReason?.description}</div>
                                </g:if>
                                <g:else>
                                    <div id="returnreason-id-${line + 1}" class="col-1 my-auto">-</div>
                                </g:else>

                                <div id="pricechange-id-${seqNum + 1}" class="col-1 my-auto">
                                    <g:if test="${basketItem instanceof PaidInBasketItem || basketItem instanceof PaidOutBasketItem}">
                                        -
                                    </g:if>
                                    <g:elseif test="${basketItem.markdownAmount}">
                                        <g:formatNumber number="${-basketItem.markdownAmount}" type="currency"/>
                                    </g:elseif>
                                    <g:else>
                                        -
                                    </g:else>
                                </div>

                                <g:if test="${basketItem instanceof ReduceToClearBasketItem}">
                                    <div id="rtc-id-${line + 1}" class="col-05 my-auto">&#10003;</div>
                                </g:if>
                                <g:else>
                                    <div id="rtc-id-${line + 1}" class="col-05 my-auto">-</div>
                                </g:else>

                                <div id="promotionstype-id-${line + 1}" class="col-1 my-auto">-</div>
                            </div>

                            <g:set var="pre_discount_total" value="${pre_discount_total + basketItem.total}"/>
                            <g:set var="line" value="${line + 1}"/>
                        </g:if>

                        <g:if test="${basketItem instanceof PromotionBasketItem}">
                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                                <div id="sequence-id-${line + 1}" class="col-05 my-auto">${seqNum + 1}</div>

                                <div id="type-id-${line + 1}" class="col-07 my-auto"><g:message
                                        code="BasketItemType.${basketItem.type}"/></div>

                                <div id="entrymethod-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.scanned ?: "-"}</div>

                                <div id="productcode-id-${line + 1}" class="col-2 my-auto p-1 overridewrap">-</div>

                                <div id="productdescription-id-${line + 1}"
                                     class="col-3 my-auto p-1">${basketItem.promotion?.description}</div>

                                <div id="barcode-id-${line + 1}"
                                     class="col-2 my-auto p-1 overridewrap">${basketItem.barcodeScanned ?: "-"}</div>

                                <div id="totalquantity-id-${line + 1}" class="col-05 my-auto">-</div>

                                <div id="unitprice-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="totalprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                        number="${-basketItem.totalSavings ?: BigDecimal.ZERO}" type="currency"/></div>

                                <g:set var="promotion_total" value="${promotion_total + basketItem.totalSavings}"/>

                                <div id="vat-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="ageverification-id-${line + 1}" class="col-05 my-auto">-</div>

                                <div id="returnreason-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="pricechange-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="rtc-id-${line + 1}" class="col-05 my-auto">-</div>

                                <div id="promotionstype-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.promotion?.type?.friendlyName ?: "-"}</div>
                            </div>

                            <g:set var="pre_discount_total"
                                   value="${pre_discount_total - (basketItem.totalSavings ?: BigDecimal.ZERO)}"/>
                            <g:set var="line" value="${line + 1}"/>
                        </g:if>

                        <g:if test="${basketItem instanceof PayPointBasketItem}">
                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                                <div id="sequence-id-${line + 1}" class="col-05 my-auto">${seqNum + 1}</div>

                                <div id="type-id-${line + 1}" class="col-07 my-auto"><g:message
                                        code="BasketItemType.${basketItem.type}"/></div>

                                <div id="entrymethod-id-${line + 1}"
                                     class="col-1 my-auto">${basketItem.scanned ? "Scanned" : "Key-in"}</div>

                                <div id="productcode-id-${line + 1}"
                                     class="col-2 my-auto p-1 overridewrap"><g:message
                                        code="PPItemType.${basketItem.itemType}"/></div>

                                <div id="productdescription-id-${line + 1}"
                                     class="col-3 my-auto p-1">
                                    ${basketItem.basketDescription}
                                </div>

                                <div id="barcode-id-${line + 1}"
                                     class="col-2 my-auto p-1 overridewrap">${basketItem.barcodeScanned ?: "-"}</div>

                                <div id="totalquantity-id-${line + 1}"
                                     class="col-05 my-auto">-</div>

                                <div id="unitprice-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="totalprice-id-${line + 1}" class="col-1 my-auto"><g:formatNumber
                                        number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                                <div id="vat-id-${line + 1}" class="col-1 my-auto">
                                    -
                                </div>

                                <div id="ageverification-id-${line + 1}" class="col-05 my-auto">-</div>

                                <div id="returnreason-id-${line + 1}" class="col-1 my-auto">-</div>

                                <div id="pricechange-id-${line + 1}" class="col-1 my-auto">
                                    -
                                </div>

                                <div id="rtc-id-${line + 1}" class="col-05 my-auto">-</div>

                                <div id="promotionstype-id-${line + 1}" class="col-1 my-auto">-</div>
                            </div>

                            <g:set var="pre_discount_total" value="${pre_discount_total + basketItem.total}"/>
                            <g:set var="line" value="${line + 1}"/>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="card mt-3 mb-3 p-2 col-md-4">
                <h4 class="mx-auto">Transaction Totals</h4>

                <div class="col-md-12">
                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Discount Totals:</div>

                        <div class="col-6"><g:formatNumber number="${discount_total}" type="currency"/></div>
                    </div>

                    <div class="row">
                        <div class="col-6 text-right font-weight-bold">Promotion Savings:</div>

                        <div class="col-6"><g:formatNumber number="${promotion_total}" type="currency"/></div>
                    </div>
                </div>
            </div>
        </div>

        <g:if test="${!eventLines.isEmpty()}">
            <div class="row">
                <div class="card mt-3 mb-3 p-2 col-md-4">
                    <h4 class="mx-auto">Till Events/Actions</h4>

                    <g:each in="${eventLines}" var="event" status="i">
                        <div class="col-md-12">
                            <div class="row">
                                <div id="eventType-${i+1}" class="col-6 text-right font-weight-bold"><g:message code="TillControlEventType.${event?.eventType}"/></div>
                                <div id="eventUser-${i+1}" class="col-6">${event?.overrideUsersName}</div>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="pt-2 pb-2 w-75">
                <h4>Tender Details</h4>

                <div id="tender-details-table">
                    <div class="row mt-2 pb-2 ml-0 mr-0 table-wl bottom-border">
                        <div class="col-2 font-weight-bold">Sequence</div>

                        <div class="col-2 font-weight-bold">Tender</div>

                        <div class="col-2 font-weight-bold">Tender Value</div>

                        <div class="col-3 font-weight-bold">Card Number</div>

                        <div class="col-2 font-weight-bold">Tender Status</div>

                        <div class="col-1 font-weight-bold">Change</div>
                    </div>
                </div>

                <g:set var="line" value="${0}"/>
                <g:each in="${basketItems}" var="basketItem" status="seqNum">
                    <g:if test="${basketItem instanceof TenderBasketItem}">
                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                            <div id="sequence-id-${line + 1}" class="col-2 my-auto">${seqNum + 1}</div>

                            <div id="tender-id-${line + 1}" class="col-2 my-auto">
                                <g:if test="${basketItem.tenderType}"><!-- tendertype might not be a legal value -->
                                    <g:message code="TenderType.${basketItem.tenderType}"/>
                                </g:if>
                                <g:else>
                                    ${basketItem.name} <!-- in which case use the name instead -->
                                </g:else>
                            </div>

                            <div id="tendervalue-id-${line + 1}" class="col-2 my-auto"><g:formatNumber
                                    number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                            <g:if test="${basketItem instanceof CardTenderBasketItem}">
                                <div id="pan-id-${line + 1}" class="col-3 my-auto">${basketItem.pan}</div>
                            </g:if>
                            <g:else>
                                <div id="pan-id-${line + 1}" class="col-3 my-auto">-</div>
                            </g:else>

                            <g:if test="${basketItem instanceof CardTenderBasketItem}">
                                <div id="tenderstatus-id-${line + 1}"
                                     class="col-2 my-auto">${basketItem.voided ? "Voided" : "Success"}</div>
                            </g:if>
                            <g:else>
                                <div id="tenderstatus-id-${line + 1}" class="col-2 my-auto">-</div>
                            </g:else>

                            <div id="change-id-${line + 1}" class="col-1 my-auto">
                                <g:if test="${basketItem instanceof TenderBasketItem}">
                                    <g:if test="${basketItem.cashTender}">
                                        <g:formatNumber number="${basket.change ?: BigDecimal.ZERO}" type="currency"/>
                                    </g:if>
                                    <g:else>
                                        -
                                    </g:else>
                                </g:if>
                                <g:else>
                                    -
                                </g:else>
                            </div>
                        </div>
                        <g:set var="line" value="${line + 1}"/>
                    </g:if>
                </g:each>
                <g:if test="${line == 0}">
                    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${line % 2} hoverable">
                        <div id="no-tender" class="col-12 my-auto">
                            <div class="mx-auto">No Tender Details</div>
                        </div>
                    </div>
                </g:if>
            </div>
        </div>
    </div>
</section>
</g:if>

</body>
</html>