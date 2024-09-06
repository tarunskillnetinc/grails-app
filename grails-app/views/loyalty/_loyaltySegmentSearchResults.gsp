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
    <div class="col-4 font-weight-bold">ID</div>
    <div class="col-4 font-weight-bold">Description</div>
    <div class="col-4 font-weight-bold">Member Count</div>
</div>

<div id="search-results">
    <g:if test="${segments == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search criteria to recall segment data.</div>
        </div>
    </g:if>

    <g:if test="${segments?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${segments}" var="segment" status="i">
        <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;">
            <div id="id-${i + 1}" class="col-4">${segment.id}</div>
            <div id="description-${i + 1}" class="col-4">${segment.description}</div>
            <div id="count-${i + 1}" class="col-4">${segment.count}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchLoyaltySegment" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[loyaltySegmentTerm: loyaltySegmentTerm, loyaltySegmentSearchBy: loyaltySegmentSearchBy]" />
</div>