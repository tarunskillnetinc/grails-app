var globalSortParams = null;
var globalFilterParams = null;

function saveReportColumns() {
    var checkboxValues = { };

    $("#reportColumnsForm input:checkbox").each(function() {
        checkboxValues[$(this).val()] = this.checked;
    }).get();

    $.ajax({
        url: "/reporting/ajaxSaveReportColumns",
        method: "POST",
        data: { reportColumns: JSON.stringify(checkboxValues), reportType: reportType },
        success: function(resp) {
            $("#columnsCollapse").collapse('hide');
            getReportData();
        }
    });
}

function getReportData(sortParams, filterParams) {
    $("#search-results").hide();
    $("#loading-indicator").show();

    // Storing sort params globally because some parts of the page don't have access to these variables due to the async nature.
    if (sortParams != null) {
        globalSortParams = sortParams;
    }

    if (filterParams != null) {
        globalFilterParams = filterParams;
    }

    var params = { reportType: reportType };
    $.extend(params, globalSortParams, globalFilterParams);

    $.ajax({
        url: getDataUrl,
        method: "POST",
        data: params,
        success: function(resp) {
            $("#results-container").html(resp);
        }
    });
}