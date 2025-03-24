<div class="container">
    <div class="row">
        <div class="col-md-4">
            <h4>Transaction Reference:</h4>

            <div>
                <div><span>Transaction Number:</span> <span>${receipt.transactionId}</span></div>

                <div><span>Transaction Date &amp; Time:</span> <span><g:formatDate format="dd/MM/yyyy HH:mm:ss"
                                                                                   date="${receipt.dateGenerated.toDate()}"
                                                                                   timeZone="Europe/London"/></span>
                </div>

                <div><span>Store:</span> <span>${store.config.storeName} - ${store.config.storeNumber}</span></div>

                <div><span>Till:</span> <span>${receipt.tillId}</span></div>
            </div>
        </div>

        <div class="col-md-4">
            <h4>Operator Details:</h4>

            <div>
                <div><span>Operator:</span> <span>${user.retailerUserId}</span></div>

                <div><span>Operator Name:</span> <span>${user.name}</span></div>

                <div><span>Operator Role:</span> <span>${user.role}</span></div>
            </div>
        </div>

        <div class="col-md-4">
            <h4>Transaction:</h4>

            <div>
                <div><span>Transaction Type:</span> <span>${receipt.paymentMethod}</span></div>

                <div><span>Transaction Status:</span> <span>UNKNOWN</span></div>

                <div><span>Transaction Total:</span> <span><g:formatNumber
                        number="${receipt.transactionAmount ?: BigDecimal.ZERO}" type="currency"/></span></div>

            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Transaction Additional Details</h4>

            <div><span>Loyalty Number:</span> <span>${basket.loyaltyMemberDetails?.memberId ?: "N/A"}</span></div>

            <div><span>Discount Card:</span> <span>${basket.loyaltyMemberDetails?.guId ?: "N/A"}</span></div>
        </div>
    </div>

    <div class="row">
        <h4>Transaction Details:</h4>

        <div id="transaction-details-table" class="table-responsive">
            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
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

            <g:each in="${basketItems}" var="basketItem" status="seqNum">
                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.ProductBasketItem}">
                    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${seqNum % 2} hoverable">
                        <div id="sequence-id-${seqNum + 1}" class="col-1 my-auto">${seqNum}</div>

                        <div id="type-id-${seqNum + 1}" class="col-1 my-auto">${basketItem.type}</div>

                        <div id="entrymethod-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.scanned ?: "N/A"}</div>

                        <div id="productcode-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.product?.itemCode}</div>

                        <div id="productdescription-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.product?.description}</div>

                        <div id="barcode-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.barcodeScanned ?: "N/A"}</div>

                        <div id="totalquantity-id-${seqNum + 1}" class="col-1 my-auto">${basketItem.qty ?: "N/A"}</div>

                        <div id="unitprice-id-${seqNum + 1}" class="col-1 my-auto">unit price</div>

                        <div id="totalprice-id-${seqNum + 1}" class="col-1 my-auto"><g:formatNumber
                                number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                        <div id="vat-id-${seqNum + 1}" class="col-1 my-auto">VAT</div>

                        <div id="ageverification-id-${seqNum + 1}" class="col-1 my-auto">age verification</div>

                        <div id="returnreason-id-${seqNum + 1}" class="col-1 my-auto">return reason</div>

                        <div id="pricechange-id-${seqNum + 1}" class="col-1 my-auto">price change</div>

                        <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.ReduceToClearBasketItem}">
                            <div id="rtc-id-${seqNum + 1}" class="col-1 my-auto">-</div>
                        </g:if>
                        <g:else>
                            <div id="rtc-id-${seqNum + 1}" class="col-1 my-auto">&#10003;</div>
                        </g:else>

                        <div id="promotionstype-id-${seqNum + 1}" class="col-1 my-auto">-</div>
                    </div>
                </g:if>

                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.PromotionBasketItem}">
                    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${seqNum % 2} hoverable">
                        <div id="sequence-id-${seqNum + 1}" class="col-1 my-auto">${seqNum}</div>

                        <div id="type-id-${seqNum + 1}" class="col-1 my-auto">${basketItem.type}</div>

                        <div id="entrymethod-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.scanned ?: "N/A"}</div>

                        <div id="productcode-id-${seqNum + 1}" class="col-1 my-auto">N/A</div>

                        <div id="productdescription-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.promotion?.description}</div>

                        <div id="barcode-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.barcodeScanned ?: "N/A"}</div>

                        <div id="totalquantity-id-${seqNum + 1}" class="col-1 my-auto">${basketItem.qty ?: "N/A"}</div>

                        <div id="unitprice-id-${seqNum + 1}" class="col-1 my-auto">unit price</div>

                        <div id="totalprice-id-${seqNum + 1}" class="col-1 my-auto"><g:formatNumber
                                number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></div>

                        <div id="vat-id-${seqNum + 1}" class="col-1 my-auto">VAT</div>

                        <div id="ageverification-id-${seqNum + 1}" class="col-1 my-auto">age verification</div>

                        <div id="returnreason-id-${seqNum + 1}" class="col-1 my-auto">return reason</div>

                        <div id="pricechange-id-${seqNum + 1}" class="col-1 my-auto">price change</div>

                        <div id="rtc-id-${seqNum + 1}" class="col-1 my-auto">-</div>

                        <div id="promotionstype-id-${seqNum + 1}"
                             class="col-1 my-auto">${basketItem.promotion?.type?.friendlyName ?: "N/A"}</div>
                    </div>
                </g:if>
            </g:each>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Transaction Discount Totals</h4>

        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Tender Details</h4>

            <g:each in="${basketItems}" var="basketItem" status="seqNum">
                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.TenderBasketItem}">
                    <div>
                        <span>${seqNum}</span>
                        <span>${basketItem.tenderType}</span>
                        <span><g:formatNumber number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></span>
                        <span>${basketItem.receiptDescription}</span> <span>TODO TENDER STATUS</span> <span>TODO CHANGE</span>
                    </div>
                </g:if>

                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.CardTenderBasketItem}">
                    <div>
                        <span>${seqNum}</span>
                        <span>Card</span>XXX
                        <span><g:formatNumber number="${basketItem.total ?: BigDecimal.ZERO}" type="currency"/></span>
                        <span>${basketItem.pan}</span>
                        <span>${basketItem.authCode}</span>
                        <span>-</span>
                    </div>
                </g:if>

                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.ChangeBasketItem}">
                    <div><span>${basketItem.id}</span> <span>Change</span> <span><g:formatNumber
                            number="${basketItem.total ?: BigDecimal.ZERO}"
                            type="currency"/></span> <span>-</span> <span>-</span> <span>-</span>
                    </div>
                </g:if>


                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.CashbackTenderBasketItem}">
                    <div><span>${basketItem.id}</span> <span>Cashback</span> <span><g:formatNumber
                            number="${basketItem.total ?: BigDecimal.ZERO}"
                            type="currency"/></span> <span>-</span> <span>-</span> <span>-</span>
                    </div>
                </g:if>

            </g:each>
        </div>
    </div>
</div>
