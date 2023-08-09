
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Wonderlane Category Maintenance</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="moment-with-locales.min.js"/>

    <script type="application/javascript">
        $(document).ready(function () {
            $('#categorySearchTerm').on('keyup', function(event) {
                if (event.key === 'Enter') {
                    searchButtonClicked();
                }
            });

            let existingSearchTerm = $('#categorySearchTerm').val();
            if (existingSearchTerm != null && existingSearchTerm !== "") {
                searchButtonClicked();
            }
        });

        function searchButtonClicked() {
            $('#offset').val(0);
            search();
        }

        function resetForm() {
            document.getElementById('categorySearchTerm').value = null;
            searchButtonClicked()
        }

        function search() {
            var url = "${createLink(controller: 'category', action: 'ajaxSearchCategories')}";
            var searchTerm = $('#categorySearchTerm').val();
            var searchBy = $('#categorySearchBy').val();

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { searchTerm: searchTerm, searchBy: searchBy },
                success: function(resp) {
                    $('#results-container').html(resp);
                    $('#categorySearchTerm').data('prev',$('#categorySearchTerm').val())
                }
            });
        }

        function saveColumns() {
            var url = "${createLink(controller: 'category', action: 'ajaxSaveColumns')}";

            var checkboxValues = { };

            $("#reportColumnsForm input:checkbox").each(function() {
                checkboxValues[$(this).val()] = this.checked;
            }).get();

            $.ajax({
                url: url,
                method: "POST",
                data: { reportColumns: JSON.stringify(checkboxValues), reportType: "CATEGORY_SEARCH" },
                success: function(resp) {
                    $("#columnsCollapse").collapse('hide');
                    search();
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
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Category Maintenance</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="categoryMaintenance" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto">Category Maintenance</h2>
            </div>
            <div class="col-2 text-right">
                <g:link elementId="add-new-category" controller="category" action="add" class="btn btn-wl">Add New Category</g:link>
            </div>
        </div>

        <g:if test="${flash.message}">
            <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </g:if>

        <div class="row mt-4">
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
                            <label for="categorySearchTerm" class="col-2 col-form-label-sm text-right">Description</label>
                            <div class="col-10 input-group">
                                <g:textField id="categorySearchTerm" name="categorySearchTerm" maxlength="100" value="${session.CATEGORY_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-4 offset-8 text-right">
                                <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-2 offset-4">
                <div class="card bg-light border-wl">
                    <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div class="col-10">Columns</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="columnsCollapse">
                        <g:form name="reportColumnsForm" id="reportColumnsForm">
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                <label class="form-check-label" for="columnsDescription">Description</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsRetailerCategoryCode" class="form-check-input" value="retailerCategoryCode" checked="${!userColumns || userColumns?.columns?.find { it.column == 'retailerCategoryCode' }?.enabled}" />
                                <label class="form-check-label" for="columnsRetailerCategoryCode">Retailer Category Code</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsBuyerId" class="form-check-input" value="buyerId" checked="${!userColumns || userColumns?.columns?.find { it.column == 'buyerId' }?.enabled}" />
                                <label class="form-check-label" for="columnsBuyerId">Buyer ID Required</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsBuyerAge" class="form-check-input" value="buyerAge" checked="${!userColumns || userColumns?.columns?.find { it.column == 'buyerAge' }?.enabled}" />
                                <label class="form-check-label" for="columnsBuyerAge">Buyer Age</label>
                            </div>


                            <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="categorySearchResults" />
        </div>
    </section>
</body>
</html>