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
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'reference', sortOrder: ${sortParams?.sortColumn == 'reference' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Reference</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'name', sortOrder: ${sortParams?.sortColumn == 'name' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Name</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'customerReference', sortOrder: ${sortParams?.sortColumn == 'customerReference' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Customer Reference</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'contactName', sortOrder: ${sortParams?.sortColumn == 'contactName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Contact Name</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'email', sortOrder: ${sortParams?.sortColumn == 'email' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Email</a>
    </div>
    <div class="col-2 font-weight-bold">
        <a href="#" onclick="searchSupplier({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'phoneNumber', sortOrder: ${sortParams?.sortColumn == 'phoneNumber' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Telephone</a>
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
            <div id="supplier-result-${i+1}-reference" class="col-2 my-auto text-truncate" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.reference}</div>
            <div id="supplier-result-${i+1}-name" class="col-2 my-auto text-truncate" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.name}</div>
            <div id="supplier-result-${i+1}-customer-reference" class="col-2 my-auto" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.customerReference}</div>
            <div id="supplier-result-${i+1}-contact-name" class="col-2 my-auto" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.contactName}</div>
            <div id="supplier-result-${i+1}-email" class="col-2 my-auto" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.email}</div>
            <div id="supplier-result-${i+1}-telephone" class="col-2 my-auto" style='padding: 3px; width: 130px; word-break: break-all; word-wrap: break-word;'>${supplier.phoneNumber}</div>
        </div>
    </g:each>
</div>

<g:if test="${totalCount > 0}">
    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxGetSearchSupplier" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy, sortParams  : sortParams]" />
    </div>
</g:if>



