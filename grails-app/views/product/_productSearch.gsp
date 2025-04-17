<div class="modal fade header-wl" id="productSearchModal" tabindex="-1" role="dialog" aria-labelledby="productSearchModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <h3 class="product-search-header">Select product</h3>

                <div class="row product-search-filters">
                    <div class="input-group col-8">
                        <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                        <div class="input-group-append">
                            <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode', 'barcode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                        </div>
                    </div>

                    <div class="col-2">
                        <asset:image src="search.png" id="productSearchButton" name="productSearchButton" data-url="${createLink(controller:'product', action:'search', params: [filterHospitalityAndNoStockSalesAllowed: filterHospitalityAndNoStockSalesAllowed])}" class="wl-search-button" />
                    </div>
                </div>

                <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                    <div class="col-2 font-weight-bold">Item Code</div>
                    <div class="col-4 font-weight-bold">Description</div>
                    <div class="col-2 font-weight-bold">SKU</div>
                    <div class="col-2 font-weight-bold">Category</div>
                    <div class="col-2 font-weight-bold">Actions</div>
                </div>

                <div id="productSearchResults" class="align-content-center">
                    <g:render template="/product/addProductSearchResults" model="${[ products: products]}" />
                </div>

                <div class="row mt-3">
                    <span class="col-12 text-right">
                        <button class="btn btn-danger" data-dismiss="modal">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<asset:javascript src="productSearch.js" />