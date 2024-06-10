<%@ page import="uk.co.wonderlane.wlpos.enums.ReasonCodeType" %>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-4 font-weight-bold"><a href="#" onclick="ajaxSearch({ sortColumn: 'description', sortOrder: ${sortColumn == 'description' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Description</a></div>
    <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
        <div class="col-2 font-weight-bold"><a href="#" onclick="ajaxSearch({ sortColumn: 'additionalFunctionality', sortOrder: ${sortColumn == 'additionalFunctionality' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Direction</a></div>
    </g:if>
    <div class="col-2 font-weight-bold"><a href="#" onclick="ajaxSearch({ sortColumn: 'secret', sortOrder: ${sortColumn == 'secret' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Secret</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="ajaxSearch({ sortColumn: 'preferredReasonCode', sortOrder: ${sortColumn == 'preferredReasonCode' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} });">Preferred Reason Code</a></div>
    <div class="col-4 font-weight-bold"></div>
</div>

<g:if test="${!reasonCodes || reasonCodes?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
</g:if>

<g:each in="${reasonCodes}" var="code" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable">
        <div id="desc-${i + 1}" class="col-4 my-auto text-truncate">${code.description}</div>
        <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
            <div id="desc-${i + 1}" class="col-2 my-auto text-truncate">${code.additionalFunctionality ? "Movement In" : "Movement Out"}</div>
        </g:if>
        <div id="secret-${i + 1}" class="col-2 my-auto text-truncate">${code.secret}</div>
        <div id="preferredReasonCode-${i + 1}" class="col-2 my-auto text-truncate">${code.preferredReasonCode}</div>
    <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
            <div class="col-2 my-auto text-right">
        </g:if>
        <g:else>
            <div class="col-4 my-auto text-right">
        </g:else>
                <button id="edit-${i + 1}" class="btn btn-wl mx-2" onclick='ajaxEdit("${code.id}");'>Edit</button>
                <button id="delete-${i + 1}" class="btn btn-danger mx-2" onclick='ajaxDelete("${code.id}", "${code.description}")'>Delete</button>
            </div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate controller="reasonCode" action="ajaxSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[type: type, sortColumn: sortColumn, sortOrder: sortOrder]" onSuccess="\$('html, body').animate({ scrollTop: 0 }, 'fast')"/>
</div>
