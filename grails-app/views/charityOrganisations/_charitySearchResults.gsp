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
    <div class="col-3 font-weight-bold">
        <a id="charity-header-name" href="#" onclick="searchCharity({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'organisationName', sortOrder: ${sortParams?.sortColumn == 'organisationName' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">
            Charity / Group Description
        </a>
    </div>
    <div class="col-3 font-weight-bold">
        <a id="charity-header-type" href="#" onclick="searchCharity({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'type', sortOrder: ${sortParams?.sortColumn == 'type' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">
            Organisation Type
        </a>
    </div>
    <div class="col-3 font-weight-bold">
        <a id="charity-header-member-number" href="#" onclick="searchCharity({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'memberNumber', sortOrder: ${sortParams?.sortColumn == 'memberNumber' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">
            Charity Member Number
        </a>
    </div>
    <div class="col-2 font-weight-bold">
        <a id="charity-header-status" href="#" onclick="searchCharity({ max: ${sortParams?.max}, offset: ${sortParams?.offset}, sortColumn: 'active', sortOrder: ${sortParams?.sortColumn == 'active' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">
            Status
        </a>
    </div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!charities || charities?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No charities found.</div>
    </g:if>

    <g:each in="${charities}" var="charity" status="i">
        <div id="charity-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="editCharity(${charity.id});">
            <div id="charity-result-${i+1}-name" class="col-3 my-auto" style='word-break: break-all; word-wrap: break-word;'>${charity.organisationName}</div>
            <div id="charity-result-${i+1}-type" class="col-3 my-auto" style='word-break: break-all; word-wrap: break-word;'>${charity.type}</div>
            <div id="charity-result-${i+1}-member-number" class="col-3 my-auto" style='word-break: break-all; word-wrap: break-word;'>${charity.memberNumber}</div>
            <div id="charity-result-${i+1}-active" class="col-2 my-auto" style='word-break: break-all; word-wrap: break-word;'>
                <g:if test="${charity.active == true}">
                    Active
                </g:if>
                <g:else>
                    Inactive
                </g:else>
            </div>
            <div id="charity-result-${i+1}-delete-toggle" class="col-1 my-auto">
                <div class="button-container d-flex justify-content-end align-items-center">
                    <g:if test="${charity.active != true}">
                        <button id="toggle-charity-deleted-button-${i+1}" class="btn btn-wl p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                onclick="event.stopPropagation(); toggleCharityDeleted(${charity.id}, ${charity.active}, {offset: ${offset ?: 0}, sortColumn: '${sortParams?.sortColumn}'})">
                            Reinstate
                        </button>
                    </g:if>
                    <g:else>
                        <button id="toggle-charity-deleted-button-${i+1}" class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                onclick="event.stopPropagation(); toggleCharityDeleted(${charity.id}, ${charity.active},{offset: 0, sortColumn: '${sortParams?.sortColumn}'})">
                            Delete
                        </button>
                    </g:else>
                </div>
            </div>
        </div>
    </g:each>
</div>


<g:if test="${totalCount > 0}">
    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxGetSearchCharity" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[
                organisationTypeTerm: params.organisationTypeTerm,
                charityMemberNumberTerm: params.charityMemberNumberTerm, charityGroupDescriptionTerm: params.charityGroupDescriptionTerm, includeDeletedCharitiesTerm: params.includeDeletedCharitiesTerm,
                sortColumn: sortParams?.sortColumn,  sortParams: sortParams]" />
    </div>
</g:if>



