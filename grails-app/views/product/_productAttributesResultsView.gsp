

<div class="container-fluid p-0">
    <div class="row mb-2 no-gutters" id="shift-menu-headers">
    <div class="col-3 text-center font-weight-bold pl-2">Attribute Name</div>
    <div class="col-1 text-center font-weight-bold">Attribute Type</div>
    <div class="col-2 text-center font-weight-bold">List Value</div>
    <div class="col-3 text-center font-weight-bold">Default Value</div>
    <div class="col-1 text-center font-weight-bold">Display Attribute</div>
</div>

    <div class="card">
    <!-- Each Shift as a Boxed Card -->
        <g:if test="${ProductAttributes?.isEmpty()}">
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <h3 class="text-muted">No results found</h3>
            </div>
        </g:if>
        <g:else>
            <g:each in="${ProductAttributes}" var="productAttribute" status="i">
                <div class="shift-card-body border rounded mb-2 pt-2 pb-2 wl-striped${i%2}">
                        <div class="row mb-2 align-items-center">
                            <div class="col-3 text-center">${productAttribute.name}</div>
                            <div class="col-1 text-center">${productAttribute.type}</div>
                            <div class="col-2 text-center">
                                <g:if test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                                    <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;">Add List Values</button>
                                </g:if>
                            </div>
                            <div class="col-3 text-center">${productAttribute.defaultValue}</div>
                            <div class="col-1 text-center">
                                <g:checkBox name="restrictions.quantityChangeAllowed" class="col-1 form-check-input wl-checkbox" checked="${productAttribute.displayAttribute}" />
                            </div>
                        </div>
                </div>
            </g:each>
        </g:else>
    </div>
</div>