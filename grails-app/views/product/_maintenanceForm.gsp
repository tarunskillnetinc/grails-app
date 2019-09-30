<div class="row my-3">
    <h1 class="mx-auto my-0">Product Maintenance</h1>
</div>

<g:form method="post" url="${product.id == 0 ? "./add" : "./maintenance?productId=" + product.id}">
    <g:hiddenField name="id" value="${product.id}"/>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="itemCode" class="col-3 col-form-label">Item Code</label>
            <g:textField name="itemCode" class="col-9" value="${product.itemCode}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="description" class="col-3 col-form-label">Description</label>
            <g:textField name="description" class="col-9" value="${product.description}"/>
        </div>
        <div class="form-group row col-5">
            <label for="category" class="col-3 offset-2">Category</label>
            <g:select name="category" from="${categoryValues}" optionKey="id" optionValue="description" value="${product.category?.id}" class="col-7"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-5 offset-1">
            <label for="status" class="col-3 col-form-label">Status</label>
            <g:select name="status" class="col-3" from="${statusValues}" value="${product.status}"/>
        </div>
        <div class="form-group row col-5">
            <label for="unitSize" class="offset-8 col-2 col-form-label">Size</label>
            <g:textField name="unitSize" class="col-2" value="${product.unitSize}"/>
        </div>
    </div>

    <div class="row">
        <div class="col-10 offset-1">
            <ul class="nav nav-pills nav-fill pills-wl sub-pills" role="tablist">
                <li class="nav-item">
                    <a id="itemDetails-tab" data-toggle="tab" href="#itemDetails" aria-selected="true" role="tab" aria-controls="itemDetails" class="nav-link ${navlink.equals('details') ? 'active' : ''}">Item Details</a>
                </li>

                <li class="nav-item">
                    <a id="suppliers-tab" data-toggle="tab" href="#suppliers" role="tab" aria-controls="suppliers" class="nav-link disabled ${navlink.equals('suppliers') ? 'active' : ''}" >Suppliers</a>
                </li>

                <li class="nav-item">
                    <a id="restrictions-tab" data-toggle="tab" href="#restrictions" role="tab" aria-controls="restrictions" class="nav-link ${navlink.equals('restrictions') ? 'active' : ''}">Restrictions</a>
                </li>

                <li class="nav-item">
                    <a id="variants-tab" data-toggle="tab" href="#variants" role="tab" aria-controls="variants" class="nav-link ${navlink.equals('variants') ? 'active' : ''}">Variants</a>
                </li>
            </ul>
        </div>
    </div>

    <div class="tab-content">
        <div class="tab-pane fade show ${navlink.equals('details') ? 'active' : ''}" id="itemDetails" role="tabpanel" aria-labelledby="itemDetails-tab">
            <div class="col-10 offset-1 mt-3">
                <div class="row">
                    <div class="col-6">
                        <div class="row">
                            <label for="ean-panel" class="offset-4 col-2 text-right">EANs</label>
                            <div id="ean-panel" class="col-6 border">
                                <g:each in="${product.variants.findAll{it.storeId == storeId}}" var="variant">
                                    <g:each in="${variant.barcodes}" var="barcode">
                                        <p class="text-right">
                                            ${barcode.barcode}
                                        </p>
                                    </g:each>
                                </g:each>
                            </div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="row">
                            <label for="promotion-panel" class="offset-1 col-3 text-right">Promotions</label>
                            <div id="promotion-panel" class="col-6 border">
                                <p class="text-right">Placeholder</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row mt-3">
                    <div class="col-6">
                        <div class="row mb-3">
                            <label for="vatCode" class="col-3 offset-2">Vat Code</label>
                            <g:select name="vatCode" from="${vatValues}" optionKey="id" optionValue="description" value="${product.vatCode?.id}" class="col-7"/>
                        </div>
                        <div class="row mb-3">
                            <label for="receiptDescription" class="col-3 offset-2">Receipt Description</label>
                            <g:textField name="receiptDescription" value="${product.receiptDescription}" class="col-5"/>
                        </div>
                        <div class="row mb-3">
                            <label for="retailPrice" class="col-3 offset-2">Retail Price</label>
                            <g:textField name="retailPrice" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.retailPrice}" class="col-3"/>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="row mb-3">
                            <label for="vatPercentageOverride" class="col-3 offset-2">Vat Override</label>
                            <g:textField name="vatPercentageOverride" value="${product.vatPercentageOverride}" class="col-3 text-right"/>
                        </div>
                        <div class="row mb-3">
                            <label for="discreetMessage" class="col-3 offset-2">Discreet Message</label>
                            <g:textField name="discreetMessage" value="${product.discreetMessage}" class="col-5"/>
                        </div>
                        <div class="row mb-3">
                            <label for="costPrice" class="col-3 offset-2">Cost Price</label>
                            <g:textField name="costPrice" value="${product.productDatas.sort { it.effectiveDate }.reverse().find { it.storeId == storeId && it.effectiveDate <= new Date() }.costPrice}" class="col-3"/>
                        </div>
                    </div>
                </div>

                <div class="row mt-3 justify-content-center">
                    <div class="col-2">
                        <div class="row">
                            <label for="weightedItem" class="col-8 my-auto text-right">Weighted</label>
                            <g:checkBox name="weightedItem" class="col-1 my-auto" checked="${product.weightedItem}"/>
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="row">
                            <label for="openPrice" class="col-8 my-auto text-right">Open Price</label>
                            <g:checkBox name="openPrice" class="col-1 my-auto" checked="${product.openPrice}"/>
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="row">
                            <label for="zeroPrice" class="col-8 my-auto text-right">Zero Price</label>
                            <g:checkBox name="zeroPrice" class="col-1 my-auto" checked="${product.zeroPrice}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="tab-pane fade show" id="suppliers" role="tabpanel" aria-labelledby="suppliers-tab">
            <div class="col-10 offset-1 mt-3">
            </div>
        </div>
        <div class="tab-pane fade show ${navlink.equals('restrictions') ? 'active' : ''}" id="restrictions" role="tabpanel" aria-labelledby="restrictions-tab">
            <div class="col-10 offset-1 mt-3 row">
                <div class="col-12 col-md-6">
                    <div class="row">
                        <div class="col-12 col-sm-7 col-md-12 col-lg-7">
                            <div class="row form-group">
                                <label for="restrictions.buyerAgeRestriction" class="col-9 text-right">Customer Age Required</label>
                                <g:textField name="restrictions.buyerAgeRestriction" value="${product.restrictions.buyerAgeRestriction}" class="col-2 text-center"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.buyerChallengeAge" class="col-9 text-right">Customer Challenge Age</label>
                                <g:textField name="restrictions.buyerChallengeAge" value="${product.restrictions.buyerChallengeAge}" class="col-2 text-center"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.sellerAgeRestriction" class="col-9 text-right">Operator Age Required</label>
                                <g:textField name="restrictions.sellerAgeRestriction" value="${product.restrictions.sellerAgeRestriction}" class="col-2 text-center"/>
                            </div>
                        </div>
                        <div class="col-12 col-sm-5 col-md-12 col-lg-5">
                            <div class="row form-group">
                                <label for="restrictions.buyerIdRequired" class="col-8 my-auto text-right">ID Check Required</label>
                                <g:checkBox name="restrictions.buyerIdRequired" class="col-1 my-auto" checked="${product.restrictions.buyerIdRequired}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.buyerIdForced" class="col-8 my-auto text-right">ID Check Forced</label>
                                <g:checkBox name="restrictions.buyerIdForced" class="col-1 my-auto" checked="${product.restrictions.buyerIdForced}"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-md-6">
                    <div class="row form-group">
                        <label for="restrictions.minOpenPrice" class="col-4 offset-2">Min Open Price</label>
                        <g:textField name="restrictions.minOpenPrice" value="${product.restrictions.minOpenPrice}" class="col-3 text-center"/>
                    </div>
                    <div class="row form-group">
                        <label for="restrictions.maxOpenPrice" class="col-4 offset-2">Max Open Price</label>
                        <g:textField name="restrictions.maxOpenPrice" value="${product.restrictions.maxOpenPrice}" class="col-3 text-center"/>
                    </div>
                    <div class="row">
                        <div class="col-6">
                            <div class="row form-group">
                                <label for="restrictions.refundAllowed" class="col-8 my-auto text-right">Refundable</label>
                                <g:checkBox name="restrictions.refundAllowed" class="col-1 my-auto" checked="${product.restrictions.refundAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.discountAllowed" class="col-8 my-auto text-right">Discountable</label>
                                <g:checkBox name="restrictions.discountAllowed" class="col-1 my-auto" checked="${product.restrictions.discountAllowed}"/>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="row form-group">
                                <label for="restrictions.markdownAllowed" class="col-8 my-auto text-right">Markdownable</label>
                                <g:checkBox name="restrictions.markdownAllowed" class="col-1 my-auto" checked="${product.restrictions.markdownAllowed}"/>
                            </div>
                            <div class="row form-group">
                                <label for="restrictions.creditPaymentAllowed" class="col-8 my-auto text-right">Credit Payments</label>
                                <g:checkBox name="restrictions.creditPaymentAllowed" class="col-1 my-auto" checked="${product.restrictions.creditPaymentAllowed}"/>
                            </div>
                        </div>
                    </div>
                    <div class="row form-group">
                        <label for="restrictions.quantityChangeAllowed" class="col-7 my-auto text-right">Quantity Change Allowed</label>
                        <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 my-auto" checked="${product.restrictions.quantityChangeAllowed}"/>
                    </div>
                    <div class="row form-group">
                        <label for="restrictions.quantityChangeForced" class="col-7 my-auto text-right">Quantity Change Forced</label>
                        <g:checkBox name="restrictions.quantityChangeForced" class="col-1 my-auto" checked="${product.restrictions.quantityChangeForced}"/>
                    </div>
                    <div class="row form-group">
                        <label for="restrictions.receiptPrintForced" class="col-7 my-auto text-right">Forced Receipt Print</label>
                        <g:checkBox name="restrictions.receiptPrintForced" class="col-1 my-auto" checked="${product.restrictions.receiptPrintForced}"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="tab-pane fade show ${navlink.equals('variants') ? 'active' : ''}" id="variants" role="tabpanel" aria-labelledby="variants-tab">
            <g:hiddenField name="relevantVariant" value=""/>
            <div class="col-10 offset-1 mt-3 ">
                <div class="row mb-3">
                    <div class="offset-1 mr-3 p-2"></div>
                    <div class="col-2 mr-3 text-center font-weight-bold">Item Code</div>
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
        <g:actionSubmit action="index" type="button" class="button btn-danger col-1 offset-1" value="Cancel"/>
        <g:actionSubmit action="save" name="save-button" value="Save" class="button btn-success col-1 offset-8"/>
    </div>
</g:form>