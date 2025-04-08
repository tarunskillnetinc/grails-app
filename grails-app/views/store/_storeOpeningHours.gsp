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
                                <g:textField name="storeOpeningHoursCommand.regularHours[${i}].startTime" id="storeOpeningHoursCommand.regularHours[${i}].startTime" value="${hour.startTime}" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </td>
                            <td>
                                <g:textField name="storeOpeningHoursCommand.regularHours[${i}].endTime" id="storeOpeningHoursCommand.regularHours[${i}].endTime" value="${hour.endTime}" class="form-control form-control-sm time-input" placeholder="HH:mm" />
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <g:checkBox name="storeOpeningHoursCommand.regularHours[${i}].closed" id="storeOpeningHoursCommand.regularHours[${i}].closed" class="form-check-input wl-checkbox" checked="${hour.closed}" />
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
                        <tr  id="special-hour-row-${i}">
                            <td class="align-middle text-center">
                                ${special?.description}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].description" id="storeOpeningHoursCommand.specialOpeningHours[${i}].description" value="${special?.description}" />
                            </td>
                            <td class="align-middle text-center">
                                ${special?.date}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].date" id="storeOpeningHoursCommand.specialOpeningHours[${i}].date" value="${special?.date}" />
                            </td>
                            <td class="align-middle text-center">
                                ${special?.startTime}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].startTime" id="storeOpeningHoursCommand.specialOpeningHours[${i}].startTime" value="${special?.startTime}" />
                            </td>
                            <td class="align-middle text-center">
                                ${special?.endTime}
                                <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].endTime" id="storeOpeningHoursCommand.specialOpeningHours[${i}].endTime" value="${special?.endTime}" />
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <input type="checkbox" class="form-check-input wl-checkbox" ${special?.closed ? 'checked' : ''} disabled />
                                    <input type="hidden" name="storeOpeningHoursCommand.specialOpeningHours[${i}].closed" id="storeOpeningHoursCommand.specialOpeningHours[${i}].closed" value="${special?.closed}" />
                                </div>
                            </td>
                            <td class="text-center align-middle">
                                <a href="#" onclick="editSpecialHour(${i})" id="edit-specialOpeningHours[${i}]" class="btn btn-sm btn-wl mr-1 fixed-width-btn">Edit</a>
                                <a href="#" onclick="deleteSpecialHour(${i})" id="delete-specialOpeningHours[${i}]" class="btn btn-sm btn-danger fixed-width-btn">Delete</a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="mt-3">
                <a href="#" onclick="addSpecialHour()" id="add-special-opening-hours" class="btn btn-wl p-1">Add special opening hours</a>
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

.fixed-width-btn {
    width: 80px;  /* Adjust this value as needed */
    display: inline-flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    margin-bottom: 5px;  /* Add some vertical spacing between buttons if they wrap */
    height: 30px;  /* Set a fixed height for the buttons */
    padding: 0;  /* Remove default padding */
    line-height: 1;  /* Reset line height */
    vertical-align: middle;
}
</style>

<script>
    let specialHoursCount = ${storeOpeningHoursCommand?.specialOpeningHours?.size() ?: 0};

    let specialOpeningHours = [];

    // Populate the array from the server-side data
    <g:each in="${storeOpeningHoursCommand?.specialOpeningHours}" var="hour" status="i">
    specialOpeningHours.push({
        date: "${hour.date}",
        description: "${hour.description}",
        startTime: "${hour.startTime}",
        endTime: "${hour.endTime}",
        closed: ${hour.closed}
    });
    </g:each>

    function setupAllTimeInputs() {
        const timeInputs = document.querySelectorAll('.time-input');
        timeInputs.forEach(input => setupTimeInput(input));
    }

    function setupTimeInput(input) {
        // Keep the input event for formatting only
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

        // Move the automatic time update logic to the blur event
        input.addEventListener('blur', function(e) {
            const value = e.target.value;

            if (value && !/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) {
                alert('Please enter a valid time in HH:mm format');
                e.target.value = '';

                // Clear auto-filled related fields
                const lowerCaseId = input.id.toLowerCase();
                if (lowerCaseId.includes('starttime')) {
                    const endTimeId = input.id.replace(/starttime/i, function(match) {
                        return match.replace(/start/i, 'end');
                    });
                    const endTimeInput = document.getElementById(endTimeId);

                    if (endTimeInput && endTimeInput.getAttribute('data-auto-filled') === 'true') {
                        endTimeInput.value = '';
                        endTimeInput.removeAttribute('data-auto-filled');
                    }
                } else if (lowerCaseId.includes('endtime')) {
                    const startTimeId = input.id.replace(/endtime/i, function(match) {
                        return match.replace(/end/i, 'start');
                    });
                    const startTimeInput = document.getElementById(startTimeId);

                    if (startTimeInput && startTimeInput.getAttribute('data-auto-filled') === 'true') {
                        startTimeInput.value = '';
                        startTimeInput.removeAttribute('data-auto-filled');
                    }
                }
            } else if (value) {
                // Only perform auto-fill on blur if the value is valid
                const lowerCaseId = input.id.toLowerCase();

                if (lowerCaseId.includes('starttime')) {
                    const endTimeId = input.id.replace(/starttime/i, function(match) {
                        return match.replace(/start/i, 'end');
                    });
                    const endTimeInput = document.getElementById(endTimeId);

                    if (endTimeInput && !endTimeInput.value) {
                        let [hours, minutes] = value.split(':').map(Number);
                        minutes += 1;
                        if (minutes >= 60) {
                            minutes = 0;
                            hours += 1;
                        }
                        if (hours >= 24) {
                            hours = 0;
                        }

                        const newEndTime = hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
                        endTimeInput.value = newEndTime;
                        endTimeInput.setAttribute('data-auto-filled', 'true');
                    }
                } else if (lowerCaseId.includes('endtime')) {
                    const startTimeId = input.id.replace(/endtime/i, function(match) {
                        return match.replace(/end/i, 'start');
                    });
                    const startTimeInput = document.getElementById(startTimeId);

                    if (startTimeInput && !startTimeInput.value) {
                        let [hours, minutes] = value.split(':').map(Number);
                        minutes -= 1;
                        if (minutes < 0) {
                            minutes = 59;
                            hours -= 1;
                        }
                        if (hours < 0) {
                            hours = 23;
                        }

                        const newStartTime = hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
                        startTimeInput.value = newStartTime;
                        startTimeInput.setAttribute('data-auto-filled', 'true');
                    }
                }
            }
        });
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

    function editSpecialHour(index) {
        let specialOpeningHour = specialOpeningHours[index];

        // Convert the JavaScript object to a JSON string and encode it for URL
        let specialOpeningHourJson = encodeURIComponent(JSON.stringify(specialOpeningHour));

        $.ajax({
            url: '/store/loadEditSpecialOpeningHoursTemplate',
            method: 'GET',
            data: {
                specialOpeningHour: specialOpeningHourJson,
                openingHourIndex: index
            },
            success: function(response) {
                $('#addSpecialOpeningHoursModalContainer').html(response);
                $('#addSpecialOpeningHoursModal').modal('show');
            },
            error: function(xhr, status, error) {
                console.error("Error loading modal content:", error);
            }
        });
    }

    function createOrUpdateRow(data, index) {
        const rowIndex = 'special-hour-row-' + index;

        const existingRow = document.getElementById(rowIndex);
        const row = existingRow || document.createElement('tr');
        row.id = rowIndex;
        if (existingRow) {
            while (row.firstChild) {
                row.removeChild(row.firstChild);
            }
        }

        // Description cell
        const descCell = document.createElement('td');
        descCell.textContent = data.description;
        const descInput = document.createElement('input');
        descInput.type = 'hidden';
        descInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].description';
        descInput.id = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].description';
        descInput.value = data.description;
        descCell.appendChild(descInput);
        row.appendChild(descCell);

        // Date cell
        const dateCell = document.createElement('td');
        dateCell.textContent = data.date;
        const dateInput = document.createElement('input');
        dateInput.type = 'hidden';
        dateInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].date';
        dateInput.id = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].date';
        dateInput.value = data.date;
        dateCell.appendChild(dateInput);
        row.appendChild(dateCell);

        // Start time cell
        const startCell = document.createElement('td');
        startCell.textContent = data.startTime;
        const startInput = document.createElement('input');
        startInput.type = 'hidden';
        startInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].startTime';
        startInput.id = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].startTime';
        startInput.value = data.startTime;
        startCell.appendChild(startInput);
        row.appendChild(startCell);

        // End time cell
        const endCell = document.createElement('td');
        endCell.textContent = data.endTime;
        const endInput = document.createElement('input');
        endInput.type = 'hidden';
        endInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].endTime';
        endInput.id = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].endTime';
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
        closedInput.name = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].closed';
        closedInput.id = 'storeOpeningHoursCommand.specialOpeningHours[' + index + '].closed';
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
        editBtn.id="edit-specialOpeningHours[" + index + "]";
        editBtn.className = 'btn btn-sm btn-wl mr-1 fixed-width-btn';
        editBtn.textContent = 'Edit';
        editBtn.onclick = function () {
            editSpecialHour(index);
        };
        const deleteBtn = document.createElement('a');
        deleteBtn.href = '#';
        deleteBtn.id="delete-specialOpeningHours[" + index + "]";
        deleteBtn.className = 'btn btn-sm btn-danger fixed-width-btn';
        deleteBtn.textContent = 'Delete';
        deleteBtn.onclick = function () {
            deleteSpecialHour(index);
        };
        actionsCell.appendChild(editBtn);
        actionsCell.appendChild(deleteBtn);
        row.appendChild(actionsCell);

        return existingRow ? null : row;
    }

    document.addEventListener('saveSpecialHours', function (e) {
        const data = e.detail;
        if (data.specialOpeningHoursIndex !== undefined && data.specialOpeningHoursIndex !== -1) {
            createOrUpdateRow(data, data.specialOpeningHoursIndex);

            specialOpeningHours[data.specialOpeningHoursIndex] = {
                date: data.date,
                description: data.description,
                startTime: data.startTime,
                endTime: data.endTime,
                closed: data.closed
            };
        } else {
            const newRow = createOrUpdateRow(data, specialHoursCount);
            if (newRow) {
                document.querySelector('.right-table tbody').appendChild(newRow);
            }

            specialOpeningHours.push({
                date: data.date,
                description: data.description,
                startTime: data.startTime,
                endTime: data.endTime,
                closed: data.closed
            });

            specialHoursCount++;
        }
    });


    function resetSpecialHourForm() {
        $('#specialDescription').val('');
        $('#specialClosedCheckbox').prop('checked', false);
        $('#specialDate').val('');
        $('#specialStartTime').val('');
        $('#specialEndTime').val('');
    }

    function deleteSpecialHour(index) {
        if (confirm("Are you sure you want to delete this special opening hour?")) {
            // Remove from the specialOpeningHours array
            specialOpeningHours.splice(index, 1);

            // Remove the row from the table
            const row = document.getElementById('special-hour-row-' + index);
            if (row) {
                row.remove();
                // Reindex the remaining rows
                reindexSpecialHours();
            }
        }
    }

    function reindexSpecialHours() {
        const rows = document.querySelectorAll('table.right-table tbody tr');
        rows.forEach((row, index) => {
            // Update row ID
            row.id = 'special-hour-row-' + index;

            // Update input names
            const inputs = row.querySelectorAll('input[name^="storeOpeningHoursCommand.specialOpeningHours["]');
            inputs.forEach(input => {
                const name = input.getAttribute('name');
                const newName = name.replace(/\[\d+\]/, '[' + index + ']');
                input.setAttribute('name', newName);
            });

            // Update onclick handlers
            const editBtn = row.querySelector('a.btn-wl');
            const deleteBtn = row.querySelector('a.btn-danger');

            if (editBtn) editBtn.setAttribute('onclick', 'editSpecialHour('+index+')');
            if (deleteBtn) deleteBtn.setAttribute('onclick', 'deleteSpecialHour('+index+')');
        });

        // Update the count
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