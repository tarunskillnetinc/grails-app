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
    <div class="col-2 font-weight-bold text-left">Type</div>
    <div class="col-2 font-weight-bold text-center"> Start Index</div>
    <div class="col-2 font-weight-bold text-center">Length</div>
    <div class="col-2 font-weight-bold text-center">Format</div>
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