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
    var index = $("#addStoreAdditionalDetailIndex").val()
    var description = $("#addStoreAdditionalDetailDescription").val()
    var value = $("#addStoreAdditionalDetailValue").val()
    params["storeAdditionalDetails.description"] = description;
    params["storeAdditionalDetails.value"] = value;

    var lastVariantContainer = $("#storeAdditionalDetailsContainer > div:last-child");

    if(index == null) {
        if (lastVariantContainer.length > 0) {
            index = parseInt(lastVariantContainer[0].id.split('-')[1]) + 1;
        } else {
            index = 0;
        }
    }

    params["index"] = index;

    $.ajax({
        url: saveStoreAdditionalDetails,
        method: "POST",
        data: params,
        success: function(resp) {
            var storeAdditionalDetailsContainer = $("#storeAdditionalDetailsContainer > #storeAdditionalDetail-" +index);

            if (storeAdditionalDetailsContainer.length === 0) {
                $("#storeAdditionalDetailsContainer").append("<div id=\"storeAdditionalDetail-" + index +"\"></div>");

                storeAdditionalDetailsContainer = $("#storeAdditionalDetailsContainer > #storeAdditionalDetail-" +index);
            }

            storeAdditionalDetailsContainer.html(resp);

            $('#addStoreAdditionalDetailsModal').modal("hide");
        }
    });
}