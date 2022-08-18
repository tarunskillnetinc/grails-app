<%@ page import="java.math.RoundingMode" %>

<g:form name="add-product-form" method="post" action="save">
    <g:hiddenField name="id" value="${product?.id}"/>

    <g:if test="${product?.getEffectiveDatesForFutureChanges()?.size() > 1}">
        <div id="effectiveDates">
            <section id="effective-dates-container" class="container-fluid px-0">
                <div class="alert alert-warning alert-wl mx-0" role="alert">View changes to this product on: <g:select name="effectiveDatesPicker" from="${product?.getEffectiveDatesForFutureChanges()}" value="${effectiveDateIndex[0]}"/></div>
            </section>
        </div>
    </g:if>

    <div id="accordion">
        <!-- Product details. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="productDetails" data-toggle="collapse" data-target="#collapseProductDetails" aria-expanded="true" aria-controls="collapseProductDetails">
                <div class="row">
                    <div class="col-10"><strong>Product Details</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseProductDetails" class="collapse show" aria-labelledby="productDetails" data-parent="#accordion">
                <div class="card-body pt-5">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group mb-3">
                                <label for="effectiveDate" class="col-3 col-form-label text-right pr-4">Effective Date</label>
                                <g:textField name="effectiveDate" type="text" class="col-5 form-control bottom-border" value="${effectiveDateIndex ? effectiveDateIndex[1]?.toString('dd/MM/yyyy') : now?.toString('dd/MM/yyyy')}" autocomplete="off" />
%{--                                <g:datePicker name="effectiveDate" class="col-5 form-control bottom-border" precision="day" value="${effectiveDateIndex ? effectiveDateIndex[1].toDate() : new java.util.Date()}" />--}%
                            </div>
                            <div class="row form-group mb-3">
                                <label for="itemCode" class="col-3 col-form-label text-right pr-4">Item Code (PLU)</label>
                                <g:field type="text" name="itemCode" class="col-5 form-control bottom-border" value="${product?.itemCode}" onblur="itemCodeChanged(this.value);" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="description" class="col-3 col-form-label text-right pr-4">Description</label>
                                <g:textField name="description" class="col-9 form-control bottom-border" value="${product?.description}" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                                <g:textField name="receiptDescription" value="${product?.receiptDescription}" class="col-5 form-control bottom-border" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="unitSize" class="col-3 col-form-label text-right pr-4">Unit Size</label>
                                <g:textField name="unitSize" class="col-3 form-control bottom-border" value="${product?.unitSize ?: 'EACH'}"/>
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                            <div class="row form-group">
                                <span class="col-lg-3 col-form-label text-right pr-4">Category</span>

                                <div class="col-lg-9 pt-2" style="max-height: 300px; overflow-y: scroll;">
                                    <g:render template="categorySelect" model="[categories: categoryValues, productCategoryList: productCategoryList, selectedCategoryId: product?.category?.id, level: 1]" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- SKUs, barcode and suppliers. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="productVariants" data-toggle="collapse" data-target="#collapseProductVariants" aria-expanded="true" aria-controls="collapseProductVariants">
                <div class="row">
                    <div class="col-10"><strong>SKUs, Barcodes &amp; Suppliers</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseProductVariants" class="collapse" aria-labelledby="productVariants" data-parent="#accordion">
                <div class="card-body py-5">
                    <g:hiddenField name="relevantVariant" value="" />

                    <div class="row mx-5 table-wl bottom-border">
                        <div class="col-2 font-weight-bold">SKU</div>
                        <div class="col-2 font-weight-bold">Retail Price</div>
                        <div class="col-2 font-weight-bold">Cost Price</div>
                        <div class="col-2 font-weight-bold">Barcodes</div>
                        <div class="col-2 font-weight-bold">Packs</div>
                        <div class="col-2 font-weight-bold">&nbsp;</div>
                    </div>

                    <div id="variantsContainer">
                        <g:if test="${!product || !product?.variants}">
                            <div id="variant-0">
                                <g:render template="variant" model="[index: 0]" />
                            </div>
                        </g:if>

                        <g:each in="${product?.variants}" var="variant" status="i">
                            <g:if test="${(variant.storeId == null || variant.storeId == storeId) && product?.isCurrentProductVariant(effectiveDateIndex[1], variant.id, variant.sku)}">
                                <div id="variant-${i}">
                                    <g:render template="variant" model="[index: i, variant: variant, barcodes: variant.barcodes]" />
                                </div>
                            </g:if>
                        </g:each>
                    </div>

                    <div class="row mx-5 mt-3">
                        <a href="#" onclick="addVariant(null);" class="btn btn-wl">Add SKU</a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Product additional attributes. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="productAdditional" data-toggle="collapse" data-target="#collapseAdditional" aria-expanded="true" aria-controls="collapseAdditional">
                <div class="row">
                    <div class="col-10"><strong>Extended Attributes</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseAdditional" class="collapse collapsed" aria-labelledby="productAdditional" data-parent="#accordion">
                <div class="card-body py-5">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group">
                                <label for="vatCode" class="col-3 col-form-label text-right pr-4">VAT Code</label>
                                <g:select from="${vatValues}" name="vatCode" value="${product?.vatCode?.id}" optionKey="id" optionValue="${{(it?.description ? it.description + ' (' +it.percentage.setScale(1, java.math.RoundingMode.HALF_UP) +'%)' : String.valueOf(it.code) + ' (' +it.percentage.setScale(1, java.math.RoundingMode.HALF_UP) +'%)')}}" dataAttrs="[code: 'code']" class="col-5 form-control select-border" />
                            </div>
                            <div class="row form-group">
                                <label for="vatPercentageOverride" class="col-3 col-form-label text-right pr-4">VAT Override</label>
                                <g:textField name="vatPercentageOverride" value="${product?.vatPercentageOverride ?: '0.00'}" class="col-3 form-control bottom-border text-right mask-money" readonly="${product?.vatCode?.code != 'O'}" />
                            </div>
                            <div class="row mt-1 form-group">
                                <label for="discreetMessage" class="col-3 col-form-label text-right pr-4">Discreet Message</label>
                                <g:textField name="discreetMessage" value="${product?.discreetMessage}" class="col-5 form-control bottom-border" />
                            </div>
                            <div class="row mt-1 form-group">
                                <label for="status" class="col-3 col-form-label text-right pr-4">Status</label>
                                <g:select name="status" class="col-3 form-control select-border" from="${statusValues}" value="${product?.status}" valueMessagePrefix="ProductStatus" />
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="weightedItem" class="col-3 col-form-label text-right pr-4">Weighted Item</label>
                                <g:checkBox name="weightedItem" class="col-1 form-check-input wl-checkbox" checked="${product?.weightedItem}" disabled="${product?.openPrice || product?.zeroPrice}" />
                            </div>


                            <div class="row mt-1 pl-0 form-group">
                                <span class="col-3 col-form-label text-right pr-4">Pricing</span>

                                <div class="col-1 form-check form-check-inline">
                                    <g:radio class="form-check-input ml-2 wl-radio" type="radio" name="pricePerKg" id="pricePerKg" value="true" checked="${product?.pricePerKg && product?.weightedItem}" disabled="${!product?.weightedItem}" />
                                    <label class="form-check-label" for="pricePerKg">/kg</label>
                                </div>
                                <div class="col-2 form-check form-check-inline">
                                    <g:radio class="form-check-input wl-radio" type="radio" name="pricePerKg" id="pricePer100g" value="false" checked="${!product?.pricePerKg && product?.weightedItem}" disabled="${!product?.weightedItem}" />
                                    <label class="form-check-label" for="pricePer100g">/100g</label>
                                </div>
                            </div>

                            <fieldset ${(snappyEnabled?:"disabled")}>
                                <div class="row mt-1 form-group form-check pl-0">
                                    <label for="snappyProduct" class="col-3 col-form-label text-right pr-4">Snappy Shopper Item</label>
                                    <g:checkBox name="snappyProduct" class="col-1 form-check-input wl-checkbox" checked="${product?.snappyProduct}"/>
                                </div>
                            </fieldset>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="deliItem" class="col-3 col-form-label text-right pr-4">Deli Item</label>
                                <g:checkBox name="deliItem" class="col-1 form-check-input wl-checkbox" checked="${product?.deliItem}" disabled="${product?.openPrice || product?.zeroPrice}" />
                            </div>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="openPrice" class="col-3 col-form-label text-right pr-4">Open Price</label>
                                <g:checkBox name="openPrice" id = "openPrice" class="col-1 form-check-input wl-checkbox" checked="${product?.openPrice}" disabled="${product?.weightedItem || product?.deliItem}" />
                            </div>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="zeroPrice" class="col-3 col-form-label text-right pr-4">Zero Price</label>
                                <g:checkBox name="zeroPrice" class="col-1 form-check-input wl-checkbox" checked="${product?.zeroPrice}" disabled="${product?.weightedItem || product?.deliItem}" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Product restrictions. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="productRestrictions" data-toggle="collapse" data-target="#collapseRestrictions" aria-expanded="true" aria-controls="collapseRestrictions">
                <div class="row">
                    <div class="col-10"><strong>Restrictions</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseRestrictions" class="collapse collapsed" aria-labelledby="productRestrictions" data-parent="#accordion">
                <div id="restrictionsContainer">
                    <g:render template="restrictions" model="[restrictions: product?.restrictions, isNewProduct: isNewProduct]" />
                </div>
            </div>
        </div>

        <!-- Product promotions. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="productPromotions" data-toggle="collapse" data-target="#collapsePromotions" aria-expanded="true" aria-controls="collapsePromotions">
                <div class="row">
                    <div class="col-10"><strong>Promotions</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapsePromotions" class="collapse collapsed" aria-labelledby="productPromotions" data-parent="#accordion">
                <div class="card-body py-5">
                    <div class="row mx-5 table-wl bottom-border">
                        <div class="col-1 font-weight-bold">ID</div>
                        <div class="col-2 font-weight-bold">Type</div>
                        <div class="col-5 font-weight-bold">Description</div>
                        <div class="col-1 font-weight-bold">Start Date</div>
                        <div class="col-1 font-weight-bold">End Date</div>
                        <div class="col-2 font-weight-bold">Retailer Reference</div>
                    </div>

                    <div id="promotionsContainer">

                    </div>
                </div>
            </div>
        </div>

        <!-- Product prices. -->
        <sec:ifAnyGranted roles="ROLE_HEAD_OFFICE,ROLE_ENGINEER">
            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                <div class="card bg-light border-wl accordion-card">
                    <div class="card-header pointer" id="productPrices" data-toggle="collapse" data-target="#collapsePrices" aria-expanded="true" aria-controls="collapsePrices">
                        <div class="row">
                            <div class="col-10"><strong>Product Prices</strong></div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapsePrices" class="collapse collapsed" aria-labelledby="productPrices" data-parent="#accordion">
                        <div class="card-body py-5" id="pricesContainer">
                            <div class="row mx-5">This product is priced as follows:</div>

                            <div class="row mx-5 mt-4 table-wl bottom-border">
                                <div class="col-3 font-weight-bold">SKU</div>

                                <g:each in="${priceBands}" var="priceBand">
                                    <div class="col font-weight-bold">${priceBand.description}</div>
                                </g:each>
                            </div>

                            <g:if test="${isNewProduct}">
                                <g:render template="addPrice" model="[skuIndex: 0, variant: null, sku: null, priceBands: priceBands, zeroPrice: false]" />
                            </g:if>

                            <g:each in="${product?.variants?.findAll { it.storeId == null }}" var="variant" status="i">
                                <g:if test="${product?.isCurrentProductVariant(effectiveDateIndex[1], variant.id, variant.sku)}">
                                    <g:render template="addPrice" model="[skuIndex: i, variant: variant, sku: variant?.sku, priceBands: priceBands, zeroPrice: product?.zeroPrice]" />
                                </g:if>
                            </g:each>
                        </div>
                    </div>
                </div>
            </g:if>
        </sec:ifAnyGranted>

        <!-- Product ranges. -->
        <sec:ifAnyGranted roles="ROLE_HEAD_OFFICE,ROLE_ENGINEER">
            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                <div class="card bg-light border-wl accordion-card">
                    <div class="card-header pointer" id="productRanges" data-toggle="collapse" data-target="#collapseRanges" aria-expanded="true" aria-controls="collapseRanges">
                        <div class="row">
                            <div class="col-10"><strong>Product Ranges</strong></div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseRanges" class="collapse collapsed" aria-labelledby="productRanges" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="row mx-5">This product is assigned to the following product ranges:</div>

                            <div class="row mx-5 mt-4">
                                <%
                                    def productRanges = product?.ranges*.rangeId
                                %>
                                <g:each in="${ranges}" var="range">
                                    <div class="col">
                                        <label class="radio-container">${range.description}
                                            <g:checkBox name="rangeId" id="rangeId" checked="${productRanges?.contains(range.id)}" value="${range.id}" class="form-check-input" />

                                            <span class="checkmark"></span>
                                        </label>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
        </sec:ifAnyGranted>

        <!-- Product history. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header" id="productHistory">
                <div class="row">
                    <div class="col-10">Product History</div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseHistory" class="collapse collapsed" aria-labelledby="productHistory" data-parent="#accordion">
                <div class="card-body py-5">

                </div>
            </div>
        </div>
    </div>
</g:form>