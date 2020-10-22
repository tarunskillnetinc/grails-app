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
            <div class="col font-weight-bold"><g:link action="tillControlEvents" params="[max: max, offset: offset, sortColumn: 'type', sortOrder: sortColumn == 'type' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Type</g:link></div>
            <div class="col font-weight-bold"><g:link action="tillControlEvents" params="[max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Total Quantity</g:link></div>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!tillControlEvents || tillControlEvents?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${tillControlEvents}" var="tillControlEvent" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'tillControlEvent', params: [type: tillControlEvent.key])}';">
                    <div class="col my-auto"><g:message code="TillControlEventType.${tillControlEvent.key}" /></div>
                    <div class="col my-auto">${tillControlEvent.value.size()}</div>
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