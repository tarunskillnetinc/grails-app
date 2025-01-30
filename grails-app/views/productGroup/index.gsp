<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Group Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#productGroupSearchTerm').on('keyup', function (event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function search() {
                var URL = "${createLink(controller: 'productGroup', action: 'ajaxGetProductGroups')}";
                var searchTerm = $('#productGroupSearchTerm').val();
                var searchBy = $('#productGroupSearchBy').val();

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

            function resetForm() {
                document.getElementById('productGroupSearchTerm').value = null;
                document.getElementById('productGroupSearchBy').value = 'everything';
                search();
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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Product Group Management</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="tag-page-title" class="mx-auto">Product Group Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="add-new-productGroup-btn" controller="productGroup" action="add"
                            class="btn btn-wl">Add New Product Group</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
            </g:if>

%{--            <div class="row mt-4">--}%
%{--                <div class="col-6">--}%
%{--                    <div class="card bg-light border-wl">--}%
%{--                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">--}%
%{--                            <div class="row">--}%
%{--                                <div class="col-10">Filters</div>--}%
%{--                                <div class="col-2 text-right">--}%
%{--                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">--}%
%{--                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>--}%
%{--                                    </svg>--}%
%{--                                </div>--}%
%{--                            </div>--}%
%{--                        </div>--}%

%{--                        <div class="card-body collapse" id="filterCollapse">--}%
%{--                            <div class="form-group row">--}%
%{--                                <label for="productGroupSearchTerm"--}%
%{--                                       class="col-2 col-form-label-sm text-right">Search Term</label>--}%
%{--                                <div class="col-10 input-group">--}%
%{--                                    <g:textField id="productGroupSearchTerm" name="productGroupSearchTerm"--}%
%{--                                                 maxlength="100" class="form-control" placeholder="Enter a search term."--}%
%{--                                                 aria-describedby="select-addon2"/>--}%
%{--                                    <div class="input-group-append">--}%
%{--                                        <g:select id="productGroupSearchBy" name="productGroupSearchBy"--}%
%{--                                                  from="${['everything', 'description', 'tagId']}" value="everything"--}%
%{--                                                  valueMessagePrefix="ProductGroupSearchBy"--}%
%{--                                                  class="form-control select-border" style="z-index: 0;"/>--}%
%{--                                    </div>--}%
%{--                                </div>--}%
%{--                            </div>--}%

%{--                            <div class="form-group row">--}%
%{--                                <div class="col-4 offset-8 text-right">--}%
%{--                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>--}%
%{--                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>--}%
%{--                                </div>--}%
%{--                            </div>--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--            </div>--}%

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label for="productGroupSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10 input-group">
                                    <g:textField id="productGroupSearchTerm" name="productGroupSearchTerm" maxlength="100" value="${session.PROMOTION_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />

                                    <div class="input-group-append">
                                        <g:select id="productGroupSearchBy" name="productGroupSearchBy"
                                                  from="${['everything', 'description', 'tagId']}" value="everything"
                                                  value="everything"
                                                  valueMessagePrefix="ProductGroupSearchBy"
                                                  class="form-control select-border" style="z-index: 0;" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="validDate" class="col-2 col-form-label-sm text-right">Date Valid</label>
                                <div class="col-4">
                                    <g:textField name="validDate" onkeydown="return false" id="validDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>

                                <label for="updatedDate" class="col-2 col-form-label-sm text-right">Updated Since</label>
                                <div class="col-4">
                                    <g:textField name="updatedDate" onkeydown="return false" id="updatedDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="types" class="col-2 col-form-label-sm text-right">Type</label>
                                <div class="col-4">
                                    <g:select name="types" id="typeFilter" placeholder="Please Select" from="${types}" optionValue="friendlyName" noSelection="['': '']" class="form-control select-border"/>
                                </div>

                                <label for="supplier" class="col-2 col-form-label-sm text-right">Supplier</label>
                                <div class="col-4">
                                    <g:select name="supplier" id="supplierFilter" from="${symbolGroups}" optionValue="name" optionKey="id" noSelection="['': '']" class="form-control select-border"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="status" class="col-2 col-form-label-sm text-right">Status</label>
                                <div class="col-4">
                                    <g:select name="status" id="statusFilter" from="${['ACTIVE', 'INACTIVE']}" valueMessagePrefix="PromotionStatus" noSelection="['': '']" class="form-control select-border"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <g:if test="${sec.loggedInUserInfo(field: 'retailer.config.loyaltyRetailerConfig.isLoyaltyEnabled').toBoolean()}">
                                    <label for="loyalty" class="col-2 col-form-label-sm text-right">Loyalty only</label>
                                    <div class="col-4">
                                        <g:checkBox name="loyalty" id="loyaltyFilter" class="form-check-input loy-checkbox promo-loyalty"/>
                                    </div>
                                </g:if>
                                <div class="col-4 offset-2 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>



            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold">Product Group ID</div>
                <div class="col-2 font-weight-bold">Description</div>
                <div class="col-2 font-weight-bold">Start Date</div>
                <div class="col-2 font-weight-bold">End Date</div>
                <div class="col-2 font-weight-bold">Restriction Type</div>
                <div class="col-2 font-weight-bold">Product Count</div>
                <div class="col-1 font-weight-bold">Status</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="productGroupSearchResults" model="[productGroups: productGroups]"/>
            </div>
        </section>
    </body>
</html>