<div class="d-flex justify-content-center">
    <div id="ad-hoc-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="ad-hoc-results">
    <g:if test="${productLists?.size() == 0}">
        <div class="row col-10 offset-1 px-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No pending ad-hoc batches.</div>
        </div>
    </g:if>

    <g:each in="${productLists}" var="productList" status="i">
        <div class="row col-10 offset-1 px-0 py-1 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(action:'showAdHocBatch', id: productList.id)}';">
            <div class="col-5 my-auto">${productList.reasonDescription}</div>
            <div class="col-2 my-auto">${productList.dateStarted?.toString('dd/MM HH:mm')}</div>
            <div class="col-2 my-auto">${productList.labelCount}</div>
            <div class="col-3 my-auto"><g:select name="labelTemplate" from="${labelTemplates}" noSelection="${[0: 'Select Label']}" optionKey="id" optionValue="name" class="form-control select-border" onclick="event.stopPropagation();" onChange="adHocBatchTemplateSelected(${productList.id}, this);" /></div>
        </div>
    </g:each>
</div>