<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.DateTimeZone" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>

<asset:stylesheet src="bootstrap-datepicker3.min.css" />
<asset:javascript src="bootstrap-datepicker.min.js" />

<script type='text/javascript'>
    $(document).ready(function() {
        // Initialize the datepicker only once after the page has loaded
        $("[id^='attribute_date_']").datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            endDate: new Date().toString(),
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto"
        })
    });
</script>


<div class="row">
    <g:each var="attributeValue" in="${productAttributeValuesList}" status="index">
        <g:if test="${index % 2 == 0 && index > 0}">
            </div>
            <div class="row">
        </g:if>
        <div class="col-12 col-lg-6">
            <div class="row form-group align-items-center">
                <g:hiddenField name="productAttributeValues[${index}].retailerId" value="${attributeValue?.retailerId }" />
                <g:hiddenField name="productAttributeValues[${index}].productId" value="${attributeValue?.productId }" />
                <g:hiddenField name="productAttributeValues[${index}].productAttributeId" value="${attributeValue?.productAttributes?.id }" />
                <div class="col-4 text-right pr-4">
                    <label for="attribute_${attributeValue?.productAttributeId}" class="col-form-label wl-label" style="white-space: nowrap; display: inline-block; max-width: 100%;">${attributeValue?.productAttributes?.name}</label>
                </div>

                <div class="col-6">
                    <g:if test="${attributeValue?.productAttributes?.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.LIST}">
                        <g:select name="productAttributeValues[${index}].value"
                                  from="${attributeValue?.productAttributes?.listValues}"
                                  value="${attributeValue?.value ?: attributeValue.productAttributes.defaultValue}"
                                  class="form-control select-border"
                                  data-attribute-id="${attributeValue.productAttributes.id}"
                                  disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.TEXT}">
                        <g:textField name="productAttributeValues[${index}].value"
                                     value="${attributeValue.value}"
                                     maxlength="50"
                                     placeholder="${attributeValue.productAttributes.defaultValue ?: ''}"
                                     class="form-control bottom-border"
                                     disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.NUMERIC}">
                        <g:field name="productAttributeValues[${index}].value"
                                 type="number"
                                 value="${attributeValue.value}"
                                 class="form-control bottom-border"
                                 disabled="${!isStore}"/>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.BOOLEAN}">
                        <div class="form-check d-flex align-items-center h-100 pl-0">
                            <g:checkBox name="productAttributeValues[${index}].value"
                                        value="true"
                                        checked="${attributeValue.value == 'true'}"
                                        class="form-check-input wl-checkbox"
                                        style="margin-left: 0;"
                                        disabled="${!isStore}"/>
                        </div>
                    </g:if>

                    <g:if test="${attributeValue.productAttributes.type == uk.co.wonderlane.wlpos.enums.ProductAttributeType.DATE}">
                        <div class="form-check d-flex align-items-center h-100 pl-0">
                            <g:textField name="productAttributeValues[${index}].value"
                                         id="attribute_date_${index}"
                                         class="col-5 form-control bottom-border"
                                         value="${attributeValue.value}"
                                         disabled="${!isStore}"/>
                        </div>
                    </g:if>

                </div>
            </div>
        </div>
    </g:each>
</div>
