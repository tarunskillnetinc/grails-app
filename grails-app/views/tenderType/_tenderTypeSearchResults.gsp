<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a id="tender-type-name" href="#" onclick="getTenderTypes({
        max: ${max},
        offset: ${offset},
        sortColumn: 'name',
        sortOrder: ${sortColumn == 'name' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Name</a></div>
    <div class="col-2 font-weight-bold"><a id="tender-type-receiptDescription" href="#" onclick="getTenderTypes({
        max: ${max},
        offset: ${offset},
        sortColumn: 'receiptDescription',
        sortOrder: ${sortColumn == 'receiptDescription' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Receipt Description</a></div>
    <div class="col-2 font-weight-bold"><a id="tender-type-reconcile-type" href="#" onclick="getTenderTypes({
        max: ${max},
        offset: ${offset},
        sortColumn: 'autoReconcile',
        sortOrder: ${sortColumn == 'autoReconcile' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Reconcile Type</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!tenderTypes || tenderTypes?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No tender types found.</div>
    </g:if>

    <g:each in="${tenderTypes}" var="tenderType" status="i">
        <g:if test="${tenderType.isProtected}">
            <div id="tender-type-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} greyed-out">
        </g:if>
        <g:else>
            <div id="tender-type-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to edit." onclick="event.stopPropagation(); editTenderType(${tenderType.id});">
        </g:else>
            <div id="tendertype-name-${i + 1}" class="col-2 my-auto">${tenderType.name}</div>
            <div id="tender-type-receiptDescription-${i + 1}" class="col-2 my-auto">${tenderType.receiptDescription}</div>
            <div id="tender-type-autoReconcile-${i + 1}" class="col-2 my-auto">${tenderType.autoReconcile ? 'Auto' : 'Manual'}</div>

            <g:if test="${!tenderType.isProtected}">
                <div class="col-6 my-auto text-right">
                    <g:if test="${tenderType.deleted}">
                        <button id="tender-type-delete-${i + 1}" class="btn btn-info" onclick="event.stopPropagation(); deleteTenderType(${tenderType.id}, '${tenderType.name}', false);">Reinstate Tender Type</button>
                    </g:if>
                    <g:else>
                        <button id="tender-type-delete-${i + 1}" class="btn btn-danger" onclick="event.stopPropagation(); deleteTenderType(${tenderType.id}, '${tenderType.name}', true);">Delete Tender Type</button>
                    </g:else>
                </div>
            </g:if>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="tenderType" action="ajaxSearch" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[sortColumn: sortColumn, sortOrder: sortOrder, tenderTypeFilter: tenderTypeFilter, showDeletedFilter: showDeletedFilter]" />
</div>