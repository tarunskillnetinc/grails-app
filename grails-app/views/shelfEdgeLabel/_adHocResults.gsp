<div class="d-flex justify-content-center">
    <div id="ad-hoc-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="ad-hoc-results">
    <g:if test="${productLists?.size() == 0}">
        <div class="row col-8 offset-2 px-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No pending ad-hoc batches.</div>
        </div>
    </g:if>

    <g:each in="${productLists}" var="productList" status="i">
        <div class="row col-8 offset-2 px-0 py-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(action:'showAdHocBatch', id: productList.id)}';">
%{--            <div class="col-1 px-0">--}%
%{--                <label class="radio-container">--}%
%{--                    <g:radio name="productListId" id="productList-${productList.id}" value="${productList.id}" class="form-check-input" />--}%
%{--                    <span class="checkmark checkmark-alternate-colour"></span>--}%
%{--                </label>--}%
%{--            </div>--}%
            <div class="col-9">${productList.reasonDescription}</div>
            <div class="col-3">${productList.labelCount}</div>
        </div>
    </g:each>
</div>