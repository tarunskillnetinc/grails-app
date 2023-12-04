<asset:javascript src="validators/input-validator.js" />

<div class="row mt-2">
    <div class="col-12">
        <ul class="nav nav-tabs nav-fill tabs-wl mx-4" role="tablist">
            <li class="nav-item">
                <a id="bogof-tab" data-toggle="tab" href="#bogof" aria-selected="true" role="tab" aria-controls="bogof" class="nav-link ${editing ? (promoType.equals('bogof') ? 'active' : 'disabled') : (promoType.equals('bogof') ? 'active' : '')}">BOGOF</a>
            </li>

            <li class="nav-item">
                <a id="xfory-tab" data-toggle="tab" href="#xfory" role="tab" aria-controls="xfory" class="nav-link ${editing ? (promoType.equals('x_for_y') ? 'active' : 'disabled') : (promoType.equals('x_for_y') ? 'active' : '')}">X for Y</a>
            </li>

            <li class="nav-item">
                <a id="percentage-tab" data-toggle="tab" href="#percentage" role="tab" aria-controls="percentage" class="nav-link ${editing ? (promoType.equals('percentage_discount') ? 'active' : 'disabled') : (promoType.equals('percentage_discount') ? 'active' : '')}">Percentage</a>
            </li>

            <li class="nav-item">
                <a id="fixedAmount-tab" data-toggle="tab" href="#fixedAmount" role="tab" aria-controls="fiexdAmount" class="nav-link ${editing ? (promoType.equals('fixed_amount_discount') ? 'active' : 'disabled') : (promoType.equals('fixed_amount_discount') ? 'active' : '')}" >Fixed Amount</a>
            </li>

            <li class="nav-item">
                <a id="fixedPrice-tab" data-toggle="tab" href="#fixedPrice" role="tab" aria-controls="fiexdPrice" class="nav-link ${editing ? (promoType.equals('fixed_price') ? 'active' : 'disabled') : (promoType.equals('fixed_price') ? 'active' : '')}" >Fixed Price</a>
            </li>
        </ul>
    </div>
</div>

<div class="tab-content">
    <div class="tab-pane fade show ${promoType.equals('bogof') ? 'active' : ''}" id="bogof" role="tabpanel" aria-labelledby="bogof-tab">
        <g:form method="post" action="save" class="mt-5" name="bogof-form">
            <g:hiddenField name="promotionType" value="bogof"/>
            <g:hiddenField name="promotionId" value="${promotion?.id}"/>
            <g:hiddenField name="bogof-promotionItemsType" value="${productItemType}"/>
            <g:hiddenField name="bogof-noItemChange" value="true"/>

            <div id="bogof-details">
                <h2 class="row col-1">Details</h2>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="bogof-description" class="col-3 col-form-label text-right pr-4">Description</label>
                        <g:textField name="bogof-description" maxLength="200" class="col-5 form-control bottom-border promo-desc" required="true" value="${promotion?.description}"/>
                    </div>
                    <div class="form-group row col-12 col-sm-6">
                        <label for="bogof-receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                        <g:textField name="bogof-receiptDescription" maxLength="50" class="col-5 form-control bottom-border promo-receiptDesc" required="true" value="${promotion?.receiptDescription}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="bogof-startDate" class="col-3 col-form-label text-right pr-4">Start Date</label>
                        <div class="input-group date startDate col-7" id="bogof-startDatepicker">
                            <g:textField name="bogof-startDate" type="text" class="row form-control promo-startDate" required="true"
                                         autoComplete="off"
                                         value="${promotion ? promotion.startDate.toString("EEEE dd MMMM yyyy") : new Date().format("EEEE dd MMMM yyyy")}"/>
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6" id="bogof-endDate-container">
                        <label for="bogof-endDate" class="col-3 col-form-label text-right pr-4">End Date</label>
                        <div class="input-group date endDate col-7 mb-auto" id="bogof-endDatepicker">
                            <g:textField name="bogof-endDate" type="text" class="row form-control promo-endDate" required="true" disabled="${promotion ? promotion.endDate ? false : true : false}"
                                         autoComplete="off"
                                         value="${promotion ?
                                                    promotion.endDate ? promotion.endDate.toString("EEEE dd MMMM yyyy") : ""
                                                 : new Date().plus(7).format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-doesNotExpire" class="col-3 col-form-label text-right pr-4">Does not expire</label>
                        <g:checkBox name="bogof-doesNotExpire" class="col-1 form-check-input wl-checkbox wl-noExpire" checked="${promotion ? promotion?.endDate == null : false}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="bogof-retailerPromoId" class="col-3 col-form-label text-right pr-4">Promotion Reference</label>
                        <g:field type="number" name="bogof-retailerPromoId" min="0" max="999999999" class="col-5 form-control bottom-border promo-id" required="true" value="${promotion?.retailerPromotionId}" onkeydown="acceptNumeric(event);"/>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-active" class="col-3 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="bogof-active" class="col-1 form-check-input wl-checkbox promo-active" checked="${promotion ? promotion.active : true}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="bogof-supplier" class="col-3 col-form-label text-right pr-4">Supplier</label>
                        <g:field readonly="readonly" name="bogof-supplier"
                                 class="col-5 form-control bottom-border promo-amount" required="false"
                                 value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" type="text"/>
                    </div>
                </div>
            </div>
            <div id="bogof-products" class="collapsible-products row mt-3">
                <h2 class="col-1 mr-2">Products</h2>
                <div id="bogof-productsRequiredSection" class="promotion-products-container col-10 offset-1">
                    <h3 class="mt-1 ml-3">Customer Buys & Receives One Free</h3>
                    <div id="bogof-productsRequiredContainer" class="row my-2">
                        <g:hiddenField name="bogof-count-required" value="${productsRequired.isEmpty() && categoriesRequired.isEmpty() && tagsRequired.isEmpty() ? productsOffer.size() + categoriesOffer.size() + tagsOffer.size() : productsRequired.size() + categoriesRequired.size() + tagsRequired.size()}"/>
                        <g:if test="${!productsRequired?.isEmpty() || !productsOffer.isEmpty()}">
                            <div id="bogof-product1" class="offset-1 promotion-product-container mt-3">
                                <g:hiddenField name="bogof-product-required-1-sku" value="${productsRequired.isEmpty() ? productsOffer.first().sku : productsRequired.first().sku}"/>
                                Quantity 1 x ${String.valueOf(productsRequired.isEmpty() ? productsOffer.first().product.itemCode : productsRequired.first().product.itemCode)} - ${productsRequired.isEmpty() ? productsOffer.first().product.description : productsRequired.first().product.description}
                                <a href="#" onclick="return deleteThis(this, 'bogof', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>

                        <g:if test="${!categoriesRequired.isEmpty() || !categoriesOffer.isEmpty()}">
                            <div id="bogof-category1" class="offset-1 promotion-product-container mt-3">
                                <g:hiddenField name="bogof-category-required-1-categoryId" value="${categoriesRequired.isEmpty() ? categoriesOffer.first().category.id : categoriesRequired.first().category.id}"/>
                                Quantity 1 x ${categoriesRequired.isEmpty() ? categoriesOffer.first().category.description : categoriesRequired.first().category.description}${categoriesRequired.isEmpty() ? (categoriesOffer.first().category.retailerCategoryCode != null ? " - " + categoriesOffer.first().category.retailerCategoryCode : "") : (categoriesRequired.first().category.retailerCategoryCode != null ? " - " + categoriesRequired.first().category.retailerCategoryCode : "")}
                                <a href="#" onclick="return deleteThis(this, 'bogof', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>

                        <g:if test="${!tagsRequired.isEmpty() || !tagsOffer.isEmpty()}">
                            <div id="bogof-tag1" class="card bg-light border-wl offset-1 mt-3">
                                <g:hiddenField name="bogof-tag-required-1-tagId" value="${tagsRequired.isEmpty() ? tagsOffer.first().tag.id : tagsRequired.first().tag.id}"/>

                                <div class="card-header pointer" data-toggle="collapse" data-target="#bogofTagCollapse" aria-expanded="false" aria-controls="bogofTagCollapse">
                                    <div class="row">
                                        <div class="col-10">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>

                                            <label for="bogof-tag-required-1-quantity" class="">Quantity</label>
                                            <g:field type="number" name="bogof-tag-required-1-quantity" value="1" class="py-1 pl-1 mx-1 col-2" disabled="true" />
                                            <span class="mr-3">x ${tagsRequired.isEmpty() ? tagsOffer.first().tag.description : tagsRequired.first().tag.description}</span>
                                        </div>
                                        <div class="col-2 text-right">
                                            <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'bogof', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse" id="bogofTagCollapse">
                                    <g:each in="${tagsRequired.isEmpty() ? tagsOffer.first().tag.tagProducts : tagsRequired.first().tag.tagProducts}" var="tagProduct" status="i">
                                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct.productId)}';">
                                            <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div class="row justify-content-end mb-3 mr-3">
                        <button id="bogof-product-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionProductSearchModal" onclick="$('#productModal-currentPromotionType').val('bogof');">+ Product</button>
                        <button id="bogof-category-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="$('#categoryModal-currentPromotionType').val('bogof');">+ Category</button>
                        <button id="bogof-tag-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionTagSearchModal" onclick="$('#tagModal-currentPromotionType').val('bogof');">+ Tag</button>
                    </div>
                </div>
            </div>
            <div id="bogof-summary" class="row mt-3">
                <h2 class="col-1 mr-2">Summary</h2>
                <div class="promotion-products-container col-10 offset-1">
%{--                    <h5 class="text-center m-3">Incomplete Promotion</h5>--}%
                </div>
            </div>
            <div class="row my-5">
                <g:link elementId="bogof-cancel" action="index" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <button id="bogof-save" type="button" name="bogof-save-button" onclick="quickValidateSubmit('bogof');" class="btn btn-success col-1 offset-8">Save</button>
            </div>
        </g:form>
    </div>
    <div class="tab-pane fade show ${promoType.equals('x_for_y') ? 'active' : ''}" id="xfory" role="tabpanel" aria-labelledby="xfory-tab">
        <g:form method="post" action="save" class="mt-5" name="xfory-form">
            <g:hiddenField name="promotionType" value="xfory" />
            <g:hiddenField name="promotionId" value="${promotion?.id}" />
            <g:hiddenField name="xfory-promotionItemsType" value="${productItemType}" />
            <g:hiddenField name="xfory-noItemChange" value="true" />

            <div id="xfory-details">
                <h2 class="row col-1">Details</h2>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="xfory-description" class="col-3 col-form-label text-right pr-4">Description</label>
                        <g:textField name="xfory-description" maxLength="200" class="col-5 form-control bottom-border promo-desc" required="true" value="${promotion?.description}"/>
                    </div>
                    <div class="form-group row col-12 col-sm-6">
                        <label for="xfory-receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                        <g:textField name="xfory-receiptDescription" maxLength="50" class="col-5 form-control bottom-border promo-receiptDesc" required="true" value="${promotion?.receiptDescription}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="xfory-startDate" class="col-3 col-form-label text-right pr-4">Start Date</label>
                        <div class="input-group date startDate col-7" id="xfory-startDatepicker">
                            <g:textField name="xfory-startDate" type="text" class="row form-control promo-startDate" required="true"
                                         autoComplete="off"
                                         value="${promotion ? promotion.startDate.toString("EEEE dd MMMM yyyy") : new Date().format("EEEE dd MMMM yyyy")}"/>
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6" id="xfory-endDate-container">
                        <label for="xfory-endDate" class="col-3 col-form-label text-right pr-4">End Date</label>
                        <div class="input-group date endDate col-7 mb-auto" id="xfory-endDatepicker">
                            <g:textField name="xfory-endDate" type="text" class="row form-control promo-endDate" required="true" disabled="${promotion ? promotion.endDate ? false : true : false}"
                                         autoComplete="off"
                                         value="${promotion ?
                                                    promotion.endDate ? promotion.endDate.toString("EEEE dd MMMM yyyy") : ""
                                                 : new Date().plus(7).format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="xfory-doesNotExpire" class="col-3 col-form-label text-right pr-4">Does not expire</label>
                        <g:checkBox name="xfory-doesNotExpire" class="col-1 form-check-input wl-checkbox wl-noExpire" checked="${promotion ? promotion?.endDate == null : false}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="xfory-retailerPromoId" class="col-3 col-form-label text-right pr-4">Promotion Reference</label>
                        <g:field type="number" name="xfory-retailerPromoId" min="0" max="999999999" class="col-5 form-control bottom-border promo-id" required="true" value="${promotion?.retailerPromotionId}" onkeydown="acceptNumeric(event);"/>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-active" class="col-3 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="xfory-active" class="col-1 form-check-input wl-checkbox promo-active" checked="${promotion ? promotion.active : true}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="xfory-supplier" class="col-3 col-form-label text-right pr-4">Supplier</label>
                        <g:field readonly="readonly" name="xfory-supplier"
                                 class="col-5 form-control bottom-border promo-amount" required="false"
                                 value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" type="text"/>
                    </div>
                </div>
            </div>
            <div id="xfory-products" class="collapsible-products row mt-3">
                <h2 class="col-1 mr-2">Products</h2>
                <div id="xfory-productsRequiredSection" class="promotion-products-container col-10 offset-1 mb-3">
                    <h3 class="mt-1 ml-3">Customer Buys</h3>
                    <div id="xfory-productsRequiredContainer" class="row my-2">
                        <g:hiddenField name="xfory-count-required" value="${productsRequired.isEmpty() && categoriesRequired.isEmpty() && tagsRequired.isEmpty() ? productsOffer.size() + categoriesOffer.size() + tagsOffer.size() : productsRequired.size() + categoriesRequired.size() + tagsRequired.size()}"/>
                        <g:if test="${!productsRequired?.isEmpty() || !productsOffer.isEmpty()}">
                            <div id="xfory-product1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="xfory-product-required-1-productId" value="${productsRequired.isEmpty() ? productsOffer.first().product.id : productsRequired.first().product.id}"/>
                                <g:hiddenField name="xfory-product-required-1-sku" value="${productsRequired.isEmpty() ? productsOffer.first().sku : productsRequired.first().sku}"/>
                                <label for="xfory-product-required-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="xfory-product-required-1-quantity" value="${productsRequired.isEmpty() ? productsOffer.first().quantity : productsRequired.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'xfory');"/>
                                <label for="xfory-product-required-1-quantity" class="mr-3"> x ${String.valueOf(productsRequired.isEmpty() ? productsOffer.first().product.itemCode : productsRequired.first().product.itemCode)} - ${productsRequired.isEmpty() ? productsOffer.first().product.description : productsRequired.first().product.description}</label>
                                <a href="#" onclick="return deleteThis(this, 'xfory', 'required');" class="text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!categoriesRequired.isEmpty() || !categoriesOffer.isEmpty()}">
                            <div id="xfory-category1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="xfory-category-required-1-categoryId" value="${categoriesRequired.isEmpty() ? categoriesOffer.first().category.id : categoriesRequired.first().category.id}"/>
                                <label for="xfory-category-required-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="xfory-category-required-1-quantity" value="${categoriesRequired.isEmpty() ? categoriesOffer.first().quantity : categoriesRequired.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'xfory');"/>
                                <label for="xfory-category-required-1-quantity" class="mr-3">x ${categoriesRequired.isEmpty() ? categoriesOffer.first().category.description : categoriesRequired.first().category.description}${categoriesRequired.isEmpty() ? (categoriesOffer.first().category.retailerCategoryCode != null ? " - " + categoriesOffer.first().category.retailerCategoryCode : "") : (categoriesRequired.first().category.retailerCategoryCode != null ? " - " + categoriesRequired.first().category.retailerCategoryCode : "")}</label>
                                <a href="#" onclick="return deleteThis(this, 'xfory', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!tagsRequired.isEmpty() || !tagsOffer.isEmpty()}">
                            <div id="xfory-tag1" class="card bg-light border-wl offset-1 mt-3">
                                <g:hiddenField name="xfory-tag-required-1-tagId" value="${tagsRequired.isEmpty() ? tagsOffer.first().tag.id : tagsRequired.first().tag.id}"/>

                                <div class="card-header pointer" data-toggle="collapse" data-target="#xforyRequiredTagCollapse" aria-expanded="false" aria-controls="xforyRequiredTagCollapse">
                                    <div class="row">
                                        <div class="col-10">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>
                                            <label for="xfory-tag-required-1-quantity" class="">Quantity</label>
                                            <g:field type="number" name="xfory-tag-required-1-quantity" value="${tagsRequired.isEmpty() ? tagsOffer.first().quantity : tagsRequired.first().quantity}" class="py-1 pl-1 mx-1 col-2" onChange="quantityChange(this, 'xfory');"/>
                                            <span class="mr-3">x ${tagsRequired.isEmpty() ? tagsOffer.first().tag.description : tagsRequired.first().tag.description}</span>
                                        </div>
                                        <div class="col-2 text-right">
                                            <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'xfory', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse" id="xforyRequiredTagCollapse">
                                    <g:each in="${tagsRequired.isEmpty() ? tagsOffer.first().tag.tagProducts : tagsRequired.first().tag.tagProducts}" var="tagProduct" status="i">
                                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct.productId)}';">
                                            <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div class="row justify-content-end mb-3 mr-3">
                        <button id="xfory-product-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionProductSearchModal" onclick="$('#productModal-currentPromotionType').val('xfory');">+ Product</button>
                        <button id="xfory-category-required-btn"type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="$('#categoryModal-currentPromotionType').val('xfory');">+ Category</button>
                        <button id="xfory-tag-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionTagSearchModal" onclick="$('#tagModal-currentPromotionType').val('xfory');">+ Tag</button>
                    </div>
                </div>

                <div id="xfory-productsOfferSection" class="promotion-products-container col-10 offset-1">
                    <h3 class="mt-1 ml-3">Customer Receives Free</h3>
                    <div id="xfory-productsOfferContainer" class="row my-2">
                        <g:if test="${!productsOffer?.isEmpty() || !productsRequired.isEmpty()}">
                            <div id="xfory-product1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="xfory-product-offer-1-productId" value="${productsOffer.isEmpty() ? productsRequired.first().product.id : productsOffer.first().product.id}"/>
                                <g:hiddenField name="xfory-product-offer-1-sku" value="${productsRequired.isEmpty() ? productsOffer.first().sku : productsRequired.first().sku}"/>
                                <label for="xfory-product-offer-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="xfory-product-offer-1-quantity" value="${productsOffer.isEmpty() ? productsRequired.first().quantity : productsOffer.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'xfory');"/>
                                <label for="xfory-product-offer-1-quantity" class="mr-3"> x ${String.valueOf(productsOffer.isEmpty() ? productsRequired.first().product.itemCode : productsOffer.first().product.itemCode)} - ${productsOffer.isEmpty() ? productsRequired.first().product.description : productsOffer.first().product.description}</label>
                                <a href="#" onclick="return deleteThis(this, 'xfory', 'offer');" class="text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!categoriesOffer.isEmpty() || !categoriesRequired.isEmpty()}">
                            <div id="xfory-category1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="xfory-category-offer-1-categoryId" value="${categoriesOffer.isEmpty() ? categoriesRequired.first().category.id : categoriesOffer.first().category.id}"/>
                                <label for="xfory-category-offer-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="xfory-category-offer-1-quantity" value="${categoriesOffer.isEmpty() ? categoriesRequired.first().quantity : categoriesOffer.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'xfory');"/>
                                <label for="xfory-category-offer-1-quantity" class="mr-3">x ${categoriesOffer.isEmpty() ? categoriesRequired.first().category.description : categoriesOffer.first().category.description}${categoriesOffer.isEmpty() ? (categoriesRequired.first().category.retailerCategoryCode != null ? " - " + categoriesRequired.first().category.retailerCategoryCode : "") : (categoriesOffer.first().category.retailerCategoryCode != null ? " - " + categoriesOffer.first().category.retailerCategoryCode : "")}</label>
                                <a href="#" onclick="return deleteThis(this, 'xfory', 'offer');" class="ml-3 text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!tagsOffer.isEmpty() || !tagsRequired.isEmpty()}">
                            <div id="xfory-tag1" class="card bg-light border-wl offset-1 mt-3">
                                <g:hiddenField name="xfory-tag-offer-1-tagId" value="${tagsOffer.isEmpty() ? tagsRequired.first().tag.id : tagsOffer.first().tag.id}"/>

                                <div class="card-header pointer" data-toggle="collapse" data-target="#xforyOfferTagCollapse" aria-expanded="false" aria-controls="xforyOfferTagCollapse">
                                    <div class="row">
                                        <div class="col-10">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>

                                            <label for="xfory-tag-offer-1-quantity" class="">Quantity</label>
                                            <g:field type="number" name="xfory-tag-offer-1-quantity" value="${tagsOffer.isEmpty() ? tagsRequired.first().quantity : tagsOffer.first().quantity}" class="py-1 pl-1 mx-1 col-2" onChange="quantityChange(this, 'xfory');"/>
                                            <span class="mr-3">x ${tagsOffer.isEmpty() ? tagsRequired.first().tag.description : tagsOffer.first().tag.description}</span>
                                        </div>
                                        <div class="col-2 text-right">
                                            <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'xfory', 'offer');" class="ml-3 text-dark"><sup>X</sup></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse" id="xforyOfferTagCollapse">
                                    <g:each in="${tagsOffer.isEmpty() ? tagsRequired.first().tag.tagProducts : tagsOffer.first().tag.tagProducts}" var="tagProduct" status="i">
                                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct.productId)}';">
                                            <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div>
            <div id="xfory-summary" class="row mt-3">
                <h2 class="col-1 mr-2">Summary</h2>
                <div class="promotion-products-container col-10 offset-1">
%{--                    <h5 class="text-center m-3">Incomplete Promotion</h5>--}%
                </div>
            </div>
            <div class="row my-5">
                <g:link elementId="xfory-cancel" action="index" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <button id="xfory-save" type="button" name="xfory-save-button" onclick="quickValidateSubmit('xfory');" class="btn btn-success col-1 offset-8">Save</button>
            </div>
        </g:form>
    </div>
    <div class="tab-pane fade show ${promoType.equals('percentage_discount') ? 'active' : ''}" id="percentage" role="tabpanel" aria-labelledby="percentage-tab">
        <g:form method="post" action="save" class="mt-5" name="percentage-form">
            <g:hiddenField name="promotionType" value="percentage"/>
            <g:hiddenField name="promotionId" value="${promotion?.id}"/>
            <g:hiddenField name="percentage-promotionItemsType" value="${productItemType}" />
            <g:hiddenField name="percentage-noItemChange" value="true"/>

            <div id="percentage-details">
                <h2 class="row col-1">Details</h2>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-description" class="col-3 col-form-label text-right pr-4">Description</label>
                        <g:textField name="percentage-description" maxLength="200" class="col-5 form-control bottom-border promo-desc" required="true" value="${promotion?.description}"/>
                    </div>
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                        <g:textField name="percentage-receiptDescription" maxLength="50" class="col-5 form-control bottom-border promo-receiptDesc" required="true" value="${promotion?.receiptDescription}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-startDate" class="col-3 col-form-label text-right pr-4">Start Date</label>
                        <div class="input-group date startDate col-7" id="percentage-startDatepicker">
                            <g:textField name="percentage-startDate" type="text" class="row form-control promo-startDate" required="true"
                                         autoComplete="off"
                                         value="${promotion ? promotion.startDate.toString("EEEE dd MMMM yyyy") : new Date().format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6" id="percentage-endDate-container">
                        <label for="percentage-endDate" class="col-3 col-form-label text-right pr-4">End Date</label>
                        <div class="input-group date endDate col-7 mb-auto" id="percentage-endDatepicker">
                            <g:textField name="percentage-endDate" type="text" class="row form-control promo-endDate" required="true" disabled="${promotion ? promotion.endDate ? false : true : false}"
                                         autoComplete="off"
                                         value="${promotion ?
                                                    promotion.endDate ? promotion.endDate.toString("EEEE dd MMMM yyyy") : ""
                                                 : new Date().plus(7).format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="percentage-doesNotExpire" class="col-3 col-form-label text-right pr-4">Does not expire</label>
                        <g:checkBox name="percentage-doesNotExpire" class="col-1 form-check-input wl-checkbox wl-noExpire" checked="${promotion ? promotion?.endDate == null : false}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-retailerPromoId" class="col-3 col-form-label text-right pr-4">Promotion Reference</label>
                        <g:field type="number" name="percentage-retailerPromoId" min="0" max="999999999" class="col-5 form-control bottom-border promo-id" required="true" value="${promotion?.retailerPromotionId}" onkeydown="acceptNumeric(event);"/>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-active" class="col-3 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="percentage-active" class="col-1 form-check-input wl-checkbox promo-active" checked="${promotion ? promotion.active : true}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-amount" class="col-3 col-form-label text-right pr-4">Percentage</label>
                        <g:textField name="percentage-amount" class="col-5 form-control bottom-border mask-money" required="true" value="${promotion?.amount != null ? promotion.amount : 0.01}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="percentage-supplier" class="col-3 col-form-label text-right pr-4">Supplier</label>
                        <g:field readonly="readonly" name="percentage-supplier"
                                 class="col-5 form-control bottom-border promo-amount" required="false"
                                 value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" type="text"/>
                    </div>
                </div>
            </div>
            <div id="percentage-products" class="collapsible-products row mt-3">
                <h2 class="col-1 mr-2">Products</h2>
                <div id="percentage-productsRequiredSection" class="promotion-products-container col-10 offset-1">
                    <h3 class="mt-1 ml-3">Customer Buys & Receives % Off</h3>
                    <div id="percentage-productsRequiredContainer" class="row my-2">
                        <g:hiddenField name="percentage-count-required" value="${productsRequired.isEmpty() && categoriesRequired.isEmpty() && tagsRequired.isEmpty() ? productsOffer.size() + categoriesOffer.size() + tagsOffer.size() : productsRequired.size() + categoriesRequired.size() + tagsRequired.size()}"/>
                        <g:if test="${!productsRequired?.isEmpty() || !productsOffer.isEmpty()}">
                            <div id="percentage-product1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="percentage-product-required-1-productId" value="${productsRequired?.isEmpty() ? productsOffer.first().product.id : productsRequired.first().product.id}"/>
                                <g:hiddenField name="percentage-product-required-1-sku" value="${productsRequired.isEmpty() ? productsOffer.first().sku : productsRequired.first().sku}"/>
                                <label for="percentage-product-required-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="percentage-product-required-1-quantity" value="${productsRequired?.isEmpty() ? productsOffer.first().quantity : productsRequired.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'percentage');"/>
                                <label for="percentage-product-required-1-quantity" class="mr-3"> x ${String.valueOf(productsRequired?.isEmpty() ? productsOffer.first().product.itemCode : productsRequired.first().product.itemCode)} - ${productsRequired?.isEmpty() ? productsOffer.first().product.description : productsRequired.first().product.description}</label>
                                <a href="#" onclick="return deleteThis(this, 'percentage', 'required');" class="text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!categoriesRequired.isEmpty() || !categoriesOffer.isEmpty()}">
                            <div id="percentage-category1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="percentage-category-required-1-categoryId" value="${categoriesRequired.isEmpty() ? categoriesOffer.first().category.id : categoriesRequired.first().category.id}"/>
                                <label for="percentage-category-required-1-quantity" class="">Quantity</label>
                                <g:field type="number" name="percentage-category-required-1-quantity" value="${categoriesRequired.isEmpty() ? categoriesOffer.first().quantity : categoriesRequired.first().quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'percentage');"/>
                                <label for="percentage-category-required-1-quantity" class="mr-3">x ${categoriesRequired.isEmpty() ? categoriesOffer.first().category.description : categoriesRequired.first().category.description}${categoriesRequired.isEmpty() ? (categoriesOffer.first().category.retailerCategoryCode != null ? " - " + categoriesOffer.first().category.retailerCategoryCode : "") : (categoriesRequired.first().category.retailerCategoryCode != null ? " - " + categoriesRequired.first().category.retailerCategoryCode : "")}</label>
                                <a href="#" onclick="return deleteThis(this, 'percentage', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!tagsRequired.isEmpty() || !tagsOffer.isEmpty()}">
                            <div id="percentage-tag1" class="card bg-light border-wl offset-1 mt-3">
                                <g:hiddenField name="percentage-tag-required-1-tagId" value="${tagsRequired.isEmpty() ? tagsOffer.first().tag.id : tagsRequired.first().tag.id}"/>

                                <div class="card-header pointer" data-toggle="collapse" data-target="#percentageTagCollapse" aria-expanded="false" aria-controls="percentageTagCollapse">
                                    <div class="row">
                                        <div class="col-10">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>
                                            <label for="percentage-tag-required-1-quantity" class="">Quantity</label>
                                            <g:field type="number" name="percentage-tag-required-1-quantity" value="${tagsRequired.isEmpty() ? tagsOffer.first().quantity : tagsRequired.first().quantity}" class="py-1 pl-1 mx-1 col-2" onChange="quantityChange(this, 'percentage');"/>
                                            <span class="mr-3">x ${tagsRequired.isEmpty() ? tagsOffer.first().tag.description : tagsRequired.first().tag.description}</span>
                                        </div>
                                        <div class="col-2 text-right">
                                            <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'percentage', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse" id="percentageTagCollapse">
                                    <g:each in="${tagsRequired.isEmpty() ? tagsOffer.first().tag.tagProducts : tagsRequired.first().tag.tagProducts}" var="tagProduct" status="i">
                                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct.productId)}';">
                                            <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div class="row justify-content-end mb-3 mr-3">
                        <button id="percentage-product-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionProductSearchModal" onclick="$('#productModal-currentPromotionType').val('percentage');">+ Product</button>
                        <button id="percentage-category-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="$('#categoryModal-currentPromotionType').val('percentage');">+ Category</button>
                        <button id="percentage-tag-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionTagSearchModal" onclick="$('#tagModal-currentPromotionType').val('percentage');">+ Tag</button>
                    </div>
                </div>
            </div>
            <div id="percentage-summary" class="row mt-3">
                <h2 class="col-1 mr-2">Summary</h2>
                <div class="promotion-products-container col-10 offset-1">
%{--                    <h5 class="text-center m-3">Incomplete Promotion</h5>--}%
                </div>
            </div>
            <div class="row my-5">
                <g:link elementId="percentage-cancel" action="index" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <button id="percentage-save" type="button" name="percentage-save-button" onclick="quickValidateSubmit('percentage');" class="btn btn-success col-1 offset-8">Save</button>
            </div>
        </g:form>
    </div>
    <div class="tab-pane fade show ${promoType.equals('fixed_amount_discount') ? 'active' : ''}" id="fixedAmount" role="tabpanel" aria-labelledby="fixedAmount-tab">
        <g:form method="post" action="save" class="mt-5" name="fixedAmount-form">
            <g:hiddenField name="promotionType" value="fixedAmount" />
            <g:hiddenField name="promotionId" value="${promotion?.id}" />
            <g:hiddenField name="fixedAmount-promotionItemsType" value="${productItemType}" />
            <g:hiddenField name="fixedAmount-noItemChange" value="true" />

            <div id="fixedAmount-details">
                <h2 class="row col-1">Details</h2>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-description" class="col-3 col-form-label text-right pr-4">Description</label>
                        <g:textField name="fixedAmount-description" maxLength="200" class="col-5 form-control bottom-border promo-desc" required="true" value="${promotion?.description}"/>
                    </div>
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                        <g:textField name="fixedAmount-receiptDescription" maxLength="50" class="col-5 form-control bottom-border promo-receiptDesc" required="true" value="${promotion?.receiptDescription}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-startDate" class="col-3 col-form-label text-right pr-4">Start Date</label>
                        <div class="input-group date startDate col-7" id="fixedAmount-startDatepicker">
                            <g:textField name="fixedAmount-startDate" class="row form-control promo-startDate" required="true"
                                         autoComplete="off"
                                         value="${promotion ? promotion.startDate.toString("EEEE dd MMMM yyyy") : new Date().format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-endDate" class="col-3 col-form-label text-right pr-4">End Date</label>
                        <div class="input-group date endDate col-7 mb-auto" id="fixedAmount-endDatepicker">
                            <g:textField name="fixedAmount-endDate" class="row form-control promo-endDate" required="true" disabled="${promotion ? promotion.endDate ? false : true : false}"
                                         autoComplete="off"
                                         value="${promotion ?
                                                    promotion.endDate ? promotion.endDate.toString("EEEE dd MMMM yyyy") : ""
                                                 : new Date().plus(7).format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="fixedAmount-doesNotExpire" class="col-3 col-form-label text-right pr-4">Does not expire</label>
                        <g:checkBox name="fixedAmount-doesNotExpire" class="col-1 form-check-input wl-checkbox wl-noExpire" checked="${promotion ? promotion?.endDate == null : false}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-retailerPromoId" class="col-3 col-form-label text-right pr-4">Promotion Reference</label>
                        <g:field type="number" name="fixedAmount-retailerPromoId" min="0" max="999999999" class="col-5 form-control bottom-border promo-id" required="true" value="${promotion?.retailerPromotionId}" onkeydown="acceptNumeric(event);"/>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-active" class="col-3 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="fixedAmount-active" class="col-1 form-check-input wl-checkbox promo-active" checked="${promotion ? promotion.active : true}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-amount" class="col-3 col-form-label text-right pr-4">Discount Amount</label>
                        <g:textField id="fixedAmount-amount" name="fixedAmount-amount" class="col-5 form-control bottom-border promo-amount mask-money" required="true" value="${promotion?.amount != null ? promotion.amount : 0.01}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedAmount-supplier" class="col-3 col-form-label text-right pr-4">Supplier</label>
                        <g:field readonly="readonly" name="fixedAmount-supplier"
                                 class="col-5 form-control bottom-border promo-amount" required="false"
                                 value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" type="text"/>
                    </div>
                </div>
            </div>
            <div id="fixedAmount-products" class="collapsible-products row mt-3">
                <h2 class="col-1 mr-2">Products</h2>
                <div id="fixedAmount-productsRequiredSection" class="promotion-products-container col-10 offset-1">
                    <h3 class="mt-1 ml-3">Customer Buys & Receives Amount Off</h3>
                    <div id="fixedAmount-productsRequiredContainer" class="row my-2">
                        <g:hiddenField name="fixedAmount-count-required" value="${productsRequired.isEmpty() && categoriesRequired.isEmpty() && tagsRequired.isEmpty() ? productsOffer.size() + categoriesOffer.size() + tagsOffer.size() : productsRequired.size() + categoriesRequired.size() + tagsRequired.size()}"/>

                        <g:if test="${!productsRequired?.isEmpty() || !productsOffer.isEmpty()}">
                            <div id="fixedAmount-product1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="fixedAmount-product-required-1-productId" value="${productsRequired?.isEmpty() ? productsOffer.first().product.id : productsRequired.first().product.id}"/>
                                <g:hiddenField name="fixedAmount-product-required-1-sku" value="${productsRequired.isEmpty() ? productsOffer.first().sku : productsRequired.first().sku}"/>
                                <label for="fixedAmount-product-required-1-value" class="">Value</label>
                                <g:field type="number" name="fixedAmount-product-required-1-value" value="${productsRequired?.isEmpty() ? productsOffer.first().value : productsRequired.first().value}" step="0.01" class="py-1 pl-1 mx-1 form-control promo-value" onChange="quantityValueChange(this, 'value', 'fixedAmount');"/>
                                <label for="fixedAmount-product-required-1-quantity" class=""> or Quantity</label>
                                <g:field type="number" name="fixedAmount-product-required-1-quantity" value="${productsRequired?.isEmpty() ? productsOffer.first().quantity : productsRequired.first().quantity}" step="1" class="py-1 pl-1 mx-1 form-control promo-quantity" onChange="quantityChange(this, 'fixedAmount');"/>
                                <label for="fixedAmount-product-required-1-quantity" class="mr-3"> x ${String.valueOf(productsRequired?.isEmpty() ? productsOffer.first().product.itemCode : productsRequired.first().product.itemCode)} - ${productsRequired?.isEmpty() ? productsOffer.first().product.description : productsRequired.first().product.description}</label>
                                <a href="#" onclick="return deleteThis(this, 'fixedAmount', 'required');" class="text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!categoriesRequired?.isEmpty() || !categoriesOffer.isEmpty()}">
                            <div id="fixedAmount-category1" class="offset-1 promotion-product-container form-inline mt-3">
                                <g:hiddenField name="fixedAmount-category-required-1-categoryId" value="${categoriesRequired?.isEmpty() ? categoriesOffer.first().category.id : categoriesRequired.first().category.id}"/>
                                <label for="fixedAmount-category-required-1-value" class="">Value</label>
                                <g:field type="number" name="fixedAmount-category-required-1-value" value="${categoriesRequired?.isEmpty() ? categoriesOffer.first().value : categoriesRequired.first().value}" step="0.01" class="py-1 pl-1 mx-1 form-control promo-value" onChange="quantityValueChange(this, 'value', 'fixedAmount');"/>
                                <label for="fixedAmount-category-required-1-quantity" class=""> or Quantity</label>
                                <g:field type="number" name="fixedAmount-category-required-1-quantity" value="${categoriesRequired?.isEmpty() ? categoriesOffer.first().quantity : categoriesRequired.first().quantity}" step="1" class="py-1 pl-1 mx-1 form-control promo-quantity" onChange="quantityChange(this, 'fixedAmount');"/>
                                <label for="fixedAmount-category-required-1-quantity" class="mr-3"> x ${categoriesRequired?.isEmpty() ? categoriesOffer.first().category.description : categoriesRequired.first().category.description}${categoriesRequired.isEmpty() ? (categoriesOffer.first().category.retailerCategoryCode != null ? " - " + categoriesOffer.first().category.retailerCategoryCode : "") : (categoriesRequired.first().category.retailerCategoryCode != null ? " - " + categoriesRequired.first().category.retailerCategoryCode : "")}</label>
                                <a href="#" onclick="return deleteThis(this, 'fixedAmount', 'required');" class="text-dark"><sup>X</sup></a>
                            </div>
                        </g:if>
                        <g:if test="${!tagsRequired?.isEmpty() || !tagsOffer.isEmpty()}">
                            <div id="fixedAmount-tag1" class="card bg-light border-wl offset-1 mt-3">
                                <g:hiddenField name="fixedAmount-tag-required-1-tagId" value="${tagsRequired?.isEmpty() ? tagsOffer.first().tag.id : tagsRequired.first().tag.id}"/>

                                <div class="card-header pointer" data-toggle="collapse" data-target="#fixedAmountTagCollapse" aria-expanded="false" aria-controls="fixedAmountTagCollapse">
                                    <div class="row">
                                        <div class="col-10">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>

                                            <label for="fixedAmount-tag-required-1-value" class="">Value</label>
                                            <g:field type="number" name="fixedAmount-tag-required-1-value" value="${tagsRequired?.isEmpty() ? tagsOffer.first().value : tagsRequired.first().value}" step="0.01" class="py-1 pl-1 mx-1 col-2 promo-value" onChange="quantityValueChange(this, 'value', 'fixedAmount');"/>
                                            <label for="fixedAmount-tag-required-1-quantity" class=""> or Quantity</label>
                                            <g:field type="number" name="fixedAmount-tag-required-1-quantity" value="${tagsRequired?.isEmpty() ? tagsOffer.first().quantity : tagsRequired.first().quantity}" step="1" class="py-1 pl-1 mx-1 col-2 promo-quantity" onChange="quantityChange(this, 'fixedAmount');"/>
                                            <span class="mr-3"> x ${tagsRequired?.isEmpty() ? tagsOffer.first().tag.description : tagsRequired.first().tag.description}</span>
                                        </div>
                                        <div class="col-2 text-right">
                                            <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'fixedAmount', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse" id="fixedAmountTagCollapse">
                                    <g:each in="${tagsRequired.isEmpty() ? tagsOffer.first().tag.tagProducts : tagsRequired.first().tag.tagProducts}" var="tagProduct" status="i">
                                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct?.productId)}';">
                                            <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div class="row justify-content-end mb-3 mr-3">
                        <button id="fixedAmount-product-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionProductSearchModal" onclick="$('#productModal-currentPromotionType').val('fixedAmount');">+ Product</button>
                        <button id="fixedAmount-category-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="$('#categoryModal-currentPromotionType').val('fixedAmount');">+ Category</button>
                        <button id="fixedAmount-tag-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionTagSearchModal" onclick="$('#tagModal-currentPromotionType').val('fixedAmount');">+ Tag</button>
                    </div>
                </div>
            </div>
            <div id="fixedAmount-summary" class="row mt-3">
                <h2 class="col-1 mr-2">Summary</h2>
                <div class="promotion-products-container col-10 offset-1">
%{--                    <h5 class="text-center m-3">Incomplete Promotion</h5>--}%
                </div>
            </div>
            <div class="row my-5">
                <g:link elementId="fixedAmount-cancel" action="index" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <button id="fixedAmount-save" type="button" name="fixedAmount-save-button" onclick="quickValidateSubmit('fixedAmount');" class="btn btn-success col-1 offset-8">Save</button>
            </div>
        </g:form>
    </div>
    <div class="tab-pane fade show ${promoType.equals('fixed_price') ? 'active' : ''}" id="fixedPrice" role="tabpanel" aria-labelledby="fixedPrice-tab">
        <g:form method="post" action="save" class="mt-5" name="fixedPrice-form">
            <g:hiddenField name="promotionType" value="fixedPrice" />
            <g:hiddenField name="promotionId" value="${promotion?.id}" />
            <g:hiddenField name="fixedPrice-promotionItemsType" value="${productItemType}" />
            <g:hiddenField name="fixedPrice-noItemChange" value="true" />

            <div id="fixedPrice-details">
                <h2 class="row col-1">Details</h2>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-description" class="col-3 col-form-label text-right pr-4">Description</label>
                        <g:textField name="fixedPrice-description" maxLength="200" class="col-5 form-control bottom-border promo-desc" required="true" value="${promotion?.description}"/>
                    </div>
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-receiptDescription" class="col-3 col-form-label text-right pr-4">Receipt Description</label>
                        <g:textField name="fixedPrice-receiptDescription" maxLength="50" class="col-5 form-control bottom-border promo-receiptDesc" required="true" value="${promotion?.receiptDescription}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-startDate" class="col-3 col-form-label text-right pr-4">Start Date</label>
                        <div class="input-group date startDate col-7" id="fixedPrice-startDatepicker">
                            <g:textField name="fixedPrice-startDate" type="text" class="row form-control promo-startDate" required="true"
                                         autoComplete="off"
                                         value="${promotion ? promotion.startDate.toString("EEEE dd MMMM yyyy") : new Date().format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-endDate" class="col-3 col-form-label text-right pr-4">End Date</label>
                        <div class="input-group date endDate col-7 mb-auto" id="fixedPrice-endDatepicker">
                            <g:textField name="fixedPrice-endDate" type="text" class="row form-control promo-endDate" required="true" disabled="${promotion ? promotion.endDate ? false : true : false}"
                                         autoComplete="off"
                                         value="${promotion ?
                                                    promotion.endDate ? promotion.endDate.toString("EEEE dd MMMM yyyy") : ""
                                                 : new Date().plus(7).format("EEEE dd MMMM yyyy")}" />
                            <span class="input-group-addon">
                                <i class="glyphicon glyphicon-calendar" content="\e109"></i>
                            </span>
                        </div>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="fixedPrice-doesNotExpire" class="col-3 col-form-label text-right pr-4">Does not expire</label>
                        <g:checkBox name="fixedPrice-doesNotExpire" class="col-1 form-check-input wl-checkbox wl-noExpire" checked="${promotion ? promotion?.endDate == null : false}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-retailerPromoId" class="col-3 col-form-label text-right pr-4">Promotion Reference</label>
                        <g:field type="number" name="fixedPrice-retailerPromoId" min="0" max="999999999" class="col-5 form-control bottom-border promo-id" required="true" value="${promotion?.retailerPromotionId}" onkeydown="acceptNumeric(event);"/>
                    </div>
                    <div class="form-group form-check row col-12 col-sm-6">
                        <label for="bogof-active" class="col-3 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="fixedPrice-active" class="col-1 form-check-input wl-checkbox promo-active" checked="${promotion ? promotion.active : true}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-amount" class="col-3 col-form-label text-right pr-4">Fixed Amount</label>
                        <g:textField name="fixedPrice-amount" class="col-5 form-control bottom-border promo-amount mask-money" required="true" value="${promotion?.amount != null ? promotion.amount : 0.01}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group row col-12 col-sm-6">
                        <label for="fixedPrice-supplier" class="col-3 col-form-label text-right pr-4">Supplier</label>
                        <g:field readonly="readonly" name="fixedPrice-supplier"
                                 class="col-5 form-control bottom-border promo-amount" required="false"
                                 value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" type="text"/>
                    </div>
                </div>
            </div>
            <div id="fixedPrice-products" class="collapsible-products row mt-3">
                <h2 class="col-1 mr-2">Products</h2>
                <div id="fixedPrice-productsRequiredSection" class="promotion-products-container col-10 offset-1">
                    <h3 class="mt-1 ml-3">Customer Buys For Amount</h3>
                    <div class="alert alert-danger alert-wl" id="fixedPrice-duplicateAlert" style="display: none;">Item already exists in the promotion</div>
                    <div id="fixedPrice-productsRequiredContainer" class="row my-2">
                        <g:hiddenField name="fixedPrice-count-required" value="${productsRequired.isEmpty() && categoriesRequired.isEmpty() && tagsRequired.isEmpty() ? productsOffer.size() + categoriesOffer.size() + tagsOffer.size() : productsRequired.size() + categoriesRequired.size() + tagsRequired.size()}"/>
                        <g:if test="${!productsRequired?.isEmpty() || !productsOffer.isEmpty()}">
                            <g:each in="${productsRequired.isEmpty() ? productsOffer : productsRequired}" var="product" status="i">
                                <div id="fixedPrice-product${i}" class="offset-1 promotion-product-container form-inline mt-3">
                                    <g:hiddenField name="fixedPrice-product-required-${i}-sku" value="${product.sku}" class="promo-itemId"/>
                                    <label for="fixedPrice-product-required-${i}-quantity" class="">Quantity</label>
                                    <g:field type="number" name="fixedPrice-product-required-${i}-quantity" value="${product.quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'fixedPrice');"/>
                                    <label for="fixedPrice-product-required-${i}-quantity" class="mr-3"> x ${String.valueOf(product.product.itemCode)} - ${product.product.description}</label>
                                    <a href="#" onclick="return deleteThis(this, 'fixedPrice', 'required');" class="text-dark"><sup>X</sup></a>
                                </div>
                            </g:each>
                        </g:if>
                        <g:if test="${!categoriesRequired?.isEmpty() || !categoriesOffer.isEmpty()}">
                            <g:each in="${categoriesRequired.isEmpty() ? categoriesOffer : categoriesRequired}" var="category" status="i">
                                <div id="fixedPrice-category${i}" class="offset-1 promotion-product-container form-inline mt-3">
                                    <g:hiddenField name="fixedPrice-category-required-${i}-categoryId" value="${category.category.id}" class="promo-itemId"/>
                                    <label for="fixedPrice-category-required-${i}-quantity" class="">Quantity</label>
                                    <g:field type="number" name="fixedPrice-category-required-${i}-quantity" value="${category.quantity}" class="py-1 pl-1 mx-1 form-control" onChange="quantityChange(this, 'fixedPrice');"/>
                                    <label for="fixedPrice-category-required-${i}-quantity" class="mr-3"> x ${category.category.description}${category.category.retailerCategoryCode != null ? " - " + category.category.retailerCategoryCode : ""}</label>
                                    <a href="#" onclick="return deleteThis(this, 'fixedPrice', 'required');" class="text-dark"><sup>X</sup></a>
                                </div>
                            </g:each>
                        </g:if>
                        <g:if test="${!tagsRequired?.isEmpty() || !tagsOffer.isEmpty()}">
                            <g:each in="${tagsRequired.isEmpty() ? tagsOffer : tagsRequired}" var="tag" status="i">
                                <div id="fixedPrice-tag${i}" class="card bg-light border-wl offset-1 mt-3">
                                    <g:hiddenField name="fixedPrice-tag-required-${i}-tagId" value="${tag.tag.id}" class="promo-itemId" />

                                    <div class="card-header pointer" data-toggle="collapse" data-target="#fixedPriceTag${i}Collapse" aria-expanded="false" aria-controls="fixedPriceTag${i}Collapse">
                                        <div class="row">
                                            <div class="col-10">
                                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                                </svg>

                                                <label for="fixedPrice-tag-required-${i}-quantity" class="">Quantity</label>
                                                <g:field type="number" name="fixedPrice-tag-required-${i}-quantity" value="${tag.quantity}" class="py-1 pl-1 mx-1 col-2" onChange="quantityChange(this, 'fixedPrice');"/>
                                                <span class="mr-3"> x ${tag.tag.description}</span>
                                            </div>
                                            <div class="col-2 text-right">
                                                <a href="#" onclick="return deleteThis(this.parentElement.parentElement.parentElement, 'fixedPrice', 'required');" class="ml-3 text-dark"><sup>X</sup></a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="card-body collapse" id="fixedPriceTag${i}Collapse">
                                        <g:each in="${tagsRequired.isEmpty() ? tagsOffer.first().tag.tagProducts : tagsRequired.first().tag.tagProducts}" var="tagProduct" status="ind">
                                            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${ind%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(controller: 'product', action: 'show', id: tagProduct.productId)}';">
                                                <div class="col-12">${tagProduct.sku} - ${tagProduct.productDescription}</div>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </g:each>
                        </g:if>
                    </div>
                    <div class="row justify-content-end mb-3 mr-3">
                        <button id="fixedPrice-product-required-btn" type="button" class="btn btn-wl mr-1" ${!categoriesRequired?.isEmpty() || !tagsRequired?.isEmpty() || !categoriesOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionProductSearchModal" onclick="$('#productModal-currentPromotionType').val('fixedPrice');">+ Product</button>
                        <button id="fixedPrice-category-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() || !tagsRequired?.isEmpty() || !productsOffer?.isEmpty() || !tagsOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="$('#categoryModal-currentPromotionType').val('fixedPrice');">+ Category</button>
                        <button id="fixedPrice-tag-required-btn" type="button" class="btn btn-wl mr-1" ${!productsRequired?.isEmpty() ||  !categoriesRequired?.isEmpty() || !productsOffer?.isEmpty() ||  !categoriesOffer?.isEmpty() ? "disabled" : ""} data-toggle="modal" data-target="#promotionTagSearchModal" onclick="$('#tagModal-currentPromotionType').val('fixedPrice');">+ Tag</button>
                    </div>
                </div>
            </div>
            <div id="fixedPrice-summary" class="row mt-3">
                <h2 class="col-1 mr-2">Summary</h2>
                <div class="promotion-products-container col-10 offset-1">
%{--                    <h5 class="text-center m-3">Incomplete Promotion</h5>--}%
                </div>
            </div>
            <div class="row my-5">
                <g:link elementId="fixedPrice-cancel" action="index" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <button id="fixedPrice-save" type="button" name="fixedPrice-save-button" onclick="quickValidateSubmit('fixedPrice');" class="btn btn-success col-1 offset-8">Save</button>
            </div>
        </g:form>
    </div>
</div>