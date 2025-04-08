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
                        <g:each var="openingTime" in="${initialRegularHours}">
                            <div class="d-flex align-items-center mb-3">
                                <!-- Spacer div to align with Description input box -->
                                <div style="width: 30px;"></div>

                                <!-- Checkbox - aligned with Description input -->
                                <div class="me-3 d-flex align-items-center justify-content-center" style="height: 38px; padding-right: 5px">
                                    <g:checkBox name="${openingTime.day}_closed" value="${openingTime.closed}" class="form-check-input" style="width: 20px; height: 20px; cursor: pointer;" />
                                </div>

                                <!-- Day name - aligned with other elements -->
                                <div class="me-3 d-flex align-items-center" style="width: 100px; height: 38px; padding-right: 5px;">
                                    <span class="form-control-plaintext">${openingTime.day}</span>
                                </div>

                                <!-- Start Time -->
                                <div class="me-2" style="width: 120px; padding-right: 5px">
                                    <g:textField name="${openingTime.day}_startTime" value="${openingTime.startTime}" placeholder="Start Time" class="form-control"/>
                                </div>

                                <!-- "to" label - centered vertically with more padding -->
                                <div class="me-2 d-flex align-items-center" style="height: 38px; padding: 0 5px 0 10px;">
                                    <span style="margin: 0 5px;"> to </span>
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