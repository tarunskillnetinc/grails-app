%{--<g:each in="${stores}" var="store" status="i">--}%
%{--    <div class="form-check">--}%
%{--        <input class="form-check-input" type="checkbox" value="${store.id}" id="store-${store.id}">--}%
%{--        <label class="form-check-label" for="${store.id}">--}%
%{--            ${store.config.storeNumber} - ${store.config.storeName}--}%
%{--        </label>--}%
%{--    </div>--}%
%{--</g:each>--}%

<ul id="store-list">
    <g:each in="${stores}" var="store" status="i">
        <div id="modal-store-result-${i + 1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable store-item">

            <div id="modal-store-number-${i + 1}" class="col-4 my-auto">${store.config.storeNumber}</div>

            <div id="modal-store-name-${i + 1}" class="col-4 my-auto">${store.config.storeName}</div>

            <div class="col-4 my-auto text-right">
                <input class="form-check-input hidden" type="checkbox" value="${store.id}" id="store-${store.id}">
                <button id="modal-store-select-${i + 1}" class="btn btn-primary"
                        onclick="toggleSelectStore(${store.id}, ${i + 1})">Select</button>
            </div>
        </div>
    </g:each>
</ul>
