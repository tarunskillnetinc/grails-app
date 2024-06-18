<!-- Modal -->
<div class="modal fade" id="promotionStoreSearchModal" tabindex="-1" role="dialog" aria-labelledby="promotionStoreSearchModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg  modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="promotionStoreSearchModalLabel">Select Stores</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">


                <div id="filters" class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <div class="form-group row">
                                <label for="storeNumberFilter" class="col-2 col-form-label-sm text-right">Store Number</label>
                                <div class="col-4">
                                    <g:field id="storeNumberFilter" type="number" min="0" max="2147483647" name="storeNumberFilter" value="${storeNumberFilter}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                                </div>

                                <label for="storeNameFilter" class="col-2 col-form-label-sm text-right">Store Name</label>
                                <div class="col-4">
                                    <g:textField id="storeNameFilter" name="storeNameFilter" value="${storeNameFilter}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row mb-0 mt-4">
                                <div class="col-6 text-right">
                                    <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getAllStores();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>


                <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                    <div class="col-4 font-weight-bold">Store Number</div>
                    <div class="col-4 font-weight-bold">Store Name</div>
                </div>
                <!-- Store List to Select From -->
                <div id="store-selection-list">
                    <!-- Store list will be loaded here via AJAX -->
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="tempSelectedStoreIds = []" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" onclick="addSelectedStores()">Add Selected Stores</button>
            </div>
        </div>
    </div>
</div>