<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="container-fluid mt-5 mb-3">


    <div class="row mb-2 no-gutters" id="partner-category-menu-headers">
        <div class="col-2 font-weight-bold text-center">Partner</div>
        <div class="col-2 font-weight-bold text-center">Partner Category</div>
        <div class="col-6 font-weight-bold text-center">Category & Subcategory List</div>
        <div class="col-2 font-weight-bold text-center">Action</div>
    </div>

    <div class="card">
        <!-- Each partner category as a Boxed Card -->
        <g:if test="${!ecomSupplierList || ecomSupplierList?.isEmpty()}">
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <h3 class="text-muted">No results found</h3>
            </div>
        </g:if>
        <g:else>
            <g:each in="${ecomSupplierList}" var="ecomSupplier" status="i">
                <div class="shift-card-body border rounded mb-2 shift-info-container pt-2 pb-2 wl-striped${i%2}">
                    <div class="row mb-2 align-items-center">
                        <div class="col-2 font-weight-bold text-center">${ecomSupplier?.name}</div>
                        <div class="col-2 font-weight-bold text-center"></div>
                        <div class="col-6 font-weight-bold text-center">

                        </div>
                        <div class="col-2 font-weight-bold text-center">
                            <button class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="">Edit</button>
                            <button class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="">Remove</button>
                        </div>
                    </div>
                </div>
            </g:each>
        </g:else>
    </div>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="tillAssignment" action="ajaxSearchForTills" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" />
</div>