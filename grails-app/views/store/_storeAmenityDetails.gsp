<div style="width: 100%; display: flex; flex-wrap: wrap; justify-content: space-between; padding: 15px;">
    <g:each in="${storeAmenities}" var="storeAmenity" status="i">
        <g:if test="${storeAmenity}">
            <div class="amenity-item responsive-item" style="background-color: #f9f9f9; padding: 15px; text-align: left; display: flex; flex-direction: row; justify-content: space-between; border: 1px solid #e0e0e0; border-radius: 5px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); width: 48%; box-sizing: border-box; margin-bottom: 25px; flex-grow: 0; min-width: 300px;">
                    <div class="amenity-content" style="flex: 1; padding-right: 15px;">
                        <g:hiddenField id="storeAmenities[${i}].amenity.id" name="storeAmenities[${i}].amenity.id" value="${storeAmenity?.amenity?.id}"/>
                        <g:hiddenField id="storeAmenities[${i}].amenity.retailerId" name="storeAmenities[${i}].amenity.retailerId" value="${storeAmenity?.amenity?.retailerId}"/>
                        <g:hiddenField id="storeAmenities[${i}].amenity.name" name="storeAmenities[${i}].amenity.name" value="${storeAmenity?.amenity?.name}"/>
                        <g:hiddenField id="storeAmenities[${i}].storeId" name="storeAmenities[${i}].storeId" value="${storeAmenity?.storeId}"/>
                        <g:hiddenField id="storeAmenities[${i}].additionalDetail" name="storeAmenities[${i}].additionalDetail" value="${storeAmenity?.additionalDetail}"/>
                        <g:hiddenField id="storeAmenities[${i}].count" name="storeAmenities[${i}].count" value="${storeAmenity?.count}"/>
                        <div class="amenity-name"><g:message code="Amenity.${storeAmenity?.amenity?.name}" default="${storeAmenity?.amenity?.name}" /></div>
                        <g:if test="${storeAmenity?.additionalDetail}">
                            <div class="amenity-detail"><strong>Description:</strong> ${storeAmenity?.additionalDetail ?: 'Not specified'}</div>
                        </g:if>

                        <g:if test="${storeAmenity?.count}">
                            <div class="amenity-detail"><strong>Available Quantity:</strong> ${storeAmenity?.count}</div>
                        </g:if>

                        <g:if test="${storeAmenity?.availability?.size() > 0}">
                            <div class="amenity-detail"><strong>Opening Hours:</strong></div>
                            <g:each in="${storeAmenity?.availability}" var="day" status="j">
                                <div class="enableDays[j]">
                                    <g:if test="${day.restrictionEnabled}">
                                        ${day.day}
                                        <g:if test="${day.timeFrom}">
                                            from ${day.timeFrom}
                                        </g:if>
                                        <g:if test="${day.timeTo}">
                                            to ${day.timeTo}
                                        </g:if>
                                    </g:if>
                                    <g:hiddenField id="storeAmenities[${i}].availability[${j}].day" name="storeAmenities[${i}].availability[${j}].day" value="${day.day}"/>
                                    <g:hiddenField id="storeAmenities[${i}].availability[${j}].timeFrom" name="storeAmenities[${i}].availability[${j}].timeFrom" value="${day.timeFrom}"/>
                                    <g:hiddenField id="storeAmenities[${i}].availability[${j}].timeTo" name="storeAmenities[${i}].availability[${j}].timeTo" value="${day.timeTo}"/>
                                    <g:hiddenField id="storeAmenities[${i}].availability[${j}].restrictionEnabled" name="storeAmenities[${i}].availability[${j}].restrictionEnabled" value="${day.restrictionEnabled}"/>
                                </div>
                            </g:each>
                        </g:if>
                    </div>
                    <div class="amenity-actions" style="flex: 0 0 auto; display: flex; flex-direction: row; gap: 10px; align-items: flex-start;">
                        <a href="#" class="btn btn-sm btn-wl mr-2" style="min-width: 80px; font-size: 0.9rem;" onclick="editAmenities(${i})">Edit</a>
                        <a href="#" class="btn btn-sm btn-danger" style="min-width: 80px; font-size: 0.9rem;" onclick="deleteStoreAmenity(${i})">Delete</a>
                    </div>
                </div>
            </g:if>
        </g:each>
    </div>

<style>
    @media screen and (max-width: 1300px) {
        .responsive-item {
            width: 100% !important;
            min-width: 100% !important;
        }
    }

    @media screen and (max-width: 576px) {
        .amenity-item {
            flex-direction: column !important;
        }

        .amenity-content {
            padding-right: 0 !important;
            margin-bottom: 15px;
        }

        .amenity-actions {
            width: 100%;
            justify-content: flex-start;
        }
    }
</style>