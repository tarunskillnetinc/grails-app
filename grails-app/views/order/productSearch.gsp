<div class="modal-header">
    <h2>Add Product</h2>
</div>


<div class="row mt-4 ml-0 mr-0">
    <div class="input-group offset-2 col-8">
        <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" value="${session.PRODUCT_SEARCH_TERM}" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

        <div class="input-group-append">
            <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode', 'barcode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
            <asset:image src="search.png" id="userSearchButton" name="userSearchButton" onclick="searchProduct()" class="wl-search-button" />
        </div>
    </div>
</div>

<div id="product-search-results-container" class="align-content-center">
    <g:render template="productSearchResults" />
</div>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
</div>