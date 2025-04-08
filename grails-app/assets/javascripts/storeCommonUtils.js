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

function addAmenities(index, description, value) {
    $("#addAmenitiesContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#addAmenitiesModal').modal({show: true, backdrop: 'static', keyboard: false});
    var params = {}
    $.ajax({
        url: addAmenity,
        method: "GET",
        data: params,
        success: function (resp) {
            $("#addAmenitiesContent").html(resp);
        }
    });
}