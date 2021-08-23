<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Promotion Maintenance</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#promotionSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked();
                    }
                });

                var existingSearchTerm = $('#promotionSearchTerm').val();
                if (existingSearchTerm != null && existingSearchTerm !== "") {
                    searchButtonClicked();
                }
            });

            function searchButtonClicked() {
                $('#offset').val(0);
                search();
            }

            function search() {
                var URL = "${createLink(controller: 'promotion', action: 'promotionSearch')}";
                var searchTerm = $('#promotionSearchTerm').val();
                var searchBy = $('#promotionSearchBy').val();

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
                        $('#promotionSearchTerm').data('prev',$('#promotionSearchTerm').val());
                        $('#promotionSearchBy').data('prev', $('#promotionSearchBy').val());
                    }
                })
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
                            <li class="breadcrumb-item active" aria-current="page">Promotion Search</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="alerts-container" class="container-fluid">
            <g:if test="${flash.message}">
                <div class="alert alert-success" role="alert">${flash.message}</div>
            </g:if>
        </section>

        <section id="promo-maintenance-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Promotion Search</h2>
            </div>

            <div class="row mt-4 ml-0 mr-0 justify-content-center">
                <div class="input-group offset-2 col-8">
                    <g:textField name="promotionSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" value="${session.PROMOTION_SEARCH_TERM}" />

                    <div class="input-group-append">
                        <g:select id="promotionSearchBy" name="productSearchBy" from="${['description', 'promotionId']}" value="everything" valueMessagePrefix="PromotionSearchBy" class="form-control select-border" style="z-index: 0;" />
                        <asset:image src="search.png" name="promotionSearchButton" onclick="searchButtonClicked()" class="wl-search-button" />
                    </div>
                </div>

                <div class="col-2 text-right px-0">
                    <g:link controller="promotion" action="add" class="btn btn-wl">Add New Promotion</g:link>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-2 font-weight-bold">Promotion ID</div>
                <div class="col-5 font-weight-bold">Description</div>
                <div class="col-1 font-weight-bold">Active</div>
                <div class="col-2 font-weight-bold">Type</div>
                <div class="col-2 font-weight-bold">Discount Amount</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="promotionSearchResults" />
            </div>
        </section>
    </body>
</html>