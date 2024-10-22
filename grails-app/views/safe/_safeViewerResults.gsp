<style>
/* Shift details border styling */
.shift-card-body {
    /*background-color: #fff;*/
    border: 2px solid black;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.shift-card-header {
    border: 2px solid black;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Button styling (can be customized more if needed) */
button.btn {
    margin-left: 5px;
}

.card {
    margin-top: 5px;
}

</style>


<div class="row mt-5 ml-0 mr-0 pb-2 table-wl bottom-border">
    <div class="col-4 font-weight-bold">Description</div>

    <div class="col-4 font-weight-bold">Type</div>

    <div class="col-2 font-weight-bold">Active</div>
</div>

<div id="search-results">
    <g:if test="${safes == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search term to get safes</div>
        </div>
    </g:if>

    <g:if test="${safes?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${safes}" var="safe" status="i">
        <div id="segment-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable"
             title="Click to edit."
             onclick="document.location.href = '${createLink(action:'segmentDetails', params: [id: safe?.id, edit: true])}';">
            <div id="segment-id-${i + 1}" class="col-4">${safe?.description}</div>

            <div id="segment-name-${i + 1}" class="col-4">${safe?.type}</div>

            <div id="segment-description-${i + 1}" class="col-2">${safe?.active}</div>
        </div>
    </g:each>

</div>