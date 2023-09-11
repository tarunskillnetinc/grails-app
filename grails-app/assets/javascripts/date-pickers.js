function initDatePickers(startId, endId, initStartDate, initEndDate) {
    const startInput = $(`#${startId}`);
    const endInput = $(`#${endId}`);

    startInput.datepicker({
        format: "dd/mm/yyyy",
        weekStart: 1,
        startDate: initStartDate,
        todayHighlight: true,
        autoclose: true,
        todayBtn: "linked",
        orientation: "bottom auto"
    }).on("change", function () {
        startInput.val(this.value);
        startInput.removeClass('is-invalid');
        endInput.datepicker('setStartDate', this.value);
    });

    endInput.datepicker({
        format: "dd/mm/yyyy",
        weekStart: 1,
        endDate: initEndDate,
        todayHighlight: true,
        autoclose: true,
        todayBtn: "linked",
        orientation: "bottom auto"
    }).on("change", function () {
        endInput.val(this.value);
        endInput.removeClass('is-invalid');
        startInput.datepicker('setEndDate', this.value);
    });

    startInput.datepicker('setEndDate', endInput.val());
    endInput.datepicker('setStartDate', startInput.val());
}

function setDatePickers(startId, endId, startDate, endDate) {
    const startInput = $(`#${startId}`);
    const endInput = $(`#${endId}`);

    startInput.val(startDate);
    startInput.datepicker('setEndDate', endDate);
    endInput.val(endDate);
    endInput.datepicker('setStartDate', startDate);
}
