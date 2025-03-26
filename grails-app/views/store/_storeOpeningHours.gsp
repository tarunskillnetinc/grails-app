<div class="store-opening-hours">
    <div class="row">
        <div class="col-12">
            <h5 class="mb-4">Regular Store Opening Hours</h5>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-bordered table-sm custom-table">
            <thead>
            <tr>
                <th class="align-middle text-center">Day</th>
                <th class="align-middle text-center">Start Time</th>
                <th class="align-middle text-center">End Time</th>
                <th class="align-middle text-center">Closed</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${storeOpeningHoursCommand?.regularHours}" var="hour" status="i">
                <tr>
                    <td class="align-middle text-center">${hour.day}</td>
                    <td>
                        <g:textField name="regularHours[${i}].startTime" value="${hour.startTime}" class="form-control form-control-sm time-input" />
                    </td>
                    <td>
                        <g:textField name="regularHours[${i}].endTime" value="${hour.endTime}" class="form-control form-control-sm time-input" />
                    </td>
                    <td class="align-middle text-center">
                        <div class="checkbox-wrapper">
                            <g:checkBox name="regularHours[${i}].close" class="form-check-input wl-checkbox" checked="${hour.close}" />
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="row">
        <div class="col-12">
            <h5 class="mt-5 mb-4">Special Store Opening/Close Date & Time</h5>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-bordered table-sm custom-table">
            <thead>
            <tr>
                <th class="align-middle text-center">Occasion</th>
                <th class="align-middle text-center">Date</th>
                <th class="align-middle text-center">Start Time</th>
                <th class="align-middle text-center">End Time</th>
                <th class="align-middle text-center">Closed</th>
                <th class="align-middle text-center">Actions</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${storeOpeningHoursCommand?.specialOpeningHours}" var="special" status="i">
                <tr>
                    <td>
                        <g:textField name="specialOpeningHours[${i}].description" value="${special?.description}" class="form-control form-control-sm" />
                    </td>
                    <td>
                        <g:textField name="specialOpeningHours[${i}].date" value="${special?.date}" class="form-control form-control-sm date-input" />
                    </td>
                    <td>
                        <g:textField name="specialOpeningHours[${i}].startTime" value="${special?.startTime}" class="form-control form-control-sm time-input" />
                    </td>
                    <td>
                        <g:textField name="specialOpeningHours[${i}].endTime" value="${special?.endTime}" class="form-control form-control-sm time-input" />
                    </td>
                    <td class="align-middle text-center">
                        <div class="checkbox-wrapper">
                            <g:checkBox name="specialOpeningHours[${i}].close" class="form-check-input wl-checkbox" checked="${special?.close}" />
                        </div>
                    </td>
                    <td class="text-center align-middle">
                        <a href="#" onclick="editSpecialHour(${i})" class="btn btn-sm btn-primary mr-1">Edit</a>
                        <a href="#" onclick="deleteSpecialHour(${i})" class="btn btn-sm btn-danger">Delete</a>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="row mt-3">
        <div class="col-12">
            <a href="#" onclick="addSpecialHour()" class="btn btn-sm btn-success">Add special opening hours</a>
        </div>
    </div>
</div>

<style>
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
    width: 120px;
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
</style>