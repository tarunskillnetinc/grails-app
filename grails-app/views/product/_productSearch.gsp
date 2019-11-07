<div class="modal fade" id="productSearchModal" tabindex="-1" role="dialog" aria-labelledby="productSearchModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <h3 class="product-search-header">Select product</h3>

                <div class="row product-search-filters">
                    <div class="input-group col-8">
                        <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                        <div class="input-group-append">
                            <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                        </div>
                    </div>

                    <div class="col-2">
                        <asset:image src="search.png" id="productSearchButton" name="productSearchButton" data-url="${createLink(controller:'product', action:'search')}" class="product-search-button" />
                    </div>
                </div>

                <table class="table product-search-table" style="margin-top: 30px;">
                    <thead>
                    <tr>
                        <th scope="col">Item Code</th>
                        <th scope="col">Description</th>
                        <th scope="col">Category</th>
                        <th scope="col">Cost Price</th>
                        <th scope="col">Retail Price</th>
                        <th scope="col">VAT</th>
                        <th scope="col">&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody id="productSearchResults">
                        <g:render template="/product/productSearchResults" model="${[ products: products ]}" />
                    </tbody>
                </table>

                <div class="row">
                    <span class="col-12 text-right">
                        <button class="btn btn-danger" data-dismiss="modal" style="margin-right: 13px;">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<asset:javascript src="productSearch.js" />