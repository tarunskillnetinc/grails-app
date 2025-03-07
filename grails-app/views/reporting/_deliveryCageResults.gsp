<script>
    $(document).ready(function () {
        $('label[for="descriptionFilter"]').text("Cage Barcode")
        $('#cageDeliveryFilter').show()
        $('#directDeliveryFilter').hide()
    })

    function loadDeliveryCage(cageId) {
        $.ajax({
            url: '${createLink(action:'deliveryCage')}',
            method: 'GET',
            data: {
                cageId: cageId,
                storeId: ${storeId},
                supplierId: ${supplierId},
                startDate: '${startDate?.toString("dd/MM/yyyy")}',
                endDate: '${endDate?.toString("dd/MM/yyyy")}',
                descriptionFilter: '${descriptionFilter}'
            },
            success: function(response) {
                $("#results-container").html(response);
            },
            error: function(xhr, status, error) {
                console.error("An error occurred: " + error);
            }
        });
    }
</script>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "cageBarcode" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="cageBarcode" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'cageBarcode',
            sortOrder: ${sortParams?.sortColumn == 'cageBarcode' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Cage Barcode</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "processingDate" }?.enabled}">
        <div class="col-6 font-weight-bold"><a id="processingDate" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'processingDate',
            sortOrder: ${sortParams?.sortColumn == 'processingDate' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">Processing Date</a></div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "cases" }?.enabled}">
        <div class="col-2 font-weight-bold"><a id="cases" href="#" onclick="getReportData({
            max: ${sortParams?.max},
            offset: ${sortParams?.offset},
            sortColumn: 'cases',
            sortOrder: ${sortParams?.sortColumn == 'cases' ? sortParams?.sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
        });">${retailer?.config?.retailerTerminologyConfig?.packTerm}s in the Cage</a></div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!cages || cages?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </g:if>

    <g:each in="${cages}" var="cage" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable" style="cursor: pointer;" title="Click to view"
             onclick="loadDeliveryCage(${cage.id})">
        <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "cageBarcode" }?.enabled}">
                <div id="cageBarcode-${i + 1}" class="col-2 my-auto">${cage?.uniqueIdentifier}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "processingDate" }?.enabled}">
                <div id="processingDate-${i + 1}" class="col-6 my-auto">${cage?.effectiveDate}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "cases" }?.enabled}">
                <div id="cases-${i + 1}" class="col-2 my-auto">${cage?.totalCases}</div>
            </g:if>
        </div>
    </g:each>
</div>

<g:if test="${totalResults > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${sortParams?.offset ? sortParams?.offset + 1 : 1} - ${((sortParams?.offset ?: 0) + (cages?.size() ?: 0))} of ${totalResults} result${totalResults > 1 ? 's' : ''}</div>

        <div class="mt-3"><g:paginateReport totalResults="${totalResults}" offset="${sortParams?.offset}"
                                            max="${sortParams?.max}" sortColumn="${sortParams?.sortColumn}"
                                            sortOrder="${sortParams?.sortOrder}"/></div>
    </div>
</g:if>