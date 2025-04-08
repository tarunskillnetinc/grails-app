<section id="modal-header">
    <div class="modal-header">
        <h2 id="page-title" class="mx-auto my-auto">Add Amenities</h2>
    </div>
</section>

<section id="modal-error"></section>

<section id="modal-form">
    <g:form name="addListItemForm">
        <script>
            $(document).ready(function () {
                // JavaScript code commented out in original
            });
        </script>

        <section id="additional-details-errors-container" class="container-fluid"></section>

        <div class="row">
            <div class="col-md-6 mt-5">
                <!-- Left column -->
                <div class="form-group row">
                    <label for="addStoreAdditionalDetailDescription" class="col-4 col-form-label-mandatory text-right" style="text-align: right; padding-right: 15px;">Description</label>
                    <div class="col-6">
                        <g:field type="text" id="addStoreAdditionalDetailDescription" name="addStoreAdditionalDetailDescription" class="form-control select-border" maxlength="20" value="${description}"/>
                    </div>
                </div>

                <div class="form-group row mt-3">
                    <label for="addAmenityAvailableHours" class="col-4 col-form-label-mandatory text-right" style="text-align: right; padding-right: 15px;">Available hours</label>
                    <div class="col-8" style="padding-left: 8px;">
                        <!-- Grid layout for fixed positioning -->
                        <div style="display: grid; grid-template-columns: 30px 40px 100px 120px 50px 120px; grid-gap: 15px; width: 100%;">
                            <g:each var="openingTime" in="${initialRegularHours}">
                                <!-- Spacer -->
                                <div style="grid-column: 1; display: flex; align-items: center;"></div>

                                <!-- Checkbox -->
                                <div style="grid-column: 2; display: flex; align-items: center; justify-content: center;">
                                    <g:checkBox name="${openingTime.day}_closed" value="${openingTime.closed}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer; margin: 0;" />
                                </div>

                                <!-- Day name - with text overflow handling -->
                                <div style="grid-column: 3; display: flex; align-items: center; overflow: hidden;">
                                    <span class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${openingTime.day}</span>
                                </div>

                                <!-- Start Time -->
                                <div style="grid-column: 4;">
                                    <g:textField name="${openingTime.day}_startTime" value="${openingTime.startTime}" placeholder="Start Time" class="form-control"/>
                                </div>

                                <!-- "to" label -->
                                <div style="grid-column: 5; display: flex; align-items: center; justify-content: center;">
                                    <span>to</span>
                                </div>

                                <!-- End Time -->
                                <div style="grid-column: 6;">
                                    <g:textField name="${openingTime.day}_endTime" value="${openingTime.endTime}" placeholder="End Time" class="form-control"/>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 mt-5">
                <!-- Right column -->
                <div class="form-group row">
                    <label id="value" for="addStoreAdditionalDetailValue" class="col-3 col-form-label text-right pr-4">Value</label>
                    <div class="col-6">
                        <g:field id="addStoreAdditionalDetailValue" name="addStoreAdditionalDetailValue" class="form-control select-border" maxlength="240" value="${value}" />
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