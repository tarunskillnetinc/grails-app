<div class="store-opening-hours">
    <div class="row">
        <div class="col-md-4">
            <h5 class="mb-4">Regular Store Opening Hours</h5>
            <div class="table-responsive">
                <table class="table table-bordered custom-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-center">Day</th>
                        <th class="align-middle text-center">Start</th>
                        <th class="align-middle text-center">End</th>
                        <th class="align-middle text-center">Closed</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeOpeningHoursCommand?.regularHours}" var="hour" status="i">
                        <tr>
                            <td class="align-middle text-center">${hour.day} <input type="hidden" name="storeOpeningHoursCommand.regularHours[${i}].day" value="${hour.day}"></td>
                            <td>
                                <g:textField name="storeOpeningHoursCommand.regularHours[${i}].startTime" value="${hour.startTime}" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </td>
                            <td>
                                <g:textField name="storeOpeningHoursCommand.regularHours[${i}].endTime" value="${hour.endTime}" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <g:checkBox name="storeOpeningHoursCommand.regularHours[${i}].close" class="form-check-input wl-checkbox" checked="${hour.close}" />
                                </div>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="col-md-8">
            <h5 class="mb-4">Special Store Opening/Close Date & Time</h5>
            <div class="table-responsive">
                <table class="table table-bordered table-sm custom-table right-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-center">Description</th>
                        <th class="align-middle text-center">Date</th>
                        <th class="align-middle text-center">Start</th>
                        <th class="align-middle text-center">End</th>
                        <th class="align-middle text-center">Closed</th>
                        <th class="align-middle text-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeOpeningHoursCommand?.specialOpeningHours}" var="special" status="i">
                        <tr>
                            <td>
                                ${special?.description}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].description" value="${special?.description}" />
                            </td>
                            <td>
                                ${special?.date}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].date" value="${special?.date}" />
                            </td>
                            <td>
                                ${special?.startTime}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].startTime" value="${special?.startTime}" />
                            </td>
                            <td>
                                ${special?.endTime}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].endTime" value="${special?.endTime}" />
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <input type="checkbox" class="form-check-input wl-checkbox" ${special?.close ? 'checked' : ''} disabled />
                                    <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].close" value="${special?.close}" />
                                </div>
                            </td>
                            <td class="text-center align-middle">
                                <a href="#" onclick="editSpecialHour(${i})" class="btn btn-sm btn-wl mr-1">Edit</a>
                                <a href="#" onclick="deleteSpecialHour(${i})" class="btn btn-sm btn-danger">Delete</a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="mt-3">
                <a href="#" onclick="addSpecialHour()" class="btn btn-wl p-1">Add special opening hours</a>
            </div>
        </div>
    </div>
</div>
<div id="addSpecialOpeningHoursModalContainer"></div>

<style>
.store-opening-hours h5 {
    font-size: 1.1rem;
    margin-bottom: 1rem;
}

.checkbox-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}

.checkbox-wrapper .form-check-input {
    margin: 0;
}

.time-input {
    width: 100px;
}

.date-input {
    width: 100px;
}

.custom-table {
    width: auto;
    max-width: 100%;
}

.table-sm td, .table-sm th {
    padding: 0.3rem;
}

.table-responsive {
    display: inline-block;
    max-width: 100%;
    overflow-x: auto;
}

.custom-table th {
    font-size: 0.85rem;
    font-weight: 600;
}

@media (max-width: 767px) {
    .col-md-4, .col-md-8 {
        width: 100%;
    }
    .col-md-8 {
        margin-top: 2rem;
    }
}

.right-table {
    width: 100%;
}

.col-md-8 .table-responsive {
    display: block;
    width: 100%;
    overflow-x: auto;
}

@media (max-width: 767px) {
    .col-md-4, .col-md-8 {
        width: 100%;
    }
    .col-md-8 {
        margin-top: 2rem;
    }
}
</style>

<script>
    let specialHoursCount = ${storeOpeningHoursCommand?.specialOpeningHours?.size() ?: 0};

    function setupAllTimeInputs() {
        const timeInputs = document.querySelectorAll('.time-input');
        timeInputs.forEach(input => setupTimeInput(input));
    }

    function setupTimeInput(input) {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');

            if (value.length > 2) {
                let hours = parseInt(value.slice(0, 2));
                let minutes = parseInt(value.slice(2));

                hours = Math.min(hours, 23);
                if (minutes > 59) {
                    minutes = 59;
                }

                value = hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
            }

            if (value.length > 5) {
                value = value.slice(0, 5);
            }

            e.target.value = value;
        });

        input.addEventListener('blur', function(e) {
            const value = e.target.value;
            if (value && !/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) {
                alert('Please enter a valid time in HH:mm format');
                e.target.value = '';
            }
        });
    }

    function toggleSpecialTimeFields() {
        const isClosed = document.getElementById('specialClosedCheckbox').checked;
        const timeFieldsStart = document.getElementById('specialTimeFieldsStart');
        const timeFieldsEnd = document.getElementById('specialTimeFieldsEnd');
        timeFieldsStart.style.display = isClosed ? 'none' : 'block';
        timeFieldsEnd.style.display = isClosed ? 'none' : 'block';
    }

    function addSpecialHour() {
        $.ajax({
            url: '/store/loadAddSpecialOpeningHoursTemplate',
            method: 'GET',
            success: function(response) {
                $('#addSpecialOpeningHoursModalContainer').html(response);
                $('#addSpecialOpeningHoursModal').modal('show');
            },
            error: function(xhr, status, error) {
                console.error("Error loading modal content:", error);
            }
        });
    }

    function createNewRow(data, index) {
        const row = document.createElement('tr');
        row.setAttribute('data-index', index);

        // Description cell
        const descCell = document.createElement('td');
        descCell.textContent = data.description;
        const descInput = document.createElement('input');
        descInput.type = 'hidden';
        descInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].description';
        descInput.value = data.description;
        descCell.appendChild(descInput);
        row.appendChild(descCell);

        // Date cell
        const dateCell = document.createElement('td');
        dateCell.textContent = data.date;
        const dateInput = document.createElement('input');
        dateInput.type = 'hidden';
        dateInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].date';
        dateInput.value = data.date;
        dateCell.appendChild(dateInput);
        row.appendChild(dateCell);

        // Start time cell
        const startCell = document.createElement('td');
        startCell.textContent = data.startTime;
        const startInput = document.createElement('input');
        startInput.type = 'hidden';
        startInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].startTime';
        startInput.value = data.startTime;
        startCell.appendChild(startInput);
        row.appendChild(startCell);

        // End time cell
        const endCell = document.createElement('td');
        endCell.textContent = data.endTime;
        const endInput = document.createElement('input');
        endInput.type = 'hidden';
        endInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].endTime';
        endInput.value = data.endTime;
        endCell.appendChild(endInput);
        row.appendChild(endCell);

        // Closed cell
        const closedCell = document.createElement('td');
        closedCell.className = 'align-middle text-center';
        const closedWrapper = document.createElement('div');
        closedWrapper.className = 'checkbox-wrapper';
        const closedCheckbox = document.createElement('input');
        closedCheckbox.type = 'checkbox';
        closedCheckbox.className = 'form-check-input wl-checkbox';
        closedCheckbox.checked = data.closed;
        closedCheckbox.disabled = true;
        const closedInput = document.createElement('input');
        closedInput.type = 'hidden';
        closedInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].close';
        closedInput.value = data.closed;
        closedWrapper.appendChild(closedCheckbox);
        closedWrapper.appendChild(closedInput);
        closedCell.appendChild(closedWrapper);
        row.appendChild(closedCell);

        // Actions cell
        const actionsCell = document.createElement('td');
        actionsCell.className = 'text-center align-middle';
        const editBtn = document.createElement('a');
        editBtn.href = '#';
        editBtn.className = 'btn btn-sm btn-wl mr-1';
        editBtn.textContent = 'Edit';
        editBtn.onclick = function() { editSpecialHour(index); };
        const deleteBtn = document.createElement('a');
        deleteBtn.href = '#';
        deleteBtn.className = 'btn btn-sm btn-danger';
        deleteBtn.textContent = 'Delete';
        deleteBtn.onclick = function() { deleteSpecialHour(index); };
        actionsCell.appendChild(editBtn);
        actionsCell.appendChild(deleteBtn);
        row.appendChild(actionsCell);

        return row;
    }

    document.addEventListener('saveSpecialHours', function(e) {
        const data = e.detail;

        // Create a new row for the table
        const newRow = createNewRow(data, specialHoursCount);

        // Append the new row to the table
        document.querySelector('.right-table tbody').appendChild(newRow);

        // Increment the special hours count
        specialHoursCount++;
    });


    function resetSpecialHourForm() {
        $('#specialDescription').val('');
        $('#specialClosedCheckbox').prop('checked', false);
        $('#specialDate').val('');
        $('#specialStartTime').val('');
        $('#specialEndTime').val('');
    }

    function editSpecialHour(index) {
        // Implement edit functionality
        console.log("Edit special hour at index:", index);
    }

    function deleteSpecialHour(index) {
        if (confirm("Are you sure you want to delete this special opening hour?")) {
            const row = document.querySelector(`input[name="specialOpeningHours[${index}].description"]`).closest('tr');
            if (row) {
                row.remove();
                // Reindex the remaining rows
                reindexSpecialHours();
            }
        }
    }

    function reindexSpecialHours() {
        const rows = document.querySelectorAll('.right-table tbody tr');
        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input[name^="specialOpeningHours["]');
            inputs.forEach(input => {
                const name = input.getAttribute('name');
                const newName = name.replace(/\[\d+\]/, `[${index}]`);
                input.setAttribute('name', newName);
            });
            const editBtn = row.querySelector('a.btn-primary');
            const deleteBtn = row.querySelector('a.btn-danger');
            editBtn.setAttribute('onclick', `editSpecialHour(${index})`);
            deleteBtn.setAttribute('onclick', `deleteSpecialHour(${index})`);
        });
        specialHoursCount = rows.length;
    }

    document.addEventListener('DOMContentLoaded', function() {
        setupAllTimeInputs();

        const rows = document.querySelectorAll('.right-table tbody tr');
        rows.forEach((row, index) => {
            row.setAttribute('data-index', index);
        });
    });

    $(document).ready(function() {
        $(document).on('click', '.btn-wl[onclick="addSpecialHour()"]', function(event) {
            event.preventDefault();
            addSpecialHour();
        });
    });
</script>