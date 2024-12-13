<div class="container-fluid p-0">
    <div class="row no-gutters table-wl bottom-border" id="shift-menu-headers">
        <div class="col-3 text-center font-weight-bold pl-2">Attribute Name</div>
        <div class="col-1 text-center font-weight-bold">Attribute Type</div>
        <div class="col-2 text-center font-weight-bold">List Value</div>
        <div class="col-3 text-center font-weight-bold">Default Value</div>
        <div class="col-1 text-center font-weight-bold">Display Attribute</div>
    </div>

    <div id="search-results">
        <g:if test="${productAttributes?.isEmpty()}">
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <h3 class="text-muted">No results found</h3>
            </div>
        </g:if>
        <g:else>
            <g:each in="${productAttributes}" var="productAttribute" status="i">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;">
                    <div class="col-3 text-center">${productAttribute.name}</div>
                    <div class="col-1 text-center">${message(code: 'ProductAttributeType.' + productAttribute.type)}</div>
                    <div class="col-2 text-center">
                        <g:if test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                            <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;">Add List Values</button>
                        </g:if>
                    </div>
                    <div class="col-3 text-center">${productAttribute.defaultValue}</div>
                    <div class="col-1 p-0 checkbox-container">
                        <g:checkBox name="restrictions.quantityChangeAllowed" class="form-check-input wl-checkbox" checked="${productAttribute.displayAttribute}" />
                    </div>
                </div>
            </g:each>
        </g:else>
    </div>

    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxProductAttributes" total="${productAttributesCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}"/>
    </div>
</div>

<script type="application/javascript">
    $(document).ready(function() {
        $('.step').on('click', function() {
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>