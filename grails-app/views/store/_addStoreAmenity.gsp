<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Add Amenities</h2>
    </div>
</section>

<section id="modal-error"></section>

<section id="modal-form">
    <g:form name="addListItemForm">
        <g:hiddenField id="amenityId" name="amenityId" value="${selectedAmenity?.amenity?.id}"/>
        <g:hiddenField id="storeId" name="storeId" value="${selectedAmenity?.store?.id}"/>

        <section id="additional-details-errors-container" class="container-fluid"></section>

        <!-- First row: Description and Quantity -->
        <div class="row mt-5">
            <!-- Description -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addStoreAdditionalDetailDescription" class="col-4 col-form-label text-right" style="text-align: right; padding-right: 15px;">Description</label>
                    <div class="col-8">
                        <g:field type="text" id="addStoreAdditionalDetailDescription" name="addStoreAdditionalDetailDescription" class="form-control select-border" max="9999" maxlength="4" value="${selectedAmenity?.additionalDetail}"/>
                    </div>
                </div>
            </div>

            <!-- Quantity -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label id="value" for="addStoreAdditionalDetailValue" class="col-3 col-form-label text-right pr-4">Quantity</label>
                    <div class="col-8">
                        <g:field id="addStoreAdditionalDetailValue" name="addStoreAdditionalDetailValue" class="form-control select-border" maxlength="240" value="${selectedAmenity?.count}" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Second row: Available hours -->
        <div class="row mt-5">
            <div class="col-md-12">
                <div class="form-group row">
                    <label for="addAmenityAvailableHours" class="col-2 col-form-label text-right" style="text-align: right; padding-right: 15px;">Available hours</label>
                    <div class="col-10" style="padding-left: 0;">
                        <!-- Grid layout for fixed positioning -->
                        <div style="display: grid; grid-template-columns: 40px 100px 120px 50px 120px; grid-gap: 15px; width: 100%; margin-left: 7px;">
                            <g:each var="openingTime" in="${initialRegularHours}" status="i">
                                <!-- Checkbox -->
                                <div style="grid-column: 1; display: flex; align-items: center; justify-content: center;">
                                    <g:checkBox name="openingTime[${i}].closed" value="${openingTime.closed}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer; margin: 0;" />
                                </div>

                                <!-- Day name - with text overflow handling -->
                                <div style="grid-column: 2; display: flex; align-items: center; overflow: hidden;">
                                    <span id="openingTime[${i}].day" class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${openingTime.day}</span>
                                </div>

                                <!-- Start Time -->
                                <div style="grid-column: 3;">
                                    <g:field type="time" name="openingTime[${i}].startTime" id="openingTime[${i}].startTime" value="${openingTime.startTime}"
                                             class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                </div>

                                <!-- "to" label -->
                                <div>
                                    <span>to</span>
                                </div>

                                <!-- End Time -->
                                <div style="grid-column: 5;">
                                    <g:field type="time" name="openingTime[${i}].endTime" id="openingTime[${i}].endTime" value="${openingTime.endTime}"
                                             class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAmenityAddModal();">Cancel</button>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="addStoreAmenity();">Save</button>
    </div>
</section>