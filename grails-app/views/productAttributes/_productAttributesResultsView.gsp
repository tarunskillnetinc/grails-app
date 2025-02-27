<section id="success-section" class="container-fluid">
</section>
<section id="error-section" class="container-fluid">
</section>
<section id="result-section" class="container-fluid">
    <div class="container-fluid p-0">
        <div class="row no-gutters table-wl bottom-border" id="shift-menu-headers">
            <div id="product-attr-header-attribute-name" class="col-3 text-center font-weight-bold pl-2">Attribute Name</div>
            <div id="product-attr-header-attribute-type" class="col-1 text-center font-weight-bold">Attribute Type</div>
            <div id="product-attr-header-list-value" class="col-2 text-center font-weight-bold">List Value</div>
            <div id="product-attr-header-default-value" class="col-3 text-center font-weight-bold">Default Value</div>
            <div id="product-attr-header-display-attributes" class="col-1 text-center font-weight-bold">Display Attribute</div>
        </div>

        <div id="search-results">
            <g:if test="${productAttributes?.isEmpty()}">
                <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                    <h3 class="text-muted">No results found</h3>
                </div>
            </g:if>
            <g:else>
                <g:each in="${productAttributes}" var="productAttribute" status="i">
                    <div id="product-attr-${i+1}"class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable">
                        <div id="product-attr-field-attribute-name-${i+1}" class="col-3 text-center">${productAttribute.name}</div>
                        <div id="product-attr-field-attribute-type-${i+1}" class="col-1 text-center">${message(code: 'ProductAttributeType.' + productAttribute.type)}</div>
                        <div id="product-attr-field-list-value-"${i+1}" class="col-2 text-center">
                            <g:if test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                                <button id="product-attr-add-list-value-button-${i+1}"  onclick="addListItem(${productAttribute.id})" class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;">Add List Values</button>
                            </g:if>
                        </div>
                        <div id="product-attr-field-default-value-${i+1}" class="col-3 text-center">
                            <g:if test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                                <g:select id="product-attr-list-value-selection-${i+1}" name="defaultValue_${productAttribute.id}"
                                          from="${productAttribute.listValues}"
                                          value="${productAttribute.defaultValue}"
                                          noSelection="['':'Select a default value']"
                                          class="form-control default-value-select"
                                          data-attribute-id="${productAttribute.id}"
                                          style="display: inline-block;"/>
                            </g:if>
                            <g:elseif test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.BOOLEAN}">
                                ${productAttribute.defaultValue.capitalize()}
                            </g:elseif>
                            <g:else>
                                ${productAttribute.defaultValue}
                            </g:else>
                        </div>
                        <div id="product-attr-field-display-attribute-${i+1}"  class="col-1 p-0 checkbox-container">
                            <g:checkBox id="product-display-attribute-checkbox-${i+1}" name="displayAttribute_${productAttribute.id}"
                                        class="form-check-input wl-checkbox display-attribute-checkbox"
                                        checked="${productAttribute.displayAttribute}"
                                        data-attribute-id="${productAttribute.id}" />
                        </div>
                    </div>
                </g:each>
            </g:else>
        </div>

        <div class="my-3 text-right">
            <util:remotePaginate action="ajaxProductAttributes" total="${productAttributesCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}"/>
        </div>
    </div>
</section>
<script type="application/javascript">
    $(document).ready(function() {
        $('.step').on('click', function() {
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });

        applyTemporaryStates();

        // Event handlers for changes
        $('.display-attribute-checkbox').on('change', function() {
            var checkbox = $(this);
            var attributeId = checkbox.data('attribute-id');
            var isChecked = checkbox.prop('checked');

            updateDisplayAttributeState(attributeId, isChecked);
            $("#error-section").html('');
            $("#success-section").html('');
        });

        $('.default-value-select').on('change', function() {
            var select = $(this);
            var attributeId = select.data('attribute-id');
            var newDefaultValue = select.val();

            updateDefaultValueState(attributeId, newDefaultValue);
            $("#error-section").html('');
            $("#success-section").html('');
        });

    });
</script>