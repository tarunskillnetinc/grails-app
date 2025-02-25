<%@ page import="uk.co.wonderlane.wlpos.enums.ReasonCodeType" %>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-4 font-weight-bold">Description</div>
    <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
        <div class="col-2 font-weight-bold">Direction</div>
    </g:if>
    <div class="col-2 font-weight-bold">Secret</div>

    <div class="col-2 font-weight-bold">Preferred Reason Code</div>
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

