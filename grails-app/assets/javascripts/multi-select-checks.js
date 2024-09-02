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

    items.forEach(item => {
        const itemElement = document.createElement('div');
        itemElement.classList.add('dropdown-item');
        itemElement.innerHTML = `<input type="checkbox" class="item-checkbox" value="${items.indexOf(item) + 1}"> ${item}`;
        itemsContainer.appendChild(itemElement);
    });

    container.appendChild(multiselectContainer);

    // Event handling
    display.addEventListener('click', function() {
        itemsContainer.classList.toggle('show');
    });

    const checkboxes = itemsContainer.querySelectorAll('.item-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectedItems();
        });
    });

    function updateSelectedItems() {
        const selectedItems = [];
        const selectedItemsDisplay = [];
        checkboxes.forEach(checkbox => {
            if (checkbox.checked) {
                selectedItems.push(checkbox.value);
                selectedItemsDisplay.push(items[parseInt(checkbox.value)])
            }
        });
        display.querySelector('span:first-child').textContent = selectedItemsDisplay.join(', ') || placeholder;
        hiddenInput.value = selectedItems.join(''); // Update hidden input value
    }

    document.addEventListener('click', function(event) {
        if (!multiselectContainer.contains(event.target)) {
            itemsContainer.classList.remove('show');
        }
    });

    if ($(hiddenInput).val() !== "") {
        const selectedItems = $(hiddenInput).val().split("")
        const selectedItemsDisplay = [];
        selectedItems.forEach(day => {
            selectedItemsDisplay.push(items[parseInt(day)])
            checkboxes[parseInt(day) - 1].checked = true
        });
        display.querySelector('span:first-child').textContent = selectedItemsDisplay.join(', ') || placeholder;;
    }

}