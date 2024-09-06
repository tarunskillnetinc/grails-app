<g:form name="add-promotion-form" action="save">
    <g:hiddenField name="id" value="${promotion?.id}" />

    <g:if test="${!canEdit}">
        <script>
            disableSaveButton();
            $(document).ready(function(){
                $('form[name="add-promotion-form"] input, form[name="add-promotion-form"] select, form[name="add-promotion-form"] textarea, form[name="add-promotion-form"] button').attr('disabled', true);
            });
        </script>
    </g:if>

    <div id="accordion">
        <!-- Promotion information. -->
        <div class="card bg-light border-wl accordion-card col-12 col-md-10 offset-md-1 px-0">
            <div class="card-header pointer" id="promotionDetails" data-toggle="collapse" data-target="#collapsePromotionDetails" aria-expanded="true" aria-controls="collapsePromotionDetails">
                <div class="row">
                    <div class="col-10 font-weight-bold">Promotion Details</div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapsePromotionDetails" class="collapse show" aria-labelledby="promotionDetails" data-parent="#accordion">
                <div class="card-body py-5">
                    <div class="row">
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <label for="type" class="col-4 col-form-label text-right pr-4">Promotion Type</label>
                            <g:select from="${promotionTypes}" name="type" value="${promotion?.type}" valueMessagePrefix="PromotionType" class="col-6 form-control select-border" />
                        </div>
                        <div class="row form-group form-check col-12 col-md-6 mx-0" style="padding-left: 15px !important;">
                            <label for="active" class="col-4 col-form-label text-right pr-4">Loyalty promotion</label>
                            <g:checkBox name="loyalty" class="col-1 form-check-input wl-checkbox mx-0" checked="${promotion ? promotion?.loyalty : false}" />
                        </div>
                    </div>

                    <div class="row">
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <label for="retailerPromotionId" class="col-4 col-form-label text-right pr-4">Promotion Reference</label>
                            <g:field type="number" name="retailerPromotionId" min="0" max="999999999" class="col-6 form-control bottom-border" value="${promotion?.retailerPromotionId}" />
                        </div>
                        <div class="row form-group form-check col-12 col-md-6 mx-0" style="padding-left: 15px !important;">
                            <label for="active" class="col-4 col-form-label text-right pr-4">Promotion Active</label>
                            <g:checkBox name="active" class="col-1 form-check-input wl-checkbox mx-0" checked="${promotion ? promotion?.active : true}" />
                        </div>
                    </div>

                    <div class="row">
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                            <g:textField name="description" maxLength="200" class="col-6 form-control bottom-border" required="true" value="${promotion?.description}" />
                        </div>
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <label for="receiptDescription" class="col-4 col-form-label text-right pr-4">Receipt Description</label>
                            <g:textField name="receiptDescription" maxLength="50" class="col-6 form-control bottom-border" required="true" value="${promotion?.receiptDescription}" />
                        </div>
                    </div>

                    <div class="row">
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <g:hiddenField name="startDate" value="${promotion?.startDate?.toString("yyyy-MM-dd") ?: new Date().format("yyyy-MM-dd")}" />

                            <label for="startDateInput" class="col-4 col-form-label text-right pr-4">Start Date</label>
                            <g:textField name="startDateInput" type="text" class="col-6 form-control" required="true"
                                         autoComplete="off"
                                         value="${promotion?.startDate?.toString("EEEE dd MMMM yyyy") ?: new Date().format("EEEE dd MMMM yyyy")}" />
                        </div>

                        <div class="row form-group col-12 col-md-6 mx-0">
                            <g:hiddenField name="endDate" value="${promotion?.endDate?.toString("yyyy-MM-dd")}" />

                            <label for="endDateInput" class="col-4 col-form-label text-right pr-4">End Date</label>
                            <g:textField name="endDateInput" type="text" class="col-6 form-control" required="true"
                                         autoComplete="off"
                                         value="${promotion?.endDate?.toString("EEEE dd MMMM yyyy")}" />
                        </div>
                    </div>

                    <div class="row" id="amount-div" style="<g:promotionTypeAmountDisplay type='${promotion?.type}' />">
                        <div class="row form-group col-12 col-md-6 mx-0">
                            <label id="amount-label" for="amount" class="col-4 col-form-label text-right pr-4">Fixed Price</label>
                            <g:textField name="amount" class="col-6 form-control bottom-border mask-money" value="${promotion?.amount }" />
                        </div>
                    </div>

                    <g:if test="${promotion?.symbolGroupPromotion}">
                        <div class="row">
                            <div class="row form-group col-12 col-md-6 mx-0">
                                <label for="supplier" class="col-4 col-form-label text-right pr-4">Supplier</label>
                                <g:textField readonly="readonly" name="supplier" class="col-6 form-control bottom-border" value="${promotion?.symbolGroupPromotion?.symbolGroup?.name}" />
                            </div>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>

        <!-- Required groups. -->
        <div id="requiredGroupsDiv" class="card bg-light border-wl accordion-card col-12 col-md-10 offset-md-1 px-0" style="<g:promotionTypeRequiredGroupsDisplay type='${promotion?.type}' />">
            <div class="card-header pointer" id="promotionRequiredGroups" data-toggle="collapse" data-target="#collapsePromotionRequiredGroups" aria-expanded="true" aria-controls="collapsePromotionRequiredGroups">
                <div class="row">
                    <div class="col-10 font-weight-bold"><span id="requiredGroupsHeading"><g:promotionTypeRequiredGroupsHeading type='${promotion?.type}' /></span></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapsePromotionRequiredGroups" class="collapse" aria-labelledby="promotionRequiredGroups" data-parent="#accordion">
                <div class="card-body py-5">
                    <div id="promotionRequiredGroupsContainer" class="row mb-4 justify-content-center">
                        <g:each in="${promotion?.requiredGroups}" var="promotionGroup" status="i">
                            <g:render template="promotionGroup" model="[promotionGroup: promotionGroup, promoGroupName: 'requiredPromoGroup-' +i, promotionGroupType: 'required', promoGroupId: i, showQuantityField: g.showQuantityField(promotionType: promotion?.type), showValueField: g.showValueField(promotionType: promotion?.type), promotionGroupDescription: g.promotionGroupHeader(promotionGroup: promotionGroup)]" />
                        </g:each>
                    </div>

                    <div class="row">
                        <div class="col-12 text-right">
                            <button id="requiredAddProductButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionProductSearchModal" onclick="selectedPromotionGroupType = 'required';">+ Product</button>
                            <button id="requiredAddCategoryButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="selectedPromotionGroupType = 'required';">+ Category</button>
                            <button id="requiredAddTagButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionTagSearchModal" onclick="selectedPromotionGroupType = 'required';">+ Tag</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Offer groups. -->
        <div class="card bg-light border-wl accordion-card col-12 col-md-10 offset-md-1 px-0">
            <div class="card-header pointer" id="promotionOfferGroups" data-toggle="collapse" data-target="#collapsePromotionOfferGroups" aria-expanded="true" aria-controls="collapsePromotionOfferGroups">
                <div class="row">
                    <div class="col-10 font-weight-bold"><span id="offerGroupsHeading"><g:promotionTypeOfferGroupsHeading type='${promotion?.type}' /></span></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapsePromotionOfferGroups" class="collapse" aria-labelledby="promotionOfferGroups" data-parent="#accordion">
                <div class="card-body py-5">
                    <div id="promotionOfferGroupsContainer" class="row mb-4 justify-content-center">
                        <g:each in="${promotion?.offerGroups}" var="promotionGroup" status="i">
                            <g:render template="promotionGroup" model="[promotionGroup: promotionGroup, promoGroupName: 'offerPromoGroup-' +i, promotionGroupType: 'offer', promoGroupId: i, showQuantityField: g.showQuantityField(promotionType: promotion?.type), showValueField: g.showValueField(promotionType: promotion?.type), promotionGroupDescription: g.promotionGroupHeader(promotionGroup: promotionGroup), canDeleteProduct: canEdit]" />
                        </g:each>
                    </div>

                    <div class="row">
                        <div class="col-12 text-right">
                            <button id="offerAddProductButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionProductSearchModal" onclick="selectedPromotionGroupType = 'offer';">+ Product</button>
                            <button id="offerAddCategoryButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionCategorySearchModal" onclick="selectedPromotionGroupType = 'offer';">+ Category</button>
                            <button id="offerAddTagButton" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionTagSearchModal" onclick="selectedPromotionGroupType = 'offer';">+ Tag</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Stores. -->
        <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
            <div class="card bg-light border-wl accordion-card col-12 col-md-10 offset-md-1 px-0">
                <div class="card-header pointer" id="promotionStores" data-toggle="collapse" data-target="#collapsePromotionStores" aria-expanded="true" aria-controls="collapsePromotionStores">
                    <div class="row">
                        <div class="col-10 font-weight-bold">Stores</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div id="collapsePromotionStores" class="collapse" aria-labelledby="promotionStores" data-parent="#accordion">
                    <div class="card-body py-5">
                        <div id="stores" class="row">
                            <div id="StoresSection" class="col-10 offset-1">
                                <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                                    <div class="col-3 font-weight-bold">Store Number</div>
                                    <div class="col-3 font-weight-bold">Store Name</div>
                                </div>

                                <div id="search-results" class="pre-scrollable store-search-results">
                                    <g:render template="storeList" model="[addedStores: promotion?.storez]" />
                                </div>

                                <div class="row justify-content-end mx-0 my-4">
                                    <button id="add-store-btn" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionStoreSearchModal" onclick="getAllStores();">Add Stores</button>
                                    <button id="add-all-stores-btn" type="button" class="btn btn-wl mr-1" onclick="addAllStores()">Add All Stores</button>
                                    <button id="remove-all-stores-btn" type="button" class="btn btn-wl mr-1" onclick="removeAllStores()">Remove All Stores</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </g:if>
    </div>
</g:form>