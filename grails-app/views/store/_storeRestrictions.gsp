<%@ page contentType="text/html;charset=UTF-8" %>
<div class="store-opening-hours">
    <div class="row">
        <div class="col-md-5">
            <h5 class="mb-4">Delivery Restrictions</h5>

            <div class="table-responsive">
                <table class="table table-bordered custom-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-center">Restriction</th>
                        <th class="align-middle text-center">Days</th>
                        <th class="align-middle text-center">Restriction Time</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeRestrictions?.regularHours}" var="enableTime" status="i">
                        <tr>
                            <td class="align-middle text-center">
                                <div class="checkbox-wrapper">
                                    <g:checkBox name="regularHours[${i}].closed" value="${enableTime.restrictionEnabled}" class="form-check-input wl-checkbox" checked="${enableTime.restrictionEnabled}" />
                                </div>
                            </td>
                            <td>
                                <span id="regularHours[${i}].day" class="form-control-plaintext" style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${enableTime.day}</span>
                            </td>
                            <td class="d-flex align-items-center">
                                <g:textField name="regularHours[${i}].startTime" value="${enableTime.timeFrom}" class="form-control form-control-sm time-input me-2" placeholder="HH:mm"/>

                                <span class="mx-2">to</span>

                                <g:textField name="regularHours[${i}].endTime" value="${enableTime.timeTo}" class="form-control form-control-sm time-input ms-2" placeholder="HH:mm"/>
                            </td>

                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-md-7">
            <h5 class="mb-4">Other Restrictions</h5>

            <div class="table-responsive">
                <table id="other-restrictions-special-days-tbl" class="table table-bordered table-sm custom-table right-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-center">Description</th>
                        <th class="align-middle text-center">Start Date & Time</th>
                        <th class="align-middle text-center">End Date & Time</th>
                        <th class="align-middle text-center">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeRestrictions?.otherRestrictions}" var="otherRestrictions" status="i">
                        <tr id="special-hour-row-${i}">
                            <td class="align-middle text-center">
                                ${otherRestrictions?.description}
                                <input type="hidden" name="otherRestrictions.specialOpeningHours[${i}].description"  value="${otherRestrictions?.description}"/>
                            </td>
                            <td class="align-middle text-center">
                                ${otherRestrictions?.startDateTime}
                                <input type="hidden" name="otherRestrictions.specialOpeningHours[${i}].date" id="otherRestrictions.specialOpeningHours[${i}].date" value="${otherRestrictions?.startDateTime}"/>
                            </td>
                            <td class="align-middle text-center">
                                ${otherRestrictions?.endDateTime}
                                <input type="hidden" name="otherRestrictions.specialOpeningHours[${i}].startTime" id="otherRestrictions.specialOpeningHours[${i}].startTime" value="${otherRestrictions?.endDateTime}"/>
                            </td>
                            <td class="text-center align-middle">
                                <a href="#" onclick="editSpecialHour(${i})" id="otherRestrictions-edit-specialOpeningHours[${i}]" class="btn btn-sm btn-wl mr-1 fixed-width-btn">Edit</a>
                                <a href="#" onclick="deleteSpecialHour(${i})" id="otherRestrictions-delete-specialOpeningHours[${i}]" class="btn btn-sm btn-danger fixed-width-btn">Delete</a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>

            <div class="mt-3">
                <a href="#" onclick="addOtherRestrictions()" id="otherRestrictions-add-special-opening-hours"
                   class="btn btn-wl pt-1 pb-1 pl-3 pr-4">Add other store restriction</a>
            </div>
        </div>
    </div>
</div>

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