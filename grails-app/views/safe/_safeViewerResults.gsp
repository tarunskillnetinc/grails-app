<script>
    var successMessage = "${successMessage}";
    var errorMessage = "${errorMessage}";

    $(document).ready(function () {

        //Handle ajax success and error messages
        if (successMessage != null && successMessage !== '') {
            displayMessage('success', successMessage);
        } else if (errorMessage != null && errorMessage !== '') {
            displayMessage('error', errorMessage);
        }

    });

</script>

<style>
    /* Button styling (can be customized more if needed) */
    button.btn {
        margin-left: 5px;
    }
</style>


<div class="row mt-5 ml-0 mr-0 pb-2 table-wl bottom-border">
    <div class="col-1 font-weight-bold text-center d-flex align-items-center justify-content-center"><div>ID</div></div>
    <div class="col-4 font-weight-bold text-center d-flex align-items-center justify-content-center"><div>Description</div></div>
    <div class="col-2 font-weight-bold text-center d-flex align-items-center justify-content-center"><div>Type</div></div>
    <div class="col-2 font-weight-bold text-center d-flex align-items-center justify-content-center"><div>Status</div></div>
    <div class="col-3 text-right font-weight-bold d-flex align-items-center justify-content-end"><div></div></div>
</div>

<div id="search-results">
    <g:if test="${safes == null || safes?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0 d-flex align-items-center justify-content-center"><div>No safes found.</div></div>
        </div>
    </g:if>
    <g:else>
        <g:each in="${safes}" var="safe" status="i">
            <div id="safe-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable pointer"
                 onclick="handleSafeRowClickEvent(event, '${createLink(action:'addSafe', params: [id: safe?.id, edit: true])}');">
                <div id="safe-id-${i + 1}" class="col-1 text-center d-flex align-items-center justify-content-center"><div>${safe?.id}</div></div>
                <div id="safe-description-${i + 1}" class="col-4 text-center d-flex align-items-center justify-content-center"><div>${safe?.description}</div></div>
                <div id="safe-type-${i + 1}" class="col-2 text-center d-flex align-items-center justify-content-center">
                    <div>
                        <g:if test="${safe?.type == uk.co.wonderlane.wlpos.enums.SafeType.MANUAL}">Manual safe</g:if>
                        <g:elseif test="${safe?.type == uk.co.wonderlane.wlpos.enums.SafeType.SMART}">Smart safe</g:elseif>
                        <g:else>${safe?.type?.toString()?.toLowerCase()?.capitalize()}</g:else>
                    </div>
                </div>
                <div id="safe-active-${i + 1}" class="col-2 text-center d-flex align-items-center justify-content-center"><div>${safe?.active ? 'Active' : 'Inactive'}</div></div>
                <div id="safe-action-${i + 1}" class="col-3 text-center d-flex align-items-center justify-content-end">
                    <div class="d-flex justify-content-end w-100">
                        <div class="text-right">
                            <g:if test="${!safe?.primary}">
                                <button class="btn btn-success p-1 mb-1"
                                        style="min-width: 80px; font-size: 0.9rem;"
                                        onclick="event.stopPropagation();
                                        updatePrimarySafe(${safe?.id}, '${safe?.description?.encodeAsHTML()}')"
                                    ${!safe?.active ? 'disabled' : ''}>
                                    Make Primary
                                </button>
                            </g:if>
                            <g:else>
                                <span class="mt-1"
                                      style="display: inline-block; width: 100%; text-align: right; font-size: inherit; font-weight: inherit;">Primary Safe</span>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>
        </g:each>
    </g:else>
</div>