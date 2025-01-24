<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div style="display: flex;">
    <div style="flex-grow: 1;">
        <div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border">
            <div class="col-2 font-weight-bold text-center">Partner</div>
            <div class="col-2 font-weight-bold text-center">Partner Category</div>
            <div class="col-6 font-weight-bold text-left">Category & Subcategory List</div>
            <div class="col-2 font-weight-bold text-center">Action</div>
        </div>
    </div>
</div>

<div id="search-results">

    <g:if test="${!ecomSupplierCategories || ecomSupplierCategories?.isEmpty()}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${ecomSupplierCategories}" var="ecomCategory" status="i">
        <div style="flex-grow: 1;">
            <div style="flex-grow: 1;">
                <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" >
                    <div class="col-2 text-center">${ecomCategory?.ecomSupplier?.name}</div>
                    <div class="col-2 text-center">${ecomCategory?.description}</div>
                    <div class="col-6 text-left">
                        <g:if test="${ecomCategory?.ecomSupplierCategoryMappings}">
                            <g:renderCategoryHierarchy
                                    mappings ="${ecomCategory?.ecomSupplierCategoryMappings}"
                                    selectedCategories="${ecomCategory?.ecomSupplierCategoryMappings?.collect { it.category.id }?.findAll { it != null }}"
                            />
                        </g:if>
                    </div>

                    <div class="col-2 text-center">
                        <g:link elementId="edit-button" type="button" class="btn btn-wl p-1 me-1"
                                action="addPartnerCategory" params="[isNew: false, supplierCategoryId: ecomCategory?.id]" style="min-width: 80px; font-size: 0.9rem;">Edit</g:link>
                        <g:link elementId="edit-button" type="button" class="btn btn-danger p-1 me-1"
                                action="deletePartnerCategory" params="[supplierCategoryId: ecomCategory?.id]" style="min-width: 80px; font-size: 0.9rem;">Delete</g:link>
                    </div>
                </div>
            </div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="partnerCategoryManagement" action="ajaxSearchForTills" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" />
</div>