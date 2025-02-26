<%@ page import="uk.co.wonderlane.wlpos.enums.ReasonCodeType" %>
<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-1 font-weight-bold">Reason Code Id</div>

    <div class="col-1 font-weight-bold">Reason Code</div>

    <div class="col-2 font-weight-bold">Description</div>
    <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
        <div class="col-2 font-weight-bold">Direction</div>
    </g:if>

    <div class="col-2 font-weight-bold">Status</div>
    <div class="col-4 font-weight-bold"></div>
</div>

<script>
    $("#sortable").sortable({
        update: function (event, ui) {
            reasonCodeNewOrders = $(this).sortable('toArray', {attribute: 'value'});
            console.log(reasonCodeNewOrders); // Log the new order


            // // Here you could send the new order to your server
            // $.ajax({
            //     url: ajaxSaveReorderReasonCode,
            //     method: 'POST',
            //     data: {order: newOrder},
            //     success: function (response) {
            //         console.log('Order updated successfully');
            //     }
            // });
        }
    });
</script>

<g:if test="${!reasonCodes || reasonCodes?.size() == 0}">
    <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
</g:if>

<ul id="sortable">
    <g:each in="${reasonCodes}" var="code" status="i">
        <li value="${code.id}" class="ui-state-default row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable">
        <svg data-baseweb="icon" title="Grab" viewBox="0 0 24 24" class="grabhandle"><path fill-rule="evenodd"
                                                                                           clip-rule="evenodd"
                                                                                           d="M5 8C4.44775 8 4 8.44775 4 9C4 9.55225 4.44775 10 5 10H19C19.5522 10 20 9.55225 20 9C20 8.44775 19.5522 8 19 8H5ZM5 14C4.44775 14 4 14.4478 4 15C4 15.5522 4.44775 16 5 16H19C19.5522 16 20 15.5522 20 15C20 14.4478 19.5522 14 19 14H5Z"></path>
        </svg>

        <div id="id-${i + 1}" class="col-1 my-auto text-truncate">${code.id}</div>

        <div id="code-${i + 1}" class="col-1 my-auto text-truncate">${code.code}</div>

        <div id="desc-${i + 1}" class="col-2 my-auto text-truncate">${code.description}</div>

        <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
            <div id="desc-${i + 1}"
                 class="col-2 my-auto text-truncate">${code.additionalFunctionality ? "Movement In" : "Movement Out"}</div>
        </g:if>

        <div id="status-${i + 1}" class="col-2 my-auto text-truncate">${code.deleted ? "Inactive" : "Active"}</div>

        <g:if test="${type == ReasonCodeType.PRODUCT_LIST.name()}">
            <li class="col-2 my-auto text-right">
        </g:if>
        <g:else>
            <div class="col-4 my-auto text-right">
        </g:else>
        <button id="edit-${i + 1}" class="btn btn-wl mx-2" onclick='ajaxEdit("${code.id}");'>Edit</button>
        <button id="delete-${i + 1}" class="btn btn-danger mx-2"
                onclick='ajaxDelete("${code.id}", "${code.description}")'>Delete</button>
        </div>
</li>
    </g:each>
</ul>

