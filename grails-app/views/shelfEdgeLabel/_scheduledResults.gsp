<div class="d-flex justify-content-center">
    <div id="ad-hoc-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="scheduled-results">
    <g:if test="${batchesToBePrinted?.size() == 0}">
        <div class="row col-10 offset-1 px-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No pending ad-hoc batches.</div>
        </div>
    </g:if>

    <g:each in="${batchesToBePrinted}" var="batch" status="i">
        <div class="row col-10 offset-1 px-0 py-1 wl-striped${i%2} hoverable">
            <div id="scheduled-print-effective-date-${i + 1}" class="col-6 my-auto">${batch.key?.toString("dd/MM/yy")}</div>
            <div id="scheduled-print-label-count-${i + 1}" class="col-3 my-auto">${batch.value?.size()}</div>
            <div id="scheduled-print-print-confirm-${i + 1}" class="col-3 my-auto"><g:select name="labelTemplate" from="${labelTemplates}" noSelection="${[0: 'Select Label']}" optionKey="id" optionValue="name" class="form-control select-border" onclick="event.stopPropagation();" onChange="scheduledBatchTemplateSelected('${batch.key.toString("dd/MM/yyyy")}', this);" /></div>
        </div>
    </g:each>

    <g:each in="${batchesToBeConfirmed}" var="batch" status="i">
        <div class="row col-10 offset-1 px-0 py-1 wl-striped${i%2} hoverable">
            <div id="scheduled-confirm-effective-date-${i + 1}" class="col-6 my-auto">${batch.key?.toString("dd/MM/yy")}</div>
            <div id="scheduled-confirm-label-count-${i + 1}" class="col-3 my-auto">${batch.value?.size()}</div>
            <div id="scheduled-confirm-print-confirm${i + 1}" class="col-3 my-auto text-center"><a id="confirmBatch${i}" class="btn btn-wl" href="#" onchange="event.stopPropagation();" onclick="applyChangesButtonClicked('${batch.key.toString("dd/MM/yyyy")}', this);">Apply Changes</a></div>
        </div>
    </g:each>
</div>