var currentTimeout;
var searchInProgress = false;

// Expand or collapse the category and show all children categories.
function expandCollapseCategory(categoryId, level, selectedCategoryId, triggerOnCategoryChange) {
    console.log("Hi auto expanding")
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

                setCheckboxClickAction('#categoryContainer-' +categoryId +' input[name="category.id[]"]');
            }
        });
    }
}

$(document).ready(function() {
    setCheckboxClickAction('input[name="category.id[]"]');
});

function setCheckboxClickAction(selector) {
    $(document).on('change', selector, function () {
        var isChecked = $(this).is(':checked');
        var categoryId = $(this).val();

        if (isChecked) {
            console.log('Child category selected:', categoryId);
        } else {
            console.log('Child category unselected:', categoryId);
        }
    });
}

// function setCheckboxClickAction(selector) {
//     $(selector).click(function () {
//         var $checkbox = $(this);
//
//         // If you want to trigger some action on change, you can add it here
//         if (typeof onCategoryChanged === 'function') {
//             onCategoryChanged($(this).val(), $(this).prop('checked'));
//         }
//     });
// }

// function searchCategories(e, level, triggerOnCategoryChange, searchTerm, selectedCategoryId) {
//     // Since all keyup events trigger this, here are a couple of standard keys to be ignored..
//     if (e.keyCode === 16 || e.keyCode === 17 || e.keyCode === 20) {
//         return;
//     }
//
//     if (searchInProgress === false) {
//         $("#category-container-results").html("<div class=\"d-flex justify-content-center pt-2\">\n" +
//             "  <div class=\"spinner-border\" role=\"status\">\n" +
//             "    <span class=\"sr-only\">Loading...</span>\n" +
//             "  </div>\n" +
//             "</div>");
//     }
//
//     clearTimeout(currentTimeout);
//
//     searchInProgress = true;
//
//     currentTimeout = setTimeout(function() {
//         var params = {};
//         params["level"] = level;
//         params["triggerOnCategoryChange"] = triggerOnCategoryChange;
//         params["searchTerm"] = searchTerm;
//         params["selectedCategoryId"] = selectedCategoryId;
//
//         $.ajax({
//             url: categorySearchUrl,
//             method: "GET",
//             data: params,
//             success: function(resp) {
//
//                 $("#category-container-results").html(resp);
//
//
//                 setRadioClickAction('#category-container-results input[name="category.id"]');
//
//                 searchInProgress = false;
//             }
//         });
//     }, 750);
// }

// function setCheckboxClickAction(selector) {
//     $(selector).each(function () {
//         // Initialize the state for each checkbox
//         $(this).data('waschecked', $(this).prop('checked'));
//     });
//
//     $(selector).click(function () {
//         var $checkbox = $(this);
//
//         // Toggle the `waschecked` state
//         if ($checkbox.data('waschecked') === true) {
//             $checkbox.prop('checked', false);
//             $checkbox.data('waschecked', false);
//         } else {
//             $checkbox.prop('checked', true);
//             $checkbox.data('waschecked', true);
//         }
//
//         // Perform additional actions if necessary
//         console.log('Checkbox clicked:', $checkbox.val(), 'Checked:', $checkbox.prop('checked'));
//     });
// }
