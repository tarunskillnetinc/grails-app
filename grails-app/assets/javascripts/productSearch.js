//= require application

$(document).ready(function() {
    $('#productSearchButton').click(function() {
        var url = event.target.getAttribute("data-url");

        $.ajax({
            url: url,
            data: { searchTerm: $("#productSearchTerm").val() },
            success: function(resp) {
                $("#productSearchResults").html(resp);
            }
        });
    });
});