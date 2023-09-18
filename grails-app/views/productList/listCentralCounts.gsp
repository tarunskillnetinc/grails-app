<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Central Count Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#centralCountSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function resetForm() {
                document.getElementById('centralCountSearchTerm').value = null;
                document.getElementById('centralCountSearchBy').value = 'everything';
            }

            function search() {
                var URL = "${createLink(controller: 'productList', action: 'ajaxGetCentralCounts')}";
                var searchTerm = $('#centralCountSearchTerm').val();
                var searchBy = $('#centralCountSearchBy').val();

                alert(searchBy)

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
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Central Counts</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Central Count Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
%{--                --------}%
                <div class="col-10 mt-4">
                    <div class="col-6">
                        <div class="card bg-light border-wl">
                            <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                                <div class="row">
                                    <div id="filters-header" class="col-10">Filters</div>
                                    <div class="col-2 text-right">
                                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <div class="card-body collapse show" id="filterCollapse">
                                <div class="form-group row">
                                    <label for="centralCountSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                    <div class="col-10 input-group">
                                        <g:textField id="centralCountSearchTerm" name="centralCountSearchTerm" maxlength="100" class="form-control" aria-describedby="select-addon2" />

                                        <div class="input-group-append">
                                            <g:select id="centralCountSearchBy" name="centralCountSearchBy" from="${['everything',
                                                                                                                     'ID',
                                                                                                                     'Store ID',
                                                                                                                     'Description',
                                                                                                                     'Status',
                                                                                                                     'Start Date',
                                                                                                                     'End Date',
                                                                                                                     'Current Owner']}" value="everything" valueMessagePrefix="CentralCountSearchBy" class="form-control select-border" style="z-index: 0;" />
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div class="col-sm-8 col-xl-6 offset-sm-4 offset-xl-6 text-right">
                                        <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                        <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
%{--                <div class="input-group offset-2 col-8">--}%
%{--                    <g:textField id="centralCountSearchTerm" name="centralCountSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />--}%

%{--                    <div class="input-group-append">--}%
%{--                        <asset:image src="search.png" id="centralCountSearchButton" name="centralCountSearchButton" onclick="search()" class="wl-search-button" />--}%
%{--                    </div>--}%
%{--                </div>--}%

                <div class="col-2 px-0 text-right">
                    <g:link elementId="add-new-central-count" controller="productList" action="addCentralCount" class="btn btn-wl">Add New Central Count</g:link>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold">ID</div>
                <div class="col-1 font-weight-bold">Store ID</div>
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