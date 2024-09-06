/**
 The function that generates a UI element that enables multi-select based on checkbox selection options.
 The top element is for displaying the selected options.
 @param {string} containerId The ID of the div element that will hold the generated multi-select elements.
 @param {string} hiddenInputId The ID of the hidden input field that can be used from outside the multi-select field, useful for submitting form data of the selected options.
 @param {string[]} items The array of strings that contain the display text for the multi-select options.
 @param {string} placeholder The text that displays on the top element when no item is selected.
 */
function createMultiSelectorChecks(containerId, hiddenInputId, items, placeholder = 'Select Items') {
    const container = document.getElementById(containerId);
    const hiddenInput = document.getElementById(hiddenInputId);

    const multiselectContainer = document.createElement('div');
    multiselectContainer.classList.add('multiselect-container');

    const display = document.createElement('div');
    display.classList.add('multiselect-display');
    display.innerHTML = `<span>${placeholder}</span><span class="dropdown-arrow">&#9662;</span>`;
    multiselectContainer.appendChild(display);

    const itemsContainer = document.createElement('div');
    itemsContainer.classList.add('multiselect-items');
    multiselectContainer.appendChild(itemsContainer);

    // Add "Select All" option
    const selectAllElement = document.createElement('div');
    selectAllElement.classList.add('dropdown-item');
    selectAllElement.innerHTML = `<input type="checkbox" class="select-all-checkbox"> Select All`;
    itemsContainer.appendChild(selectAllElement);

    // Add individual items
    items.forEach(item => {
        const itemElement = document.createElement('div');
        itemElement.classList.add('dropdown-item');
        itemElement.innerHTML = `<input type="checkbox" class="item-checkbox" value="${items.indexOf(item) + 1}"> ${item}`;
        itemsContainer.appendChild(itemElement);
    });

    container.appendChild(multiselectContainer);

    const checkboxes = itemsContainer.querySelectorAll('.item-checkbox');
    const selectAllCheckbox = itemsContainer.querySelector('.select-all-checkbox');

    // Event handling for displaying the dropdown
    display.addEventListener('click', function() {
        itemsContainer.classList.toggle('show');
    });

    // Handle individual checkbox changes
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectedItems();
            updateSelectAllState();
        });
    });

    // Handle "Select All" checkbox change
    selectAllCheckbox.addEventListener('change', function() {
        const isChecked = selectAllCheckbox.checked;
        checkboxes.forEach(checkbox => {
            checkbox.checked = isChecked;
        });
        updateSelectedItems();
    });

    // Update the hidden input and display text based on selected items
    function updateSelectedItems() {
        const selectedItems = [];
        const selectedItemsDisplay = [];
        checkboxes.forEach(checkbox => {
            if (checkbox.checked) {
                selectedItems.push(checkbox.value);
                selectedItemsDisplay.push(items[parseInt(checkbox.value) - 1]);
            }
        });
        display.querySelector('span:first-child').textContent = selectedItemsDisplay.join(', ') || placeholder;
        hiddenInput.value = selectedItems.join(''); // Update hidden input value
        $(hiddenInput).trigger('change');
    }

    // Update the state of the "Select All" checkbox based on individual checkbox states
    function updateSelectAllState() {
        const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);
        selectAllCheckbox.checked = allChecked;
    }

    // Close the dropdown when clicking outside
    document.addEventListener('click', function(event) {
        if (!multiselectContainer.contains(event.target)) {
            itemsContainer.classList.remove('show');
        }
    });

    // Pre-populate selected items if hidden input has values
    if ($(hiddenInput).val() !== "") {
        const selectedItems = $(hiddenInput).val().split("");
        const selectedItemsDisplay = [];
        selectedItems.forEach(day => {
            selectedItemsDisplay.push(items[parseInt(day) - 1]);
            checkboxes[parseInt(day) - 1].checked = true;
        });
        display.querySelector('span:first-child').textContent = selectedItemsDisplay.join(', ') || placeholder;
        updateSelectAllState(); // Ensure the "Select All" checkbox state is accurate
    } else {
        // Ensure "Select All" is unchecked by default
        selectAllCheckbox.checked = false;
    }
}
