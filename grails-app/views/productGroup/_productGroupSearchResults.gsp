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
        <a id="productgroup-list-id" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'id',
            sortOrder: ${sortColumn == 'id' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Product Group ID
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="productgroup-list-description" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'description',
            sortOrder: ${sortColumn == 'description' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Description
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="productgroup-list-startdate" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'startDate',
            sortOrder: ${sortColumn == 'startDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Start Date
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="productgroup-list-enddate" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'endDate',
            sortOrder: ${sortColumn == 'endDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            End Date
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="productgroup-list-timerestriction" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'timeRestriction',
            sortOrder: ${sortColumn == 'timeRestriction' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Restriction Type
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="productgroup-list-productcount" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'productCount',
            sortOrder: ${sortColumn == 'productCount' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Product Count
        </a>
    </div>
    <div class="col-1 font-weight-bold">
        <a id="productgroup-list-status" href="#" onclick="getProductGroups({
            max: '${max}', offset: '${offset}', sortColumn: 'active',
            sortOrder: ${sortColumn == 'active' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">
            Status
        </a>
    </div>
</div>

<g:if test="${!productGroups || productGroups?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${productGroups}" var="productGroup" status="i">
    <div id="productGroup-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable pointer"
         title="Click to view." onclick="document.location.href = '${createLink(action:'addEdit', id: productGroup?.id)}';">
        <div id="productGroup-${i + 1}-id" class="col-1">${productGroup?.id}</div>
        <div id="productGroup-${i + 1}-description" class="col-2">${productGroup?.description}</div>

        <div id="productGroup-${i + 1}-startdate" class="col-2">
            <g:if test="${productGroup?.startDate}">
                <g:formatDate format="dd/MM/yyyy" date="${productGroup?.startDate?.toDate()}"/>
            </g:if>
            <g:else>&nbsp;</g:else>
        </div>

        <div id="productGroup-${i + 1}-enddate" class="col-2">
            <g:if test="${productGroup?.endDate}">
                <g:formatDate format="dd/MM/yyyy" date="${productGroup?.endDate?.toDate()}"/>
            </g:if>
            <g:else>&nbsp;</g:else>
        </div>

        <div id="productGroup-${i + 1}-restrictiontype" class="col-2">${productGroup?.getRestrictionType()}</div>
        <div id="productGroup-${i + 1}-prod-count" class="col-2">${productGroup?.productGroupProducts?.size()}</div>
        <div id="productGroup-${i + 1}-max-sell-quantity" class="col-1">
            <g:if test="${productGroup?.active}">Active</g:if>
            <g:else>Inactive</g:else>
        </div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxGetProductGroups"
                         total="${productGroups?.size() ?: 0}"
                         update="search-results"
                         offset="${offset ?: 0}" max="${max ?: 50}"
                         params="['productGroupSearchTerm': productGroupSearchTerm,
                                  'productGroupSearchBy': productGroupSearchBy,
                                  'startDate': startDate,
                                  'endDate': endDate,
                                  'status': status]"/>
</div>