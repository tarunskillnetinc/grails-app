<div class="row mt-4 offset-3 col-6 px-0">
    <div class="col-9 text-right px-0">
        <g:groupBreadcrumb groupId="${parentId}" />
    </div>

    <div class="col-3 text-right px-0">
        <g:link controller="group" action="add" class="btn btn-wl">Add New Group</g:link>
    </div>
</div>

<div class="row mt-5 offset-3 col-6 pb-2 table-wl bottom-border">
    <div class="col-4 font-weight-bold">Store ID</div>
    <div class="col-4 font-weight-bold">Name</div>
</div>

<g:if test="${!group || group?.stores?.size() == 0}">
    <div id="noResultsRow" class="col-6 offset-3 pt-2 pb-2 text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${group.stores?.sort{ it.storeId }}" var="store" status="i">
    <div class="row offset-3 col-6 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="removeStoreFromGroup(${store.id}, ${group.id});">
        <div class="col-4">${store.config.storeNumber}</div>
        <div class="col-8">${store.config.storeName}</div>
    </div>
</g:each>