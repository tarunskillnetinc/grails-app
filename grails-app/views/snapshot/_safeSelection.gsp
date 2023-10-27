<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
        <g:each in="${safeLocations}" var="safe" status="i">
            <button type="button" id="selectSafe${i}button" class="btn btn-wl" onclick="showSafeModal(${safe.id})">${safe.description}</button>
        </g:each>
    </div>
    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">

    </div>
</div>