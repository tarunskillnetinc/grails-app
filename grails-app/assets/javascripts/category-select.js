var currentTimeout;
var searchInProgress = false;

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
});

function setRadioClickAction(selector) {
    var selected = {};

    $(selector).click(function () {
        var $radio = $(this);

        if (this.name in selected && this !== selected[this.name]) {
            $(selected[this.name]).trigger("deselect");
        }
        selected[this.name] = this;

        // If this was previously checked.
        if ($radio.data('waschecked') === true) {
            $radio.prop('checked', false);
            $radio.data('waschecked', false);
        } else {
            $radio.prop('checked', true);
            $radio.data('waschecked', true);
        }
    });

    $(selector).on('deselect', function () {
        // Remove waschecked from previous checked radio.
        $(this).data('waschecked', false)
    })
}

function searchCategories(e, level, triggerOnCategoryChange, searchTerm) {
    // Since all keyup events trigger this, here are a couple of standard keys to be ignored..
    if (e.keyCode === 16 || e.keyCode === 17 || e.keyCode === 20) {
        return;
    }

    if (searchInProgress === false) {
        $("#category-container-results").html("<div class=\"d-flex justify-content-center pt-2\">\n" +
            "  <div class=\"spinner-border\" role=\"status\">\n" +
            "    <span class=\"sr-only\">Loading...</span>\n" +
            "  </div>\n" +
            "</div>");
    }

    clearTimeout(currentTimeout);

    searchInProgress = true;

    currentTimeout = setTimeout(function() {
        var params = {};
        params["level"] = level;
        params["triggerOnCategoryChange"] = triggerOnCategoryChange;
        params["searchTerm"] = searchTerm;

        $.ajax({
            url: categorySearchUrl,
            method: "GET",
            data: params,
            success: function(resp) {
                $("#category-container-results").html(resp);

                setRadioClickAction('#category-container-results input[name="category.id"]');

                searchInProgress = false;
            }
        });
    }, 750);
}