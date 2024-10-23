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

    function handleRowClick(event, url) {
        // Check if the click didn't come from the button
        if (!event.target.closest('button')) {
            document.location.href = url;
        }
    }

</script>

<style>
/* Button styling (can be customized more if needed) */
button.btn {
    margin-left: 5px;
}
</style>

<div class="row mt-5 ml-0 mr-0 pb-2 table-wl bottom-border">
    <div class="col-1 font-weight-bold">ID</div>
    <div class="col-4 font-weight-bold">Description</div>
    <div class="col-2 font-weight-bold">Type</div>
    <div class="col-2 font-weight-bold">Status</div>
    <div class="col-3 text-right font-weight-bold"></div>
</div>

<div id="search-results">
    <g:if test="${safes == null || isDropdownOnly}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search term to get safes</div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${safes?.size() == 0}">
            <div class="row ml-0 mr-0 text-center">
                <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
            </div>
        </g:if>
        <g:else>
            <g:each in="${safes}" var="safe" status="i">
                <div id="safe-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable"
                     onclick="handleRowClick(event, '${createLink(action:'segmentDetails', params: [id: safe?.id, edit: true])}');">
                    <div id="safe-id-${i + 1}" class="col-1">${safe?.id}</div>
                    <div id="safe-description-${i + 1}" class="col-4">${safe?.description}</div>
                    <div id="safe-type-${i + 1}" class="col-2">${safe?.type?.toString()?.toLowerCase()?.capitalize()}</div>
                    <div id="safe-active-${i + 1}" class="col-2">${safe?.active ? 'Active' : 'In Active'}</div>
                    <div id="safe-action-${i + 1}" class="col-3">
                        <div class="d-flex flex-column align-items-start">
                            <g:if test="${!safe?.primary}">
                                <button class="btn btn-success p-1 mb-1" style="min-width: 80px; font-size: 0.9rem;"
                                        onclick="event.stopPropagation(); updatePrimarySafe(${safe?.id})"
                                        ${!safe?.active ? 'disabled' : ''}>Make Primary</button>
                            </g:if>
                            <g:else>
                                <span class="mt-1 ml-1" style="font-size: 0.9rem; display: inline-block; width: auto;">Primary Safe</span>
                            </g:else>
                        </div>
                    </div>
                </div>
            </g:each>
        </g:else>
    </g:else>

</div>