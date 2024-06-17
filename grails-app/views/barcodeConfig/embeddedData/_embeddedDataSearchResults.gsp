<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-1 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold text-center"> <a id="sinifier-list-type" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'type',
        order: ${sortParams?.sort == 'type' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Type</a></div>
    <div class="col-2 font-weight-bold text-center"> <a id="sinifier-list-pattern" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'startIndex',
        order: ${sortParams?.sort == 'startIndex' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
});">Pattern</a></div>
    <div class="col-2 font-weight-bold text-center"> <a id="sinifier-list-length" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'length',
        order: ${sortParams?.sort == 'length' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
});">Length</a></div>
    <div class="col-2 font-weight-bold text-center"> <a id="sinifier-list-description" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'format',
        order: ${sortParams?.sort == 'format' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Format</a></div>
    <div class="col-4 font-weight-bold text-center"></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:set var="row" value="${0}" scope="request"/>
    <g:render template="embeddedData/embeddedDataSearchRows" model="[embeddedDataList: embeddedDataList, level: 0]"/>
</div>

%{--<div class="my-3 text-right">--}%
%{--    <util:remotePaginate controller="barcodeConfig" action="ajaxShowEmbeddedDataList" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" />--}%
%{--</div>--}%