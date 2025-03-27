
<div class="text-right mt-2">
    <g:if test="${rows.any{ it.validRow } && successful}">
        <button class="btn btn-success p-2" id="uploadSave" name="save">Import</button>
    </g:if>
    <button class="btn btn-wl p-2 ml-2" id="uploadCancel" name="cancel">Cancel</button>
    <g:if test="${rows.any{ it.validRow } && successful}">
        <button class="btn btn-wl p-2 ml-2" id="exportResults" name="export" onclick="window.location='${createLink(action:'exportResults')}';">Export Results</button>
    </g:if>
</div>

<g:if test="${!successful}">
    <div class="alert alert-danger alert-wl" role="alert" id="failureMessage">${importError}  </div>
</g:if>
<g:else>
    <div class="row mt-5 pb-2 table-wl bottom-border align-content-center ml-0 mr-0">
        <div class="col-4 font-weight-bold">Serial Number</div>
        <div class="col-4 font-weight-bold">Model</div>
        <div class="col-4 font-weight-bold">Valid to Import</div>
    </div>

    <g:each in="${rows}" var="row" status="i">
        <div class="row align-content-center pt-2 pb-2 wl-striped${i%2} ml-0 mr-0">
            <div class="col-4">
                <g:if test="${row.serialNumber.length() > 50}">
                    ${row.serialNumber.substring(0, 25)}...
                </g:if>
                <g:else>
                    ${row.serialNumber}
                </g:else>
            </div>
            <div class="col-4">
                <g:if test="${row.model.length() > 50}">
                    ${row.model.substring(0, 25)}...
                </g:if>
                <g:else>
                    ${row.model}
                </g:else>
            </div>
            <div class="col-4">
                <g:if test="${row.validRow}">
                    Valid
                </g:if>
                <g:elseif test="${row.errorRow != ""}">
                    ${row.errorRow}
                </g:elseif>
                <g:else>
                    Invalid
                </g:else>
            </div>
        </div>
    </g:each>
</g:else>

