<div class="row mt-5 offset-3 col-6">
    <p>Select a store below to add it to this group.</p>
</div>

<div class="row mt-4 offset-3 col-6 pb-2 table-wl bottom-border">
    <div class="col-4 font-weight-bold">Store ID</div>
    <div class="col-4 font-weight-bold">Store Name</div>
</div>

<g:if test="${!stores || stores?.size() == 0}">
    <div id="noResultsRow" class="col-6 offset-3 pt-2 pb-2 text-center wl-striped0">No stores available.</div>
</g:if>

<g:each in="${stores}" var="store" status="i">
    <div class="row offset-3 col-6 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="addStoreToGroup(${store.id}, '${groupId}');">
        <div class="col-4">${store.storeId}</div>
        <div class="col-8">${store.storeName}</div>
    </div>
</g:each>