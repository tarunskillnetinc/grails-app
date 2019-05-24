<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonGridType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ProcessType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.TenderType" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy Button Grids</title>

    <asset:javascript src="buttonGrid.js" />
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <div class="col-12 col-lg-6 offset-lg-3">
        <ul class="nav nav-pills nav-fill pills-wl sub-pills" role="tablist">
            <g:if test="${button.buttonGrid?.type != ButtonGridType.TENDER}">
                <li class="nav-item">
                    <a id="product-tab" data-toggle="tab" href="#product" role="tab" aria-controls="product" aria-selected="${button.type == ButtonType.PRODUCT}" class="nav-link ${button.type == ButtonType.PRODUCT ? 'active' : ''}">Product</a>
                </li>

                <li class="nav-item">
                    <a id="subpage-tab" data-toggle="tab" href="#subpage" role="tab" aria-controls="subpage" aria-selected="${button.type == ButtonType.PROCESS && button.quantity}" class="nav-link ${button.type == ButtonType.PROCESS && button.quantity ? 'active' : ''}">Sub Page</a>
                </li>

                <li class="nav-item">
                    <a id="process-tab" data-toggle="tab" href="#processtab" role="tab" aria-controls="processtab" aria-selected="${button.type == ButtonType.PROCESS}" class="nav-link ${button.type == ButtonType.PROCESS ? 'active' : ''}">Action</a>
                </li>
            </g:if>

            <g:if test="${button.buttonGrid?.type == ButtonGridType.TENDER}">
                <li class="nav-item">
                    <a id="tender-tab" data-toggle="tab" href="#tender" role="tab" aria-controls="tender" aria-selected="${button.type == ButtonType.TENDER}" class="nav-link ${button.type == ButtonType.TENDER ? 'active' : ''}">Tender</a>
                </li>
            </g:if>
        </ul>

        <div class="tab-content" style="margin-top: 50px;">
            <g:if test="${button.buttonGrid?.type != ButtonGridType.TENDER}">
                <!-- Product -->
                <div class="tab-pane fade show ${button.type == ButtonType.PRODUCT ? 'active' : ''}" id="product" role="tabpanel" aria-labelledby="product-tab">
                    <g:form name="save-button" action="save">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="${ButtonType.PRODUCT}" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="tenderType" value="" />
                        <g:hiddenField name="process" value="" />
                        <g:hiddenField name="productId" id="buttonProductId" value="${button?.productId}" />

                        <div class="form-group row">
                            <label for="description" class="col-2 col-form-label">Description</label>
                            <div class="col-8">
                                <g:textField name="description" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="quantity" class="col-2 col-form-label">Quantity</label>
                            <div class="col-2">
                                <g:field type="number" min="0" max="99999" name="quantity" value="${button.quantity}" class="form-control bottom-border" />
                            </div>
                        </div>

    <!--                    <g:hiddenField name="productId" value="" />-->
                        <div class="product-select-box">
                            <div class="form-group row" style="padding-top: 10px;">
                                <label for="itemCode" class="col-2 col-form-label">Item code</label>
                                <div class="col-4">
                                    <input type="text" readonly class="form-control-plaintext" id="itemCode" value="Test">
                                </div>
                                <div class="col-4">
    <!--                                <a href="#" class="btn btn-secondary product-select-button">Select Product</a>-->
                                    <!-- Button trigger modal -->
                                    <button type="button" class="btn btn-secondary product-select-button" data-toggle="modal" data-target="#productSearchModal">
                                        Select Product
                                    </button>
                                </div>
                            </div>

                            <div class="form-group row" style="margin-top: 0px; padding-bottom: 10px;">
                                <label for="productDescription" class="col-2 col-form-label">Description</label>
                                <div class="col-8">
                                    <input type="text" readonly class="form-control-plaintext" id="productDescription" value="Hydromol capsules ">
                                </div>
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-8 offset-2">
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-secondary">Unassign</g:link>
                                <g:submitButton class="btn btn-success" name="save" value="Save" />
                            </div>
                        </div>
                    </g:form>
                </div>

                <!-- Sub Page -->
                <div class="tab-pane fade show ${button.type == ButtonType.PROCESS && button.quantity ? 'active' : ''}" id="subpage" role="tabpanel" aria-labelledby="subpage-tab">
                    <g:form name="save-button" action="save">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="${ButtonType.PROCESS}" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="tenderType" value="" />
                        <g:hiddenField name="process" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-2 col-form-label">Description</label>
                            <div class="col-8">
                                <g:textField name="description" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="quantity" class="col-2 col-form-label">Sub page</label>
                            <div class="col-5">
                                <g:select name="quantity" from="${availableSubPages}" value="${button?.quantity}" optionKey="id" optionValue="description" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-8 offset-2">
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-secondary">Unassign</g:link>
                                <g:submitButton class="btn btn-success" name="save" value="Save" />
                            </div>
                        </div>
                    </g:form>
                </div>

                <!-- Process -->
                <div class="tab-pane fade show ${button.type == ButtonType.PROCESS && !button.quantity ? 'active' : ''}" id="processtab" role="tabpanel" aria-labelledby="process-tab">
                    <g:form name="save-button" action="save">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="${ButtonType.PROCESS}" />
                        <g:hiddenField name="amount" value="" />
                        <g:hiddenField name="quantity" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="tenderType" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-2 col-form-label">Description</label>
                            <div class="col-8">
                                <g:textField name="description" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="process" class="col-2 col-form-label">Action</label>
                            <div class="col-5">
                                <g:select name="process" from="${availableProcesses}" valueMessagePrefix="ProcessType" value="${button.process}" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-8 offset-2">
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-secondary">Unassign</g:link>
                                <g:submitButton class="btn btn-success" name="save" value="Save" />
                            </div>
                        </div>
                    </g:form>
                </div>
            </g:if>

            <!-- Tender -->
            <g:if test="${button.buttonGrid?.type == ButtonGridType.TENDER}">
                    <div class="tab-pane fade show ${button.type == ButtonType.TENDER ? 'active' : ''}" id="tender" role="tabpanel" aria-labelledby="tender-tab">
                    <g:form name="save-button" action="save">
                        <g:hiddenField name="id" value="${button?.id}" />
                        <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                        <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                        <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                        <g:hiddenField name="row" value="${button?.row}" />
                        <g:hiddenField name="column" value="${button?.column}" />
                        <g:hiddenField name="type" value="${ButtonType.TENDER}" />
                        <g:hiddenField name="quantity" value="" />
                        <g:hiddenField name="productId" value="" />
                        <g:hiddenField name="process" value="" />

                        <div class="form-group row">
                            <label for="description" class="col-2 col-form-label">Description</label>
                            <div class="col-8">
                                <g:textField name="description" value="${button?.description}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="amount" class="col-2 col-form-label">Amount</label>
                            <div class="col-2">
                                <g:field type="number" min="0" max="99999" step=".01" name="amount" value="${button.amount}" class="form-control bottom-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="tenderType" class="col-2 col-form-label">Tender type: ${button.tenderType}</label>
                            <div class="col-4">
                                <g:select name="tenderType" from="${TenderType}" valueMessagePrefix="TenderType" value="${button.tenderType}" noSelection="['':'']" class="form-control select-border" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-8 offset-2">
                                <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>
                                <g:link action="unassign" id="${button?.id}" tabindex="-1" role="button" class="btn btn-secondary">Unassign</g:link>
                                <g:submitButton class="btn btn-success" name="save" value="Save" />
                            </div>
                        </div>
                    </g:form>
                </div>
            </g:if>
        </div>
    </div>

    <!-- Product search modal -->
    <g:render template="/product/productSearch" />
</body>
</html>