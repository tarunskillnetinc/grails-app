<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Button Grids</title>

    <asset:javascript src="buttonGrid.js" />
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <g:hasErrors bean="${button}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${button}" as="list" />
        </div>
    </g:hasErrors>

    <div class="col-12 col-md-10 col-lg-8 col-xl-6 offset-md-1 offset-lg-2 offset-xl-3">
        <ul class="nav nav-pills nav-fill pills-wl sub-pills" role="tablist">
            <g:if test="${button.buttonGrid?.type?.name() != 'TENDER'}">
                <li class="nav-item">
                    <a id="product-tab" data-toggle="tab" href="#product" role="tab" aria-controls="product" aria-selected="${!button.type || button.type.name() == 'PRODUCT'}" class="nav-link ${!button.type || button.type.name() == 'PRODUCT' ? 'active' : ''}">Product</a>
                </li>

                <li class="nav-item">
                    <a id="subpage-tab" data-toggle="tab" href="#subpage" role="tab" aria-controls="subpage" aria-selected="${button.type?.name() == 'SUB_PAGE'}" class="nav-link ${button.type?.name() == 'SUB_PAGE' ? 'active' : ''}">Sub Page</a>
                </li>

                <li class="nav-item">
                    <a id="process-tab" data-toggle="tab" href="#processtab" role="tab" aria-controls="processtab" aria-selected="${button.type?.name() == 'PROCESS'}" class="nav-link ${button.type?.name() == 'PROCESS' ? 'active' : ''}">Action</a>
                </li>
            </g:if>

            <g:if test="${button.buttonGrid?.type?.name() == 'TENDER'}">
                <li class="nav-item">
                    <a id="tender-tab" data-toggle="tab" href="#tender" role="tab" aria-controls="tender" aria-selected="${!button.type || button.type.name() == 'TENDER'}" class="nav-link ${!button.type || button.type.name() == 'TENDER' ? 'active' : ''}">Tender</a>
                </li>
            </g:if>
        </ul>

        <div class="tab-content" style="margin-top: 50px;">
            <g:if test="${button.buttonGrid?.type?.name() != 'TENDER'}">
                <!-- Product -->
                <div class="tab-pane fade show ${!button.type || button.type.name() == 'PRODUCT' ? 'active' : ''}" id="product" role="tabpanel" aria-labelledby="product-tab">
                    <g:form name="save-button" action="save" novalidate="novalidate">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="PRODUCT" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="tenderType" value="" />
                        <g:hiddenField name="process" value="" />
                        <g:hiddenField name="subPageId" value="" />
                        <g:hiddenField name="productId" id="buttonProductId" value="${button?.productId}" />

                        <div class="form-group row">
                            <label for="description" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                            <div class="col-8 col-sm-6">
                                <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="quantity" class="col-4 col-sm-2 offset-sm-2 col-form-label">Quantity</label>
                            <div class="col-4 col-sm-2">
                                <g:field type="number" min="1" max="999" name="quantity" value="${button.quantity}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="product-select-box">
                            <div class="form-group row" style="padding-top: 10px;">
                                <label for="itemCode" class="col-4 col-sm-2 offset-sm-2 col-form-label">Item code</label>
                                <div class="col-4">
                                    <label id="itemCode" class="col-form-label value">${productItemCode}</label>
                                </div>
                                <div class="col-4">
                                    <!-- Button trigger modal -->
                                    <a href="#" class="btn product-select-button" data-toggle="modal" data-target="#productSearchModal">
                                        Select Product
                                    </a>
                                </div>
                            </div>

                            <div class="form-group row" style="margin-top: 0px; padding-bottom: 10px;">
                                <label for="productDescription" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                                <div class="col-8">
                                    <label id="productDescription" class="col-form-label value">${productDescription}</label>
                                </div>
                            </div>
                        </div>

                        <g:render template="saveCancelButtons" model="${[button: button]}" />
                    </g:form>
                </div>

                <!-- Sub Page -->
                <div class="tab-pane fade show ${button.type?.name() == 'SUB_PAGE' ? 'active' : ''}" id="subpage" role="tabpanel" aria-labelledby="subpage-tab">
                    <g:form name="save-button" action="save" novalidate="novalidate">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="SUB_PAGE" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="tenderType" value="" />
                        <g:hiddenField name="quantity" value="" />
                        <g:hiddenField name="process" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                            <div class="col-8 col-sm-6">
                                <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="subPageId" class="col-4 col-sm-2 offset-sm-2  col-form-label">Sub page</label>
                            <div class="col-8 col-sm-5">
                                <g:select name="subPageId" from="${availableSubPages}" value="${button?.subPageId}" optionKey="id" optionValue="description" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <g:render template="saveCancelButtons" model="${[button: button]}" />
                    </g:form>
                </div>

                <!-- Process -->
                <div class="tab-pane fade show ${button.type?.name() == 'PROCESS' ? 'active' : ''}" id="processtab" role="tabpanel" aria-labelledby="process-tab">
                    <g:form name="save-button" action="save" novalidate="novalidate">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="PROCESS" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="quantity" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="subPageId" value="" />
                        <g:hiddenField name="tenderType" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                            <div class="col-8 col-sm-6">
                                <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="process" class="col-4 col-sm-2 offset-sm-2 col-form-label">Action</label>
                            <div class="col-8 col-sm-5">
                                <g:select name="process" from="${availableProcesses}" valueMessagePrefix="ProcessType" value="${button.process}" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <g:render template="saveCancelButtons" model="${[button: button]}" />
                    </g:form>
                </div>
            </g:if>

            <!-- Tender -->
            <g:if test="${button.buttonGrid?.type?.name() == 'TENDER'}">
                <div class="tab-pane fade show ${!button.type || button.type.name() == 'TENDER' ? 'active' : ''}" id="tender" role="tabpanel" aria-labelledby="tender-tab">
                    <g:form name="save-button" action="save" novalidate="novalidate">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="TENDER" />
                        <g:hiddenField name="quantity" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="subPageId" value="" />
                        <g:hiddenField name="process" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                            <div class="col-8 col-sm-6">
                                <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="amount" class="col-4 col-sm-2 offset-sm-2 col-form-label">Amount</label>
                            <div class="col-4 col-sm-2">
                                <g:field type="number" min="0" max="9999" step=".01" name="amount" value="${button.amount}" class="form-control bottom-border" />
                            </div>
                            <div class="col-4 col-sm-4" style="margin-top: 7px;"><small class="text-muted">Leave blank for manual entry.</small></div>
                        </div>

                        <div class="form-group row">
                            <label for="tenderType" class="col-4 col-sm-2 offset-sm-2 col-form-label">Tender type:</label>
                            <div class="col-6 col-sm-4">
                                <g:select name="tenderType" from="${availableTenderTypes}" valueMessagePrefix="TenderType" value="${button.tenderType}" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <g:render template="saveCancelButtons" model="${[button: button]}" />
                    </g:form>
                </div>
            </g:if>
        </div>
    </div>

    <!-- Product search modal -->
    <g:render template="/product/productSearch" />
</body>
</html>