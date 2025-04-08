<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Add Amenities</h2>
    </div>
</section>
<section id="modal-error">
</section>
<section id="modal-form">
    <g:form name="addListItemForm">

        <script>
            $(document).ready(function () {
                    // $('#addListItemForm').bind('keydown', function (e) {
                    //     if (e.keyCode == 13) {
                    //         $('#saveAddSupplierButton').click();
                    //         e.preventDefault()
                    //     }
                    // });

                // Add event listener for the description field
                // $("#addStoreAdditionalDetailDescription").on('input', function() {
                //     // Clear error message when user starts typing
                //     $("#additional-details-errors-container").empty().hide();
                //     // Remove invalid class from the input
                //     $(this).removeClass("is-invalid");
                // });

            });
        </script>

        <section id="additional-details-errors-container" class="container-fluid"></section>

        <div class="row form-group mb-4 mt-4">
            <div class="col-5">
                <div class="row">
                    <label for="addStoreAdditionalDetailDescription" class="col-4 col-form-label-mandatory text-right" style="text-align: right; padding-right: 15px;">Description</label>
                    <div class="col-8" style="padding-left: 27px;">
                        <g:field type="text" id="addStoreAdditionalDetailDescription" name="addStoreAdditionalDetailDescription" class="form-control select-border" maxlength="20" value="${description}"/>
                    </div>
                </div>
            </div>

            <div class="col-7">
                <div class="row">
                    <label id="value" for="addStoreAdditionalDetailValue" class="col-3 col-form-label text-right pr-4">Value</label>
                    <div class="col-9 pr-5">
                        <g:field id="addStoreAdditionalDetailValue" name="addStoreAdditionalDetailValue" class="form-control select-border" maxlength="240" value="${value}"  />
                    </div>
                </div>
            </div>
        </div>

        <!-- Remove the mb-4 mt-4 classes to eliminate the gap -->
        <div class="row form-group">
            <div class="col-12">
                <div class="row align-items-start">
                    <div class="col-2" style="flex: 0 0 20.833%; padding-right: 0;">
                        <label for="addAmenityAvailableHours" class="col-form-label-mandatory" style="float: right; padding-top: 10px; white-space: nowrap;">Available hours:</label>
                    </div>
                    <div class="business-hours-form col-10" style="flex: 0 0 79.167%; padding-left: 10px;">
                        <g:each var="openingTime" in="${initialRegularHours}">
                            <div class="d-flex align-items-center mb-3">
                                <!-- Checkbox - aligned with the label -->
                                <div class="me-3 d-flex align-items-center justify-content-center" style="width: 30px; height: 38px; margin-left: -10px;">
                                    <g:checkBox name="${openingTime.day}_closed" value="${openingTime.closed}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer;" />
                                </div>

                                <!-- Day name - aligned with other elements -->
                                <div class="me-3 d-flex align-items-center" style="width: 100px; height: 38px;">
                                    <span class="form-control-plaintext">${openingTime.day}</span>
                                </div>

                                <!-- Start Time -->
                                <div class="me-2" style="width: 120px;">
                                    <g:textField name="${openingTime.day}_startTime" value="${openingTime.startTime}" placeholder="Start Time" class="form-control"/>
                                </div>

                                <!-- "to" label - centered vertically -->
                                <div class="me-2 d-flex align-items-center" style="height: 38px;">
                                    <span> to </span>
                                </div>

                                <!-- End Time -->
                                <div style="width: 120px;">
                                    <g:textField name="${openingTime.day}_endTime" value="${openingTime.endTime}" placeholder="End Time" class="form-control"/>
                                </div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </div>
    </g:form>

    <div class="modal-footer">
        <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAdditionalDetailAddModal();">Cancel</button>
        <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveStoreAdditionalDetail();">Save</button>
    </div>
</section>