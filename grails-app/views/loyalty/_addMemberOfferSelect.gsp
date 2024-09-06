<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.DateTimeZone" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>

<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });

        $('#remainingRedemptions').on('input', function(event) {
            let numericValue = event.target.value.replace(/[^\d]/g, '');
            // Limit input to two digits
            if (numericValue.length > 2) {
                numericValue = numericValue.slice(0, 2);
            }
            event.target.value = numericValue;
        });

        let oneMonthAgo = new Date();
        oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);

        let twelveMonthsLater = new Date();
        twelveMonthsLater.setMonth(twelveMonthsLater.getMonth() + 12);

        // Subtract 7 days from twelveMonthsLater since end date to automatically mapped into 12 months
        let endDate = new Date(twelveMonthsLater);
        endDate.setDate(endDate.getDate() - 7);

        $('#startDateFilter').datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto",
            startDate: oneMonthAgo,
            endDate: endDate
        });

        $('#endDateFilter').datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto",
            startDate: new Date(),
            endDate: twelveMonthsLater
        });

        //By using the changingDate flag, ensure that the function is only called once per change event
        var changingDate = false;

        // Add blur event listener to end date to validate and correct dates
        $('#endDateFilter').blur(function() {
            if (!changingDate) {
                changingDate = true;
                validateAndCorrectDates();
                changingDate = false;
            }
        });

        // Add blur event listener to start date to validate and correct dates
        $('#startDateFilter').blur(function() {
            if (!changingDate) {
                changingDate = true;
                validateAndCorrectDates();
                changingDate = false;
            }
        });

        function validateAndCorrectDates() {
            var startDate = $('#startDateFilter').datepicker('getDate');
            var endDate = $('#endDateFilter').datepicker('getDate');
            var today = new Date();

            // Reset time components to 00:00:00 to compare dates only
            today.setHours(0, 0, 0, 0);

            // If startDate is null or empty, set it to today
            if (!startDate) {
                startDate = today;
                $('#startDateFilter').datepicker('setDate', startDate);
            }

            // Re-fetch startDate after potential adjustments
            startDate = $('#startDateFilter').datepicker('getDate');

            // If endDate is null or empty, set it to one week from startDate
            if (!endDate) {
                endDate = new Date(startDate);
                endDate.setDate(startDate.getDate() + 7);
                $('#endDateFilter').datepicker('setDate', endDate);
            }

            // Re-fetch endDate after potential adjustment
            endDate = $('#endDateFilter').datepicker('getDate');

            // Ensure startDate is less than endDate
            if (endDate <= startDate) {
                endDate = new Date(startDate);
                endDate.setDate(startDate.getDate() + 7);
                $('#endDateFilter').datepicker('setDate', endDate);
            }
        }

        // Ensure the start date can be cleared and entered manually
        $('#startDateFilter').on('input', function() {
            if (!$(this).val()) {
                $('#startDateFilter').data('manual-empty', true);
            } else {
                $('#startDateFilter').data('manual-empty', false);
            }
        });

        // Ensure the end date can be cleared and entered manually
        $('#endDateFilter').on('input', function() {
            if (!$(this).val()) {
                $('#endDateFilter').data('manual-empty', true);
            } else {
                $('#endDateFilter').data('manual-empty', false);
            }
        });

        // Fill start date if it's empty when losing focus
        $('#startDateFilter').blur(function() {
            if ($('#startDateFilter').data('manual-empty')) {
                var today = new Date();
                today.setHours(0, 0, 0, 0);
                $('#startDateFilter').datepicker('setDate', today);
                $('#startDateFilter').data('manual-empty', false);
            }
        });

        // Fill end date if it's empty when losing focus
        $('#endDateFilter').blur(function() {
            if ($('#endDateFilter').data('manual-empty')) {
                var startDate = $('#startDateFilter').datepicker('getDate') || new Date();
                var newEndDate = new Date(startDate);
                newEndDate.setDate(startDate.getDate() + 7);
                $('#endDateFilter').datepicker('setDate', newEndDate);
                $('#endDateFilter').data('manual-empty', false);
            }
        });

    });
</script>

<div class="d-flex justify-content-center mb-4">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${offer != null && member != null}">
        <section>
            <div id="validation-errors" class="alert alert-danger alert-wl mx-0" role="alert" hidden></div>

            <g:form method="post" action="ajaxSaveMemberOffer" class="mt-4" name="saveMemberOffer">
                <g:hiddenField name="cardNumber" value="${member.cardNumber}" />
                <g:hiddenField name="memberId" value="${member.id}" />
                <g:hiddenField name="offerId" value="${offer.id}" />
                <g:hiddenField name="description" value="${offer.offerDescription}" />
                <div class="card-body pt-4">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group mb-3">
                                <label for="remainingRedemptions" class="col-5 col-form-label text-right pr-4">Remaining Redemptions</label>
                                <g:textField id="remainingRedemptions" name="remainingRedemptions" type="number" class="col-3 form-control bottom-border" pattern="[0-9]*" min="0" max="99" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="status" class="col-5 col-form-label text-right pr-4">Status</label>
                                <g:checkBox id="status" name="status" checked="${true}" />
                            </div>
                        </div>
                        <div class="col-12 col-lg-6">
                            <div class="row form-group mb-3">
                                <label for="startDate" class="col-5 col-form-label text-right pr-4">Start Date</label>
                                <g:textField id="startDateFilter" name="startDate" class="col-3 form-control bottom-border" value="${DateTimeFormat.forPattern('dd/MM/yyyy').print(new DateTime().withTimeAtStartOfDay())}"  />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="endDate" class="col-5 col-form-label text-right pr-4">End Date</label>
                                <g:textField id="endDateFilter" name="endDate" class="col-3 form-control bottom-border" value="${DateTimeFormat.forPattern('dd/MM/yyyy').withZone(DateTimeZone.getDefault()).print(new DateTime().plusDays(7))}" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tab-content">
                    <div class="row my-3">
                        <g:link elementId="memberOfferSave-cancel" class="btn btn-wl col-1 offset-1" action="offers" params="[cardNumber: member.cardNumber]" onClick="return confirm('Any unsaved changes will be lost, ar you sure you wish to continue?');">Cancel</g:link>
                        <button id="memberOfferSave" type="button" name="memberOfferUpdate-save-button" onclick="saveOffer()" class="btn btn-success col-1 offset-8">Save</button>
                    </div>
                </div>
            </g:form>
        </section>
    </g:if>
</div>