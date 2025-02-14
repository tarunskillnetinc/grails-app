<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Group Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type="text/javascript">
            $(document).ready(function () {

                $('#startDateFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#endDateFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#productGroupSearchTerm').on('keyup', function (event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function getProductGroups(sortParams) {
                $('#results-container').html("");
                $("#loading-indicator").show();

                var filterParams = {};

                $("#filtersForm input").each(function() {
                    filterParams[$(this).attr("name")] = $(this).val();
                }).get();

                $("#filtersForm select").each(function() {
                    filterParams[$(this).attr("name")] = $(this).find(":selected").val();
                }).get();

                Object.assign(filterParams, sortParams);

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: "${createLink(controller: 'productGroup', action: 'ajaxGetProductGroups')}",
                    data: filterParams,
                    success: function(resp) {
                        $('#search-results').html(resp);
                    },
                    error: function () {
                        $("#messages-container").html(
                            '<div class="alert alert-danger" role="alert">Failed to load page. Please try again later.</div>'
                        );
                    }
                });
            }

            function resetForm() {
                $("#productGroupSearchTerm").val("");
                $("#productGroupSearchBy").val("everything");
                $("#startDateFilter").val("");
                $("#endDateFilter").val("");
                $("#statusFilter").val("");
                getProductGroups();
            }

            function restrictInput(event) {
                // Allow Backspace, Delete, Tab, Escape, and Arrow keys
                const allowedKeys = [8, 9, 27, 37, 39, 46];
                if (allowedKeys.includes(event.keyCode) || event.ctrlKey || event.metaKey) {
                    return true; // Allow these keys
                }
                return false; // Block all other key inputs
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
                    <g:link elementId="add-new-productGroup-btn" controller="productGroup" action="addEdit"
                            class="btn btn-wl">Add New Product Group</g:link>
                </div>
            </div>

            <div id="messages-container">
                <g:if test="${flash.message}">
                    <div id="alerts-success-container-message" class="alert alert-success" role="alert">${flash.message}</div>
                </g:if>
                <g:if test="${flash.error}">
                    <div id="alerts-success-container-message" class="alert alert-danger" role="alert">${flash.error}</div>
                </g:if>
            </div>

            <div class="row mt-4">
                <div class="col-7">
                    <div class="card bg-light border-wl" >
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
                            <g:form name="filtersForm" id="filtersForm" >
                                <div class="form-group row">
                                    <label for="productGroupSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                    <div class="col-10 input-group">
                                        <g:textField id="productGroupSearchTerm" name="productGroupSearchTerm" maxlength="100" value="${session.SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />

                                        <div class="input-group-append">
                                            <g:select id="productGroupSearchBy" name="productGroupSearchBy"
                                                      from="${['everything', 'name', 'productGroupId']}" value="everything"
                                                      value="${session.SEARCH_BY ? session.SEARCH_BY : 'everything'}"
                                                      valueMessagePrefix="ProductGroupSearchBy"
                                                      class="form-control select-border" style="z-index: 0;" />
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                    <div class="col-4">
                                        <g:textField name="startDate" onkeydown="return restrictInput(event)" id="startDateFilter" class="form-control bottom-border" value="${session.START_DATE}" autocomplete="off"/>
                                    </div>

                                    <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                    <div class="col-4">
                                        <g:textField name="endDate" onkeydown="return restrictInput(event)" id="endDateFilter" class="form-control bottom-border" value="${session.END_DATE}" autocomplete="off"/>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="status" class="col-2 col-form-label-sm text-right">Status</label>
                                    <div class="col-4">
                                        <g:select name="status"
                                                  id="statusFilter"
                                                  from="${['ACTIVE', 'INACTIVE']}"
                                                  value="${session.STATUS}"
                                                  valueMessagePrefix="PromotionStatus" noSelection="['': '']"
                                                  class="form-control select-border"/>
                                    </div>
                                    <div class="col-6  text-right">
                                        <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                        <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getProductGroups()">Search</button>
                                    </div>
                                </div>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>


            <div id="search-results" class="align-content-center">
                <g:render template="productGroupSearchResults" model="[productGroups: productGroups]"/>
            </div>
        </section>
    </body>
</html>