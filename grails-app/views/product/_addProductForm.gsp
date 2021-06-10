<g:form method="post" url="${product?.id == 0 ? "./add" : "./maintenance?productId=" + product?.id}">
    <g:hiddenField name="id" value="${product?.id}"/>

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
                                <label for="itemCode" class="col-3 col-form-label text-right pr-4">Item Code</label>
                                <g:textField name="itemCode" class="col-5 form-control bottom-border" value="${product?.itemCode}" onblur="itemCodeChanged(this.value);" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="description" class="col-3 col-form-label text-right pr-4">Description</label>
                                <g:textField name="description" class="col-9 form-control bottom-border" value="${product?.description}"/>
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
                                    <g:render template="categorySelect" model="[categories: categoryValues, selectedCategoryId: product?.category?.id, level: 1]" />
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
                            <g:if test="${variant.storeId == storeId}">
                                <div id="variant-${i}">
                                    <g:render template="variant" model="[index: i, variant: variant]" />
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
                                <g:select from="${vatValues}" name="vatCode" value="${product?.vatCode?.id}" optionKey="id" optionValue="description" dataAttrs="[code: 'code']" class="col-5 form-control select-border" />
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
                            <div class="row form-group">
                                <label for="vatPercentageOverride" class="col-3 col-form-label text-right pr-4">VAT Override</label>
                                <g:textField name="vatPercentageOverride" value="${product?.vatPercentageOverride ?: '0.00'}" class="col-3 form-control bottom-border text-right mask-money" readonly="${product?.vatCode?.code != 'O'}" disabled="${product?.vatCode?.code != 'O'}" />
                            </div>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="weightedItem" class="col-3 col-form-label text-right pr-4">Weighted Item</label>
                                <g:checkBox name="weightedItem" class="col-1 form-check-input wl-checkbox" checked="${product?.weightedItem}"/>
                            </div>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="openPrice" class="col-3 col-form-label text-right pr-4">Open Price</label>
                                <g:checkBox name="openPrice" class="col-1 form-check-input wl-checkbox" checked="${product?.openPrice}"/>
                            </div>
                            <div class="row mt-1 form-group form-check pl-0">
                                <label for="zeroPrice" class="col-3 col-form-label text-right pr-4">Zero Price</label>
                                <g:checkBox name="zeroPrice" class="col-1 form-check-input wl-checkbox" checked="${product?.zeroPrice}"/>
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
                <div class="card-body py-5">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.buyerIdRequired" class="col-4 col-form-label text-right pr-4">Age Restricted Item</label>
                                <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${product?.restrictions?.buyerIdRequired}" />
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.buyerIdForced" class="col-4 col-form-label text-right pr-4">ID Check Forced</label>
                                <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${product?.restrictions?.buyerIdForced}" disabled="${!product?.restrictions?.buyerIdRequired}" />
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.buyerAgeRestriction" class="col-4 col-form-label text-right pr-4">Customer Age Required</label>
                                <g:field name="restrictions.buyerAgeRestriction" type="number" value="${product?.restrictions?.buyerAgeRestriction}" class="col-2 form-control bottom-border" readonly="${!product?.restrictions?.buyerIdRequired}" />
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.buyerChallengeAge" class="col-4 col-form-label text-right pr-4">Customer Challenge Age</label>
                                <g:field name="restrictions.buyerChallengeAge" type="number" value="${product?.restrictions?.buyerChallengeAge}" class="col-2 form-control bottom-border" readonly="${!product?.restrictions?.buyerIdRequired}" />
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.sellerAgeRestriction" class="col-4 col-form-label text-right pr-4">Operator Age Required</label>
                                <g:field name="restrictions.sellerAgeRestriction" type="number" value="${product?.restrictions?.sellerAgeRestriction}" class="col-2 form-control bottom-border" readonly="${!product?.restrictions?.buyerIdRequired}" />
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.minOpenPrice" class="col-4 col-form-label text-right pr-4">Min Open Price</label>

                                <div class="input-group col-3 px-0">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">&pound;</span>
                                    </div>
                                    <g:textField name="restrictions.minOpenPrice" value="${product?.restrictions?.minOpenPrice ?: '0.01'}" class="form-control mask-money" readonly="${!product?.openPrice}" disabled="${!product?.openPrice}" />
                                </div>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.maxOpenPrice" class="col-4 col-form-label text-right pr-4">Max Open Price</label>

                                <div class="input-group col-3 px-0">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">&pound;</span>
                                    </div>
                                    <g:textField name="restrictions.maxOpenPrice" value="${product?.restrictions?.maxOpenPrice ?: '9999.99'}" class="form-control mask-money" readonly="${!product?.openPrice}" disabled="${!product?.openPrice}" />
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.refundAllowed" class="col-4 col-form-label text-right pr-4">Allow Refunds</label>
                                <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product?.restrictions?.refundAllowed}"/>
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.discountAllowed" class="col-4 col-form-label text-right pr-4">Allow Discounts</label>
                                <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product?.restrictions?.discountAllowed}"/>
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.markdownAllowed" class="col-4 col-form-label text-right pr-4">Allow Price Changes</label>
                                <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product?.restrictions?.markdownAllowed}"/>
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.creditPaymentAllowed" class="col-4 col-form-label text-right pr-4">Allow Credit Payments</label>
                                <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product?.restrictions?.creditPaymentAllowed}" />
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.quantityChangeAllowed" class="col-4 col-form-label text-right pr-4">Allow Quantity Changes</label>
                                <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product?.restrictions?.quantityChangeAllowed}"/>
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.quantityChangeForced" class="col-4 col-form-label text-right pr-4">Force Quantity Changes</label>
                                <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${product?.restrictions?.quantityChangeForced}" />
                            </div>
                            <div class="row form-group form-check pl-0">
                                <label for="restrictions.receiptPrintForced" class="col-4 col-form-label text-right pr-4">Force Receipt Print</label>
                                <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${product?.restrictions?.receiptPrintForced}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

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

    <div class="row my-5">
        <g:actionSubmit action="index" type="button" class="btn btn-danger col-1 offset-1" value="Cancel"/>
        <g:actionSubmit action="save" name="save-button" value="Save" class="btn btn-success col-1 offset-8"/>
    </div>
</g:form>