<div class="modal-header">
    <g:if test="${enableEdit}">
        <h2>Edit Signifier</h2>
    </g:if>
    <g:else>
        <h2>Add Signifier</h2>
    </g:else>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new Signifier. Type, Pattern, and Length are required.</div>

    <g:form name="addSignifierForm">

        <div class="row form-group mb-4">
            <label for="descriptionValue" class="col-3 offset-1 col-form-label text-right">Description</label>

            <div class="input-group col-4">
                <g:textField name="descriptionValue" value="${signifier?.description}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="typeValue" class="col-3 offset-1 col-form-label-mandatory text-right">Type</label>
            <div class="col-4">
                <div class="input-group">
                    <g:select name="typeValue" from="${signifierTypes}" valueMessagePrefix="BarcodeSignifierType"
                              optionKey="${{it}}"
                              noSelection="['': 'Select Type']"
                              class="form-control select-border"
                              value="${signifier?.type}" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'type', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="patternValue" class="col-3 offset-1 col-form-label-mandatory text-right">Pattern</label>
            <div class="col-4">
                <div class="input-group">
                    <g:field type="text" id="pattern" name="patternValue" value="${signifier?.pattern}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'pattern', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="lengthValue" class="col-3 offset-1 col-form-label-mandatory text-right">Length</label>
            <div class="col-4">
                <div class="input-group">
                    <g:field type="number" id="length" name="lengthValue" value="${signifier?.length}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumericInt(event);" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'length', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="receiptDescriptionValue" class="col-3 offset-1 col-form-label text-right">Receipt Description</label>

            <div class="input-group col-4">
                <g:textField name="receiptDescriptionValue" value="${signifier?.receiptDescription}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4 custom-checkbox-align">
            <label for="checkDigitValue" class="col-3 offset-1 col-form-label text-right">Check Digit</label>

            <div class="col-4">
                <div class="form-check">
                    <g:checkBox name="checkDigitValue" value="${signifier?.checkDigit}" class="form-check-input" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="discountPercentageValue" class="col-3 offset-1 col-form-label text-right">Discount Percentage</label>

            <div class="col-4">
                <div class="input-group">
                    <g:field type="number" name="discountPercentageValue" value="${signifier?.discountPercentage}" class="form-control bottom-border" min="0" max="100" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'discountPercentage', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

    </g:form>
    <g:render template="/errors/errorMessage" model="[errorKey: 'general', errorMessages: errorMessages, error: error]" />
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddTillButton" class="btn btn-wl" onclick="cancelSignifier();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveSignifier();">Save</button>
</div>
