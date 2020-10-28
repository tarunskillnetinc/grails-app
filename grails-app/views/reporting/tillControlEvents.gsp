<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>

    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var saveReportColumnsUrl = "${createLink(controller: 'reporting', action: 'ajaxSaveReportColumns')}";
    </script>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'tillControlEvents']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Till Control Events Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-6">
                <div class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form class="form-inline" action="tillControlEvents" params="[max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                            <div class="form-group">
                                <g:textField name="searchText" placeholder="Type search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
                            </div>
                            <g:submitButton name="Search" class="btn btn-wl" />
                        </g:form>
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
                                <g:checkBox name="columns" id="columnsType" class="form-check-input" value="type" checked="${!userColumns || userColumns?.columns?.find { it.column == 'type' }?.enabled}" />
                                <label class="form-check-label" for="columnsType">Type</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsQuantity" class="form-check-input" value="quantity" checked="${!userColumns || userColumns?.columns?.find { it.column == 'quantity' }?.enabled}" />
                                <label class="form-check-label" for="columnsQuantity">Total Quantity</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns(saveReportColumnsUrl, 'TILL_CONTROL_EVENTS');">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvents" params="[max: max, offset: offset, sortColumn: 'type', sortOrder: sortColumn == 'type' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Type</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvents" params="[max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Quantity</g:link></div>
            </g:if>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!tillControlEvents || tillControlEvents?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${tillControlEvents}" var="tillControlEvent" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'tillControlEvent', params: [type: tillControlEvent.key])}';">
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                        <div class="col my-auto"><g:message code="TillControlEventType.${tillControlEvent.key}" /></div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "quantity" }?.enabled}">
                        <div class="col my-auto">${tillControlEvent.value.size()}</div>
                    </g:if>
                </div>
            </g:each>
        </div>

        <div class="my-3 text-right">
            <div class="text-right">Displaying ${tillControlEvents.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>