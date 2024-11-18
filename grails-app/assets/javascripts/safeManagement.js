function getSafeSessions() {

    $.ajax({
        url: SafeManagementUrls.getSafeSessionUrl(),
        method: "POST",
        success: function(resp) {
            $("#results-container").html(resp);
        },
        error: function() {
            $("#search-results").show();
            const result = document.createElement('div');
            $(result).addClass('col pt-2 pb-2 text-center my-auto wl-striped0').html('No shifts found.');
            $("#search-results").html(result);
        },
    });
}
