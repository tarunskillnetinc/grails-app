<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#searchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked2();
                    }
                });
            });

            function searchButtonClicked2() {
                $('#offset').val(0);
                search();
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'rangesSearch')}";

                var searchTerm = $('#searchTerm').val();
                var category = $('#category').val();
                var tag = $('#tag').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm, category: category, tag: tag },
                    success: function(resp) {
                        $('#search-results').html(resp);
                    }
                });
            }

            function saveRanges() {
                var saveButton = $("save-changes-button");
                saveButton.prop("disabled", true);

                var data = { };

                var checkedBoxes = $("input.selections:checked");

                checkedBoxes.each(function(i, checkbox) {
                    var ranges = $("[id^=range-" +$(checkbox).attr("id").substring(8) +"-]");

                    ranges.each(function(index, range) {
                        var id = $(range).attr("id");

                        var productId = id.substring(6, id.lastIndexOf("-"));
                        var rangeId = id.substring(id.lastIndexOf("-") + 1);

                        data["rangeProducts[" +((i * 3) + index) +"].productId"] = productId;
                        data["rangeProducts[" +((i * 3) + index) +"].rangeId"] = rangeId;
                        data["rangeProducts[" +((i * 3) + index) +"].ranged"] = $(range).prop("checked");
                    });
                });

                var url = "${createLink(controller: 'product', action: 'ajaxSaveRangeProducts')}";

                $.ajax({
                    url: url,
                    method: "POST",
                    data: data,
                    success: function(resp) {
                        $('#confirmModal').modal({ show: true });

                        checkedBoxes.each(function(i, checkbox) {
                            $(checkbox).prop("checked", false);
                        });

                        saveButton.prop("disabled", false);
                    }
                });
            }
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">Product Search</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <g:if test="${flash.message}">
            <section id="alerts-container" class="container-fluid">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Product Ranges</h2>
            </div>

            <div class="row mt-4">
                <div class="col-5">
                    <div class="card bg-light border-wl">
                        <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
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
                            <div class="form-group row">
                                <label for="searchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10">
                                    <g:textField name="searchTerm" class="form-control bottom-border" value="${searchTerm}" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="category" class="col-2 col-form-label-sm text-right">Category</label>
                                <div class="col-4">
                                    <g:categorySelect name="category" categories="${categories}" />
                                </div>

                                <label for="tag" class="col-2 col-form-label-sm text-right">Tag</label>
                                <div class="col-4">
                                    <g:select name="tag" from="${tags}" noSelection="['':'']" value="${tag}" optionValue="description" optionKey="id" class="form-control select-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-4 offset-8 text-right">
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked2()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-2 offset-5 text-right">
                    <button id="save-changes-button" class="btn btn-wl" onclick="saveRanges();">Save Changes</button>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-2 font-weight-bold">Item Code</div>
                <div class="col-6 font-weight-bold">Description</div>
                <g:each in="${ranges}" var="range">
                    <div class="col font-weight-bold text-center">${range.description}</div>
                </g:each>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="rangesSearchResults" />
            </div>
        </section>

        <!-- Confirmation modal -->
        <section id="confirm-modal" class="container-fluid">
            <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2>Success</h2>
                        </div>

                        <div class="modal-body">Product range updates saved successfully.</div>

                        <div class="modal-footer">
                            <button type="button" id="closeConfirmModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>