var currentTimeout;
var searchInProgress = false;

$(document).ready(function () {
    // Set up click actions for checkboxes initially
    setCheckboxClickAction('input[name="category.id[]"]');
});

function searchCategories(e, level, triggerOnCategoryChange, searchTerm, selectedCategoryIds, specialId) {
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
        params["selectedCategoryId"] = selectedCategoryIds;
        params["specialId"] = specialId;

        $.ajax({
            url: categorySearchUrl,
            method: "GET",
            data: params,
            success: function(resp) {
                $("#category-container-results").html(resp);
                setCheckboxClickAction('#categoryContainer-' + categoryId + ' input[name="category.id[]"]');
                searchInProgress = false;
            }
        });
    }, 750);
}

function filterCategories(supplierId, supplierCategoryId) {
    var params = {};
    params["supplierId"] = (supplierId) ? supplierId : null; // Pass null if supplierId is missing
    params["supplierCategoryId"] = (supplierCategoryId) ? supplierCategoryId : null; // Pass null if supplierCategoryId is missing

    $.ajax({
        url: categoryFilterUrl,
        method: "GET",
        data: params,
        success: function(resp) {
            $("#category-container-results").html(resp);
            setCheckboxClickAction('#categoryContainer-' + categoryId + ' input[name="category.id[]"]');
        }
    });
}


function expandCollapseCategory(categoryId, level, selectedCategoryId, triggerOnCategoryChange) {
    event.preventDefault();

    var plusMinusButton = $("#plusMinus-" + categoryId);
    var expanded = plusMinusButton.attr("aria-expanded");

    // // Check if parent is selected
    var parentCheckbox = $('#category-' + categoryId);
    if (parentCheckbox.is(':checked')) {
        // Disable the '-' button if parent is selected
        plusMinusButton.prop('disabled', true);
        return; // Stop further execution
    } else {
        // Re-enable the button if parent is not selected
        plusMinusButton.prop('disabled', false);
    }


    // Save checkbox states before collapsing
    if (expanded === "true") {
        plusMinusButton.text("+");
        plusMinusButton.attr("aria-expanded", "false");

        // Store the current state of checkboxes before collapsing
        // checkboxStates[categoryId] = getCheckboxStates(categoryId);

        $("#categoryContainer-" + categoryId).html("");

      //  $('#category-' + categoryId).prop('checked', false);
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
            success: function (resp) {
                plusMinusButton.text("-");
                plusMinusButton.attr("aria-expanded", "true");
                $("#categoryContainer-" + categoryId).html(resp);
                setCheckboxClickAction('#categoryContainer-' + categoryId + ' input[name="category.id[]"]');
            }
        });
    }
}


function setCheckboxClickAction(selector) {
    $(document).on('change', selector, function () {
        var isChecked = $(this).is(':checked');
        var categoryId = $(this).val();

        if (isChecked) {
            //Ensure all parents are selected
            //Here assume if intermediate or leaf node is selected then all the way to parent nodes are get selected
            //This is what done by this function
            selectParentCategories(categoryId);

            //Expand all child categories when a parent is selected
            //If parent is selected then loop over all child nodes including intermediate and leaf nodes
            //Then expand them and tick them
            expandAndSelectAllChildCategories(categoryId);
        } else {
            //Uncheck children and check if parent needs to be deselected
            //When you deselect this will loop over all available child and check if they are checked or not
            //If all child for a parent is deselect then deselect parent as well
            uncheckAllChildCategories(categoryId);
        }
    });
}

function expandAndSelectAllChildCategories(parentCategoryId) {
    // Find the "plus-minus" button of the parent category
    var plusMinusButton = $("#plusMinus-" + parentCategoryId);

    // Dynamically retrieve the level from the data-level attribute
    var level = parseInt(plusMinusButton.data('level')) || 1; // Default to 1 if level is not set

    // If the category is collapsed, expand it first
    if (plusMinusButton.attr("aria-expanded") === "false") {
        var params = {};
        params["categoryId"] = parentCategoryId;
        params["level"] = level;
        params["selectedCategoryId"] = null;
        params["triggerOnCategoryChange"] = false;

        $.ajax({
            url: getChildCategoriesUrl,
            method: "GET",
            data: params,
            success: function (resp) {
                // Expand the parent
                plusMinusButton.text("-");
                plusMinusButton.attr("aria-expanded", "true");

                // Populate child categories
                $("#categoryContainer-" + parentCategoryId).html(resp);

                // Automatically select the parent checkbox
                $('#category-' + parentCategoryId).prop('checked', true);

                // Select all child checkboxes and expand them recursively
                $("#categoryContainer-" + parentCategoryId + ' input[name="category.id[]"]').each(function () {
                    var childId = $(this).val();
                    $(this).prop('checked', true); // Select the child checkbox
                    expandAndSelectAllChildCategories(childId); // Recursive call for child categories
                });
            }
        });
    } else {
        // If already expanded, ensure all children are selected
        $('#category-' + parentCategoryId).prop('checked', true); // Select the parent checkbox

        $("#categoryContainer-" + parentCategoryId + ' input[name="category.id[]"]').each(function () {
            var childId = $(this).val();
            $(this).prop('checked', true); // Select the child checkbox
            expandAndSelectAllChildCategories(childId); // Recursive call for child categories
        });
    }
}

function checkAllChildCategories(categoryId) {
    // Check all child categories recursively
    $("#categoryContainer-" + categoryId + ' input[name="category.id[]"]').each(function () {
        $(this).prop('checked', true);
        var childId = $(this).val();
        checkAllChildCategories(childId);
    });
}

function uncheckAllChildCategories(categoryId) {
    // Uncheck all child categories recursively
    $("#categoryContainer-" + categoryId + ' input[name="category.id[]"]').each(function () {
        var checkbox = $(this);
        var childId = checkbox.val();
        checkbox.prop('checked', false); // Uncheck the current child
        uncheckAllChildCategories(childId); // Recursively uncheck its children
    });

    // Check if the current category is a child and update its parent
    var parentId = $('#category-' + categoryId).data('parent-id'); // Get the parent ID from the child checkbox

    if (parentId) {
        updateParentCheckboxState(parentId); // Reevaluate the parent's state
    }
}

function updateParentCheckboxState(parentCategoryId) {
    var parentCheckbox = $('#category-' + parentCategoryId); // Get the parent checkbox
    var allUnchecked = true; // Flag to check if all children are unchecked

    // Check all child checkboxes of the parent
    $(`#categoryContainer-${parentCategoryId} input[name="category.id[]"]`).each(function () {
        if ($(this).prop('checked')) {
            allUnchecked = false; // Found a checked child
        }
    });

    // Update the parent checkbox state based on the state of its children
    if (allUnchecked) {
        parentCheckbox.prop('checked', false); // Uncheck the parent if all children are unchecked
    } else {
        parentCheckbox.prop('checked', true); // Ensure parent is checked if any child is checked
    }
}

// Select all child categories recursively
function selectChildCategories(categoryId) {
    $("#categoryContainer-" + categoryId + ' input[name="category.id[]"]').each(function () {
        $(this).prop('checked', true);
        var childId = $(this).val();
        selectChildCategories(childId);
    });
}

// Select all parent categories up the hierarchy
function selectParentCategories(categoryId) {
    // Get the parent ID of the current category
    var parentId = $('#category-' + categoryId).data('parent-id'); // Retrieve parent ID using `data-parent-id`
    if (parentId) {
        var parentCheckbox = $('#category-' + parentId); // Get the parent checkbox
        parentCheckbox.prop('checked', true); // Check the parent checkbox

        // Recursively select the parent's parent
        selectParentCategories(parentId);
    }
}

