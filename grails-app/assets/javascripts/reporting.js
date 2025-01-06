var globalSortParams = null;
var globalFilterParams = null;

function saveReportColumns() {
    var checkboxValues = { };

    $("#reportColumnsForm input:checkbox").each(function() {
        checkboxValues[$(this).val()] = this.checked;
    }).get();

    $.ajax({
        url: saveReportColumnsUrl,
        method: "POST",
        data: { reportColumns: JSON.stringify(checkboxValues), reportType: reportType },
        success: function(resp) {
            $("#columnsCollapse").collapse('hide');
            getReportData();
        }
    });
}

function filterReport() {
    var filterValues = { };

    $("#filtersForm input").each(function() {
        filterValues[$(this).attr("name")] = $(this).val();
    }).get();

    $("#filtersForm select").each(function() {
        filterValues[$(this).attr("name")] = $(this).find(":selected").val();
    }).get();

    globalSortParams = null; // reset sorting from previous results
    getReportData(null, filterValues);
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

function exportToPPCsv() {
    if (!$("#storeFilter").val()) {
        alert("Please enter a valid store Id filter for this report.")
        return
    }

    var params = { reportType: reportType, csv: true, standard: true, storeFilter: $("#storeFilter").val() };
    window.location = getDataUrl + "?" + $.param(params);
}

function exportToCsv() {
    var params = { reportType: reportType, csv: true };
    $.extend(params, globalSortParams, globalFilterParams);

    window.location = getDataUrl + "?" + $.param(params);
}