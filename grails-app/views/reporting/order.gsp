<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>

        <title>Trust Retail</title>

        <asset:javascript src="reporting.js"/>

        <script type='text/javascript'>
            var reportType = "${reportType}";
            var getDataUrl = "${createLink(controller: 'reporting', action: 'ajaxOrder')}";
            var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";

            $(document).ready(function () {
                filterReport();
            });
        </script>
    </head>

    <body>
        <section id="reporting-container" class="container-fluid">
            <g:reportBreadcrumb reportType="${reportType}" supplierReference="${supplierReference}" />

            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Orders Report</h2>
            </div>

            <div class="row mt-4">
                <g:form name="filtersForm" id="filtersForm">
                    <g:hiddenField name="productListId" value="${productListId}" />
                </g:form>

                <div class="col-2 offset-8 text-right" style="margin-top: 8px;">
                    <g:if test="${isEditable}">
                        <g:link controller="order" action="edit" id="${productListId}" class="btn btn-wl">Edit Order</g:link>
                    </g:if>

                    <button id="export-to-csv" class="btn btn-wl" onclick="exportToCsv();">Export to CSV</button>
                </div>

                <div class="col-2">
                    <div class="card bg-light border-wl">
                        <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse"
                             aria-expanded="false" aria-controls="columnsCollapse">
                            <div class="row">
                                <div id="columns-text" class="col-10">Columns</div>

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
                                    <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsDescription">Description</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsOrderedQuantity" class="form-check-input"
                                                value="orderedQuantity"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'orderedQuantity' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsOrderedQuantity">Ordered Quantity</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsPackQuantity" class="form-check-input"
                                                value="packQuantity"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'packQuantity' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsPackQuantity">Pack Quantity</label>
                                </div>

                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsLineValue" class="form-check-input"
                                                value="lineValue"
                                                checked="${!userColumns || userColumns?.columns?.find { it.column == 'lineValue' }?.enabled}"/>
                                    <label class="form-check-label" for="columnsLineValue">Line Value</label>
                                </div>

                                <button id="columns-submit-button" type="button" class="btn btn-wl"
                                        onclick="saveReportColumns();">Apply</button>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="orderResults" />
            </div>
        </section>
    </body>
</html>