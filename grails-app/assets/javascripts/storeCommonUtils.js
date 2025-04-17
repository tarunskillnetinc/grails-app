function addStoreAdditionalDetail(index, description, value) {
    $("#addStoreAdditionalDetailsContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#addStoreAdditionalDetailsModal').modal({show: true, backdrop: 'static', keyboard: false});
    var params = {index: index, description: description, value: value}
    $.ajax({
        url: addStoreAdditionalDetails,
        method: "GET",
        data: params,
        success: function (resp) {
            $("#addStoreAdditionalDetailsContent").html(resp);
        }
    });
}

function saveStoreAdditionalDetail(){
    var params = {}
    var additionalDetailContainer = $("#storeAdditionalDetailsContainer > div");

    additionalDetailContainer.each(function(loopIndex) {
        var description = $("#storeAdditionalDetails\\[" + loopIndex + "\\]\\.description").val()
        var value = $("#storeAdditionalDetails\\[" + loopIndex + "\\]\\.value").val()
        params["storeAdditionalDetails[" + loopIndex + "].description"] = description;
        params["storeAdditionalDetails[" + loopIndex + "].value"] = value;
    });

    var updatedDescription = $("#addStoreAdditionalDetailDescription").val()
    var updatedValue = $("#addStoreAdditionalDetailValue").val()

    // Add validation for updatedDescription
    if (!updatedDescription || updatedDescription.trim() === "") {
        $("#additional-details-errors-container").html(
            '<div class="alert alert-danger alert-wl mx-0" role="alert">Description cannot be empty</div>'
        ).show();
        $("#addStoreAdditionalDetailDescription").addClass("is-invalid");
        return; // Stop execution of the function
    } else {
        // Clear any previous error message and styling
        $("#additional-details-errors-container").hide();
        $("#addStoreAdditionalDetailDescription").removeClass("is-invalid");
    }

    var index = $("#addStoreAdditionalDetailIndex").val()
    if (index === null || index === undefined || index === "") {
        var lastVariantContainer = $("#storeAdditionalDetailsContainer > div:last-child");
        if (lastVariantContainer.length > 0) {
            index = parseInt(lastVariantContainer[0].id.split('-')[1]) + 1;
        } else {
            index = 0;
        }
    } else {
        index = parseInt(index, 10);
    }

    params["storeAdditionalDetails[" + index + "].description"] = updatedDescription;
    params["storeAdditionalDetails[" + index + "].value"] = updatedValue;
    params["index"] = index;

    $.ajax({
        url: saveStoreAdditionalDetails,
        method: "POST",
        data: params,
        success: function(resp) {
            var storeAdditionalDetailsContainer = $("#storeAdditionalDetailsContainer");
            storeAdditionalDetailsContainer.html(resp);
            safelyCloseModal('#addStoreAdditionalDetailsModal');
        }
    });
}

function deleteAdditionalDetail(index) {
    if (confirm("Are you sure you want to delete?")) {
        var additionalDetailContainer = $("#storeAdditionalDetailsContainer > div");
        if (additionalDetailContainer.length) {
            var params = {}
            additionalDetailContainer.each(function(loopIndex) {
                if (loopIndex !== index) {
                    var description = $("#storeAdditionalDetails\\[" + loopIndex + "\\]\\.description").val();
                    var value = $("#storeAdditionalDetails\\[" + loopIndex + "\\]\\.value").val();
                    params["storeAdditionalDetails[" + loopIndex + "].description"] = description;
                    params["storeAdditionalDetails[" + loopIndex + "].value"] = value;
                }
            });

            $.ajax({
                url: saveStoreAdditionalDetails,
                method: "POST",
                data: params,
                success: function(resp) {
                    var storeAdditionalDetailsContainer = $("#storeAdditionalDetailsContainer");
                    storeAdditionalDetailsContainer.html(resp);
                    safelyCloseModal('#addStoreAdditionalDetailsModal');
                }
            });
        }
    }
}

function closeStoreAdditionalDetailAddModal() {
    if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        safelyCloseModal('#addStoreAdditionalDetailsModal');
    }
}

function addOtherRestrictions(index, description, startDateTime, endDateTime) {
    $("#addStoreOtherRestrictionContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#addStoreOtherRestrictionModal').modal({show: true, backdrop: 'static', keyboard: false});
    var params = {index: index, description: description, startDateTime: startDateTime, endDateTime:endDateTime }
    $.ajax({
        url: addStoreOtherRestrictionsValues, // Replace with the actual URL endpoint
        method: "GET",
        data: params,
        success: function (resp) {
            try {
                $("#addStoreOtherRestrictionContent").html(resp);
            } catch (e) {
                console.error('Error processing response:', e);
                $("#addStoreOtherRestrictionContent").html("Error loading content. Please try again.");
            }
        },
        error: function(xhr, status, error) {
            console.error('AJAX error:', error);
            $("#addStoreOtherRestrictionContent").html("Error loading content. Please try again.");
        }
    });
}

function closeOtherRestrictionsAddModal(){
    if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        safelyCloseModal('#addStoreOtherRestrictionModal');
    }
}

function saveOtherRestrictions() {
    // Get values from the form
    const description = $("#otherRestrictionDescription").val();
    const startDateTime = $("#startDateTimePicker").val();
    const endDateTime = $("#endDateTimePicker").val();

    // Create params object for AJAX submission
    let params = {};

    // Add regular hours to params
    $("#restriction-days-tbl tbody tr").each(function(loopIndex) {
        const row = $(this);
        const isEnabled = row.find("input[name^='storeRestrictions.regularHours'][name$='.restrictionEnabled']").is(':checked');
        const day = row.find("td:nth-child(2)").text().trim();
        params[`regularHours[${loopIndex}].restrictionEnabled`] = isEnabled;
        params[`regularHours[${loopIndex}].day`] = day;

        if (isEnabled) {
            // Only get and set time values if enabled
            const timeFrom = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeFrom']").val();
            const timeTo = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeTo']").val();
            params[`regularHours[${loopIndex}].timeFrom`] = timeFrom;
            params[`regularHours[${loopIndex}].timeTo`] = timeTo;
        } else {
            // Explicitly set to empty string or null depending on what your backend expects
            params[`regularHours[${loopIndex}].timeFrom`] = ""; // or null
            params[`regularHours[${loopIndex}].timeTo`] = ""; // or null
        }

    });

    // Add existing other restrictions to params
    let otherRestrictionsCount = 0;
    $("#other-restrictions-special-days-tbl tbody tr").each(function(index) {
        const row = $(this);
        const existingDescription = row.find("input[name$='.description']").val();
        const existingStartDateTime = row.find("input[name$='.startDateTime']").val() || row.find("input[name$='.date']").val();
        const existingEndDateTime = row.find("input[name$='.endDateTime']").val() || row.find("input[name$='.startTime']").val();
        params["otherRestrictions[" + otherRestrictionsCount + "].description"] = existingDescription;
        params["otherRestrictions[" + otherRestrictionsCount + "].startDateTime"] = existingStartDateTime;
        params["otherRestrictions[" + otherRestrictionsCount + "].endDateTime"] = existingEndDateTime;
        otherRestrictionsCount++;
    });

    var index = $("#otherRestrictionIndex").val();
    if (index === null || index === undefined || index === "") {
        index = getNextOtherRestrictionsIndex();
    } else {
        index = parseInt(index, 10);
    }

    // Add the new restriction
    params["otherRestrictions[" + index + "].description"] = description;
    params["otherRestrictions[" + index + "].startDateTime"] = startDateTime;
    params["otherRestrictions[" + index + "].endDateTime"] = endDateTime;
    params["index"] = index;

    // Submit via AJAX
    $.ajax({
        url: saveStoreOtherRestrictions,
        type: 'POST',
        data: params,
        success: function(resp) {
            safelyCloseModal('#addStoreOtherRestrictionModal');
            var storeRestrictionContainer = $("#storeRestrictionsContainer");
            storeRestrictionContainer.html(resp);
            attachCheckboxListeners()
        },
        error: function(xhr, status, error) {
            $("#other-restrictions-errors-container").append(
                '<div class="alert alert-danger">Error saving data: ' + error + '</div>'
            ).show();
        }
    });
}

function deleteOtherRestriction(index) {
    if (confirm("Are you sure you want to delete?")) {
        var otherRestrictionContainer = $("#storeRestrictionsContainer > div");
        if (otherRestrictionContainer.length) {
            var params = {}
            $("#restriction-days-tbl tbody tr").each(function(loopIndex) {
                const row = $(this);
                const isEnabled = row.find("input[name^='storeRestrictions.regularHours'][name$='.restrictionEnabled']").is(':checked');
                const day = row.find("td:nth-child(2)").text().trim();
                params[`regularHours[${loopIndex}].restrictionEnabled`] = isEnabled;
                params[`regularHours[${loopIndex}].day`] = day;
                if (isEnabled) {
                    // Only get and set time values if enabled
                    const timeFrom = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeFrom']").val();
                    const timeTo = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeTo']").val();
                    params[`regularHours[${loopIndex}].timeFrom`] = timeFrom;
                    params[`regularHours[${loopIndex}].timeTo`] = timeTo;
                } else {
                    // Explicitly set to empty string or null depending on what your backend expects
                    params[`regularHours[${loopIndex}].timeFrom`] = ""; // or null
                    params[`regularHours[${loopIndex}].timeTo`] = ""; // or null
                }
            });


            // Add existing other restrictions to params
            $("#other-restrictions-special-days-tbl tbody tr").each(function(loopIndex) {
                const row = $(this);
                const existingDescription = row.find("input[name$='.description']").val();
                const existingStartDateTime = row.find("input[name$='.startDateTime']").val() || row.find("input[name$='.date']").val();
                const existingEndDateTime = row.find("input[name$='.endDateTime']").val() || row.find("input[name$='.startTime']").val();
                if (loopIndex !== index) {
                    params["otherRestrictions[" + loopIndex + "].description"] = existingDescription;
                    params["otherRestrictions[" + loopIndex + "].startDateTime"] = existingStartDateTime;
                    params["otherRestrictions[" + loopIndex + "].endDateTime"] = existingEndDateTime;
                }
            });


            $.ajax({
                url: saveStoreOtherRestrictions,
                method: "POST",
                data: params,
                success: function(resp) {
                    otherRestrictionContainer.html(resp);
                    safelyCloseModal('#addStoreOtherRestrictionModal');
                }
            });
        }
    }
}

function getNextOtherRestrictionsIndex() {
    // Get the current highest index from the table rows
    var highestIndex = -1;

    $("#other-restrictions-special-days-tbl tbody tr").each(function() {
        var rowId = $(this).attr('id');
        if (rowId && rowId.startsWith('special-hour-row-')) {
            var rowIndex = parseInt(rowId.split('-').pop(), 10);
            if (!isNaN(rowIndex) && rowIndex > highestIndex) {
                highestIndex = rowIndex;
            }
        }
    });

    // Return the next available index
    return highestIndex + 1;
}

function attachCheckboxListeners() {
    const checkboxes = document.querySelectorAll('input[name^="storeRestrictions.regularHours"][name$=".restrictionEnabled"]');

    checkboxes.forEach(function(checkbox) {
        // Remove any existing event listeners to prevent duplicates
        checkbox.removeEventListener('change', toggleTimeInputs);

        // Add event listener for checkbox changes
        checkbox.addEventListener('change', toggleTimeInputs);
    });
}

// Function to handle checkbox changes
function toggleTimeInputs() {
    // Find the time input fields in the same row
    const timeInputsContainer = this.closest('tr').querySelector('.restriction-time-inputs');
    timeInputsContainer.style.display = this.checked ? 'flex' : 'none';
}

function safelyCloseModal(modalId) {
    // First, get references to all elements
    const modal = $(modalId);

    // Store references to dropdowns before closing modal
    const activeDropdowns = $('.dropdown-toggle[aria-expanded="true"]');

    // Move the modal to the body element before closing it
    if (!modal.parent().is('body')) {
        modal.detach().appendTo('body');
    }

    // Hide the modal properly
    modal.modal('hide');

    // Force cleanup of backdrop
    $('.modal-backdrop').remove();
    $('body').removeClass('modal-open').css('padding-right', '').css('overflow', '');

    // Fix dropdown aria attributes after modal is closed
    setTimeout(function() {
        // Reset all dropdowns first
        $('.dropdown-toggle').attr('aria-expanded', 'false');
        $('.dropdown-menu').removeClass('show');

        // Then properly reinitialize them
        $('.dropdown-toggle').each(function() {
            try {
                $(this).dropdown('dispose');
            } catch(e) {}
            $(this).dropdown();
        });

        // Force any open dropdowns to close
        $('.dropdown-menu.show').removeClass('show');
        $('.dropdown.show').removeClass('show');

        // Force browser to recalculate layout
        document.body.offsetHeight;

        // Return focus to the accordion
        $('.accordion-button:not(.collapsed)').focus();

        // Force a browser repaint
        $(window).trigger('resize');
    }, 150);

    // Add direct click handler to ensure dropdowns work
    $(document).off('click.fixDropdowns', '.dropdown-toggle').on('click.fixDropdowns', '.dropdown-toggle', function(e) {
        const menu = $(this).next('.dropdown-menu');
        const isExpanded = $(this).attr('aria-expanded') === 'true';

        // Toggle this dropdown's aria-expanded attribute correctly
        $(this).attr('aria-expanded', !isExpanded);
    });
}

function editAmenities(index, amenityId, storeId) {
    $("#addAmenitiesContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#addAmenitiesModal').modal({show: true, backdrop: 'static', keyboard: false});
    var params = {
        amenityId: amenityId,
        storeId: storeId,
        index: index
    }
    $.ajax({
        url: addAmenity,
        method: "GET",
        data: params,
        success: function (resp) {
            $("#addAmenitiesContent").html(resp);
        }
    });
}

function addAmenities() {
    const selectedCheckboxes = $('input[name="amenities"]:checked');
    var params = {}

    // Extract just the IDs into an array
    const selectedIds = selectedCheckboxes.map(function() {
        return $(this).val();
    }).get();

    // If you need to add each ID individually with an index
    selectedIds.forEach((id, index) => {
        params["amenities[" + index + "].amenityId"] = id;
    });

    $.ajax({
        url: addAmenity,
        method: "GET",
        data: params,
        success: function (resp) {
            $("#addAmenitiesContent").html(resp);
        }
    });
}

function closeStoreAmenityAddModal(){
    if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        $('#addAmenitiesModal').modal('hide')
    }
}

function saveAmenities(selectedIndex) {
    var params = {};

    // Select all amenity items in the container
    var amenityItems = $(".amenities-container > div.amenity-item");

    amenityItems.each(function(index) {
        // Amenity basic properties
        params["storeAmenities[" + index + "].amenity.id"] = $("#storeAmenities\\[" + index + "\\]\\.amenity\\.id").val();
        params["storeAmenities[" + index + "].amenity.retailerId"] = $("#storeAmenities\\[" + index + "\\]\\.amenity\\.retailerId").val();
        params["storeAmenities[" + index + "].amenity.name"] = $("#storeAmenities\\[" + index + "\\]\\.amenity\\.name").val();
        params["storeAmenities[" + index + "].storeId"] = $("#storeAmenities\\[" + index + "\\]\\.storeId").val();

        // Additional details if present
        var additionalDetail = $("#storeAmenities\\[" + index + "\\]\\.additionalDetails");
        if (additionalDetail.length) {
            params["storeAmenities[" + index + "].additionalDetails"] = additionalDetail.val();
        }

        // Count if present
        var count = $("#storeAmenities\\[" + index + "\\]\\.count");
        if (count.length) {
            params["storeAmenities[" + index + "].count"] = count.val();
        }

        // Availability hours
        var j = 0;
        var day = $("#storeAmenities\\[" + index + "\\]\\.availability\\[" + j + "\\]\\.day");
        while (day.length) {
            params["storeAmenities["  + index + "].availability[" + j + "].day"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.day").val();
            params["storeAmenities["  + index + "].availability[" + j + "].timeFrom"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.timeFrom").val();
            params["storeAmenities["  + index + "].availability[" + j + "].timeTo"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.timeTo").val();
            params["storeAmenities["  + index + "].availability[" + j + "].restrictionEnabled"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.restrictionEnabled").val();
            j++;
        }
    });

    // If selectedIndex is provided, directly replace that amenity with the modal form data
    if (selectedIndex !== undefined && selectedIndex !== null) {
        // Replace amenity basic properties
        params["storeAmenities[" + selectedIndex + "].amenity.id"] = $("#selected\\.amenity\\.id").val();
        params["storeAmenities[" + selectedIndex + "].amenity.retailerId"] = $("#selected\\.amenity\\.retailerId").val();
        params["storeAmenities[" + selectedIndex + "].amenity.name"] = $("#selected\\.amenity\\.name").val();
        params["storeAmenities[" + selectedIndex + "].additionalDetail"] = $("#selected\\.amenity\\.description").val();
        params["storeAmenities[" + selectedIndex + "].count"] = $("#selected\\.amenity\\.quantity").val() || "0";

        // Find all day containers
        $("div[id^='enableHours[']").each(function(i) {
            var dayValue = $("input[name='enableHour[" + i + "].day']").val();
            var timeFrom = $("input[name='enableHour[" + i + "].startTime']").val();
            var timeTo = $("input[name='enableHour[" + i + "].endTime']").val();
            var restrictionEnabled = $("input[name='enableHour[" + i + "].restrictionEnabled']").is(":checked");

            params["storeAmenities[" + selectedIndex + "].availability[" + i + "].day"] = dayValue;
            params["storeAmenities[" + selectedIndex + "].availability[" + i + "].timeFrom"] = timeFrom;
            params["storeAmenities[" + selectedIndex + "].availability[" + i + "].timeTo"] = timeTo;
            params["storeAmenities[" + selectedIndex + "].availability[" + i + "].restrictionEnabled"] = restrictionEnabled;
        });
    }

    $.ajax({
        url: addStoreAmenities,
        type: 'POST',
        data: params,
        success: function(resp) {
            safelyCloseModal('#addAmenitiesModal');
            var storeAmenitiesContainer = $("#storeAmenitiesContainer");
            storeAmenitiesContainer.html(resp);
        },
        error: function(xhr, status, error) {
            console.error('Error updating amenities:', error);
        }
    });
}

function deleteStoreAmenity(index) {
    if (confirm("Are you sure you want to delete?")) {
        var storeAmenitiesContainer = $("#storeAmenitiesContainer > div");
        if (storeAmenitiesContainer.length) {
            var params = {}
            var amenityItems = $(".amenities-container > div.amenity-item");
            amenityItems.each(function(loopIndex) {
                if (index !== loopIndex) {
                    // Amenity basic properties
                    params["storeAmenities[" + loopIndex + "].amenity.id"] = $("#storeAmenities\\[" + loopIndex + "\\]\\.amenity\\.id").val();
                    params["storeAmenities[" + loopIndex + "].amenity.retailerId"] = $("#storeAmenities\\[" + loopIndex + "\\]\\.amenity\\.retailerId").val();
                    params["storeAmenities[" + loopIndex + "].amenity.name"] = $("#storeAmenities\\[" + loopIndex + "\\]\\.amenity\\.name").val();
                    params["storeAmenities[" + loopIndex + "].storeId"] = $("#storeAmenities\\[" + loopIndex + "\\]\\.storeId").val();

                    // Additional details if present
                    var additionalDetail = $("#storeAmenities\\[" + loopIndex + "\\]\\.additionalDetail");
                    if (additionalDetail.length) {
                        params["storeAmenities[" + loopIndex + "].additionalDetail"] = additionalDetail.val();
                    }

                    // Count if present
                    var count = $("#storeAmenities\\[" + loopIndex + "\\]\\.count");
                    if (count.length) {
                        params["storeAmenities[" + loopIndex + "].count"] = count.val();
                    }

                    // Availability hours
                    var j = 0;
                    var day = $("#storeAmenities\\[" + loopIndex + "\\]\\.availability\\[" + j + "\\]\\.day");
                    while (day.length) {
                        params["storeAmenities["  + loopIndex + "].availability[" + j + "].day"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.day").val();
                        params["storeAmenities["  + loopIndex + "].availability[" + j + "].timeFrom"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.timeFrom").val();
                        params["storeAmenities["  + loopIndex + "].availability[" + j + "].timeTo"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.timeTo").val();
                        params["storeAmenities["  + loopIndex + "].availability[" + j + "].restrictionEnabled"] = $("#storeAmenities\\[" + i + "\\]\\.availability\\[" + j + "\\]\\.restrictionEnabled").val();
                        j++;
                    }
                }
            });
            $.ajax({
                url: addStoreAmenities,
                method: "POST",
                data: params,
                success: function(resp) {
                    safelyCloseModal('#addAmenitiesModal');
                    var storeAmenitiesContainer = $("#storeAmenitiesContainer");
                    storeAmenitiesContainer.html(resp);
                }
            });
        }
    }
}