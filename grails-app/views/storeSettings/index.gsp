<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Store Settings</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'storesettings']" />

    <g:hasErrors bean="${storeSettings}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${storeSettings}" as="list" />
        </div>
    </g:hasErrors>

    <g:if test="${flash.message}">
        <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <g:form name="save-button" action="save" novalidate="novalidate">
        <g:hiddenField name="id" value="${storeSettings?.id}" />
        <g:hiddenField name="retailerId" value="${storeSettings?.retailerId}" />
        <g:hiddenField name="storeId" value="${storeSettings?.storeId}" />

        <div class="col-12 col-sm-6 offset-sm-3">
            <ul class="nav nav-pills nav-fill pills-wl sub-pills" role="tablist">
                <li class="nav-item">
                    <a id="storeSettings-tab" data-toggle="tab" href="#storeSettings" aria-selected="true" role="tab" aria-controls="storeSettings" class="nav-link active">Store Settings</a>
                </li>

                <li class="nav-item">
                    <a id="inventoryManagement-tab" data-toggle="tab" href="#inventoryManagement" role="tab" aria-controls="inventoryManagement" class="nav-link">Inventory Management</a>
                </li>

                <li class="nav-item">
                    <a id="contactInformation-tab" data-toggle="tab" href="#contactInformation" role="tab" aria-controls="contactInformation" class="nav-link">Contact Information</a>
                </li>
            </ul>
        </div>

        <div class="tab-content" style="margin-top: 50px;">
            <!-- Store Settings -->
            <div class="tab-pane fade show active" id="storeSettings" role="tabpanel" aria-labelledby="storeSettings-tab">
                <div class="col-12 col-md-6 offset-md-3">
                    <div class="form-group row margin-top-2rem">
                        <label for="receiptMessage1" class="col-5 col-lg-3 col-form-label">Receipt line 1</label>
                        <div class="col-7 col-lg-8 col-xl-6">
                            <g:textField name="receiptMessage1" maxlength="100" value="${storeSettings?.receiptMessage1}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="receiptMessage2" class="col-5 col-lg-3 col-form-label">Receipt line 2</label>
                        <div class="col-7 col-lg-8 col-xl-6">
                            <g:textField name="receiptMessage2" maxlength="100" value="${storeSettings?.receiptMessage2}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="vatRegistrationNumber" class="col-5 col-lg-3 col-form-label">VAT reg. number</label>
                        <div class="col-7 col-lg-6 col-xl-4">
                            <g:textField name="vatRegistrationNumber" maxlength="45" value="${storeSettings?.vatRegistrationNumber}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="printReceiptOption" class="col-5 col-lg-3 col-form-label">Receipt print option</label>
                        <div class="col-7 col-lg-6 col-xl-4">
                            <g:select name="printReceiptOption" from="${availablePrintReceiptOptions}" value="${storeSettings?.printReceiptOption}" valueMessagePrefix="PrintReceiptOption" class="form-control select-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="quantityPromptThreshold" class="col-5 col-lg-3 col-form-label">Quantity Prompt Threshold</label>
                        <div class="col-7 col-lg-4 col-xl-3">
                            <g:field type="number" min="0" max="9999" maxlength="3" name="quantityPromptThreshold" value="${storeSettings?.quantityPromptThreshold}" class="form-control bottom-border" />
                        </div>
                        <small id="quantityPromptHelp" class="form-text text-muted">Selling this quantity of any item will trigger a confirmation prompt.</small>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="valuePromptThreshold" class="col-5 col-lg-3 col-form-label">Value Prompt Threshold</label>
                        <div class="col-7 col-lg-4 col-xl-3">
                            <g:field type="number" min="0" max="99999" maxlength="4" step=".01" name="valuePromptThreshold" value="${storeSettings?.valuePromptThreshold}" class="form-control bottom-border" />
                        </div>
                        <small id="valuePromptHelp" class="form-text text-muted">Selling this value of any item will trigger a confirmation prompt.</small>
                    </div>
                </div>
            </div>

            <!-- Inventory Management -->
            <div class="tab-pane fade show" id="inventoryManagement" role="tabpanel" aria-labelledby="inventoryManagement-tab">
                <div class="col-12 col-md-6 offset-md-3">
                    <div class="form-group row margin-top-2rem">
                        <label for="varianceQuantity" class="col-5 col-lg-3 col-form-label">Variance Quantity Threshold</label>
                        <div class="col-7 col-lg-4 col-xl-3">
                            <g:field type="number" min="0" max="9999" maxlength="3" name="varianceQuantity" value="${storeSettings?.varianceQuantity}" class="form-control bottom-border" />
                        </div>
                        <small id="varianceQuantityHelp" class="form-text text-muted">Adjustments of this quantity will trigger a variance report.</small>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="varianceValue" class="col-5 col-lg-3 col-form-label">Variance Value Threshold</label>
                        <div class="col-7 col-lg-4 col-xl-3">
                            <g:field type="number" min="0" max="99999" maxlength="4" step=".01" name="varianceValue" value="${storeSettings?.varianceValue}" class="form-control bottom-border" />
                        </div>
                        <small id="varianceValueHelp" class="form-text text-muted">Adjustments of this value will trigger a variance report.</small>
                    </div>

                    <div class="form-group form-check row margin-top-2rem">
                        <div class="col-12 col-lg-9 offset-lg-3">
                            <g:checkBox name="pickListForceZeroCount" value="${storeSettings?.pickListForceZeroCount}" class="form-check-input" />
                            <label class="form-check-label" for="pickListForceZeroCount">Force users to count items in a pick list which have zero balance on hand.</label>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Contact Information -->
            <div class="tab-pane fade show" id="contactInformation" role="tabpanel" aria-labelledby="contactInformation-tab">
                <div class="col-12 col-md-6 offset-md-3">
                    <div class="form-group row margin-top-2rem">
                        <label for="storeName" class="col-5 col-lg-3 col-form-label">Store name</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="storeName" maxlength="45" value="${storeSettings?.storeName}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressBuildingNumberOrName" class="col-5 col-lg-3 col-form-label">Building name / number</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressBuildingNumberOrName" maxlength="45" value="${storeSettings?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressLine1" class="col-5 col-lg-3 col-form-label">Address line 1</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressLine1" maxlength="45" value="${storeSettings?.addressLine1}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressLine2" class="col-5 col-lg-3 col-form-label">Address line 2</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressLine2" maxlength="45" value="${storeSettings?.addressLine2}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressTown" class="col-5 col-lg-3 col-form-label">Town / city</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressTown" maxlength="45" value="${storeSettings?.addressTown}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressCounty" class="col-5 col-lg-3 col-form-label">County</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressCounty" maxlength="45" value="${storeSettings?.addressCounty}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressCountry" class="col-5 col-lg-3 col-form-label">Country</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressCountry" maxlength="45" value="${storeSettings?.addressCountry}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="addressPostCode" class="col-5 col-lg-3 col-form-label">Post code</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="addressPostCode" maxlength="45" value="${storeSettings?.addressPostCode}" class="form-control bottom-border" />
                        </div>
                    </div>

                    <div class="form-group row margin-top-2rem">
                        <label for="phoneNumber" class="col-5 col-lg-3 col-form-label">Phone number</label>
                        <div class="col-7 col-lg-6">
                            <g:textField name="phoneNumber" maxlength="45" value="${storeSettings?.phoneNumber}" class="form-control bottom-border" />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-group row margin-top-2rem">
            <div class="col-12 col-md-6 offset-md-3">
                <div class="col-6 col-md-5 offset-md-3">
                    <g:link controller="storeSettings" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                    <g:submitButton class="btn btn-success" name="save" value="Save" />
                </div>
            </div>
        </div>
    </g:form>
</body>
</html>