<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>
    <g:render template="/nav/reporting" model="[active: 'promotions']" />

    <section id="reporting-container" class="container-fluid">
        <div class="row header-wl">
            <h2 class="mx-auto">Promotion Sales Report</h2>
        </div>

        <div class="col-8 offset-2 mt-4">
            <div class="card bg-light border-wl">
                <div class="card-header" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                    Filters
                </div>
                <div class="card-body collapse" id="filterCollapse">
                    <g:form class="form-inline" action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: sortColumn, sortOrder: sortOrder]">
                        <div class="form-group">
                            <g:textField name="searchText" placeholder="Type search" maxlength="100" value="${searchText}" class="form-control bottom-border" />
                        </div>
                        <g:submitButton name="Search" class="btn btn-wl" />
                    </g:form>
                </div>
            </div>
        </div>

        <div class="row mt-5 mb-2 ml-0 mr-0 table-wl">
            <div class="col-2 font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'description', sortOrder: sortColumn == 'description' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Description</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'type', sortOrder: sortColumn == 'type' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Type</g:link></div>
            <div class="col-1 font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'quantity', sortOrder: sortColumn == 'quantity' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Quantity</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'fullPrice', sortOrder: sortColumn == 'fullPrice' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Full Price</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'discount', sortOrder: sortColumn == 'discount' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Discount</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'profit', sortOrder: sortColumn == 'profit' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Profit</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'margin', sortOrder: sortColumn == 'margin' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">Margin</g:link></div>
            <div class="col font-weight-bold"><g:link action="promotionsGrouped" params="[max: max, offset: offset, sortColumn: 'vat', sortOrder: sortColumn == 'vat' ? sortOrder == 'asc' ? 'desc' : 'asc' : 'asc']">VAT</g:link></div>
        </div>

        <div id="search-results" class="align-content-center">
            <g:if test="${!promotionSales || promotionSales?.size() == 0}">
                <div id="noResultsRow" class="col pt-2 text-center my-auto">No results found.</div>
            </g:if>

            <g:each in="${promotionSales}" var="promotionSale" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'promotions', params: [promotionId: promotionSale.promotionId])}';">
                    <div class="col-2 my-auto">${promotionSale.description}</div>
                    <div class="col my-auto"><g:message code="PromotionType.${promotionSale.type}" /></div>
                    <div class="col-1 my-auto">${promotionSale.quantity}</div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSale.fullPrice}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSale.discount}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSale.profit}" type="currency" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSale.margin / 100}" type="percent" minFractionDigits="2" /></div>
                    <div class="col my-auto"><g:formatNumber number="${promotionSale.vat}" type="currency" /></div>
                </div>
            </g:each>
        </div>

        <div class="my-3 text-right">
            <div class="text-right">Displaying ${promotionSales.size()} of ${totalResults} results.</div>
            <g:paginate total="$totalResults" offset="$offset" max="$max" params="[sortColumn: sortColumn, sortOrder: sortOrder]" />
        </div>
    </section>
</body>
</html>