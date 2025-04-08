<style>
    .amenities-container {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        justify-content: space-between;
        width: 100%;
        max-width: 900px;
        margin: 0 auto;
    }

    .amenity-column {
        flex: 0 0 48%;
        min-width: 300px;
        max-width: 450px;
    }

    .amenity-item {
        background-color: #f9f9f9;
        padding: 5px;
        margin-bottom: 10px;
        text-align: left;
    }

    .amenity-name {
        font-weight: bold;
        font-size: 16px;
        margin-bottom: 5px;
    }

    .amenity-detail {
        margin-bottom: 3px;
    }

    .hours-list {
        margin-top: 5px;
        padding-left: 15px;
    }
</style>

<div class="amenities-container">

    <!-- Left Column -->
    <div class="amenity-column">
        <g:each in="${storeAmenities}" var="storeAmenity" status="i">
            <g:if test="${i % 2 == 0}">
                <div class="amenity-item">
                    <div class="amenity-name"><g:message code="Amenity.${storeAmenity?.amenity?.name}" default="${storeAmenity?.amenity?.name}" /></div>
                    <div class="amenity-detail">Description: ${storeAmenity?.additionalDetail ?: 'Not specified'}</div>
                    <div class="amenity-detail">Available quantity: ${storeAmenity?.count}</div>

                    <g:if test="${storeAmenity?.availability}">
                        <div class="amenity-detail">Opening hours:</div>
                        <ul class="hours-list">
                            <g:each in="${new groovy.json.JsonSlurper().parseText(storeAmenity?.availability ?: '{}')}" var="day">
                                <li>${day.key} ${day.value.from} to ${day.value.to}</li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <div class="amenity-detail">Opening hours: Not specified</div>
                    </g:else>
                </div>
            </g:if>
        </g:each>
    </div>

    <!-- Right Column -->
    <div class="amenity-column">
        <g:each in="${storeAmenities}" var="storeAmenity" status="i">
            <g:if test="${i % 2 == 1}">
                <div class="amenity-item">
                    <div class="amenity-name"><g:message code="Amenity.${storeAmenity?.amenity?.name}" default="${storeAmenity?.amenity?.name}" /></div>
                    <div class="amenity-detail">Description: ${storeAmenity?.additionalDetail ?: 'Not specified'}</div>
                    <div class="amenity-detail">Available quantity: ${storeAmenity?.count}</div>

                    <g:if test="${storeAmenity?.availability}">
                        <div class="amenity-detail">Opening hours:</div>
                        <ul class="hours-list">
                            <g:each in="${new groovy.json.JsonSlurper().parseText(storeAmenity?.availability ?: '{}')}" var="day">
                                <li>${day.key} ${day.value.from} to ${day.value.to}</li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <div class="amenity-detail">Opening hours: Not specified</div>
                    </g:else>
                </div>
            </g:if>
        </g:each>
    </div>
</div>