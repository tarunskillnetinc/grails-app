<div class="modal fade" id="promotionProductGroupSearchModal" tabindex="-1" role="dialog"
     aria-labelledby="productGroupSearchModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <g:hiddenField name="productGroupModal-currentPromotionType" value=""/>
                <h3 class="productGroup-search-header">Select Product Group</h3>

                <div class="row mt-4 ml-0 mr-0">
                    <div class="input-group offset-2 col-8">
                        <g:textField name="productGroupSearchTerm" maxlength="100" class="form-control"
                                     placeholder="Enter a search term." aria-describedby="select-addon2"/>

                        <div class="input-group-append">
                        <asset:image src="search.png" id="productGroupSearchButton" name="productGroupSearchButton"
                                         onclick="productGroupSearchButtonClicked()" class="wl-search-button"/>
                        </div>
                    </div>
                </div>

                <div class="row mt-5 mb-2 ml-0 mr-0">
                    <div class="col-11 font-weight-bold">Description</div>
                </div>

                <div id="productGroup-search-results" class="align-content-center">

                </div>

                <div class="row mt-3">
                    <span class="col-12 text-right">
                        <button id="productgroup-cancel" class="btn btn-danger" data-dismiss="modal"
                                style="margin-right: 13px;">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $('#productGroupSearchTerm').on('keyup', function (event) {
            if (event.key === 'Enter') {
                $('#offset').val(0);
                productGroupSearch();
            }
        });
    });

    function productGroupSearchButtonClicked() {
        $('#offset').val(0);
        productGroupSearch();
    }

    function productGroupSearch() {
        var URL = "${createLink(controller: 'promotion', action: 'ajaxSearchProductGroups')}";
        var searchTerm = $('#productGroupSearchTerm').val();

        $('#productGroup-search-results').html("<div class=\"d-flex justify-content-center\">\n" +
            "  <div class=\"spinner-border\" role=\"status\">\n" +
            "    <span class=\"sr-only\">Loading...</span>\n" +
            "  </div>\n" +
            "</div>");

        $.ajax({
            url: URL,
            data: { searchTerm: searchTerm },
            success: function(resp) {
                $('#productGroup-search-results').html(resp);
                $('#productGroupSearchTerm').data('prev', $('#productGroupSearchTerm').val())
                $('#productGroupSearchBy').data('prev', $('#productGroupSearchBy').val())
            }
        })
    }
</script>