<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Central Count Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#centralCountSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function search() {
                var URL = "${createLink(controller: 'productList', action: 'ajaxGetCentralCounts')}";
                var searchTerm = $('#centralCountSearchTermSearchTerm').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm },
                    success: function(resp) {
                        $('#search-results').html(resp);
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
                            <li class="breadcrumb-item active" aria-current="page">Central Counts</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl">
                <h2 class="mx-auto">Central Count Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
                <div class="input-group offset-2 col-8">
                    <g:textField id="centralCountSearchTerm" name="centralCountSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                    <div class="input-group-append">
                        <asset:image src="search.png" id="centralCountSearchButton" name="centralCountSearchButton" onclick="search()" class="wl-search-button" />
                    </div>
                </div>

                <div class="col-2 px-0 text-right">
                    <g:link controller="productList" action="addCentralCount" class="btn btn-wl">Add New Central Count</g:link>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold">ID</div>
                <div class="col font-weight-bold">Description</div>
                <div class="col font-weight-bold">Status</div>
                <div class="col font-weight-bold">Start Date</div>
                <div class="col font-weight-bold">End Date</div>
                <div class="col font-weight-bold">Current Owner</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="centralCountSearchResults" model="[productLists: productLists]" />
            </div>
        </section>
    </body>
</html>