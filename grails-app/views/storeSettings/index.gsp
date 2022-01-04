<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Store Settings</title>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li class="breadcrumb-item active" aria-current="page">Store Settings</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <div class="header-wl mt-3">
        <h2 class="mx-auto">Settings</h2>
    </div>

    <g:hasErrors bean="${storeSettings}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${storeSettings}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:if test="${flash.message}">
        <section id="errors-container2" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

    <section id="addProduct-section" class="container-fluid mt-4">
        <g:form name="save-button" action="save" novalidate="novalidate">
            <g:hiddenField name="id" value="${storeSettings?.id}" />
            <g:hiddenField name="retailerId" value="${storeSettings?.retailerId}" />
            <g:hiddenField name="storeId" value="${storeSettings?.storeId}" />

            <div id="accordion">
                <!-- Contact information. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="productDetails" data-toggle="collapse" data-target="#collapseProductDetails" aria-expanded="true" aria-controls="collapseProductDetails">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Contact Information</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseProductDetails" class="collapse show" aria-labelledby="productDetails" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="storeName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Name</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="storeName" maxlength="45" value="${storeSettings?.storeName}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressBuildingNumberOrName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Building Name / Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressBuildingNumberOrName" maxlength="45" value="${storeSettings?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressLine1" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 1</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressLine1" maxlength="45" value="${storeSettings?.addressLine1}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressLine2" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 2</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressLine2" maxlength="45" value="${storeSettings?.addressLine2}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressTown" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Town / City</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressTown" maxlength="45" value="${storeSettings?.addressTown}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressCounty" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">County</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressCounty" maxlength="45" value="${storeSettings?.addressCounty}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressCountry" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Country</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressCountry" maxlength="45" value="${storeSettings?.addressCountry}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressPostCode" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Post Code</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressPostCode" maxlength="45" value="${storeSettings?.addressPostCode}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="phoneNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Phone Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="phoneNumber" maxlength="45" value="${storeSettings?.phoneNumber}" class="form-control bottom-border" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- EPOS Settings. -->
                <div class="card bg-light border-wl accordion-card col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="eposSettings" data-toggle="collapse" data-target="#collapseEposSettings" aria-expanded="true" aria-controls="collapseEposSettings">
                        <div class="row">
                            <div class="col-10 font-weight-bold">EPOS Settings</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseEposSettings" class="collapse" aria-labelledby="eposSettings" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="receiptMessage1" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Receipt Line 1</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="receiptMessage1" maxlength="100" value="${storeSettings?.receiptMessage1}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="receiptMessage2" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Receipt Line 2</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="receiptMessage2" maxlength="100" value="${storeSettings?.receiptMessage2}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="vatRegistrationNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">VAT Reg. Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="vatRegistrationNumber" maxlength="45" value="${storeSettings?.vatRegistrationNumber}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="printReceiptOption" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Receipt Print Option</label>
                                    <div class="col-7 col-lg-2">
                                        <g:select name="printReceiptOption" from="${availablePrintReceiptOptions}" value="${storeSettings?.printReceiptOption}" valueMessagePrefix="PrintReceiptOption" class="form-control select-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="quantityPromptThreshold" class="col-4 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Quantity Prompt Threshold</label>
                                    <div class="col-8 col-lg-2">
                                        <g:field type="number" min="0" max="9999" maxlength="3" name="quantityPromptThreshold" value="${storeSettings?.quantityPromptThreshold}" class="form-control bottom-border" />
                                    </div>
                                    <small id="quantityPromptHelp" class="form-text text-muted">Selling this quantity of any item will trigger a confirmation prompt.</small>
                                </div>

                                <div class="form-group row">
                                    <label for="valuePromptThreshold" class="col-4 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Price Change Value Prompt Threshold</label>
                                    <div class="col-8 col-lg-2">
                                        <g:field type="number" min="0" max="99999" maxlength="4" step=".01" name="valuePromptThreshold" value="${storeSettings?.valuePromptThreshold}" class="form-control bottom-border" />
                                    </div>
                                    <small id="valuePromptHelp" class="form-text text-muted">Changing the price of any item by this value will trigger a confirmation prompt.</small>
                                </div>

                                <div class="form-group row">
                                    <label for="priceBand.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Price Band</label>
                                    <div class="col-7 col-lg-2">
                                        <g:select name="priceBand.id" from="${availablePriceBands}" value="${storeSettings?.priceBand?.id}" optionValue="description" optionKey="id" class="form-control select-border" disabled="${sec.ifAnyGranted([roles:'ROLE_ENGINEER,ROLE_HEAD_OFFICE'])}" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="range.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Product Range</label>
                                    <div class="col-7 col-lg-2">
                                        <g:select name="range.id" from="${availableProductRanges}" value="${storeSettings?.range?.id}" optionValue="description" optionKey="id" class="form-control select-border" disabled="${sec.ifAnyGranted([roles:'ROLE_ENGINEER,ROLE_HEAD_OFFICE'])}" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Inventory Management. -->
                <div class="card bg-light border-wl accordion-card col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="inventoryManagement" data-toggle="collapse" data-target="#collapseInventoryManagement" aria-expanded="true" aria-controls="collapseInventoryManagement">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Inventory Management</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseInventoryManagement" class="collapse" aria-labelledby="inventoryManagement" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="varianceQuantity" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Variance Quantity Threshold</label>
                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:field type="number" min="0" max="9999" maxlength="3" name="varianceQuantity" value="${storeSettings?.varianceQuantity}" class="form-control bottom-border" />
                                    </div>
                                    <small id="varianceQuantityHelp" class="form-text text-muted">Adjustments of this quantity will trigger a variance report.</small>
                                </div>

                                <div class="form-group row">
                                    <label for="varianceValue" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Variance Value Threshold</label>
                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:field type="number" min="0" max="99999" maxlength="4" step=".01" name="varianceValue" value="${storeSettings?.varianceValue}" class="form-control bottom-border" />
                                    </div>
                                    <small id="varianceValueHelp" class="form-text text-muted">Adjustments of this value will trigger a variance report.</small>
                                </div>

                                <div class="form-group form-check row">
                                    <div class="col-12 col-lg-8 offset-lg-5">
                                        <g:checkBox name="pickListForceZeroCount" value="${storeSettings?.pickListForceZeroCount}" class="form-check-input" />
                                        <label class="form-check-label" for="pickListForceZeroCount">Force users to count items in a pick list which have zero quantity in stock.</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="my-5">
                <g:link controller="storeSettings" action="index" tabindex="-1" role="button" class="btn btn-danger col-1 offset-1">Cancel</g:link>
                <g:submitButton class="btn btn-success col-1 offset-8" name="save" value="Save" />
            </div>
        </g:form>
    </section>
</body>
</html>