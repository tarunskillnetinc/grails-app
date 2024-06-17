<div class="modal-header">
    <g:if test="${enableEdit}">
        <h2>Edit Signifier</h2>
    </g:if>
    <g:else>
        <h2>Add Signifier</h2>
    </g:else>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new EmbeddedData. Type, Pattern, and Length are required.</div>

    <g:form name="addEmbeddedDataForm">
        <!-- Hidden field for id -->
        <g:hiddenField name="id" value="${embeddedData?.id}" />

        <!-- Hidden field for barcodeSignifierId -->
        <g:hiddenField name="barcodeSignifierId" value="${signifierId}" />

        <!-- Drop down selection box for type -->
        <div class="row form-group mb-4">
            <label for="typeValue" class="col-3 offset-1 col-form-label-mandatory text-right">Type</label>
            <div class="col-4">
                <div class="input-group">
                    <g:select name="typeValue" from="${embeddedDataTypes}" valueMessagePrefix="EmbeddedDataType"
                              optionKey="${{it}}"
                              noSelection="['': 'Select Type']"
                              class="form-control select-border"
                              value="${embeddedData?.type}" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'type', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <!-- Integer field for startIndex -->
        <div class="row form-group mb-4">
            <label for="startIndexValue" class="col-3 offset-1 col-form-label-mandatory text-right">Start Index</label>
            <div class="col-4">
                <div class="input-group">
                    <g:field type="number" id="startIndexValue" name="startIndexValue" value="${embeddedData?.startIndex}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'startIndex', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <!-- Text field for format -->
        <div class="row form-group mb-4">
            <label for="formatValue" class="col-3 offset-1 col-form-label text-right">Format</label>
            <div class="col-4">
                <div class="input-group">
                    <g:field type="text" id="formatValue" name="formatValue" value="${embeddedData?.format}" class="form-control bottom-border" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'format', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="lengthValue" class="col-3 offset-1 col-form-label-mandatory text-right">Length</label>
            <div class="col-4">
                <div class="input-group">
                    <g:field type="number" id="lengthValue" name="lengthValue" value="${embeddedData?.length}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                </div>
                <div class="field-error text-sm-left mt-2">
                    <g:render template="/errors/fieldError" model="[errorKey: 'length', errorMessages: errorMessages, error: error]" />
                </div>
            </div>
        </div>

    </g:form>
    <g:render template="/errors/errorMessage" model="[errorKey: 'general', errorMessages: errorMessages, error: error]" />
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddTillButton" class="btn btn-wl" onclick="cancelEmbeddedData();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveEmbeddedData();">Save</button>
</div>
