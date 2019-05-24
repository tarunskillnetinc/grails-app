<div class="modal fade" id="productSearchModal" tabindex="-1" role="dialog" aria-labelledby="productSearchModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <h3>Select product</h3>

                <div class="row">
                    <div class="col-5">
                        <g:textField id="productSearchTerm" name="productSearchTerm" class="form-control" />
                    </div>

                    <div class="col-2">
                        <g:select name="searchType" from="${['Everything']}" value="Everything" class="form-control select-border" />
                    </div>

                    <div class="col-2">
                        <button class="btn btn-success" id="productSearchButton" name="productSearchButton" data-url="${createLink(controller:'product', action:'search')}">Search</button>
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
                        <button class="btn btn-danger" data-dismiss="modal">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<asset:javascript src="productSearch.js" />