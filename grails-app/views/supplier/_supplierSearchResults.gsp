<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-1 font-weight-bold">
        <a id="supplier-header-id" href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'id', sortOrder: ${sortParams?.sortColumn == 'id' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier ID</a>
    </div>

    <div class="col-2 font-weight-bold">
        <a id="supplier-header-reference" href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'reference', sortOrder: ${sortParams?.sortColumn == 'reference' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Reference</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="supplier-header-name" href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'name', sortOrder: ${sortParams?.sortColumn == 'name' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Name</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="supplier-header-customer-reference" href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'customerReference', sortOrder: ${sortParams?.sortColumn == 'customerReference' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Customer Reference</a>
    </div>
    <div class="col-3 font-weight-bold" id="supplier-header-contact-details">
        Contact Details
    </div>
    <div class="col-1 font-weight-bold">
        <a id="supplier-header-status" href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'deleted', sortOrder: ${sortParams?.sortColumn == 'deleted' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Status</a>
    </div>
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
        <div id="supplier-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="editSupplier(${supplier.id});">
            <div id="supplier-result-${i+1}-id" class="col-1 my-auto" style='word-break: break-all; word-wrap: break-word;'>${supplier.id}</div>
            <div id="supplier-result-${i+1}-reference" class="col-2 my-auto" style='word-break: break-all; word-wrap: break-word;'>${supplier.reference}</div>
            <div id="supplier-result-${i+1}-name" class="col-2 my-auto" style='word-break: break-all; word-wrap: break-word;'>${supplier.name}</div>
            <div id="supplier-result-${i+1}-customer-reference" class="col-2 my-auto" style='word-break: break-all; word-wrap: break-word;'>${supplier.customerReference}</div>
            <div id="supplier-result-${i+1}-contact-details" class="col-3 my-auto" style='word-break: break-all; word-wrap: break-word;'>
                <g:if test="${supplier.contactName && supplier.contactName.trim()}">
                    <div id="supplier-result-${i+1}-contact-name"><b>Name:</b> ${supplier.contactName}</div>
                </g:if>
                <g:if test="${supplier.email && supplier.email.trim()}">
                    <div id="supplier-result-${i+1}-email"><b>Email:</b> ${supplier.email}</div>
                </g:if>
                <g:if test="${supplier.phoneNumber && supplier.phoneNumber.trim()}">
                    <div id="supplier-result-${i+1}-telephone"><b>Tel:</b> ${supplier.phoneNumber}</div>
                </g:if>
            </div>
            <div id="supplier-result-${i+1}-deleted" class="col-1 my-auto" style='word-break: break-all; word-wrap: break-word;'>
                <g:if test="${supplier.deleted == true}">
                    Inactive
                </g:if>
                <g:else>
                    Active
                </g:else>
            </div>

            <div id="supplier-result-${i + 1}-delete-toggle" class="col-1 my-auto">
                <div class="button-container d-flex justify-content-end align-items-center">
                    <g:if test="${supplier.deleted == true}">
                        <button id="toggle-supplier-deleted-button-${i+1}" class="btn btn-wl p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                onclick="event.stopPropagation(); toggleSupplierDeleted(${supplier.id}, ${supplier.deleted}, {offset: ${offset ?: 0}, sortColumn: '${sortParams?.sortColumn}'})">
                            Reinstate
                        </button>
                    </g:if>
                    <g:else>
                        <button id="toggle-supplier-deleted-button-${i+1}" class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                onclick="event.stopPropagation(); toggleSupplierDeleted(${supplier.id}, ${supplier.deleted},{offset: 0, sortColumn: '${sortParams?.sortColumn}'})">
                            Delete
                        </button>
                    </g:else>
                </div>
            </div>
        </div>
    </g:each>
</div>

<g:if test="${totalCount > 0}">
    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxGetSearchSupplier" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[
                supplierNameTerm: params.supplierNameTerm, supplierReferenceTerm: params.supplierReferenceTerm, customerReferenceTerm: params.customerReferenceTerm, includeDeletedSuppliers: params.includeDeletedSuppliers,
                sortColumn: sortParams?.sortColumn,  sortParams: sortParams]" />
    </div>
</g:if>



