//= require jquery-3.3.1.min

$(document).ready(function() {
    // Bind product search button click.
    $('#productSearchButton').click(function() {
        var url = event.target.getAttribute("data-url");

        $.ajax({
            url: url,
            data: { searchTerm: $("#productSearchTerm").val(), searchBy: $("#productSearchBy").val() },
            success: function(resp) {
                $("#productSearchResults").html(resp);
            }
        });
    });

    // Binding the on click to the enter button in the product search term text box.
    // Used jQuery initially here but was getting some weird looping, so now using old fashioned JS.
    var input = document.getElementById("productSearchTerm");

    input.addEventListener("keyup", function(event) {
        // Number 13 is the "Enter" key on the keyboard.
        if (event.keyCode === 13) {
            event.preventDefault();
            document.getElementById("productSearchButton").click();
        }
    });
});