<div class="modal fade" id="categoryAddProductsModal" tabindex="-1" aria-labelledby="categoryAddProductsModalTitle"
     aria-hidden="true" role="dialog">
    <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <g:hiddenField name="categoryModal-currentPromotionType" value=""/>
                <h3 class="category-search-header">Select category</h3>

                <div class="row mt-4 ml-0 mr-0">
                    <div class="input-group offset-2 col-8">
                        <g:textField name="categorySearchTerm" maxlength="100" class="form-control"
                                     placeholder="Enter a search term." aria-describedby="select-addon2"/>

                        <div class="input-group-append">
                            <g:select name="categorySearchBy" from="${['description', 'categoryCode']}"
                                      value="everything" valueMessagePrefix="CategorySearchBy"
                                      class="form-control select-border" style="z-index: 0;"/>
                            <asset:image src="search.png" id="categorySearchButton" name="categorySearchButton"
                                         onclick="categorySearchButtonClicked()" class="wl-search-button"/>
                        </div>
                    </div>
                </div>

                <div class="row mt-5 mb-2 ml-0 mr-0">
                    <div class="col-2 font-weight-bold">Category Code</div>

                    <div class="col-9 font-weight-bold">Description</div>
                </div>

                <div id="category-search-results" class="align-content-center">

                </div>

                <div class="row mt-3">
                    <span class="col-12 text-right">
                        <button id="category-cancel" class="btn btn-danger" data-dismiss="modal"
                                style="margin-right: 13px;">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $('#categorySearchTerm').on('keyup', function (event) {
            if (event.key === 'Enter') {
                $('#offset').val(0);
                categorySearch();
            }
        });
    });

    function categorySearchButtonClicked() {
        $('#offset').val(0);
        categorySearch();
    }

    function categorySearch() {
        var URL = "${createLink(controller: 'productGroup', action: 'ajaxSearchCategories')}";
        var searchTerm = $('#categorySearchTerm').val();
        var searchBy = $('#categorySearchBy').val();

        $('#category-search-results').html("<div class=\"d-flex justify-content-center\">\n" +
            "  <div class=\"spinner-border\" role=\"status\">\n" +
            "    <span class=\"sr-only\">Loading...</span>\n" +
            "  </div>\n" +
            "</div>");

        $.ajax({
            url: URL,
            data: {searchTerm: searchTerm, searchBy: searchBy},
            success: function (resp) {
                $('#category-search-results').html(resp);
                $('#categorySearchTerm').data('prev', $('#categorySearchTerm').val())
                $('#categorySearchBy').data('prev', $('#categorySearchBy').val())
            }
        })
    }
</script>