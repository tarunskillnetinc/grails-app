<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>

        <script type="text/javascript">
            function search() {
                var URL = "${createLink(controller: 'product', action: 'maintenanceSearch')}";
                var searchTerm = $('#productSearchTerm').val();
                var searchBy = $('#productSearchBy').val();
                var page = $('#page-number').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: {searchTerm: searchTerm, searchBy: searchBy, page: page},
                    success: function(resp) {
                        $('#search-results').html(resp);
                        $('#productSearchTerm').data('prev',$('#productSearchTerm').val())
                        $('#productSearchBy').data('prev', $('#productSearchBy').val())
                    }
                })
            }

            function searchButton() {
                $('#page-number').val(1);
                search();
            }

            function changePage(page) {
                $('#page-number').val(page);
                $('#productSearchTerm').val($('#productSearchTerm').data('prev'));
                $('#productSearchBy').val($('#productSearchBy').data('prev'));
                search();
            }
        </script>
    </head>

    <body>
        <g:render template="/nav/epos" model="[active: 'product']" />
        <g:if test="${flash.message}">
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>
        <section id="maintenance-search" class="container-fluid">
            <div class="row my-3">
                <h1 class="mx-auto my-0">Product Maintenance</h1>
            </div>

            <div class="row product-search-filters align-content-center">
                <div class="input-group col-7 offset-1">
                    <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                    <div class="input-group-append">
                        <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                    </div>
                </div>

                <div class="col-1">
                    <asset:image src="search.png" id="productSearchButton" name="productSearchButton" onclick="searchButton()" class="product-search-button" />
                </div>

                <div class="col-2">
                    <g:link controller="product" action="add" class="btn btn-wl">Add New Product</g:link>
                </div>
            </div>

            <div class="row mt-3 mb-2 mx-3 text-center">
                <div class="col-2 font-weight-bold my-auto">Item Code</div>
                <div class="col-6 font-weight-bold my-auto">Description</div>
                <div class="col-2 font-weight-bold my-auto">Size</div>
                <div class="col-2 font-weight-bold my-auto">Category</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="maintenanceSearchResults" model="[products: products, page: page, pageCount: pageCount, pageNumbers: pageNumbers]"/>
            </div>
        </section>
    </body>
</html>