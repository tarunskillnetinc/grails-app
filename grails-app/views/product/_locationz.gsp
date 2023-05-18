<g:each in="${locations}" var="location" status="locationIndex">
    <div id="locationContainer${locationIndex}">
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].location" value="${location.location}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].aisle" value="${location.aisle}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].bay" value="${location.bay}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].shelf" value="${location.shelf}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].position" value="${location.position}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].shelfCapacity" value="${location.shelfCapacity}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].minimumDisplayQuantity" value="${location.minimumDisplayQuantity}" />

        <g:if test="${locationsType == "SIMPLE"}">
            <div id="variants[${variantIndex}].locationz[${locationIndex}].locationText">${location.location}</div>
        </g:if>
        <g:elseif test="${locationsType == "ADVANCED"}">
            <div id="variants[${variantIndex}].locationz[${locationIndex}].locationText">${location.aisle} - ${location.bay} - ${location.shelf} - ${location.position}</div>
        </g:elseif>
    </div>
</g:each>