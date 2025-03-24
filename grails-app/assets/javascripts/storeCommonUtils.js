function closeStoreAdditionalDetailAddModal() {
    if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        $('#addStoreAdditionalDetailsModal').modal('hide')
    }
}

function saveStoreAdditionalDetail(){
    var saveStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxSaveStoreAdditionalDetail')}"
    var index = 0
    var params = {}
    var description = $("#addStoreAdditionalDetailDescription").val()
    var value = $("#addStoreAdditionalDetailValue").val()
    params["storeAdditionalDetails.description"] = description;
    params["storeAdditionalDetails.value"] = value;

    var lastVariantContainer = $("#storeAdditionalDetailsContainer > div:last-child");

    if (lastVariantContainer.length > 0) {
        index = parseInt(lastVariantContainer[0].id.split('-')[1]) + 1;
    } else {
        index = 0;
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


function deleteAdditionalDetail(index) {
    var storeAdditionalDetailsContainer = $("#storeAdditionalDetailsContainer > #storeAdditionalDetail-" + index);
    if (storeAdditionalDetailsContainer.length) {
        storeAdditionalDetailsContainer.remove();
    }
}

function editAdditionalDetail(index){

}