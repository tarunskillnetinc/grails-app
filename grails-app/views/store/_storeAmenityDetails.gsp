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
</style>

<div class="amenities-container">
    <g:each in="${storeAmenities}" var="storeAmenity" status="counter">
        <div class="amenity-item">
            <div class="amenity-content">
                <div class="amenity-name"><g:message code="Amenity.${storeAmenity?.amenity?.name}" default="${storeAmenity?.amenity?.name}" /></div>
                <g:if test="${storeAmenity?.additionalDetail}">
                    <div class="amenity-detail">Description: ${storeAmenity?.additionalDetail ?: 'Not specified'}</div>
                </g:if>

                <g:if test="${storeAmenity?.count}">
                    <div class="amenity-detail">Available quantity: ${storeAmenity?.count}</div>
                </g:if>

                <g:if test="${storeAmenity?.availability}">
                    <div class="amenity-detail">Opening hours:</div>
                    <ul class="hours-list">
                        <g:each in="${new groovy.json.JsonSlurper().parseText(storeAmenity?.availability ?: '{}')}" var="day">
                            <li>${day.key} ${day.value.from} to ${day.value.to}</li>
                        </g:each>
                    </ul>
                </g:if>
            </div>
            <div class="amenity-actions">
                <a href="#" class="btn btn-sm btn-wl mr-2" style="min-width: 80px; font-size: 0.9rem;" onclick="addStoreAdditionalDetail('${counter}', '${storeAmenity?.amenity?.name}', '${storeAmenity?.additionalDetail}')">Edit</a>
                <a href="#" class="btn btn-sm btn-danger" style="min-width: 80px; font-size: 0.9rem;" onclick="deleteAdditionalDetail(${counter})">Delete</a>
            </div>
        </div>
    </g:each>
</div>