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
                            <button onclick="addListItem(${productAttribute.id})" class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;">Add List Values</button>
                        </g:if>
                    </div>
                    <div class="col-3 text-center">
                        <g:if test="${productAttribute.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                            <g:select name="defaultValue_${productAttribute.id}"
                                      from="${productAttribute.listValues}"
                                      value="${productAttribute.defaultValue}"
                                      noSelection="['':'Select a default value']"
                                      class="form-control default-value-select"
                                      data-attribute-id="${productAttribute.id}"
                                      style="width: auto; display: inline-block;"/>
                        </g:if>
                        <g:else>
                            ${productAttribute.defaultValue}
                        </g:else>
                    </div>
                    <div class="col-1 p-0 checkbox-container">
                        <g:checkBox name="displayAttribute_${productAttribute.id}"
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

<script type="application/javascript">
    $(document).ready(function() {
        $('.step').on('click', function() {
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });

        $('.display-attribute-checkbox').on('change', function() {
            var checkbox = $(this);
            var attributeId = checkbox.data('attribute-id');
            var isChecked = checkbox.prop('checked');
            var previousState = !isChecked;
            var row = checkbox.closest('.row');
            var originalColor = row.css('background-color');

            if (confirm('Are you sure you want to ' + (isChecked ? 'display' : 'hide') + ' this attribute?')) {
                // User confirmed, make AJAX call
                $.ajax({
                    url: '${createLink(controller: 'product', action: 'updateDisplayAttribute')}',
                    method: 'POST',
                    data: {
                        id: attributeId,
                        displayAttribute: isChecked
                    },
                    success: function(response) {
                        if (response.success) {
                            // Update was successful, change color to green and fade out
                            row.css('background-color', '#E8F5E9')  // Light green color
                                .animate({ backgroundColor: originalColor }, 1000);
                        } else {
                            // Update failed, revert checkbox state
                            checkbox.prop('checked', previousState);
                            alert('Failed to update display attribute: ' + response.message);
                        }
                    },
                    error: function() {
                        // Update was successful, change color to green and fade out
                        row.css('background-color', '#FFDCE0')  // Light green color
                            .animate({ backgroundColor: originalColor }, 1000);
                        // AJAX call failed, revert checkbox state
                        checkbox.prop('checked', previousState);
                    }
                });
            } else {
                // User canceled, revert checkbox state
                checkbox.prop('checked', previousState);
            }
        });

        $('.default-value-select').on('change', function() {
            var select = $(this);
            var attributeId = select.data('attribute-id');
            var newDefaultValue = select.val();
            var row = select.closest('.row');
            var originalColor = row.css('background-color');

            if (confirm('Are you sure you want to update the default value to "' + newDefaultValue + '"?')) {
                $.ajax({
                    url: '${createLink(controller: 'product', action: 'updateDefaultValue')}',
                    method: 'POST',
                    data: {
                        id: attributeId,
                        defaultValue: newDefaultValue
                    },
                    success: function(response) {
                        if (response.success) {
                            row.css('background-color', '#E8F5E9')  // Very light green color
                                .animate({ backgroundColor: originalColor }, 1000);
                        } else {
                            row.css('background-color', '#FFDCE0')  // Light red color
                                .animate({ backgroundColor: originalColor }, 1000);
                            alert('Failed to update default value: ' + response.message);
                        }
                    },
                    error: function() {
                        row.css('background-color', '#FFDCE0')  // Light red color
                            .animate({ backgroundColor: originalColor }, 1000);
                        alert('An error occurred while updating the default value.');
                    }
                });
            } else {
                // User canceled, revert select to previous value
                select.val(select.find('option[selected]').val());
            }
        });

    });
</script>