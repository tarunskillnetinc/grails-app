<div class="modal-header">
    <g:if test="${enableEdit}">
        <h2 id="page-title" class="mx-auto my-auto">Edit Embedded Data</h2>
    </g:if>
    <g:else>
        <h2 id="page-title" class="mx-auto my-auto">Add Embedded Data</h2>
    </g:else>
</div>
<g:render template="/errors/errorMessage" model="[errorMessages: errorMessages, error: error]" />
<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new EmbeddedData. Type, Start Index, Format and Length are required.</div>

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
            </div>
        </div>

        <!-- Integer field for startIndex -->
        <div class="row form-group mb-4">
            <label for="startIndexValue" class="col-3 offset-1 col-form-label-mandatory text-right">Start Index</label>
            <div class="col-4">
                <div class="input-group number-box">
                    <g:field type="number" id="startIndexValue" name="startIndexValue" value="${embeddedData?.startIndex}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumericInt(event);" />
                </div>
            </div>
        </div>

        <!-- Text field for format -->
        <div class="row form-group mb-4">
            <label for="formatValue" class="col-3 offset-1 col-form-label-mandatory text-right">Format</label>
            <div class="col-4">
                <div class="input-group">
                    <g:select name="formatValue" from="${formatList}" valueMessagePrefix="EmbeddedDataFormats"
                              optionKey="${{it}}"
                              noSelection="['': 'Select Format']"
                              class="form-control select-border"
                              value="${embeddedData?.format}" />
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="lengthValue" class="col-3 offset-1 col-form-label-mandatory text-right">Length</label>
            <div class="col-4">
                <div class="input-group number-box">
                    <g:field type="number" id="lengthValue" name="lengthValue" value="${embeddedData?.length}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumericInt(event);" />
                </div>
            </div>
        </div>

    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddTillButton" class="btn btn-wl" onclick="cancelEmbeddedData();">Cancel</button>
    <sec:ifAnyGranted roles='ROLE_ENGINEER'>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveEmbeddedData();">Save</button>
    </sec:ifAnyGranted>
</div>
