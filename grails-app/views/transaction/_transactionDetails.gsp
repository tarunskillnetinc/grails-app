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
            <div><span>Loyalty Number:</span> <span>${user.name}</span></div>

            <div><span>Discount Card:</span> <span>${user.name}</span></div>
        </div>
    </div>

    <div class="row">
        <h4>Transaction Details:</h4>
        <!-- TODO individual lines go here -->
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Transaction Discount Totals</h4>

            <g:each in="${basketItems}" var="basketItem">
                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.PromotionBasketItem}">
                    <div><span>${basketItem.promotion.description}</span> <span>${basketItem.promotion.totalSavings}</span>
                    </div>
                </g:if>
                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basket.PromotionBasketItem}">
                    <div><span>${basketItem.promotion.description}</span> <span>${basketItem.promotion.totalSavings}</span>
                    </div>
                </g:if>
            </g:each>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Tender Details</h4>

            <g:each in="${basketItems}" var="basketItem">
                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.TenderBasketItem}">
                    <div><span>${basketItem.id}</span>
                        <span>${basketItem.tenderType}</span>
                        <span><g:formatNumber number="${basketItem.total ?: BigDecimal.ZERO}"
                                              type="currency"/></span>
                        <span>${basketItem.receiptDescription}</span> <span>TODO TENDER STATUS</span> <span>TODO CHANGE</span>
                    </div>
                </g:if>

                <g:if test="${basketItem instanceof uk.co.wonderlane.wlpos.entities.basketv2.CardTenderBasketItem}">
                    <div><span>${basketItem.panSeq}</span>
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
