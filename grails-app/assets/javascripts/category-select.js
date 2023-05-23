// Expand or collapse the category and show all children categories.
function expandCollapseCategory(categoryId, level, selectedCategoryId) {
    event.preventDefault();

    var plusMinusButton = $("#plusMinus-" +categoryId);
    var expanded = plusMinusButton.attr("aria-expanded");
    if (expanded === "true") {
        plusMinusButton.text("+");
        plusMinusButton.attr("aria-expanded", "false");
        $("#categoryContainer-" +categoryId).html("");
    } else {
        var params = {};
        params["categoryId"] = categoryId;
        params["level"] = level;
        params["selectedCategoryId"] = selectedCategoryId;
        $.ajax({
            url: getChildCategoriesUrl,
            method: "GET",
            data: params,
            success: function(resp) {
                plusMinusButton.text("-");
                plusMinusButton.attr("aria-expanded", "true");
                $("#categoryContainer-" +categoryId).html(resp);
            }
        });
    }
}