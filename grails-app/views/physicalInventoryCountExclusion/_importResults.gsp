<div class="text-right mt-2">
    <g:if test="${preview}">
        <button class="btn btn-success p-2" id="uploadSave" name="save" onclick="confirmImport()">Import</button>
        <button class="btn btn-wl p-2 ml-2" id="uploadCancel" name="cancel">Cancel</button>
    </g:if>
    <g:else>
        <g:if test="${results.invalidSkus && results.invalidSkus.size() > 0}">
            <button class="btn btn-wl p-2 ml-2" id="exportResults" name="export" onclick="window.location='${createLink(action:'exportResults')}';">Export Results</button>
        </g:if>
    </g:else>
</div>

<g:if test="${importError}">
    <div class="alert alert-danger alert-wl" role="alert" id="failureMessage">${importError}</div>
</g:if>
<g:else>
    <g:if test="${preview}">
        <h3>Preview SKUs</h3>
        <div class="row mt-5 pb-2 table-wl bottom-border align-content-center ml-0 mr-0">
            <div class="col-4 font-weight-bold">SKU</div>
        </div>

        <g:each in="${skus}" var="sku" status="i">
            <div class="row align-content-center pt-2 pb-2 wl-striped${i%2} ml-0 mr-0">
                <div class="col-4">${sku}</div>
            </div>
        </g:each>
    </g:if>
    <g:else>
        <g:if test="${results.invalidSkus && results.invalidSkus.size() > 0}">
            <h3>Invalid SKUs</h3>
            <div class="row mt-5 pb-2 table-wl bottom-border align-content-center ml-0 mr-0">
                <div class="col-4 font-weight-bold">SKU</div>
            </div>

            <g:each in="${results.invalidSkus}" var="sku" status="i">
                <div class="row align-content-center pt-2 pb-2 wl-striped${i%2} ml-0 mr-0">
                    <div class="col-4">${sku}</div>
                </div>
            </g:each>
        </g:if>
    </g:else>
</g:else>
