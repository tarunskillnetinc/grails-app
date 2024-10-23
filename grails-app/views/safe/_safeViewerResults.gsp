<script>
    var successMessage = "${successMessage}";
    var errorMessage = "${errorMessage}";

    $(document).ready(function () {

        //Handle ajax success and error messages
        if(successMessage != null && successMessage !== ''){
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

<!-- Move the Primary Safe dropdown to the middle -->
<div class="row justify-content-center mt-5">
    <div class="col-6">
        <div id="primarySafeForm" class="text-center">
            <label for="primarySafe" class="mr-3 primary-safe-label">Primary Safe</label>
            <g:select id="primarySafeSelect"
                      name="primarySafeSelect"
                      from="${safeDescriptions}"
                      optionKey="key"
                      optionValue="value"
                      value="${primaryDescription ? safeDescriptions?.find { it.value == primaryDescription }?.key : ''}"
                      class="form-control d-inline-block primary-safe-select"
                      style="width: auto; min-width: 200px;"
                      onchange="updatePrimarySafe(this.value)"/>
        </div>
    </div>
</div>


<div class="row mt-5 ml-0 mr-0 pb-2 table-wl bottom-border">
    <div class="col-6 font-weight-bold">Description</div>
    <div class="col-4 font-weight-bold">Type</div>
    <div class="col-2 font-weight-bold">Active</div>
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
                <div id="segment-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable"
                     onclick="document.location.href = '${createLink(action:'segmentDetails', params: [id: safe?.id, edit: true])}';">
                    <div id="segment-id-${i + 1}" class="col-6">${safe?.description}</div>
                    <div id="segment-name-${i + 1}" class="col-4">${safe?.type}</div>
                    <div id="segment-description-${i + 1}" class="col-2">${safe?.active}</div>
                </div>
            </g:each>
        </g:else>
    </g:else>

</div>