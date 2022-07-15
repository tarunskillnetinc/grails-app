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

            function resetForm() {
                document.getElementById('productSearchTerm').value = null;
                document.getElementById('productSearchBy').value = 'everything';
            }

            function search() {
                var url = "${createLink(controller: 'product', action: 'ajaxSearchProducts')}";
                var searchTerm = $('#productSearchTerm').val();
                var searchBy = $('#productSearchBy').val();

                $("#search-results").hide();
                $("#loading-indicator").show();

                $.ajax({
                    url: url,
                    data: { searchTerm: searchTerm, searchBy: searchBy },
                    success: function(resp) {
                        $('#results-container').html(resp);
                        $('#productSearchTerm').data('prev',$('#productSearchTerm').val())
                        $('#productSearchBy').data('prev', $('#productSearchBy').val())
                    }
                });
            }

            function saveColumns() {
                var url = "${createLink(controller: 'product', action: 'ajaxSaveColumns')}";

                var checkboxValues = { };

                $("#reportColumnsForm input:checkbox").each(function() {
                    checkboxValues[$(this).val()] = this.checked;
                }).get();

                $.ajax({
                    url: url,
                    method: "POST",
                    data: { reportColumns: JSON.stringify(checkboxValues), reportType: "PRODUCT_SEARCH" },
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
            </section>
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto">Product Search</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="product" action="add" class="btn btn-wl">Add New Product</g:link>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-6">
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

                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label for="productSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10 input-group">
                                    <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" value="${session.PRODUCT_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />

                                    <div class="input-group-append">
                                        <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-4 offset-8 text-right">
                                    <button type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-2 offset-4">
                    <div class="card bg-light border-wl">
                        <div class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
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
                                    <g:checkBox name="columns" id="columnsItemCode" class="form-check-input" value="itemCode" checked="${!userColumns || userColumns?.columns?.find { it.column == 'itemCode' }?.enabled}" />
                                    <label class="form-check-label" for="columnsItemCode">Item Code</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                    <label class="form-check-label" for="columnsDescription">Description</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsUnitSize" class="form-check-input" value="unitSize" checked="${!userColumns || userColumns?.columns?.find { it.column == 'unitSize' }?.enabled}" />
                                    <label class="form-check-label" for="columnsUnitSize">Unit Size</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsVatRate" class="form-check-input" value="vatRate" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vatRate' }?.enabled}" />
                                    <label class="form-check-label" for="columnsVatRate">VAT Rate</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCategory" class="form-check-input" value="category" checked="${!userColumns || userColumns?.columns?.find { it.column == 'category' }?.enabled}" />
                                    <label class="form-check-label" for="columnsCategory">Category</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCostPrice" class="form-check-input" value="costPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'costPrice' }?.enabled}" />
                                    <label class="form-check-label" for="columnsCostPrice">Cost</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsRetailPrice" class="form-check-input" value="retailPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'retailPrice' }?.enabled}" />
                                    <label class="form-check-label" for="columnsRetailPrice">Retail Price</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsMargin" class="form-check-input" value="margin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'margin' }?.enabled}" />
                                    <label class="form-check-label" for="columnsMargin">Margin</label>
                                </div>

                                <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveColumns();">Apply</button>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="productSearchResults" />
            </div>
        </section>
    </body>
</html>