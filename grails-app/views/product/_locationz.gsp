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

        <div id="variants[${variantIndex}].locations[${locationIndex}].locationText">${location.location} @ (${location.bay})</div>
    </div>
</g:each>