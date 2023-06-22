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
        <div class="row col-10 offset-1 px-0 py-1 wl-striped${i%2} hoverable">
            <div id="ad-hoc-description-${i + 1}" class="col-4 my-auto">${productList.reasonDescription ?: g.message(code: 'ShelfEdgeLabelType.' +productList.type)}</div>
            <div id="ad-hoc-date-started-${i + 1}" class="col-2 my-auto">${productList.dateStarted?.toString('dd/MM HH:mm')}</div>
            <div id="ad-hoc-label-count-${i + 1}" class="col-2 my-auto">${productList.labelCount}</div>
            <div id="ad-hoc-print-${i + 1}" class="col-3 my-auto"><g:select id="labelTemplate" name="labelTemplate" from="${labelTemplates}" noSelection="${[0: 'Select Label']}" optionKey="id" optionValue="name" class="form-control select-border" onclick="event.stopPropagation();" onChange="adHocBatchTemplateSelected(${productList.id}, this);" /></div>
            <div class="col-1 my-auto">
                <g:if test="${deletableStatuses.contains(productList.status)}">
                    <asset:image id="ad-hoc-delete-${i + 1}" src="trash.svg" width="30" height="30" class="pointer" onclick="deleteProductListButtonPressed(${productList.id});" title="Click to delete." />
                </g:if>
                <g:else>&nbsp;</g:else>
            </div>
        </div>
    </g:each>
</div>