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
    <div class="col-1 font-weight-bold">Segment ID</div>
    <div class="col-2 font-weight-bold">Segment Name</div>
    <div class="col-3 font-weight-bold">Segment Description</div>
    <div class="col-2 font-weight-bold">Segment Value</div>
    <div class="col-2 font-weight-bold">Members Count</div>
    <div class="col-1 font-weight-bold">Status</div>
</div>

<div id="search-results">
    <g:if test="${segments == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search term to get Loyalty segments</div>
        </div>
    </g:if>

    <g:if test="${segments?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${segments}" var="segment" status="i">
        <div id="segment-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'segmentDetails', params:[id: segment.id, edit: true])}';">
            <div id="segment-id-${i + 1}" class="col-1">${segment.id}</div>
            <div id="segment-name-${i + 1}" class="col-2">${segment.name}</div>
            <div id="segment-description-${i + 1}" class="col-3">${segment.description}</div>
            <div id="segment-value-${i + 1}" class="col-2">${segment.getSegmentValue()}</div>
            <div id="segment-count-${i + 1}" class="col-2">${segment.count}</div>
            <div id="segment-status-${i + 1}" class="col-1">${segment.status}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchLoyaltySegment" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[loyaltySegmentTerm: loyaltySegmentTerm, loyaltySegmentSearchBy: loyaltySegmentSearchBy]" />
</div>