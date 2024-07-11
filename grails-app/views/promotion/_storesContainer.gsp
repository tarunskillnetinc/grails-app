<div id="stores" class="collapsible-products row mt-3">
    <h2 class="col-1 mr-2">Stores</h2>
    <div id="StoresSection" class="promotion-products-container col-10 offset-1">
        <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
            <div class="col-2 font-weight-bold">Store Number</div>
            <div class="col-2 font-weight-bold">Store Name</div>
        </div>
        <div id="search-results" class="pre-scrollable store-search-results">
            <g:render template="storeList"/>
        </div>
        <div class="row justify-content-end mb-3 mr-3">
            <button id="add-store-btn" type="button" class="btn btn-wl mr-1" data-toggle="modal" data-target="#promotionStoreSearchModal" onclick="getAllStores()">Add Stores</button>
            <button id="add-all-stores-btn" type="button" class="btn btn-wl mr-1" onclick="addAllStores()">Add All Stores</button>
            <button id="remove-all-stores-btn" type="button" class="btn btn-wl mr-1" onclick="removeAllStores()">Remove All Stores</button>
        </div>
    </div>
</div>