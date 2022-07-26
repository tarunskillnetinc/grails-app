<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'retailerPromotionId', sortOrder: ${sortColumn == 'retailerPromotionId' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Promotion ID</a></div>
    <div class="col-3 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'description', sortOrder: ${sortColumn == 'description' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'updateDatetime', sortOrder: ${sortColumn == 'updateDatetime' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Last Updated</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'startDate', sortOrder: ${sortColumn == 'startDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Start Date</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'endDate', sortOrder: ${sortColumn == 'endDate' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">End Date</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'active', sortOrder: ${sortColumn == 'active' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Active</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'type', sortOrder: ${sortColumn == 'type' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Type</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'amount', sortOrder: ${sortColumn == 'amount' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Discount Amount</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchButtonClicked({ max: '${max}', offset: '${offset}', sortColumn: 'supplierName', sortOrder: ${sortColumn == 'supplierName' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Supplier Name</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id ="search-results">
    <g:if test="${promotions == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please apply some filters.</div>
        </div>
    </g:if>

    <g:if test="${promotions?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${promotions}" var="promotion" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'maintenance', params:[promotionId: promotion.id])}';">
            <div class="col-1">${promotion.retailerPromotionId}</div>
            <div class="col-3">${promotion.description}</div>
            <div class="col-1 font-weight-bold">${promotion.updateDatetime.toString("dd/MM/yyyy")}</div>
            <div class="col-1 font-weight-bold">${promotion.startDate.toString("dd/MM/yyyy")}</div>
            <div class="col-1 font-weight-bold">${promotion.endDate.toString("dd/MM/yyyy")}</div>
            <div class="col-1">
                <g:checkBox name="promo-${i}-active" value="${promotion.active}" disabled="true"/>
            </div>
            <div class="col-2"><g:message code="PromotionType.${promotion.type}" /></div>
            <div class="col-1">${promotion.amount}</div>
            <div class="col-1">${promotion.symbolGroupPromotion?.symbolGroup?.name} ${promotion.symbolGroupPromotion?.isLeaflet ? " - Leaflet" : ""}</div>
        </div>
    </g:each>

    <div class="my-3 text-right">
        <util:remotePaginate action="promotionSearch" total="${totalResults ?: 0}" update="search-results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm,
                                                                                                                                                                         searchBy: searchBy,
                                                                                                                                                                         validDate: validDate,
                                                                                                                                                                         updatedSince: updatedSince,
                                                                                                                                                                         type: type,
                                                                                                                                                                         sortColumn: sortColumn,
                                                                                                                                                                         sortOrder: sortOrder,
                                                                                                                                                                         supplier: supplier,
                                                                                                                                                                         status: status]" />
    </div>
</div>