<g:form method="post" url="${product.id == 0 ? "./add" : "./maintenance?productId=" + product.id}" class="mt-4">
    <g:hiddenField name="id" value="${product.id}"/>

    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="itemCode" class="col-3 col-form-label">Item Code</label>
            <g:textField name="itemCode" class="col-6 form-control bottom-border" value="${product.itemCode}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="description" class="col-3 col-form-label">Description</label>
            <g:textField name="description" class="col-9 form-control bottom-border" value="${product.description}"/>
        </div>
        <div class="form-group row col-5">
            <label for="category" class="col-3 offset-2 col-form-label">Category</label>
            <g:select name="category" from="${categoryValues}" optionKey="id" optionValue="description" value="${product.category?.id}" class="col-7 form-control select-border" />
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="status" class="col-3 col-form-label">Status</label>
            <g:select name="status" class="col-3 form-control select-border" from="${statusValues}" value="${product.status}" />
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="unitSize" class="col-3 col-form-label">Unit Size</label>
            <g:textField name="unitSize" class="col-3 form-control bottom-border" value="${product.unitSize}"/>
        </div>
    </div>

    <div class="row mt-5">
        <div class="col-10 offset-1">
            <ul class="nav nav-tabs nav-fill" role="tablist">
                <li class="nav-item">
                    <a id="itemDetails-tab" data-toggle="tab" href="#itemDetails" aria-selected="true" role="tab" aria-controls="itemDetails" class="nav-link ${navlink.equals('details') ? 'active' : ''}">Item Details</a>
                </li>

                <li class="nav-item">
                    <a id="variants-tab" data-toggle="tab" href="#variants" role="tab" aria-controls="variants" class="nav-link ${navlink.equals('variants') ? 'active' : ''}">Variants &amp; Barcodes</a>
                </li>

                <li class="nav-item">
                    <a id="restrictions-tab" data-toggle="tab" href="#restrictions" role="tab" aria-controls="restrictions" class="nav-link ${navlink.equals('restrictions') ? 'active' : ''}">Restrictions</a>
                </li>

                <li class="nav-item">
                    <a id="suppliers-tab" data-toggle="tab" href="#suppliers" role="tab" aria-controls="suppliers" class="nav-link disabled ${navlink.equals('suppliers') ? 'active' : ''}" >Suppliers</a>
                </li>
            </ul>
        </div>
    </div>

    <div class="tab-content">
        <div class="tab-pane fade show ${navlink.equals('details') ? 'active' : ''}" id="itemDetails" role="tabpanel" aria-labelledby="itemDetails-tab">
            <div class="col-10 offset-1 mt-5">
                <div class="row mt-3">
                    <div class="col-6">
                        <div class="row mb-3">
                            <label for="vatCode" class="col-3 offset-2">VAT Code</label>
                            <g:select name="vatCode" from="${vatValues}" optionKey="id" optionValue="description" value="${product.vatCode?.id}" class="col-7 form-control select-border" />
                        </div>
                        <div class="row mb-3">
                            <label for="receiptDescription" class="col-3 offset-2">Receipt Description</label>
                            <g:textField name="receiptDescription" value="${product.receiptDescription}" class="col-5 form-control bottom-border" />
                        </div>
                        <div class="row mb-3 product-maintenance-input">
                            <label for="retailPrice" class="col-3 offset-2">Retail Price</label>
                            <g:field name="retailPrice" type="number" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.retailPrice}" class="col-3 form-control bottom-border" step="0.01" />
                        </div>
                        <div class="row mb-3 product-maintenance-input">
                            <label for="costPrice" class="col-3 offset-2">Cost Price</label>
                            <g:field name="costPrice" type="number" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.costPrice}" class="col-3 form-control bottom-border" step="0.01" />
                        </div>
                    </div>

                    <div class="col-6">
                        <div class="row mb-3 product-maintenance-input">
                            <label for="vatPercentageOverride" class="col-3 offset-2">VAT Override</label>
                            <g:field name="vatPercentageOverride" type="number" value="${product.vatPercentageOverride}" class="col-3 form-control bottom-border text-right" step="0.01" />
                        </div>
                        <div class="row mb-3">
                            <label for="discreetMessage" class="col-3 offset-2">Discreet Message</label>
                            <g:textField name="discreetMessage" value="${product.discreetMessage}" class="col-5 form-control bottom-border" />
                        </div>
                        <div class="row mt-1">
                            <label for="weightedItem" class="col-3 offset-2">Weighted Item</label>
                            <g:checkBox name="weightedItem" class="col-1 my-auto form-control" checked="${product.weightedItem}"/>
                        </div>
                        <div class="row mt-1">
                            <label for="openPrice" class="col-3 offset-2">Open Price</label>
                            <g:checkBox name="openPrice" class="col-1 my-auto form-control" checked="${product.openPrice}"/>
                        </div>
                        <div class="row mt-1">
                            <label for="zeroPrice" class="col-3 offset-2">Zero Price</label>
                            <g:checkBox name="zeroPrice" class="col-1 my-auto form-control" checked="${product.zeroPrice}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="tab-pane fade show" id="suppliers" role="tabpanel" aria-labelledby="suppliers-tab">
            <div class="col-10 offset-1 mt-5">
            </div>
        </div>

        <div class="tab-pane fade show ${navlink.equals('restrictions') ? 'active' : ''}" id="restrictions" role="tabpanel" aria-labelledby="restrictions-tab">
            <div class="col-10 offset-1 mt-5 row">
                <div class="col-12 col-md-6">
                    <div class="row">
                        <div class="col-12 col-sm-7 col-md-12 col-lg-7">
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.buyerIdRequired" class="col-9 my-auto text-right">ID Check Required</label>
                                <g:checkBox name="restrictions.buyerIdRequired" class="col-1 my-auto form-control" checked="${product.restrictions.buyerIdRequired}"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.buyerIdForced" class="col-9 my-auto text-right">ID Check Forced</label>
                                <g:checkBox name="restrictions.buyerIdForced" class="col-1 my-auto form-control" checked="${product.restrictions.buyerIdForced}"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.buyerAgeRestriction" class="col-9 text-right">Customer Age Required</label>
                                <g:field name="restrictions.buyerAgeRestriction" type="number" value="${product.restrictions.buyerAgeRestriction}" class="col-2 form-control bottom-border"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.buyerChallengeAge" class="col-9 text-right">Customer Challenge Age</label>
                                <g:field name="restrictions.buyerChallengeAge" type="number" value="${product.restrictions.buyerChallengeAge}" class="col-2 form-control bottom-border"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.sellerAgeRestriction" class="col-9 text-right">Operator Age Required</label>
                                <g:field name="restrictions.sellerAgeRestriction" type="number" value="${product.restrictions.sellerAgeRestriction}" class="col-2 form-control bottom-border"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.minOpenPrice" class="col-9 text-right">Min Open Price</label>
                                <g:field name="restrictions.minOpenPrice" type="number" value="${product.restrictions.minOpenPrice}" class="col-3 form-control bottom-border" step="0.01"/>
                            </div>
                            <div class="row form-group product-maintenance-input">
                                <label for="restrictions.maxOpenPrice" class="col-9 text-right">Max Open Price</label>
                                <g:field name="restrictions.maxOpenPrice" type="number" value="${product.restrictions.maxOpenPrice}" class="col-3 form-control bottom-border" step="0.01" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6">
                    <div class="row">
                        <div class="col-6">
                            <div class="row form-group">
                                <label for="restrictions.refundAllowed" class="col-8 my-auto text-right">Allow refunds</label>
                                <g:checkBox name="restrictions.refundAllowed" class="col-1 my-auto form-control" checked="${isNewProduct || product.restrictions.refundAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.discountAllowed" class="col-8 my-auto text-right">Allow discounts</label>
                                <g:checkBox name="restrictions.discountAllowed" class="col-1 my-auto form-control" checked="${isNewProduct || product.restrictions.discountAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.markdownAllowed" class="col-8 my-auto text-right">Allow price change</label>
                                <g:checkBox name="restrictions.markdownAllowed" class="col-1 my-auto form-control" checked="${isNewProduct || product.restrictions.markdownAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.creditPaymentAllowed" class="col-8 my-auto text-right">Allow credit Payments</label>
                                <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 my-auto form-control" checked="${isNewProduct || product.restrictions.creditPaymentAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.quantityChangeAllowed" class="col-8 my-auto text-right">Quantity Change Allowed</label>
                                <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 my-auto form-control" checked="${isNewProduct || product.restrictions.quantityChangeAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.quantityChangeForced" class="col-8 my-auto text-right">Quantity Change Forced</label>
                                <g:checkBox name="restrictions.quantityChangeForced" class="col-1 my-auto form-control" checked="${product.restrictions.quantityChangeForced}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.receiptPrintForced" class="col-8 my-auto text-right">Forced Receipt Print</label>
                                <g:checkBox name="restrictions.receiptPrintForced" class="col-1 my-auto form-control" checked="${product.restrictions.receiptPrintForced}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="tab-pane fade show ${navlink.equals('variants') ? 'active' : ''}" id="variants" role="tabpanel" aria-labelledby="variants-tab">
            <g:hiddenField name="relevantVariant" value=""/>

            <div class="col-10 offset-1 mt-5">
                <div class="row mb-3">
                    <div class="offset-1 mr-3 p-2"></div>
                    <div class="col-2 mr-3 text-center font-weight-bold">SKU</div>
                    <div class="col-1 mr-3 text-center font-weight-bold">Size</div>
                    <div class="col-1 mr-3 text-center font-weight-bold">Colour</div>
                    <div class="col-2 mr-3 text-center font-weight-bold">Barcodes</div>
                </div>
                <g:each status="i" in="${product.variants}" var="variant" >
                    <g:if test="${variant.storeId == storeId}">
                        <div class="row mb-3 ${variant.delete ? "d-none" : ""}">
                            <g:hiddenField name="variants[${i}].storeId" value="${variant.storeId}"/>
                            <g:hiddenField name="variants[${i}].delete" value="${variant.delete}"/>
                            <g:checkBox name="variants[${i}].selected" checked="false" class="my-auto mr-3 offset-1"/>
                            <g:textField name="variants[${i}].itemCode" value="${variant.itemCode}" class="col-2 mr-3 variant-display-item"/>
                            <g:textField name="variants[${i}].size" value="${variant.size}" class="col-1 mr-3 text-center variant-display-item"/>
                            <g:textField name="variants[${i}].colour" value="${variant.colour}" class="col-1 mr-3 text-center variant-display-item"/>

                            <div class="col-4 row border ml-3">
                                <div class="col-6">
                                    <g:each in="${variant.barcodes}" var="barcode" status="j">
                                        <div class="row  ${barcode.delete ? "d-none" : ""}">
                                            <g:hiddenField name="variants[${i}].barcodes[${j}].effectiveDate" value="${barcode.effectiveDate.format('yyyy-MM-dd')}"/>
                                            <g:hiddenField name="variants[${i}].barcodes[${j}].recordStatus" value="${barcode.recordStatus}"/>
                                            <g:hiddenField name="variants[${i}].barcodes[${j}].delete" value="${barcode.delete}"/>
                                            <g:checkBox name="variants[${i}].barcodes[${j}].selected" checked="false" class="my-auto col-1"/>
                                            <g:textField name="variants[${i}].barcodes[${j}].barcode" value="${barcode.barcode}" class="col-11"/>
                                        </div>
                                    </g:each>
                                    <g:actionSubmit value="+ Add Barcode" action="addBarcode" class="variant-text-button my-auto" onclick="setRelevantVariant(${i})"/>
                                </div>
                                <div class="col-6 row ml-2">
                                    <g:actionSubmit value="Delete Selected" action="deleteBarcodes" class="button btn-danger m-auto" onclick="setRelevantVariant(${i})"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                </g:each>
                <div class="row">
                    <div class="offset-1 mr-3 p-2"></div>
                    <g:actionSubmit value="+ Add Variant" controller="product" action="addVariant" class="variant-text-button"/>
                </div>
                <div class="row">
                    <div class="offset-1 mr-3 p-2"></div>
                    <g:actionSubmit value="Delete Selected" controller="product" action="deleteVariants" class="button btn-danger mt-3"/>
                </div>
            </div>
        </div>
    </div>

    <div class="row my-4">
        <g:actionSubmit action="index" type="button" class="btn btn-danger col-1 offset-1" value="Cancel"/>
        <g:actionSubmit action="save" name="save-button" value="Save" class="btn btn-success col-1 offset-8"/>
    </div>
</g:form>