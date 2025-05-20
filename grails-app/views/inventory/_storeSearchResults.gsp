<!-- _modalStoreSearchResults.gsp -->
<div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
    <table class="table table-hover mb-0">
        <thead class="thead-light sticky-top">
            <tr>
                <th width="30%">Store Number</th>
                <th width="50%">Store Name</th>
                <th width="20%">Select Store</th>
            </tr>
        </thead>
        <tbody id="storesTableBody">
            <g:if test="${!stores || stores?.size() == 0}">
                <tr>
                    <td colspan="3" class="text-center">No stores found.</td>
                </tr>
            </g:if>
            <g:each in="${stores}" var="store" status="i">
                <tr>
                    <td>${store.config.storeNumber}</td>
                    <td>${store.config.storeName}</td>
                    <td><input type="checkbox" class="store-checkbox" data-store-id="${store.id}" data-store-number="${store.config.storeNumber}" data-store-name="${store.config.storeName}"></td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="inventory" action="ajaxGetStores" total="${totalResults ?: 0}" update="results-container" offset="${sortParams?.offset ?: 0}" max="${sortParams?.max ?: 50}" params="[sort: sortParams?.sort, order: sortParams?.order, storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter]" />
</div>