function processTenderLift() {
    $.ajax({
        url: TenderMovementUrls.getProcessTenderLift(),
        method: "POST",
        data: $("#processTenderLift").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}

function updateTenderMovementContainer(resp) {
    $("#tender-movement-container").html(resp);
    $(".mask-money").maskMoney({allowZero: true}).maskMoney('mask');
}