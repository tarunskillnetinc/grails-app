<div class="modal fade" id="promotionTagSearchModal" tabindex="-1" role="dialog" aria-labelledby="tagSearchModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <g:hiddenField name="tagModal-currentPromotionType" value=""/>
                <h3 class="tag-search-header">Select Product Group</h3>

                <div class="row mt-4 ml-0 mr-0">
                    <div class="input-group offset-2 col-8">
                        <g:textField name="tagSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                        <div class="input-group-append">
                            <asset:image src="search.png" id="tagSearchButton" name="tagSearchButton" onclick="tagSearchButtonClicked()" class="wl-search-button" />
                        </div>
                    </div>
                </div>

                <div class="row mt-5 mb-2 ml-0 mr-0">
                    <div class="col-11 font-weight-bold">Description</div>
                </div>

                <div id="tag-search-results" class="align-content-center">

                </div>

                <div class="row mt-3">
                    <span class="col-12 text-right">
                        <button id="tag-cancel" class="btn btn-danger" data-dismiss="modal" style="margin-right: 13px;">Cancel</button>
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $('#tagSearchTerm').on('keyup', function(event) {
            if (event.key === 'Enter') {
                $('#offset').val(0);
                tagSearch();
            }
        });
    });

    function tagSearchButtonClicked() {
        $('#offset').val(0);
        tagSearch();
    }

    function tagSearch() {
        var URL = "${createLink(controller: 'promotion', action: 'ajaxSearchTags')}";
        var searchTerm = $('#tagSearchTerm').val();

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
                $('#tagSearchTerm').data('prev',$('#tagSearchTerm').val())
                $('#tagSearchBy').data('prev', $('#tagSearchBy').val())
            }
        })
    }
</script>