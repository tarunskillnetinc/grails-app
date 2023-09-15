<%--
  Created by IntelliJ IDEA.
  User: JacobBrewer
  Date: 26/08/2022
  Time: 12:02
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Trust Retail</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="reporting.js"/>

    <script type='text/javascript'>
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxProductList')}";
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

        $(document).ready(function () {
            filterReport();
        });
    </script>
</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}" type="${productList?.type}" dateStarted="${productList?.dateStarted}" storeId="${storeId}" typeFilter="${type}" startDate="${startDate}" endDate="${endDate}" />

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Product List Report</h2>
        </div>

        <div class="row mt-4">
            <g:form name="filtersForm" id="filtersForm">
                <g:hiddenField name="productListId" value="${productListId}" />
                <g:hiddenField name="startDate" value="${startDate?.toString("dd/MM/yyyy")}" />
                <g:hiddenField name="endDate" value="${endDate?.toString("dd/MM/yyyy")}" />
                <g:hiddenField name="storeId" value="${storeId}" />
                <g:hiddenField name="type" value="${type}" />
            </g:form>

            <div class="col-lg-2 offset-lg-8 col-md-3 offset-md-6 text-right" style="margin-top: 8px;">
                <button id="export-to-csv" class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
            </div>

            <div class="col-lg-2 col-md-3">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse"
                         aria-expanded="false" aria-controls="columnsCollapse">
                        <div class="row">
                            <div class="col-10">Columns</div>

                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                                     fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div class="card-body collapse" id="columnsCollapse">
                        <g:form name="reportColumnsForm" id="reportColumnsForm">
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsSku" class="form-check-input"
                                            value="sku"
                                            checked="${!userColumns || userColumns?.columns?.find { it.column == 'sku' }?.enabled}"/>
                                <label class="form-check-label" for="columnsSku">Product SKU</label>
                            </div>

                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDescription" class="form-check-input"
                                            value="description"
                                            checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}"/>
                                <label class="form-check-label" for="columnsDescription">Product Description</label>
                            </div>

                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsItemQuantity" class="form-check-input"
                                            value="itemQuantity"
                                            checked="${!userColumns || userColumns?.columns?.find { it.column == 'itemQuantity' }?.enabled}"/>
                                <label class="form-check-label" for="columnsItemQuantity">Items Delivered</label>
                            </div>

                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsLineValue" class="form-check-input"
                                            value="totalCost"
                                            checked="${!userColumns || userColumns?.columns?.find { it.column == 'totalCost' }?.enabled}"/>
                                <label class="form-check-label" for="columnsLineValue">Total Cost</label>
                            </div>

                            <button id="columns-submit-button" type="button" class="btn btn-wl"
                                    onclick="saveReportColumns();">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
%{--            <g:render template="productListResults" />--}%
        </div>
    </section>
</body>
</html>