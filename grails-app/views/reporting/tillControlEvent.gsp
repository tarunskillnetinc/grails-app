<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'tillControlEvents']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Till Control Events Report</h2>
        </div>

        <div class="col-8 offset-2 mt-4">
            <div class="card bg-light border-wl">
                <div class="card-header" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                    Filters
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

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'type', sortOrder: sortColumn == 'type' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Type</g:link></div>
            <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'usersName', sortOrder: sortColumn == 'usersName' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">User</g:link></div>
            <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'reason', sortOrder: sortColumn == 'reason' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Reason</g:link></div>
            <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'dateCreated', sortOrder: sortColumn == 'dateCreated' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Date</g:link></div>
            <div class="col font-weight-bold"><g:link action="tillControlEvent" params="[type: type, max: max, offset: offset, sortColumn: 'amount', sortOrder: sortColumn == 'amount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Amount</g:link></div>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!tillControlEvents || tillControlEvents?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${tillControlEvents}" var="tillControlEvent" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                    <div class="col my-auto"><g:message code="TillControlEventType.${tillControlEvent.type}" /></div>
                    <div class="col my-auto">${tillControlEvent.usersName}</div>
                    <div class="col my-auto">${tillControlEvent.reason ?: 'N/A'}</div>
                    <div class="col my-auto"><g:formatDate date="${tillControlEvent.dateCreated}" format="dd/MM/yy HH:mm:ss" /></div>
                    <div class="col my-auto">
                        <g:if test="${tillControlEvent.amount != null}">
                            <g:formatNumber number="${tillControlEvent.amount}" type="currency" />
                        </g:if>
                        <g:else>N/A</g:else>
                    </div>
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