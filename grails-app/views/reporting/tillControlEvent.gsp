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
                        <g:form class="form-inline" action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
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
                                <g:checkBox name="columns" id="columnsUsersName" class="form-check-input" value="usersName" checked="${!userColumns || userColumns?.columns?.find { it.column == 'usersName' }?.enabled}" />
                                <label class="form-check-label" for="columnsUsersName">User</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsReason" class="form-check-input" value="reason" checked="${!userColumns || userColumns?.columns?.find { it.column == 'reason' }?.enabled}" />
                                <label class="form-check-label" for="columnsReason">Reason</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsDateCreated" class="form-check-input" value="dateCreated" checked="${!userColumns || userColumns?.columns?.find { it.column == 'dateCreated' }?.enabled}" />
                                <label class="form-check-label" for="columnsDateCreated">Date</label>
                            </div>
                            <div class="form-group form-check">
                                <g:checkBox name="columns" id="columnsAmount" class="form-check-input" value="amount" checked="${!userColumns || userColumns?.columns?.find { it.column == 'amount' }?.enabled}" />
                                <label class="form-check-label" for="columnsAmount">Amount</label>
                            </div>

                            <button type="button" class="btn btn-wl" onclick="saveReportColumns(saveReportColumnsUrl, 'TILL_CONTROL_EVENT');">Apply</button>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'type', sortOrder: sortColumn == 'type' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Type</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'usersName', sortOrder: sortColumn == 'usersName' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">User</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'reason', sortOrder: sortColumn == 'reason' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Reason</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'dateCreated', sortOrder: sortColumn == 'dateCreated' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Date</g:link></div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
                <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'amount', sortOrder: sortColumn == 'amount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Amount</g:link></div>
            </g:if>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!tillControlEvents || tillControlEvents?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${tillControlEvents}" var="tillControlEvent" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "type" }?.enabled}">
                        <div class="col my-auto"><g:message code="TillControlEventType.${tillControlEvent.type}" /></div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "usersName" }?.enabled}">
                        <div class="col my-auto">${tillControlEvent.usersName}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "reason" }?.enabled}">
                        <div class="col my-auto">${tillControlEvent.reason ?: 'N/A'}</div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "dateCreated" }?.enabled}">
                        <div class="col my-auto"><g:formatDate date="${tillControlEvent.dateCreated}" format="dd/MM/yy HH:mm:ss" /></div>
                    </g:if>
                    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "amount" }?.enabled}">
                        <div class="col my-auto">
                            <g:if test="${tillControlEvent.amount != null}">
                                <g:formatNumber number="${tillControlEvent.amount}" type="currency" />
                            </g:if>
                            <g:else>N/A</g:else>
                        </div>
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