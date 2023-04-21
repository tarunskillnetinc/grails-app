<g:if test="${!successful}">
    <div id="noResultsRow" class="col-6 offset-3 pt-2 pb-2 text-center wl-striped0">The hardware could not be processed because of one or more errors</div>
</g:if>
<g:else>
    <div class="row mt-5 pb-2 table-wl bottom-border align-content-center">
        <div class="col-4 font-weight-bold">Serial Number</div>
        <div class="col-4 font-weight-bold">Model</div>
        <div class="col-4 font-weight-bold">Valid to import</div>
    </div>

    <g:each in="${rows}" var="row" status="i">
        <div class="row align-content-center pt-2 pb-2 wl-striped${i%2}">
            <div class="col-4">${row.serialNumber}</div>
            <div class="col-4">${row.model}</div>
            <div class="col-4">
                <g:if test="${row.validRow}">
                    Valid
                </g:if>
                <g:else>
                    Invalid
                </g:else>
            </div>
        </div>
    </g:each>
</g:else>

<div class="col-2 text-right">
    <g:if test="${rows.any{ it.validRow }}">
        <button class="btn btn-wl" id="uploadSave" name="save">Import</button>
    </g:if>
    <button class="btn btn-wl" id="uploadCancel" name="cancel">Cancel</button>
    <g:if test="${rows.any{ it.validRow }}">
        <button class="btn btn-wl" id="exportResults" name="export" onclick="window.location='${createLink(action:'exportResults')}';">Export Results</button>
    </g:if>
</div>