<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy</title>
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

    <g:form name="save-button" action="save">
        <g:hiddenField name="id" value="${storeSettings?.id}" />
        <g:hiddenField name="retailerId" value="${storeSettings?.retailerId}" />
        <g:hiddenField name="storeId" value="${storeSettings?.storeId}" />

        <div class="row">
            <div class="col-12 col-md-5 offset-md-1">
                <div style="margin-top: 30px;">
                    <h2>Store Settings</h2>
                </div>

                <div class="form-group row">
                    <label for="receiptMessage1" class="col-5 col-md-3 col-form-label">Receipt line 1</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="receiptMessage1" maxlength="100" value="${storeSettings?.receiptMessage1}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="receiptMessage2" class="col-5 col-md-3 col-form-label">Receipt line 2</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="receiptMessage2" maxlength="100" value="${storeSettings?.receiptMessage2}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="vatRegistrationNumber" class="col-5 col-md-3 col-form-label">VAT reg. number</label>
                    <div class="col-7 col-md-4">
                        <g:textField name="vatRegistrationNumber" maxlength="45" value="${storeSettings?.vatRegistrationNumber}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="printReceiptOption" class="col-5 col-md-3 col-form-label">Receipt print option</label>
                    <div class="col-7 col-md-4">
                        <g:select name="printReceiptOption" from="${availablePrintReceiptOptions}" value="${storeSettings?.printReceiptOption}" valueMessagePrefix="PrintReceiptOption" class="form-control select-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="quantityPromptThreshold" class="col-5 col-md-3 col-form-label">Quantity Prompt Threshold</label>
                    <div class="col-7 col-md-4">
                        <g:field type="number" min="0" max="9999" maxlength="3" name="quantityPromptThreshold" value="${storeSettings?.quantityPromptThreshold}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="valuePromptThreshold" class="col-5 col-md-3 col-form-label">Value Prompt Threshold</label>
                    <div class="col-7 col-md-4">
                        <g:field type="number" min="0" max="99999" maxlength="4" step=".01" name="valuePromptThreshold" value="${storeSettings?.valuePromptThreshold}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-12 col-md-5 offset-md-3">
                        <g:link controller="storeSettings" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                        <g:submitButton class="btn btn-success" name="save" value="Save" />
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-6">
                <div style="margin-top: 30px;">
                    <h2>Contact Information</h2>
                </div>

                <div class="form-group row">
                    <label for="storeName" class="col-5 col-md-3 col-form-label">Store name</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="storeName" maxlength="45" value="${storeSettings?.storeName}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressBuildingNumberOrName" class="col-5 col-md-3 col-form-label">Building name / number</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressBuildingNumberOrName" maxlength="45" value="${storeSettings?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressLine1" class="col-5 col-md-3 col-form-label">Address line 1</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressLine1" maxlength="45" value="${storeSettings?.addressLine1}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressLine2" class="col-5 col-md-3 col-form-label">Address line 2</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressLine2" maxlength="45" value="${storeSettings?.addressLine2}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressTown" class="col-5 col-md-3 col-form-label">Town / city</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressTown" maxlength="45" value="${storeSettings?.addressTown}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressCounty" class="col-5 col-md-3 col-form-label">County</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressCounty" maxlength="45" value="${storeSettings?.addressCounty}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressCountry" class="col-5 col-md-3 col-form-label">Country</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressCountry" maxlength="45" value="${storeSettings?.addressCountry}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressPostCode" class="col-5 col-md-3 col-form-label">Post code</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="addressPostCode" maxlength="45" value="${storeSettings?.addressPostCode}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="phoneNumber" class="col-5 col-md-3 col-form-label">Phone number</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="phoneNumber" maxlength="45" value="${storeSettings?.phoneNumber}" class="form-control bottom-border" />
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</body>
</html>