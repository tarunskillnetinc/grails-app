<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Button Grids</title>

    <asset:javascript src="buttonGrid.js" />
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li class="breadcrumb-item active" aria-current="page">Button Grids</li>
                        <li class="breadcrumb-item" aria-current="page">
                            <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}">
                                <g:if test="${button?.buttonGrid?.type?.name() == 'OTHER'}">
                                    ${button?.buttonGrid?.description}
                                </g:if>
                                <g:else>
                                    <g:message code="ButtonGridType.${button?.buttonGrid?.type?.name()}" />
                                </g:else>
                            </g:link>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">${button?.description ? button.description : "New Button"}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <div class="header-wl mt-3">
        <h2 class="mx-auto">Edit Button</h2>
    </div>

    <g:hasErrors bean="${button}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${button}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <section id="addProduct-section" class="container-fluid mt-4">
        <div id="accordion">
            <g:if test="${button.buttonGrid?.type?.name() != 'TENDER'}">
                <!-- Product button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="productButton" data-toggle="collapse" data-target="#collapseProductButton" aria-expanded="true" aria-controls="collapseProductButton">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Product Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseProductButton" class="collapse ${!button?.type || button?.type?.name() == 'PRODUCT' ? 'show' : ''}" aria-labelledby="productButton" data-parent="#accordion">
                        <div class="card-body py-5">
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
                                <g:hiddenField name="sku" id="buttonSku" value="${button?.sku}" />

                                <div class="form-group row">
                                    <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                    <div class="col-8 col-sm-4">
                                        <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="quantity" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Quantity</label>
                                    <div class="col-4 col-sm-2">
                                        <g:field type="number" min="1" max="999" name="quantity" value="${button.quantity}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="product-select-box">
                                    <div class="form-group row" style="padding-top: 15px;">
                                        <label for="sku" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">SKU</label>
                                        <div class="col-4 my-auto">
                                            <span id="sku">${productSku}</span>
                                        </div>
                                        <div class="col-4">
                                            <!-- Button trigger modal -->
                                            <a href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                                                Select Product
                                            </a>
                                        </div>
                                    </div>

                                    <div class="form-group row" style="margin-top: 0px; padding-bottom: 10px;">
                                        <label for="productDescription" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                        <div class="col-8 my-auto">
                                            <span id="productDescription">${productDescription}</span>
                                        </div>
                                    </div>
                                </div>

                                <g:render template="saveCancelButtons" model="${[button: button]}" />
                            </g:form>
                        </div>
                    </div>
                </div>

                <!-- Navigation button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="navigationButton" data-toggle="collapse" data-target="#collapseNavigationButton" aria-expanded="true" aria-controls="collapseNavigationButton">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Navigation Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseNavigationButton" class="collapse ${button?.type?.name() == 'SUB_PAGE' ? 'show' : ''}" aria-labelledby="navigationButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <g:form name="save-button" action="save" novalidate="novalidate">
                                <g:hiddenField name="id" value="${button?.id}" />
                                <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                                <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                                <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                                <g:hiddenField name="row" value="${button?.row}" />
                                <g:hiddenField name="column" value="${button?.column}" />
                                <g:hiddenField name="type" value="SUB_PAGE" />
                                <g:hiddenField name="amount" value="" />
                                <g:hiddenField name="sku" value="" />
                                <g:hiddenField name="tenderType" value="" />
                                <g:hiddenField name="quantity" value="" />
                                <g:hiddenField name="process" value="" />

                                <div class="form-group row">
                                    <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                    <div class="col-8 col-sm-6 col-lg-4">
                                        <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="subPageId" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Sub page</label>
                                    <div class="col-8 col-sm-5 col-lg-3">
                                        <g:select name="subPageId" from="${availableSubPages}" value="${button?.subPageId}" optionKey="id" optionValue="description" noSelection="['':'']" class="form-control select-border" />
                                    </div>
                                </div>

                                <g:render template="saveCancelButtons" model="${[button: button]}" />
                            </g:form>
                        </div>
                    </div>
                </div>

                <!-- Action/process button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="actionButton" data-toggle="collapse" data-target="#collapseActionButton" aria-expanded="true" aria-controls="collapseActionButton">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Action Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseActionButton" class="collapse ${button?.type?.name() == 'PROCESS' ? 'show' : ''}" aria-labelledby="actionButton" data-parent="#accordion">
                        <div class="card-body py-5">
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
                                <g:hiddenField name="sku" value="" />
                                <g:hiddenField name="subPageId" value="" />
                                <g:hiddenField name="tenderType" value="" />

                                <div class="form-group row">
                                    <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                    <div class="col-8 col-sm-6 col-lg-4">
                                        <g:textField name="description" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="process" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Action</label>
                                    <div class="col-8 col-sm-5 col-lg-3">
                                        <g:select name="process" from="${availableProcesses}" valueMessagePrefix="ProcessType" value="${button.process}" noSelection="['':'']" class="form-control select-border" />
                                    </div>
                                </div>

                                <g:render template="saveCancelButtons" model="${[button: button]}" />
                            </g:form>
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${button.buttonGrid?.type?.name() == 'TENDER'}">
                <!-- Tender button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="tenderButton" data-toggle="collapse" data-target="#collapseTenderButton" aria-expanded="true" aria-controls="collapseTenderButton">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Tender Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseTenderButton" class="collapse show" aria-labelledby="tenderButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <g:form name="save-button" action="save" novalidate="novalidate">
                                <g:hiddenField name="id" value="${button?.id}" />
                                <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                                <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                                <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                                <g:hiddenField name="row" value="${button?.row}" />
                                <g:hiddenField name="column" value="${button?.column}" />
                                <g:hiddenField name="type" value="TENDER" />
                                <g:hiddenField name="quantity" value="" />
                                <g:hiddenField name="sku" value="" />
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
                    </div>
                </div>
            </g:if>
        </div>
    </section>

    <!-- Product search modal -->
    <g:render template="/product/productSearch" />
</body>
</html>