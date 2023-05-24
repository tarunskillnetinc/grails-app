// Expand or collapse the category and show all children categories.
function expandCollapseCategory(categoryId, level, selectedCategoryId, triggerOnCategoryChange) {
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
        params["triggerOnCategoryChange"] = triggerOnCategoryChange;

        $.ajax({
            url: getChildCategoriesUrl,
            method: "GET",
            data: params,
            success: function(resp) {
                plusMinusButton.text("-");
                plusMinusButton.attr("aria-expanded", "true");

                $("#categoryContainer-" +categoryId).html(resp);

                setRadioClickAction('#categoryContainer-' +categoryId +' input[name="category.id"]');
            }
        });
    }
}

$(document).ready(function() {
    setRadioClickAction('input[name="category.id"]');

    var categoryFilterSearch = $("#category-filter-search");
    categoryFilterSearch.on("keyup", function() {
        var searchTerm = categoryFilterSearch.val();

        if (searchTerm.length > 2) {
            console.log("Search term: " +searchTerm);
        }
    });
});

function setRadioClickAction(selector) {
    $(selector).click(function () {
        var $radio = $(this);

        // If this was previously checked.
        if ($radio.data('waschecked') === true) {
            $radio.prop('checked', false);
            $radio.data('waschecked', false);
        } else {
            $radio.prop('checked', true);
            $radio.data('waschecked', true);
        }

        // Remove was checked from other radios.
        $radio.siblings(selector).data('waschecked', false);
    });
}