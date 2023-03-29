<g:each in="${locations}" var="location" status="locationIndex">
    <div id="locationContainer${locationIndex}">
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].id" value="${location.id ?: ''}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].productVariantId" value="${location.productVariantId}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].storeId" value="${location.storeId}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].sku" value="${location.sku}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].location" value="${location.location}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].aisle" value="${location.aisle}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].bay" value="${location.bay}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].shelf" value="${location.shelf}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].position" value="${location.position}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].shelfCapacity" value="${location.shelfCapacity}" />
        <g:hiddenField name="variants[${variantIndex}].locations[${locationIndex}].minimumDisplayQuantity" value="${location.minimumDisplayQuantity}" />

        <g:if test="${locationsType == "SIMPLE"}">
            <div id="variants[${variantIndex}].locations[${locationIndex}].locationText">${location.location}</div>
        </g:if>
        <g:elseif test="${locationsType == "ADVANCED"}">
            <div id="variants[${variantIndex}].locations[${locationIndex}].locationText">${location.aisle} - ${location.bay} - ${location.shelf} - ${location.position}</div>
        </g:elseif>
    </div>
</g:each>