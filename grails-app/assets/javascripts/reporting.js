function saveReportColumns(saveReportColumnsUrl, reportType) {
    var checkboxValues = {};

    $("#reportColumnsForm input:checkbox").each(function() {
        checkboxValues[$(this).val()] = this.checked;
    }).get();

    $.ajax({
        url: saveReportColumnsUrl,
        method: "POST",
        data: { reportColumns: JSON.stringify(checkboxValues), reportType: reportType },
        success: function(resp) {

        }
    });
}