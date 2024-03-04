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
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchButtonClicked({max: '${max}', offset: '${offset}', sortColumn: 'cardNumber', sortOrder: ${sortColumn == 'cardNumber' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Membership Number</a></div>
    <div class="col-3 font-weight-bold"><a href="#" onclick="searchButtonClicked({max: '${max}', offset: '${offset}', sortColumn: 'email', sortOrder: ${sortColumn == 'email' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">E-Mail Address</a></div>
    <div class="col font-weight-bold"><a href="#" onclick="searchButtonClicked({max: '${max}', offset: '${offset}', sortColumn: 'firstName', sortOrder: ${sortColumn == 'firstName' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">First Name</a></div>
    <div class="col font-weight-bold"><a href="#" onclick="searchButtonClicked({max: '${max}', offset: '${offset}', sortColumn: 'lastName', sortOrder: ${sortColumn == 'lastName' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Last Name</a></div>
    <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border"></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${members == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search criteria to recall membership data</div>
        </div>
    </g:if>

    <g:if test="${members?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No members found.</div>
        </div>
    </g:if>

    <g:each in="${members}" var="member" status="i">
        <div id="member-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'showMemberDetails', params:[cardNumber: member.cardNumber])}';">
            <div id="member-result-${i+1}-card-number" class="col-2 text-truncate">${member.cardNumber}</div>
            <div id="member-result-${i+1}-email" class="col-3">${member.email}</div>
            <div id="member-result-${i+1}-first-name" class="col">${member.firstName}</div>
            <div id="member-result-${i+1}-last-name" class="col">${member.lastName}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchMembers" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 20}" params="[searchTerm: searchTerm,
                                                                                                                                                                sortColumn: sortColumn,
                                                                                                                                                                sortOrder: sortOrder]" />
</div>