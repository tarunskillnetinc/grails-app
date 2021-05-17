<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold">Supplier Reference</div>
    <div class="col-2 font-weight-bold">Supplier Name</div>
    <div class="col-2 font-weight-bold">Customer Reference</div>
    <div class="col-2 font-weight-bold">Contact Name</div>
    <div class="col-2 font-weight-bold">Email</div>
    <div class="col-2 font-weight-bold">Telephone</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!suppliers || suppliers?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No suppliers found.</div>
    </g:if>

    <g:each in="${suppliers}" var="supplier" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="editSupplier(${supplier.id});">
            <div class="col-2 my-auto">${supplier.reference}</div>
            <div class="col-2 my-auto">${supplier.name}</div>
            <div class="col-2 my-auto">${supplier.customerReference}</div>
            <div class="col-2 my-auto">${supplier.contactName}</div>
            <div class="col-2 my-auto">${supplier.email}</div>
            <div class="col-2 my-auto">${supplier.phoneNumber}</div>
        </div>
    </g:each>
</div>