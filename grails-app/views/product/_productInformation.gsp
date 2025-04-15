<script type='text/javascript'>

    $(document).ready(function() {
        // Initialize the datepicker only once after the page has loaded
        $("[id^='product_attribute_information_date_']").datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto"
        })

        addNumericMaskLogic();
    });


    function addNumericMaskLogic() {
        $('.numeric-mask').on('keydown', function (e) {
            // Allow navigation keys, backspace, delete, tab, enter, and arrow keys
            if ($.inArray(e.key, ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'Home', 'End']) !== -1) {
                // Clear the field if Backspace is pressed and the value is "0.00"
                if (e.key === 'Backspace' && $(this).val() === '0.00') {
                    $(this).val('');
                    e.preventDefault(); // Prevent default Backspace behavior
                }
                return;
            }

            let currentValue = $(this).val();

            // Allow 0 as a valid starting value
            if (currentValue === '0' && e.key === '0') {
                return;
            }

            // If empty, allow the user to input a new value
            if (!currentValue && e.key.match(/[0-9.]/)) {
                return;
            }

            currentValue = currentValue.replace(/,/g, '').replace(/[^0-9.]/g, '') + e.key;

            const newValue = parseFloat(currentValue)
            let maxValue = parseFloat(this.max);
            if (isNaN(maxValue)) {
                maxValue = 999999.99; // Default max value
            }

            const minValue = 0.00;

            if (isNaN(newValue) || newValue < minValue || newValue > maxValue) {
                e.preventDefault();
            }
        });

        // Ensure proper formatting on blur
        $('.numeric-mask').on('blur', function () {

            let value = $(this).val();

            // If value is empty, allow it
            if (!value) {
                $(this).val(''); // Leave empty
                return;
            }

            value = value.replace(/,/g, ''); // Remove commas for parsing
            const parsedValue = parseFloat(value);

            let maxValue = parseFloat(this.max);
            if (isNaN(maxValue)) {
                maxValue = 999999.99; // Default max value
            }

            if (isNaN(parsedValue) || parsedValue < 0.00) {
                $(this).val(''); // Allow empty instead of defaulting to 0.00
            } else if (parsedValue > maxValue) {
                $(this).val(maxValue.toFixed(2)); // Clamp to max value
            } else {
                // If the parsed value is an integer (i.e., no decimals or whole number like 4.00, 5.00)
                if (parsedValue % 1 === 0) {
                    $(this).val(parsedValue.toString()); // Display without decimals
                } else {
                    $(this).val(parsedValue.toFixed(2)); // Display with 2 decimals if not an integer
                }
            }
        });
    }

</script>

<div id="extraAttributesHeader" class="modal-header mt-3 mb-3">
    <h4>Extra Attributes</h4>
</div>

<div class="row mx-4 pt-4" style="word-break: break-all; word-wrap: break-word;">
    <g:each var="attributeValue" in="${attributeValues}" status="index">
        <g:if test="${index % 3 == 0 && index > 0}">
            </div>
            <div class="row mx-4 pt-4" style="word-break: break-all; word-wrap: break-word;">
        </g:if>
        <div class="col-4">
            <g:hiddenField id="productAttributeValues[${index}].retailerId" name="productAttributeValues[${index}].retailerId" value="${attributeValue?.retailerId }" />
            <g:hiddenField id="productAttributeValues[${index}].storeId" name="productAttributeValues[${index}].storeId" value="${attributeValue?.storeId }" />
            <g:hiddenField id="productAttributeValues[${index}].productAttributeId" name="productAttributeValues[${index}].productAttributeId" value="${attributeValue?.productAttributeId }" />
            <g:hiddenField id="productAttributeValues[${index}].attributeName" name="productAttributeValues[${index}].attributeName" value="${attributeValue?.attributeName }" />
            <g:hiddenField id="productAttributeValues[${index}].attributeType" name="productAttributeValues[${index}].attributeType" value="${attributeValue?.attributeType }" />

            <g:if test="${attributeValue?.attributeType != uk.co.wonderlane.wlpos.enums.ProductAttributeType.BOOLEAN}">
                <div class="col-12 pb-2 pl-0 my-auto font-weight-bold" id="attribute_label_${attributeValue?.productAttributeId}">
                    ${attributeValue?.attributeName}
                </div>
            </g:if>

            <g:if test="${attributeValue?.attributeType == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                <g:select id="productAttributeValues[${index}].value"
                          name="productAttributeValues[${index}].value"
                          from="${attributeValue?.listValues?.sort { it.toLowerCase() }}"
                          noSelection="['': '']"
                          value="${attributeValue?.value ?: ''}"
                          class="col-12 form-control select-border"
                          data-attribute-id="${attributeValue?.productAttributeId}"
                />
            </g:if>

            <g:if test="${attributeValue?.attributeType == uk.co.wonderlane.wlpos.enums.ProductAttributeType.TEXT}">
                <g:textField id="productAttributeValues[${index}].value"
                             name="productAttributeValues[${index}].value"
                             value="${attributeValue?.value}"
                             maxlength="50"
                             size="50"
                             class="col-12 form-control bottom-border"
                />
            </g:if>

            <g:if test="${attributeValue?.attributeType == uk.co.wonderlane.wlpos.enums.ProductAttributeType.NUMERIC}">
                <g:textField id="productAttributeValues[${index}].value"
                             name="productAttributeValues[${index}].value"
                             min="0"
                             max="999999.99"
                             value="${attributeValue?.value ? attributeValue.value : ''}"
                             class="col-12 form-control bottom-border numeric-mask"
                />
            </g:if>

            <g:if test="${attributeValue?.attributeType == uk.co.wonderlane.wlpos.enums.ProductAttributeType.BOOLEAN}">
                <div id="attribute_${attributeValue?.productAttributeId}" class="col-12 pt-3 mr-0 d-flex align-items-center">
                    <div class="form-check form-check-inline">
                        <g:checkBox id="productAttributeValues[${index}].value"
                                    name="productAttributeValues[${index}].value"
                                    value="true"
                                    checked="${attributeValue?.value == 'true'}"
                                    class="form-check-input wl-checkbox"
                        />
                        <label class="form-check-label font-weight-bold pl-2">${attributeValue?.attributeName}</label>
                    </div>
                </div>
            </g:if>

            <g:if test="${attributeValue?.attributeType == uk.co.wonderlane.wlpos.enums.ProductAttributeType.DATE}">
                <div class="form-check d-flex align-items-center pl-0">
                    <g:textField name="productAttributeValues[${index}].value"
                                 id="product_attribute_information_date_${index}"
                                 class="col-12 form-control bottom-border"
                                 value="${attributeValue.value}"
                                 onkeydown="return false"
                    />
                </div>
            </g:if>
        </div>
    </g:each>
</div>
