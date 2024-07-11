<style>
    .hierarchy-dropdown {
        width: 50px; /* Set your desired width */

    }

    .location-number {
        width: 100px; /* Set your desired width */
        height: 50px; /* Set your desired height */
    }
</style>

<g:each in="${locations}" var="location" status="locationIndex">
    <div id="locationContainer${locationIndex}" class="row w-100 flex-content">
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].id" value="${location.id ?: ''}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].location" value="${location.location}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].aisle" value="${location.aisle}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].bay" value="${location.bay}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].shelf" value="${location.shelf}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].position" value="${location.position}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].shelfCapacity" value="${location.shelfCapacity}" />
        <g:hiddenField name="variants[${variantIndex}].locationz[${locationIndex}].minimumDisplayQuantity" value="${location.minimumDisplayQuantity}" />

        <g:if test="${locationsType == "SIMPLE"}">
            <div id="variants[${variantIndex}].locationz[${locationIndex}].locationText" class="col-5">${location.location}</div>
        </g:if>
        <g:elseif test="${locationsType == "ADVANCED"}">
            <g:textField name="variants[${variantIndex}].locationz[${locationIndex}].locationDescription"  class="col-4 form-control bottom-border location-number" value="${location.locationDescription}" autocomplete="off" maxLength="40"/>
            <g:textField name="variants[${variantIndex}].locationz[${locationIndex}].locationNumber"  class="col-4 form-control bottom-border location-number" value="${location.locationNumber}" autocomplete="off" maxLength="8" />
            <g:select name="variants[${variantIndex}].locationz[${locationIndex}].locationHierarchy" class="col-2 form-control select-border hierarchy-dropdown ml-3" from="[1, 2, 3]" value="${location.locationHierarchy}" />
        </g:elseif>
    </div>
</g:each>