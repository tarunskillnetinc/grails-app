<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Edit Amenities</h2>
    </div>
</section>

<section id="modal-error"></section>

<section id="modal-form">
    <g:form name="addListItemForm">
        <g:hiddenField id="selected.amenity.id" name="selected.amenity.id" value="${selectedAmenity?.amenity?.id}"/>
        <g:hiddenField id="selected.amenity.retailerId" name="selected.amenity.retailerId" value="${selectedAmenity?.amenity?.retailerId}"/>
        <g:hiddenField id="selected.amenity.name" name="selected.amenity.name" value="${selectedAmenity?.amenity?.name}"/>
        <g:hiddenField id="selected.storeId" name="selected.storeId" value="${storeId}"/>

        <section id="additional-details-errors-container" class="container-fluid"></section>

        <!-- First row: Description and Quantity -->
        <div class="row mt-5">
            <!-- Description -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="selected.amenity.description" class="col-4 col-form-label text-right" style="text-align: right; padding-right: 15px;">Description</label>
                    <div class="col-8">
                        <g:field type="text" id="selected.amenity.description" name="selected.amenity.description" class="form-control select-border" maxlength="30" value="${selectedAmenity?.additionalDetail}"/>
                    </div>
                </div>
            </div>

            <!-- Quantity -->
            <div class="col-md-6">
                <div class="form-group row">
                    <label id="value" for="selected.amenity.quantity" class="col-3 col-form-label text-right pr-4">Quantity</label>
                    <div class="col-8">
                        <g:field type="number" id="selected.amenity.quantity" name="selected.amenity.quantity" class="form-control select-border" max="9999" value="${selectedAmenity?.count}" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Second row: Available hours -->
        <div class="row mt-5">
            <div class="col-md-12">
                <div class="form-group row">
                    <label for="selected.amenity.availability" class="col-2 col-form-label text-right" style="text-align: right; padding-right: 15px;">Available Hours</label>
                    <div class="col-10" style="padding-left: 0;">
                        <!-- Grid layout for fixed positioning -->
                        <g:each var="enableHours" in="${selectedAmenity?.availability}" status="i">
                            <div id="enableHours[${i}].days" style="display: grid; grid-template-columns: 40px 100px 120px 50px 120px; grid-gap: 15px; width: 100%; margin-left: 7px;">
                                        <div style="grid-column: 1; display: flex; align-items: center; justify-content: center;"> <!-- Checkbox -->
                                            <g:checkBox name="enableHour[${i}].restrictionEnabled" value="${enableHours.restrictionEnabled}" checked="${enableHours.restrictionEnabled}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer; margin: 0;" />
                                        </div>

                                        <div style="grid-column: 2; display: flex; align-items: center; overflow: hidden;">  <!-- Day name - with text overflow handling -->
                                            <span  class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${enableHours.day}</span>
                                            <g:hiddenField id="enableHour[${i}].day" name="enableHour[${i}].day" value="${enableHours.day}"/>
                                        </div>

                                        <div style="grid-column: 3;"> <!-- Start Time -->
                                            <g:field type="time" name="enableHour[${i}].startTime" id="enableHour[${i}].startTime" value="${enableHours.timeFrom}" class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                        </div>

                                        <div> <!-- "to" label -->
                                            <span>to</span>
                                        </div>

                                        <div style="grid-column: 5;"> <!-- End Time -->
                                            <g:field type="time" name="enableHour[${i}].endTime" id="enableHour[${i}].endTime" value="${enableHours.timeTo}" class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                                        </div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAmenityAddModal();">Cancel</button>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveAmenities(${index}, ${storeId});">Save</button>
    </div>
</section>