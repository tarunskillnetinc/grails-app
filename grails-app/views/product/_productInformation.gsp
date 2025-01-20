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
        $('.numeric-mask').maskMoney({
            prefix: '',
            allowNegative: false,
            thousands: ',',
            decimal: '.',
            affixesStay: true,
            precision: 2,
        });


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

            // If empty, allow the user to input a new value
            if (!currentValue && e.key.match(/[0-9]/)) {
                return;
            }

            currentValue = currentValue.replace(/,/g, '').replace(/[^0-9]/g, '') + e.key;

            const newValue = parseFloat(currentValue) / 100; // Handle two decimal places
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


<div class="row">
    <g:each var="attributeValue" in="${productAttributeValuesList}" status="index">
        <g:if test="${index % 2 == 0 && index > 0}">
            </div>
            <div class="row">
        </g:if>
        <div class="col-12 col-lg-6 mt-2">
            <div class="row form-group align-items-center">
                <g:hiddenField id="productAttributeValues[${index}].retailerId" name="productAttributeValues[${index}].retailerId" value="${attributeValue?.retailerId }" />
                <g:hiddenField id="productAttributeValues[${index}].productId" name="productAttributeValues[${index}].productId" value="${attributeValue?.productId }" />
                <g:hiddenField id="productAttributeValues[${index}].productAttributeId" name="productAttributeValues[${index}].productAttributeId" value="${attributeValue?.productAttributes?.id }" />
                <g:hiddenField id="productAttributeValues[${index}].attributeName" name="productAttributeValues[${index}].attributeName" value="${attributeValue?.productAttributes?.name }" />
                <g:hiddenField id="productAttributeValues[${index}].attributeType" name="productAttributeValues[${index}].attributeType" value="${attributeValue?.productAttributes?.type }" />
                <div class="col-4 text-right pr-4">
                    <label for="attribute_${attributeValue?.productAttributeId}" class="col-form-label wl-label" style="white-space: nowrap; display: inline-block; max-width: 100%;">${attributeValue?.productAttributes?.name}</label>
                </div>

                <div class="col-6">
                    <g:if test="${attributeValue?.productAttributes?.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                        <g:select id="productAttributeValues[${index}].value"
                                  name="productAttributeValues[${index}].value"
                                  from="${attributeValue?.productAttributes?.listValues?.sort { it.toLowerCase() }}"
                                  value="${attributeValue?.value ?: attributeValue.productAttributes.defaultValue}"
                                  class="col-lg-12 form-control select-border"
                                  data-attribute-id="${attributeValue.productAttributes.id}"
                                  disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.TEXT}">
                        <g:textField id="productAttributeValues[${index}].value"
                                     name="productAttributeValues[${index}].value"
                                     value="${attributeValue.value}"
                                     maxlength="50"
                                     size="50"
                                     placeholder="${attributeValue.productAttributes.defaultValue ?: ''}"
                                     class="col-lg-12 form-control bottom-border"
                                     disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.NUMERIC}">
                        <g:textField id="productAttributeValues[${index}].value"
                                 name="productAttributeValues[${index}].value"
                                 max="999999.99"
                                 value="${attributeValue.value ? attributeValue.value : ''}"
                                 class="col-lg-12 form-control bottom-border numeric-mask"
                                 disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.BOOLEAN}">
                        <div class="form-check d-flex align-items-center h-100 pl-0">
                            <g:checkBox id="productAttributeValues[${index}].value"
                                        name="productAttributeValues[${index}].value"
                                        value="true"
                                        checked="${attributeValue.value == 'true'}"
                                        class="col-lg-12 form-check-input wl-checkbox"
                                        style="margin-left: 0;"
                                        disabled="${!isStore}"/>
                        </div>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.DATE}">
                        <div class="form-check d-flex align-items-center h-100 pl-0">
                            <g:textField name="productAttributeValues[${index}].value"
                                         id="product_attribute_information_date_${index}"
                                         class="col-lg-12 form-control bottom-border"
                                         value="${attributeValue.value}"
                                         disabled="${!isStore}"/>
                        </div>
                    </g:if>

                </div>
            </div>
        </div>
    </g:each>
</div>
