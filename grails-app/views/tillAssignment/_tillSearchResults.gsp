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
    <div class="col-2 font-weight-bold text-center">Store</div>
    <div class="col-1 font-weight-bold text-center">Till Number</div>
    <div class="col-2 font-weight-bold text-center">Description</div>
    <div class="col-2 font-weight-bold text-center">Serial Number</div>
</div>

<div id="search-results">
    <g:set var="row" value="${0}" scope="request"/>
    <g:render template="tillSearchRows" model="[tillList: tillList, level: 0]"/>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="tillAssignment" action="ajaxSearchForTills" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" />
</div>