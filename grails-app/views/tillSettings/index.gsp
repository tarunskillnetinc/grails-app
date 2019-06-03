<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'tillsettings']" />

    <g:hasErrors bean="${tillSettings}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${tillSettings}" as="list" />
        </div>
    </g:hasErrors>

    <g:if test="${flash.message}">
        <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <g:form name="save-button" action="save">
        <g:hiddenField name="id" value="${tillSettings?.id}" />
        <g:hiddenField name="retailerId" value="${tillSettings?.retailerId}" />
        <g:hiddenField name="storeId" value="${tillSettings?.storeId}" />

        <div class="row">
            <div class="col-12 col-md-5 offset-md-1">
                <div class="header-wl">
                    <h2>Store Settings</h2>
                </div>

                <div class="form-group row">
                    <label for="receiptMessage1" class="col-5 col-md-3 col-form-label">Receipt line 1</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="receiptMessage1" maxlength="45" value="${tillSettings?.receiptMessage1}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="receiptMessage2" class="col-5 col-md-3 col-form-label">Receipt line 2</label>
                    <div class="col-7 col-md-6">
                        <g:textField name="receiptMessage2" maxlength="45" value="${tillSettings?.receiptMessage2}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="vatRegistrationNumber" class="col-5 col-md-3 col-form-label">VAT reg. number</label>
                    <div class="col-7 col-md-4">
                        <g:textField name="vatRegistrationNumber" maxlength="45" value="${tillSettings?.vatRegistrationNumber}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="printReceiptOption" class="col-5 col-md-3 col-form-label">Receipt print option</label>
                    <div class="col-7 col-md-4">
                        <g:select name="printReceiptOption" from="${availablePrintReceiptOptions}" value="${tillSettings?.printReceiptOption}" valueMessagePrefix="PrintReceiptOption" class="form-control select-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-12 col-md-5 offset-md-3">
                        <g:link controller="tillSettings" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                        <g:submitButton class="btn btn-success" name="save" value="Save" />
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-5">
                <div class="header-wl">
                    <h2>Contact Information</h2>
                </div>

                <div class="form-group row">
                    <label for="addressBuildingNumberOrName" class="col-5 col-md-3 col-form-label">Building name / number</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressBuildingNumberOrName" maxlength="45" value="${tillSettings?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressLine1" class="col-5 col-md-3 col-form-label">Address line 1</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressLine1" maxlength="45" value="${tillSettings?.addressLine1}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressLine2" class="col-5 col-md-3 col-form-label">Address line 2</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressLine2" maxlength="45" value="${tillSettings?.addressLine2}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressTown" class="col-5 col-md-3 col-form-label">Town / city</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressTown" maxlength="45" value="${tillSettings?.addressTown}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressCounty" class="col-5 col-md-3 col-form-label">County</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressCounty" maxlength="45" value="${tillSettings?.addressCounty}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressCountry" class="col-5 col-md-3 col-form-label">Country</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressCountry" maxlength="45" value="${tillSettings?.addressCountry}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="addressPostCode" class="col-5 col-md-3 col-form-label">Post code</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="addressPostCode" maxlength="45" value="${tillSettings?.addressPostCode}" class="form-control bottom-border" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="phoneNumber" class="col-5 col-md-3 col-form-label">Phone number</label>
                    <div class="col-7 col-md-7">
                        <g:textField name="phoneNumber" maxlength="45" value="${tillSettings?.phoneNumber}" class="form-control bottom-border" />
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</body>
</html>