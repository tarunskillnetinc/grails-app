<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 pt-5 ml-0 mr-0 table-wl bottom-border">
    <div class="col-3 font-weight-bold text-center"> <a id="sinifier-list-description" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'description',
        order: ${sortParams?.sort == 'description' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Description<a/></div>
    <div class="col-1 font-weight-bold text-center"> <a id="sinifier-list-type" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'type',
        order: ${sortParams?.sort == 'type' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Type</a></div>
    <div class="col-1 font-weight-bold text-center"> <a id="sinifier-list-pattern" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'pattern',
        order: ${sortParams?.sort == 'pattern' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
});">Pattern</a></div>
    <div class="col-1 font-weight-bold text-center"> <a id="sinifier-list-length" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'length',
        order: ${sortParams?.sort == 'length' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
});">Length</a></div>
    <div class="col-2 font-weight-bold text-center"> <a id="sinifier-list-receipt-description" href="#" onclick="getSignifiers({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'receiptDescription',
        order: ${sortParams?.sort == 'receiptDescription' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Receipt Description</a></div>
    <div class="col-2 font-weight-bold text-center">Embedded Data</div>
    <div class="col-2 font-weight-bold text-center"></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:set var="row" value="${0}" scope="request"/>
    <g:render template="signifierSearchRows" model="[signifiers: signifiers, level: 0]"/>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="barcodeConfig" action="ajaxSearchForBarcodeSignifiers" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" />
</div>