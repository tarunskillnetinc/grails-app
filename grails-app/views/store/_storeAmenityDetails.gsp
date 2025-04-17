<style>
    .amenities-container {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 50px; /* Significantly increased gap between columns */
        width: 100%;
        max-width: 1000px; /* Increased max-width to accommodate wider spacing */
        margin: 0 auto;
        padding: 0 15px; /* Added padding to container */
    }

    .amenity-item {
        background-color: #f9f9f9;
        padding: 15px; /* Increased padding */
        margin-bottom: 15px;
        text-align: left;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        border-radius: 4px;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }

    .amenity-content {
        flex: 1;
        padding-right: 15px;
    }

    .amenity-actions {
        flex: 0 0 auto;
        display: flex;
        flex-direction: row;
        gap: 10px; /* Increased gap between buttons */
        align-items: flex-start;
    }

    .amenity-name {
        font-weight: bold;
        font-size: 16px;
        margin-bottom: 8px;
    }

    .amenity-detail {
        margin-bottom: 5px;
    }

    .hours-list {
        margin-top: 5px;
        padding-left: 15px;
    }

    @media (max-width: 992px) {
        .amenities-container {
            gap: 30px; /* Reduced gap on smaller screens */
        }
    }

    @media (max-width: 768px) {
        .amenities-container {
            grid-template-columns: 1fr;
            gap: 20px;
        }

        .amenity-actions {
            flex-direction: row;
        }
    }

    .hours-list {
        margin-top: 5px;
        padding-left: 15px;
    }

    .hour-item {
        margin-bottom: 3px;
    }
</style>

<div class="amenities-container">
    <g:each in="${storeAmenities}" var="storeAmenity" status="i">
        <g:if test="${storeAmenity}">
            <div class="amenity-item">
                <div class="amenity-content">
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

                    <g:if test="${storeAmenity?.availability}">
                        <div class="amenity-detail"><strong>Opening Hours:</strong></div>
                        <g:each in="${storeAmenity?.availability}" var="day" status="j">
                            <div class="hour-item">
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
                <div class="amenity-actions">
                    <a href="#" class="btn btn-sm btn-wl mr-2" style="min-width: 80px; font-size: 0.9rem;" onclick="editAmenities(${i}, ${storeAmenity?.amenity?.id}, ${storeAmenity?.storeId})">Edit</a>
                    <a href="#" class="btn btn-sm btn-danger" style="min-width: 80px; font-size: 0.9rem;" onclick="deleteStoreAmenity(${i})">Delete</a>
                </div>
            </div>
        </g:if>
    </g:each>
</div>