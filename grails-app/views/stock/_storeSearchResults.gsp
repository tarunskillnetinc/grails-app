<script>

$(document).ready(function () {
                $('#saveButton').on('click', function () {
                    // Collect checked store numbers

                    let selectedStore = $("input[name='selectedStocks']:checked")
                                      .map(function () {
                                          return this.value;
                                      }).get();

                       if (!selectedStore) {
                            alert('Please select a store.');
                            return;
                        }
                    $.ajax({
                        url: "${createLink(controller: 'stock', action: 'ajaxResetInventoryTemplate')}",
                        type: "GET",
                        traditional: true,
                        data: {
                                    selectedStore: selectedStore
                        },
                        success: function (response) {
                            console.log("AJAX response:", response);
                            $('#results-container').hide();
                            $('#resetInventoryContainer').html(response).show();
                        },
                        error: function () {
                            alert('Failed to load template');
                        }
                    });
                });
            });
             $(document).ready(function () {
                    $('input[name="selectedStocks"]').on('change', function () {
                        if ($(this).is(':checked')) {
                            $('input[name="selectedStocks"]').not(this).prop('checked', false);
                        }
                    });
                });
</script>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a id="product-list-store-number" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeNumber',
        order: ${sortParams?.sort == 'storeNumber' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Number</a></div>
    <div class="col-4 font-weight-bold"><a id="product-list-store-name" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeName',
        order: ${sortParams?.sort == 'storeName' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Name</a></div>
    <div class="col-2 font-weight-bold"><a id="product-list-store-type" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeType',
        order: ${sortParams?.sort == 'storeType' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Select Store</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!stores || stores?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No stores found.</div>
    </g:if>

    <g:each in="${stores}" var="store" status="i">
        <div id="store-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" ">
            <div id="store-number-${i + 1}" class="col-2 my-auto">${store.config.storeNumber}</div>
            <div id="store-name-${i + 1}" class="col-2 my-auto">${store.config.storeName}</div>
            <div class="col-3 my-auto text-right">
                 <g:checkBox name="selectedStocks" id="selectedStock${i + 1}"
                             value="${store.config.storeNumber}-${store.config.storeName}"
                             class="form-check-input wl-checkbox" checked="false"/>
            </div>
        </div>
    </g:each>
    <div class="col-12 text-right">
        <button id="cancelButton" type="button" class="btn btn-danger text-right" onclick="">Cancel</button>
        <button id="saveButton" type="button" class="btn btn-wl text-right" onclick="">Save</button>
    </div>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="store" action="ajaxGetStores" total="${totalResults ?: 0}" update="results-container" offset="${sortParams?.offset ?: 0}" max="${sortParams?.max ?: 50}" params="[sort: sortParams?.sort, order: sortParams?.order, storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter]" />
</div>