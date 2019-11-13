<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#productSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        $('#offset').val(0);
                        search();
                    }
                });
            });

            function searchButtonClicked() {
                $('#offset').val(0);
                search();
            }

            function search() {
                var URL = "${createLink(controller: 'product', action: 'maintenanceSearch')}";
                var searchTerm = $('#productSearchTerm').val();
                var searchBy = $('#productSearchBy').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
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
                })
            }
        </script>
    </head>

    <body>
        <g:render template="/nav/epos" model="[active: 'product']" />

        <g:if test="${flash.message}">
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="row header-wl">
                <h2 class="mx-auto">Product Maintenance</h2>
            </div>

            <div class="row mt-4 ml-0 mr-0">
                <div class="input-group offset-2 col-8">
                    <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                    <div class="input-group-append">
                        <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                        <asset:image src="search.png" id="productSearchButton" name="productSearchButton" onclick="searchButtonClicked()" class="wl-search-button" />
                    </div>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="product" action="add" class="btn btn-wl">Add New Product</g:link>
                </div>
            </div>

            <div class="row mt-5 mb-2 ml-0 mr-0">
                <div class="col-2 font-weight-bold">Item Code</div>
                <div class="col-6 font-weight-bold">Description</div>
                <div class="col-2 font-weight-bold">Unit Size</div>
                <div class="col-2 font-weight-bold">Category</div>
            </div>

            <div id="search-results" class="align-content-center">

            </div>
        </section>
    </body>
</html>