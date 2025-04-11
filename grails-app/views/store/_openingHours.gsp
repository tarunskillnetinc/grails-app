<%@ page contentType="text/html;charset=UTF-8" %>
<g:set var="commandObject" value="${pageScope[commandPrefix]}" />
<div class="store-opening-hours">
    <div class="row">
        <div class="col-md-4">
            <h5 class="mb-4">${titleRegularOpeningHours}</h5>

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
                    <g:each in="${commandObject?.regularHours}" var="hour" status="i">
                        <tr>
                            <td class="align-middle text-center">${hour.day} <input type="hidden"
                                                                                    name="${commandPrefix}.regularHours[${i}].day"
                                                                                    value="${hour.day}"></td>
                            <td>
                                <g:textField name="${commandPrefix}.regularHours[${i}].startTime"
                                             id="${commandPrefix}.regularHours[${i}].startTime"
                                             value="${hour.startTime}" class="form-control form-control-sm time-input"
                                             placeholder="HH:mm"/>
                            </td>
                            <td>
                                <g:textField name="${commandPrefix}.regularHours[${i}].endTime"
                                             id="${commandPrefix}.regularHours[${i}].endTime" value="${hour.endTime}"
                                             class="form-control form-control-sm time-input" placeholder="HH:mm"/>
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <g:checkBox name="${commandPrefix}.regularHours[${i}].closed"
                                                id="${commandPrefix}.regularHours[${i}].closed"
                                                class="form-check-input wl-checkbox" checked="${hour.closed}" />
                                </div>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-md-8">
            <h5 class="mb-4">${titleSpecialOpeningHours}</h5>

            <div class="table-responsive">
                <table id="${commandPrefix}-special-hours-tbl" class="table table-bordered custom-table right-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-center col-6">Description</th>
                        <th class="align-middle text-center col-2">Date</th>
                        <th class="align-middle text-center col-1">Start</th>
                        <th class="align-middle text-center col-1">End</th>
                        <th class="align-middle text-center col-1">Closed</th>
                        <th class="align-middle text-center col-1">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${commandObject?.specialOpeningHours}" var="special" status="i">
                        <tr id="${commandPrefix}-special-hour-row-${i}">
                            <td class="align-middle text-center text-break">
                                ${special?.description}
                                <input type="hidden" name="${commandPrefix}.specialOpeningHours[${i}].description"
                                       id="${commandPrefix}.specialOpeningHours[${i}].description"
                                       value="${special?.description}"/>
                            </td>
                            <td class="align-middle text-center">
                                ${special?.date}
                                <input type="hidden" name="${commandPrefix}.specialOpeningHours[${i}].date"
                                       id="${commandPrefix}.specialOpeningHours[${i}].date" value="${special?.date}"/>
                            </td>
                            <td class="align-middle text-center">
                                ${special?.startTime}
                                <input type="hidden" name="${commandPrefix}.specialOpeningHours[${i}].startTime"
                                       id="${commandPrefix}.specialOpeningHours[${i}].startTime"
                                       value="${special?.startTime}"/>
                            </td>
                            <td class="align-middle text-center">
                                ${special?.endTime}
                                <input type="hidden" name="${commandPrefix}.specialOpeningHours[${i}].endTime"
                                       id="${commandPrefix}.specialOpeningHours[${i}].endTime"
                                       value="${special?.endTime}"/>
                            </td>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <input type="checkbox"
                                           class="form-check-input wl-checkbox" ${special?.closed ? 'checked' : ''}
                                           disabled/>
                                    <input type="hidden" name="${commandPrefix}.specialOpeningHours[${i}].closed"
                                           id="${commandPrefix}.specialOpeningHours[${i}].closed"
                                           value="${special?.closed}"/>
                                </div>
                            </td>
                            <td class="text-center align-middle">
                                <a href="#" onclick="${commandPrefix}editSpecialHour(${i},'${commandPrefix}')" id="${commandPrefix}-edit-specialOpeningHours[${i}]"
                                   class="btn btn-sm btn-wl mr-1 fixed-width-btn">Edit</a>
                                <a href="#" onclick="${commandPrefix}deleteSpecialHour(${i},'${commandPrefix}')" id="${commandPrefix}-delete-specialOpeningHours[${i}]"
                                   class="btn btn-sm btn-danger fixed-width-btn">Delete</a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>

            <div class="mt-3">
                <a href="#" onclick="${commandPrefix}addSpecialHour('${commandPrefix}')" id="${commandPrefix}-add-special-opening-hours"
                   class="btn btn-wl pt-1 pb-1 pl-3 pr-4">${addSpecialHoursButtonText}</a>
            </div>
        </div>
    </div>
</div>

<div id="${commandPrefix}-addSpecialOpeningHoursModalContainer"></div>

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
    width: 80px; /* Adjust this value as needed */
    display: inline-flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    margin-bottom: 5px; /* Add some vertical spacing between buttons if they wrap */
    height: 30px; /* Set a fixed height for the buttons */
    padding: 0; /* Remove default padding */
    line-height: 1; /* Reset line height */
    vertical-align: middle;
}
</style>

<script>
    {
        const commandPrefix = "${commandPrefix}";
        let ${commandPrefix}specialHoursCount = ${commandObject?.specialOpeningHours?.size() ?: 0};

        let ${commandPrefix}SpecialOpeningHours = [];

        // Populate the array from the server-side data
        <g:each in="${commandObject?.specialOpeningHours}" var="hour" status="i">
        ${commandPrefix}SpecialOpeningHours.push({
            date: "${hour.date}",
            description: "${hour.description}",
            startTime: "${hour.startTime}",
            endTime: "${hour.endTime}",
            closed: ${hour.closed}
        });
        </g:each>

        function ${commandPrefix}setupAllTimeInputs() {
            const timeInputs = document.querySelectorAll('.time-input');
            timeInputs.forEach(input => ${commandPrefix}setupTimeInput(input));
        }

        function ${commandPrefix}setupTimeInput(input) {
            // Keep the input event for formatting only
            input.addEventListener('input', function (e) {
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
            input.addEventListener('blur', function (e) {
                const value = e.target.value;

                if (value && !/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) {
                    alert('Please enter a valid time in HH:mm format');
                    e.target.value = '';

                    const lowerCaseId = input.id.toLowerCase();
                    if (lowerCaseId.includes('starttime')) {
                        const endTimeId = input.id.replace(/starttime/i, function (match) {
                            return match.replace(/start/i, 'end');
                        });
                        const endTimeInput = document.getElementById(endTimeId);

                        if (endTimeInput && endTimeInput.getAttribute('data-auto-filled') === 'true') {
                            endTimeInput.value = '';
                            endTimeInput.removeAttribute('data-auto-filled');
                        }
                    } else if (lowerCaseId.includes('endtime')) {
                        const startTimeId = input.id.replace(/endtime/i, function (match) {
                            return match.replace(/end/i, 'start');
                        });
                        const startTimeInput = document.getElementById(startTimeId);

                        if (startTimeInput && startTimeInput.getAttribute('data-auto-filled') === 'true') {
                            startTimeInput.value = '';
                            startTimeInput.removeAttribute('data-auto-filled');
                        }
                    }
                } else if (value) {
                    const lowerCaseId = input.id.toLowerCase();

                    if (lowerCaseId.includes('starttime')) {
                        const endTimeId = input.id.replace(/starttime/i, function (match) {
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
                        const startTimeId = input.id.replace(/endtime/i, function (match) {
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

        function ${commandPrefix}addSpecialHour(targetCommandPrefix) {
            $.ajax({
                url: '/store/loadAddSpecialOpeningHoursTemplate',
                method: 'GET',
                data: {
                    commandPrefix: targetCommandPrefix
                },
                success: function (response) {
                    $('#' + targetCommandPrefix + '-addSpecialOpeningHoursModalContainer').html(response);
                    $('#' + targetCommandPrefix + '-addSpecialOpeningHoursModal').modal('show');
                },
                error: function (xhr, status, error) {
                    console.error("Error loading modal content:", error);
                }
            });
        }

        function ${commandPrefix}editSpecialHour(index, targetCommandPrefix) {
            let specialOpeningHour = ${commandPrefix}SpecialOpeningHours[index];
            // Convert the JavaScript object to a JSON string and encode it for URL
            let specialOpeningHourJson = encodeURIComponent(JSON.stringify(specialOpeningHour));
            $.ajax({
                url: '/store/loadEditSpecialOpeningHoursTemplate',
                method: 'GET',
                data: {
                    specialOpeningHour: specialOpeningHourJson,
                    openingHourIndex: index,
                    commandPrefix: targetCommandPrefix
                },
                success: function (response) {
                    $('#' + targetCommandPrefix + '-addSpecialOpeningHoursModalContainer').html(response);
                    $('#' + targetCommandPrefix + '-addSpecialOpeningHoursModal').modal('show');
                },
                error: function (xhr, status, error) {
                    console.error("Error loading modal content:", error);
                }
            });
        }

        function ${commandPrefix}createOrUpdateRow(data, index) {
            const rowIndex = commandPrefix + '-special-hour-row-' + index;

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
            descInput.name = commandPrefix + '.specialOpeningHours[' + index + '].description';
            descInput.id = commandPrefix + '.specialOpeningHours[' + index + '].description';
            descInput.value = data.description;
            descCell.appendChild(descInput);
            descCell.className = 'align-middle text-center text-break';
            row.appendChild(descCell);

            // Date cell
            const dateCell = document.createElement('td');
            dateCell.textContent = data.date;
            const dateInput = document.createElement('input');
            dateInput.type = 'hidden';
            dateInput.name = commandPrefix + '.specialOpeningHours[' + index + '].date';
            dateInput.id = commandPrefix + '.specialOpeningHours[' + index + '].date';
            dateInput.value = data.date;
            dateCell.className = 'align-middle text-center';
            dateCell.appendChild(dateInput);
            row.appendChild(dateCell);

            // Start time cell
            const startCell = document.createElement('td');
            startCell.textContent = data.startTime;
            const startInput = document.createElement('input');
            startInput.type = 'hidden';
            startInput.name = commandPrefix + '.specialOpeningHours[' + index + '].startTime';
            startInput.id = commandPrefix + '.specialOpeningHours[' + index + '].startTime';
            startInput.value = data.startTime;
            startCell.appendChild(startInput);
            startCell.className = 'align-middle text-center';
            row.appendChild(startCell);

            // End time cell
            const endCell = document.createElement('td');
            endCell.textContent = data.endTime;
            const endInput = document.createElement('input');
            endInput.type = 'hidden';
            endInput.name = commandPrefix + '.specialOpeningHours[' + index + '].endTime';
            endInput.id = commandPrefix + '.specialOpeningHours[' + index + '].endTime';
            endInput.value = data.endTime;
            endCell.appendChild(endInput);
            endCell.className = 'align-middle text-center';
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
            closedInput.name = commandPrefix + '.specialOpeningHours[' + index + '].closed';
            closedInput.id = commandPrefix + '.specialOpeningHours[' + index + '].closed';
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
            editBtn.id = "edit-specialOpeningHours[" + index + "]";
            editBtn.className = 'btn btn-sm btn-wl mr-1 fixed-width-btn';
            editBtn.textContent = 'Edit';
            editBtn.onclick = function () {
                ${commandPrefix}editSpecialHour(index, commandPrefix);
            };
            const deleteBtn = document.createElement('a');
            deleteBtn.href = '#';
            deleteBtn.id = "delete-specialOpeningHours[" + index + "]";
            deleteBtn.className = 'btn btn-sm btn-danger fixed-width-btn';
            deleteBtn.textContent = 'Delete';
            deleteBtn.onclick = function () {
                ${commandPrefix}deleteSpecialHour(index, commandPrefix);
            };
            actionsCell.appendChild(editBtn);
            actionsCell.appendChild(deleteBtn);
            row.appendChild(actionsCell);

            return existingRow ? null : row;
        }

        document.addEventListener(commandPrefix + 'SaveSpecialHours', function (e) {
            const data = e.detail;
            if (data.specialOpeningHoursIndex !== undefined && data.specialOpeningHoursIndex !== -1) {
                ${commandPrefix}createOrUpdateRow(data, data.specialOpeningHoursIndex);

                ${commandPrefix}SpecialOpeningHours[data.specialOpeningHoursIndex] = {
                    date: data.date,
                    description: data.description,
                    startTime: data.startTime,
                    endTime: data.endTime,
                    closed: data.closed
                };
            } else {
                const newRow = ${commandPrefix}createOrUpdateRow(data, ${commandPrefix}specialHoursCount);
                if (newRow) {
                    document.querySelector('#' + commandPrefix + '-special-hours-tbl tbody').appendChild(newRow);
                }

                ${commandPrefix}SpecialOpeningHours.push({
                    date: data.date,
                    description: data.description,
                    startTime: data.startTime,
                    endTime: data.endTime,
                    closed: data.closed
                });

                ${commandPrefix}specialHoursCount++;
            }
        });

        function ${commandPrefix}resetSpecialHourForm() {
            $('#specialDescription').val('');
            $('#specialClosedCheckbox').prop('checked', false);
            $('#specialDate').val('');
            $('#specialStartTime').val('');
            $('#specialEndTime').val('');
        }

        function ${commandPrefix}deleteSpecialHour(index, targetCommandPrefix) {
            if (confirm("Are you sure you want to delete this special opening hour?")) {
                // Remove from the specialOpeningHours array
                ${commandPrefix}SpecialOpeningHours.splice(index, 1);
                // Remove the row from the table
                const row = document.getElementById(targetCommandPrefix + '-special-hour-row-' + index);
                if (row) {
                    row.remove();
                    // Reindex the remaining rows
                    ${commandPrefix}reindexSpecialHours(targetCommandPrefix);
                }
            }
        }

        function ${commandPrefix}reindexSpecialHours(targetCommandPrefix) {
            const rows = document.querySelectorAll('#' + targetCommandPrefix + '-special-hours-tbl tbody tr');
            rows.forEach((row, index) => {
                // Update row ID
                row.id = targetCommandPrefix + '-special-hour-row-' + index;

                // Update input names
                const inputs = row.querySelectorAll('input[name^="' + targetCommandPrefix + '.specialOpeningHours["]');
                inputs.forEach(input => {
                    const name = input.getAttribute('name');
                    const newName = name.replace(/\[\d+\]/, '[' + index + ']');
                    input.setAttribute('name', newName);

                    // Also update IDs if they exist
                    if (input.id) {
                        const id = input.id;
                        const newId = id.replace(/\[\d+\]/, '[' + index + ']');
                        input.id = newId;
                    }
                });

                // Update onclick handlers
                const editBtn = row.querySelector('a.btn-wl');
                const deleteBtn = row.querySelector('a.btn-danger');

                if (editBtn) {
                    editBtn.setAttribute('onclick', '${commandPrefix}editSpecialHour(' + index + ',"'+targetCommandPrefix+'")');
                    editBtn.id = targetCommandPrefix + '-edit-specialOpeningHours[' + index + ']';
                }
                if (deleteBtn) {
                    deleteBtn.setAttribute('onclick', '${commandPrefix}deleteSpecialHour(' + index + ',"'+targetCommandPrefix+'")');
                    deleteBtn.id = targetCommandPrefix + '-delete-specialOpeningHours[' + index + ']';
                }
            });
            // Update the count
            ${commandPrefix}specialHoursCount = rows.length;
        }

        document.addEventListener('DOMContentLoaded', function () {
            ${commandPrefix}setupAllTimeInputs();

            const rows = document.querySelectorAll('#' + commandPrefix + '-special-hours-tbl tbody tr');
            rows.forEach((row, index) => {
                row.setAttribute('data-index', index);
            });
        });
    }
</script>