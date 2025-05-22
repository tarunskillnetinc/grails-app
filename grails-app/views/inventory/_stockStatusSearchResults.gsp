<div style="display: flex;">
    <div style="flex-grow: 1;margin-top:20px;">
        <div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border">
            <div style="width: 50px; display: flex; justify-content: center; align-items: center;">
            </div>
                <div class="col-1 font-weight-bold">ID</div>
                <div class="col-2 font-weight-bold">Status</div>
                <div class="col-3 font-weight-bold">Total Number of Stores</div>
                <div class="col-3 font-weight-bold">Total Number of Products</div>
                <div class="col-2 font-weight-bold">Completed</div>
        </div>

        <g:if test="${stockAdjustments && !stockAdjustments.isEmpty()}">
            <g:each in="${stockAdjustments}" var="adjustment">
                <div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border">
                    <div style="width: 50px; display: flex; justify-content: center; align-items: center;">
                    </div>
                    <div class="col-1">${adjustment.id}</div>
                    <div class="col-2">${adjustment.status?.getFriendlyName() ?: adjustment.status}</div>
                    <div class="col-3">
                        <g:if test="${adjustment.storeId}">
                            1 <!-- Static value for stores since each adjustment is typically for one store -->
                        </g:if>
                        <g:else>
                            0
                        </g:else>
                    </div>
                    <div class="col-3">
                        <g:if test="${adjustment.productListItems}">
                            ${adjustment.productListItems.size()}
                        </g:if>
                        <g:else>
                            0
                        </g:else>
                    </div>
                    <div class="col-2">
                        <g:if test="${adjustment.dateCompleted}">
                            <g:formatDate date="${adjustment.dateCompleted}" format="dd/MM/yyyy HH:mm" />
                        </g:if>
                        <g:else>
                            -
                        </g:else>
                    </div>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <div class="row ml-0 mr-0 pt-4 pb-4 table-wl bottom-border text-center">
                <div class="col-12">No stock adjustments found</div>
            </div>
        </g:else>
    </div>
</div>