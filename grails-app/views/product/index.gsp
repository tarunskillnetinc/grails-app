<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#productSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked();
                    }
                });

                var existingSearchTerm = $('#productSearchTerm').val();
                if (existingSearchTerm != null && existingSearchTerm !== "") {
                    searchButtonClicked();
                }
            });

            function searchButtonClicked() {
                $('#offset').val(0);
                search();
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'maintenanceSearch')}";
                var searchTerm = $('#productSearchTerm').val();
                var searchBy = $('#productSearchBy').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm, searchBy: searchBy },
                    success: function(resp) {
                        $('#search-results').html(resp);
                        $('#productSearchTerm').data('prev',$('#productSearchTerm').val())
                        $('#productSearchBy').data('prev', $('#productSearchBy').val())
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
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <h2 class="mx-auto">Product Search</h2>
            </div>

            <div class="row mt-4 ml-0 mr-0">
                <div class="input-group offset-2 col-8">
                    <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" value="${session.PRODUCT_SEARCH_TERM}" />

                    <div class="input-group-append">
                        <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                        <asset:image src="search.png" id="productSearchButton" name="productSearchButton" onclick="searchButtonClicked()" class="wl-search-button" />
                    </div>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="product" action="add" class="btn btn-wl">Add New Product</g:link>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-2 font-weight-bold">Item Code</div>
                <div class="col-6 font-weight-bold">Description</div>
                <div class="col-2 font-weight-bold">Unit Size</div>
                <div class="col-2 font-weight-bold">Category</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="maintenanceSearchResults" />
            </div>
        </section>
    </body>
</html>