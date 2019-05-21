<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonGridType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ProcessType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.TenderType" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy Button Grids</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <div class="col-12 col-lg-6 offset-lg-3">
        <ul class="nav nav-pills nav-fill pills-wl sub-pills" role="tablist">
            <li class="nav-item">
                <a id="product-tab" data-toggle="tab" href="#product" role="tab" aria-controls="product" aria-selected="${button.type == ButtonType.PRODUCT}" class="nav-link ${button.type == ButtonType.PRODUCT ? 'active' : ''}">Product</a>
            </li>

            <li class="nav-item">
                <a id="subpage-tab" data-toggle="tab" href="#subpage" role="tab" aria-controls="subpage" aria-selected="${button.type == ButtonType.PROCESS && button.quantity}" class="nav-link ${button.type == ButtonType.PROCESS && button.quantity ? 'active' : ''}">Sub Page</a>
            </li>

            <li class="nav-item">
                <a id="process-tab" data-toggle="tab" href="#processtab" role="tab" aria-controls="processtab" aria-selected="${button.type == ButtonType.PROCESS}" class="nav-link ${button.type == ButtonType.PROCESS ? 'active' : ''}">Action</a>
            </li>

            <li class="nav-item">
                <a id="tender-tab" data-toggle="tab" href="#tender" role="tab" aria-controls="tender" aria-selected="${button.type == ButtonType.TENDER}" class="nav-link ${button.type == ButtonType.TENDER ? 'active' : ''}">Tender</a>
            </li>
        </ul>

        <div class="tab-content">
            <!-- Product -->
            <div class="tab-pane fade show ${button.type == ButtonType.PRODUCT ? 'active' : ''}" id="product" role="tabpanel" aria-labelledby="product-tab">
                bla product
            </div>

            <!-- Sub Page -->
            <div class="tab-pane fade show ${button.type == ButtonType.PROCESS && button.quantity ? 'active' : ''}" id="subpage" role="tabpanel" aria-labelledby="subpage-tab">
                <g:form name="save-button" action="save" style="margin-top: 50px;">
                    <g:hiddenField name="id" value="${button?.id}" />
                    <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                    <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                    <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                    <g:hiddenField name="row" value="${button?.row}" />
                    <g:hiddenField name="column" value="${button?.column}" />
                    <g:hiddenField name="type" value="${button?.type}" />
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
                        <label for="description" class="col-2 col-form-label">Sub page</label>
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
                <g:form name="save-button" action="save" style="margin-top: 50px;">
                    <g:hiddenField name="id" value="${button?.id}" />
                    <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                    <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                    <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                    <g:hiddenField name="row" value="${button?.row}" />
                    <g:hiddenField name="column" value="${button?.column}" />
                    <g:hiddenField name="type" value="${button?.type}" />
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
                        <label for="description" class="col-2 col-form-label">Action</label>
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

            <!-- Tender -->
            <div class="tab-pane fade show ${button.type == ButtonType.TENDER ? 'active' : ''}" id="tender" role="tabpanel" aria-labelledby="tender-tab">
                <g:form name="save-button" action="save" style="margin-top: 50px;">
                    <g:hiddenField name="id" value="${button?.id}" />
                    <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                    <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                    <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                    <g:hiddenField name="row" value="${button?.row}" />
                    <g:hiddenField name="column" value="${button?.column}" />
                    <g:hiddenField name="type" value="${button?.type}" />
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
                            <g:field type="number" min="0" max="99999" name="amount" value="${button.amount}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row">
                        <label for="description" class="col-2 col-form-label">Tender type</label>
                        <div class="col-4">
                            <g:select name="tenderType" from="${TenderType}" valueMessagePrefix="TenderType" value="${button.tenderType}" noSelection="['':'']" class="form-control select-border" />
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
        </div>
    </div>

</body>
</html>