<g:form method="post" url="${product.id == 0 ? "./add" : "./maintenance?productId=" + product.id}" class="mt-5">
    <g:hiddenField name="id" value="${product.id}"/>

    <div class="row">
        <div class="form-group row col-12 col-lg-6">
            <label for="itemCode" class="col-3 col-form-label text-right pr-4">Item Code</label>
            <g:textField name="itemCode" class="col-5 form-control bottom-border" value="${product.itemCode}"/>
        </div>
        <div class="form-group row col-12 col-lg-6">
            <label for="category" class="col-3 col-form-label text-right pr-4">Category</label>
            <g:select name="category" from="${categoryValues}" optionKey="id" optionValue="description" value="${product.category?.id}" class="col-7 form-control select-border" />
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-6">
            <label for="description" class="col-3 col-form-label text-right pr-4">Description</label>
            <g:textField name="description" class="col-9 form-control bottom-border" value="${product.description}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-6">
            <label for="receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
            <g:textField name="receiptDescription" value="${product.receiptDescription}" class="col-5 form-control bottom-border" />
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-6">
            <label for="unitSize" class="col-3 col-form-label text-right pr-4">Unit Size</label>
            <g:textField name="unitSize" class="col-3 form-control bottom-border" value="${product.unitSize}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-6 product-maintenance-input">
            <label for="retailPrice" class="col-3 col-form-label text-right pr-4">Retail Price</label>
            <g:field name="retailPrice" type="number" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.retailPrice}" class="col-3 form-control bottom-border" step="0.01" />
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-12">
            <ul class="nav nav-tabs nav-fill tabs-wl" role="tablist">
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
            <div class="col-12 mt-5">
                <div class="row mt-3">
                    <div class="col-6">
                        <div class="row mb-3">
                            <label for="vatCode" class="col-3 col-form-label text-right pr-4">VAT Code</label>
                            <select id="vatCode" name="vatCode" class="col-5 form-control select-border">
                                <g:each in="${vatValues}" var="vatCode">
                                    <option value="${vatCode.id}" data-code="${vatCode.code}" ${product.vatCode?.id == vatCode.id ? 'selected' : ''}>${vatCode.description}</option>
                                </g:each>
                            </select>
                        </div>
                        <div class="row mb-3 product-maintenance-input">
                            <label for="costPrice" class="col-3 col-form-label text-right pr-4">Cost Price</label>
                            <g:field name="costPrice" type="number" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.costPrice}" class="col-3 form-control bottom-border" step="0.01" />
                        </div>
                        <div class="row mb-3">
                            <label for="discreetMessage" class="col-3 col-form-label text-right pr-4">Discreet Message</label>
                            <g:textField name="discreetMessage" value="${product.discreetMessage}" class="col-5 form-control bottom-border" />
                        </div>
                        <div class="row mb-3 product-maintenance-input">
                            <label for="status" class="col-3 col-form-label text-right pr-4">Status</label>
                            <g:select name="status" class="col-3 form-control select-border" from="${statusValues}" value="${product.status}" />
                        </div>
                    </div>

                    <div class="col-6">
                        <div class="row mb-3 form-group product-maintenance-input">
                            <label for="vatPercentageOverride" class="col-3 col-form-label text-right pr-4">VAT Override</label>
                            <g:field name="vatPercentageOverride" type="number" value="${product.vatPercentageOverride ?: '0.00'}" class="col-3 form-control bottom-border text-right" step="0.01" readonly="${product?.vatCode?.code != 'O'}" />
                        </div>
                        <div class="row mt-1 form-group form-check">
                            <label for="weightedItem" class="col-3 col-form-label text-right pr-4">Weighted Item</label>
                            <g:checkBox name="weightedItem" class="col-1 form-check-input wl-checkbox" checked="${product.weightedItem}"/>
                        </div>
                        <div class="row mt-1 form-group form-check">
                            <label for="openPrice" class="col-3 col-form-label text-right pr-4">Open Price</label>
                            <g:checkBox name="openPrice" class="col-1 form-check-input wl-checkbox" checked="${product.openPrice}"/>
                        </div>
                        <div class="row mt-1 form-group form-check">
                            <label for="zeroPrice" class="col-3 col-form-label text-right pr-4">Zero Price</label>
                            <g:checkBox name="zeroPrice" class="col-1 form-check-input wl-checkbox" checked="${product.zeroPrice}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="tab-pane fade show pt-5 ${navlink.equals('variants') ? 'active' : ''}" id="variants" role="tabpanel" aria-labelledby="variants-tab">
            <g:hiddenField name="relevantVariant" value="" />

            <div class="row">
                <div class="col-1 offset-1">
                    <span class="font-weight-bold">SKU</span>
                </div>
                <div class="col-2 offset-1">
                    <span class="font-weight-bold">Size</span>
                </div>
                <div class="col-2">
                    <span class="font-weight-bold">Colour</span>
                </div>
                <div class="col-4">
                    <span class="font-weight-bold">Barcodes</span>
                </div>
                <div class="col-1">
                    <span class="font-weight-bold">&nbsp;</span>
                </div>
            </div>

            <g:each status="i" in="${product.variants}" var="variant" >
                <g:if test="${variant.storeId == storeId}">
                    <g:hiddenField name="variants[${i}].storeId" value="${variant.storeId}"/>
                    <g:hiddenField name="variants[${i}].delete" value="${variant.delete}"/>
                    <g:hiddenField name="variants[${i}].selected" value="false" />

                    <div class="row mt-3 ${variant.delete ? "d-none" : ""}">
                        <div class="col-2 offset-1 form-group">
                            <g:textField name="variants[${i}].itemCode" value="${variant.itemCode}" class="form-control bottom-border" />
                        </div>
                        <div class="col-2 form-group">
                            <g:textField name="variants[${i}].size" value="${variant.size}" class="form-control bottom-border" />
                        </div>
                        <div class="col-2 form-group">
                            <g:textField name="variants[${i}].colour" value="${variant.colour}" class="form-control bottom-border" />
                        </div>
                        <div class="col-3">
                            <g:each in="${variant.barcodes}" var="barcode" status="j">
                                <div class="form-group ${barcode.delete ? "d-none" : ""}">
                                    <g:hiddenField name="variants[${i}].barcodes[${j}].effectiveDate" value="${barcode.effectiveDate.format('yyyy-MM-dd')}"/>
                                    <g:hiddenField name="variants[${i}].barcodes[${j}].recordStatus" value="${barcode.recordStatus}"/>
                                    <g:hiddenField name="variants[${i}].barcodes[${j}].delete" value="${barcode.delete}"/>
                                    <g:hiddenField name="variants[${i}].barcodes[${j}].selected" value="false" />

                                    <g:textField name="variants[${i}].barcodes[${j}].barcode" value="${barcode.barcode}" class="form-control bottom-border" />
                                    <g:actionSubmitImage value=" " src="bin.png" action="deleteBarcodes" class="button btn-danger m-auto" onclick="document.getElementById('variants[${i}].barcodes[${j}].selected').value = true;" />
                                </div>
                            </g:each>
                        </div>
                        <div class="col-1">
                            <g:actionSubmit value="Delete Variant" controller="product" action="deleteVariants" onclick="document.getElementById('variants[${i}].selected').value = true;" class="btn btn-danger" />
                        </div>
                    </div>

                    <div class="row mt-2 ml-3">
                        <g:actionSubmit value="+ Add Barcode" action="addBarcode" class="offset-7 btn btn-wl" onclick="setRelevantVariant(${i})" />
                    </div>
                </g:if>
            </g:each>

            <div class="row">
                <g:actionSubmit value="+ Add Variant" controller="product" action="addVariant" class="offset-1 btn btn-wl" />
            </div>
%{--                delete barcodes <g:actionSubmit value="Delete Selected" action="deleteBarcodes" class="button btn-danger m-auto" onclick="setRelevantVariant(${i})"/>--}%
        </div>

        <div class="tab-pane fade show" id="suppliers" role="tabpanel" aria-labelledby="suppliers-tab">
            <div class="col-12 mt-5">
            </div>
        </div>

        <div class="tab-pane fade show pt-5 ${navlink.equals('restrictions') ? 'active' : ''}" id="restrictions" role="tabpanel" aria-labelledby="restrictions-tab">
            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.buyerIdRequired" class="col-6 col-form-label text-right pr-4">Age Restricted Item</label>
                        <g:checkBox name="restrictions.buyerIdRequired" class="col-1 form-check-input wl-checkbox" checked="${product.restrictions.buyerIdRequired}" />
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.refundAllowed" class="col-6 col-form-label text-right pr-4">Allow Refunds</label>
                        <g:checkBox name="restrictions.refundAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product.restrictions.refundAllowed}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group form-check">
                        <label for="restrictions.buyerIdForced" class="col-6 col-form-label text-right pr-4">ID Check Forced</label>
                        <g:checkBox name="restrictions.buyerIdForced" class="col-1 form-check-input wl-checkbox" checked="${product.restrictions.buyerIdForced}"/>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.discountAllowed" class="col-6 col-form-label text-right pr-4">Allow Discounts</label>
                        <g:checkBox name="restrictions.discountAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product.restrictions.discountAllowed}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group pl-125">
                        <label for="restrictions.buyerAgeRestriction" class="col-6 col-form-label text-right pr-4">Customer Age Required</label>
                        <g:field name="restrictions.buyerAgeRestriction" type="number" value="${product.restrictions.buyerAgeRestriction}" class="col-2 form-control bottom-border"/>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.markdownAllowed" class="col-6 col-form-label text-right pr-4">Allow Price Changes</label>
                        <g:checkBox name="restrictions.markdownAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product.restrictions.markdownAllowed}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group pl-125">
                        <label for="restrictions.buyerChallengeAge" class="col-6 col-form-label text-right pr-4">Customer Challenge Age</label>
                        <g:field name="restrictions.buyerChallengeAge" type="number" value="${product.restrictions.buyerChallengeAge}" class="col-2 form-control bottom-border"/>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.creditPaymentAllowed" class="col-6 col-form-label text-right pr-4">Allow Credit Payments</label>
                        <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product.restrictions.creditPaymentAllowed}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group pl-125">
                        <label for="restrictions.sellerAgeRestriction" class="col-6 col-form-label text-right pr-4">Operator Age Required</label>
                        <g:field name="restrictions.sellerAgeRestriction" type="number" value="${product.restrictions.sellerAgeRestriction}" class="col-2 form-control bottom-border"/>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.quantityChangeAllowed" class="col-6 col-form-label text-right pr-4">Allow Quantity Changes</label>
                        <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${isNewProduct || product.restrictions.quantityChangeAllowed}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group pl-125">
                        <label for="restrictions.minOpenPrice" class="col-6 col-form-label text-right pr-4">Min Open Price</label>
                        <g:field name="restrictions.minOpenPrice" type="number" value="${product.restrictions.minOpenPrice ?: '0.01'}" class="col-3 form-control bottom-border" step="0.01"/>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.quantityChangeForced" class="col-6 col-form-label text-right pr-4">Force Quantity Changes</label>
                        <g:checkBox name="restrictions.quantityChangeForced" class="col-1 form-check-input wl-checkbox" checked="${product.restrictions.quantityChangeForced}"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12 col-md-6">
                    <div class="row mt-1 form-group pl-125">
                        <label for="restrictions.maxOpenPrice" class="col-6 col-form-label text-right pr-4">Max Open Price</label>
                        <g:field name="restrictions.maxOpenPrice" type="number" value="${product.restrictions.maxOpenPrice ?: '9999.99'}" class="col-3 form-control bottom-border" step="0.01" />
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="mt-1 form-group form-check">
                        <label for="restrictions.receiptPrintForced" class="col-6 col-form-label text-right pr-4">Force Receipt Print</label>
                        <g:checkBox name="restrictions.receiptPrintForced" class="col-1 form-check-input wl-checkbox" checked="${product.restrictions.receiptPrintForced}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row my-5">
        <g:actionSubmit action="index" type="button" class="btn btn-danger col-1 offset-1" value="Cancel"/>
        <g:actionSubmit action="save" name="save-button" value="Save" class="btn btn-success col-1 offset-8"/>
    </div>
</g:form>