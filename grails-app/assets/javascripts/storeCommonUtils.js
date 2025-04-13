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
            $('#addStoreAdditionalDetailsModal').modal("hide");
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
                    $('#addStoreAdditionalDetailsModal').modal("hide");
                }
            });
        }
    }
}

function closeStoreAdditionalDetailAddModal() {
    if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        $('#addStoreAdditionalDetailsModal').modal('hide')
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
        $('#addStoreOtherRestrictionModal').modal('hide')
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
    $("input[name^='regularHours']").each(function() {
        const nameMatch = $(this).attr('name').match(/regularHours\[(\d+)\]\.(\w+)/);
        if (nameMatch) {
            const index = parseInt(nameMatch[1]);
            const field = nameMatch[2];

            if (field === 'startTime') {
                params["regularHours[" + index + "].timeFrom"] = $(this).val();
            } else if (field === 'endTime') {
                params["regularHours[" + index + "].timeTo"] = $(this).val();
            } else if (field === 'closed') {
                params["regularHours[" + index + "].restrictionEnabled"] = $(this).is(':checked');
            }

            // Add day if not already added
            if (!params["regularHours[" + index + "].day"]) {
                params["regularHours[" + index + "].day"] = $(`#regularHours\\[${index}\\]\\.day`).text();
            }
        }
    });

    // Add existing other restrictions to params
    let otherRestrictionsCount = 0;
    $("#other-restrictions-special-days-tbl tbody tr").each(function(index) {
        const row = $(this);
        const existingDescription = row.find("input[name$='.description']").val();
        const existingStartDateTime = row.find("input[name$='.startDateTime']").val() || row.find("input[name$='.date']").val();
        const existingEndDateTime = row.find("input[name$='.endDateTime']").val() || row.find("input[name$='.startTime']").val();

        if (existingDescription && existingStartDateTime) {
            params["otherRestrictions[" + otherRestrictionsCount + "].description"] = existingDescription;
            params["otherRestrictions[" + otherRestrictionsCount + "].startDateTime"] = existingStartDateTime;
            params["otherRestrictions[" + otherRestrictionsCount + "].endDateTime"] = existingEndDateTime;
            otherRestrictionsCount++;
        }
    });

    // Add the new restriction
    params["otherRestrictions[" + otherRestrictionsCount + "].description"] = description;
    params["otherRestrictions[" + otherRestrictionsCount + "].startDateTime"] = startDateTime;
    params["otherRestrictions[" + otherRestrictionsCount + "].endDateTime"] = endDateTime;

    // Log the params for debugging
    console.log("Params:", params);

    // Submit via AJAX
    $.ajax({
        url: saveStoreOtherRestrictions,
        type: 'POST',
        data: params,
        success: function(resp) {
            var storeRestrictionContainer = $("#storeRestrictionsContainer");
            storeRestrictionContainer.html(resp);
            $('#addStoreOtherRestrictionModal').modal("hide");
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
                const timeFrom = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeFrom']").val();
                const timeTo = row.find("input[name^='storeRestrictions.regularHours'][name$='.timeTo']").val();

                params[`regularHours[${loopIndex}].restrictionEnabled`] = isEnabled;
                params[`regularHours[${loopIndex}].day`] = day;
                params[`regularHours[${loopIndex}].timeFrom`] = timeFrom;
                params[`regularHours[${loopIndex}].timeTo`] = timeTo;
            });


            // Add existing other restrictions to params
            $("#other-restrictions-special-days-tbl tbody tr").each(function(loopIndex) {
                console.log(index)
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

            console.log(params)

            $.ajax({
                url: saveStoreOtherRestrictions,
                method: "POST",
                data: params,
                success: function(resp) {
                    otherRestrictionContainer.html(resp);
                    $('#addStoreOtherRestrictionModal').modal("hide");
                }
            });
        }
    }
}