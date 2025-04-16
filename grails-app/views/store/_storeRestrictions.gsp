<%@ page contentType="text/html;charset=UTF-8" %>
<div class="store-opening-hours">
    <!-- First row with first table -->
    <div class="row mb-5">
        <div class="col-8">
            <h5 class="mb-4">Delivery Restrictions</h5>

            <div class="table-responsive">
                <table id="restriction-days-tbl" class="table table-bordered custom-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-left">Restriction</th>
                        <th class="align-middle text-left">Days</th>
                        <th class="align-middle text-left">Restriction Time</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeRestrictions?.regularHours}" var="enableTime" status="i">
                        <tr>
                            <td class="align-middle text-left">
                                <div class="checkbox-wrapper">
                                    <g:checkBox name="storeRestrictions.regularHours[${i}].restrictionEnabled" value="${enableTime.restrictionEnabled}" class="form-check-input wl-checkbox" checked="${enableTime.restrictionEnabled}" />
                                </div>
                            </td>
                            <td class="align-middle text-left">${enableTime.day} <input type="hidden" name="storeRestrictions.regularHours[${i}].day" value="${enableTime.day}">
                            </td>
                            <td class="align-middle text-left">
                                <div class="restriction-time-inputs" style="${enableTime.restrictionEnabled ? 'display: flex;' : 'display: none;'}">
                                    <g:textField name="storeRestrictions.regularHours[${i}].timeFrom" value="${enableTime.timeFrom}" class="form-control form-control-sm time-input me-2" placeholder="HH:mm"/>
                                    <span class="mx-2">to</span>
                                    <g:textField name="storeRestrictions.regularHours[${i}].timeTo" value="${enableTime.timeTo}" class="form-control form-control-sm time-input ms-2" placeholder="HH:mm"/>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Second row with second table -->
    <div class="row">
        <div class="col-12">
            <h5 class="mb-4">Other Restrictions</h5>

            <div class="table-responsive">
                <table id="other-restrictions-special-days-tbl" class="table table-bordered table-sm custom-table right-table">
                    <thead>
                    <tr>
                        <th class="align-middle text-left">Description</th>
                        <th class="align-middle text-left">Start Date & Time</th>
                        <th class="align-middle text-left">End Date & Time</th>
                        <th class="align-middle text-left">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${storeRestrictions?.otherRestrictions}" var="otherRestrictions" status="i">
                        <tr id="special-hour-row-${i}">
                            <td class="align-middle text-left">
                                ${otherRestrictions?.description}
                                <input type="hidden" name="storeRestrictions.otherRestrictions[${i}].description" value="${otherRestrictions?.description}"/>
                            </td>
                            <td class="align-middle text-left">
                                ${otherRestrictions?.startDateTime}
                                <input type="hidden" name="storeRestrictions.otherRestrictions[${i}].startDateTime" id="otherRestrictions[${i}].startDateTime" value="${otherRestrictions?.startDateTime}"/>
                            </td>
                            <td class="align-middle text-left">
                                ${otherRestrictions?.endDateTime}
                                <input type="hidden" name="storeRestrictions.otherRestrictions[${i}].endDateTime" id="otherRestrictions[${i}].endDateTime" value="${otherRestrictions?.endDateTime}"/>
                            </td>
                            <td class="text-left align-middle">
                                <a href="#" onclick="addOtherRestrictions('${i}','${otherRestrictions?.description}', '${otherRestrictions?.startDateTime}', '${otherRestrictions?.endDateTime}' )" id="otherRestrictions-edit-specialOpeningHours[${i}]" class="btn btn-sm btn-wl mr-1 fixed-width-btn">Edit</a>
                                <a href="#" onclick="deleteOtherRestriction(${i})" id="otherRestrictions-delete-specialOpeningHours[${i}]" class="btn btn-sm btn-danger fixed-width-btn">Delete</a>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>

            <div class="mt-3">
                <a href="#" onclick="addOtherRestrictions(null, null, null, null)" id="otherRestrictions-add-special-opening-hours" class="btn btn-wl pt-1 pb-1 pl-3 pr-4">Add other store restriction</a>
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
        justify-content: flex-start; /* Align checkbox to the left */
        align-items: center;
        height: 100%;
        padding-left: 10px;
    }

    .checkbox-wrapper .form-check-input {
        margin: 0;
    }

    .restriction-time-inputs {
        display: flex;
        align-items: center;
        justify-content: space-between;
        min-height: 38px;
        width: 100%;
    }

    .time-input {
        flex: 1 1 0 !important;
        min-width: 0 !important;
        width: auto !important;
        max-width: none !important;
    }

    /* Add a container for the "to" text to prevent it from expanding */
    .mx-2 {
        flex: 0 0 auto;
        padding: 0 10px;
    }

    .date-input {
        width: 100px;
    }

    .custom-table {
        width: 100%; /* Make tables full width */
        table-layout: fixed;
    }

    /* Set fixed column widths for the restriction days table */
    #restriction-days-tbl th:nth-child(1) {
        width: 15%;
    }

    #restriction-days-tbl th:nth-child(2) {
        width: 15%;
    }

    #restriction-days-tbl th:nth-child(3) {
        width: 70%;
    }

    /* Set fixed column widths for the other restrictions table */
    #other-restrictions-special-days-tbl th:nth-child(1) {
        width: 25%;
    }

    #other-restrictions-special-days-tbl th:nth-child(2) {
        width: 25%;
    }

    #other-restrictions-special-days-tbl th:nth-child(3) {
        width: 25%;
    }

    #other-restrictions-special-days-tbl th:nth-child(4) {
        width: 25%;
    }

    /* Make table headers consistent height across both tables */
    #restriction-days-tbl thead tr,
    #other-restrictions-special-days-tbl thead tr {
        height: 32px;
    }

    #restriction-days-tbl th,
    #other-restrictions-special-days-tbl th {
        padding-top: 0.2rem;
        padding-bottom: 0.2rem;
        vertical-align: middle;
        font-size: 0.85rem;
        font-weight: 600;
    }

    .table-sm td, .table-sm th {
        padding: 0.3rem;
    }

    /* Ensure rows have consistent height */
    #restriction-days-tbl tr {
        height: 54px;
    }

    /* When inputs are hidden, maintain the space */
    .restriction-time-inputs[style*="display: none"] {
        display: block !important;
        visibility: hidden;
        height: 38px;
    }

    .table-responsive {
        display: block;
        width: 100%;
        overflow: hidden;
    }

    /* Specific fix for small screens */
    @media (max-width: 767px) {
        /* Force minimum width for tables on small screens */
        #restriction-days-tbl, #other-restrictions-special-days-tbl {
            min-width: 500px; /* Set a minimum width to prevent column collapse */
        }

        /* Enable horizontal scrolling only when needed */
        .table-responsive {
            overflow-x: auto;
        }
    }

    .fixed-width-btn {
        width: 80px;
        display: inline-flex;
        justify-content: center;
        align-items: center;
        text-align: center;
        margin-bottom: 5px;
        height: 30px;
        padding: 0;
        line-height: 1;
        vertical-align: middle;
    }
</style>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        attachCheckboxListeners();
    });
</script>