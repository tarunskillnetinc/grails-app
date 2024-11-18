<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.DateTimeZone" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>${isUpdate ? 'Edit' : 'Add'} Loyalty Offer</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <style>
        .no-bullets { list-style-type: none; padding-left: 0; /* Remove left padding */ }
        .no-bullets li { margin-left: 0; /* Remove left margin */ }

        .selected-item {
            background-color: #f0f0f0;
            padding: 5px;
            margin-bottom: 5px;
            border-radius: 3px;
            position: relative;
        }

        .cancel-icon {
            position: absolute;
            top: 3px;
            right: 3px;
            cursor: pointer;
        }
    </style>

    <script type='text/javascript'>
        let isUpdate = false

        window.onload = function() {
            /* sets elements of this class to only allow numeric entries */
            var numericFields = document.querySelectorAll('.numeric-field');
            numericFields.forEach(function(field) {
                field.addEventListener('input', function(event) {
                    if (!/^\d*$/.test(event.target.value)) {
                        event.target.value = event.target.value.replace(/[^\d]/g, '');
                    }
                });
            });
        };

        //Adding event listener to load existing multi selected segments  + promotions
        window.addEventListener('load', function() {
            updatePromotionDescriptionOnLoading()
            updateSegmentInputOnLoading()
            isUpdate = ${isUpdate}
        })

        $(document).ready(function() {
            <g:if test="${isUpdate}">
                $('#offerStartDateId').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#offerEndDateId').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });
            </g:if>
        });

        function updateSegmentInputOnLoading() {
            var selectedSegmentIdList = ${selectedSegmentIds}; // Get the selected segment IDs from the server response

            var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
            selectedSegmentsContainer.innerHTML = ''; // Clear previous content

            // Retrieve selected segment descriptions based on IDs
            var selectedDescriptions = [];
            for (var i = 0; i < ('${selectedSegmentIds}').length; i++) {
                var segmentId = selectedSegmentIdList[i];
                var selectedSegment = findSegmentById(segmentId);
                if (selectedSegment) {
                    selectedDescriptions.push(selectedSegment.description);
                    // Create selected segment element
                    var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(event)">&#10006;</span>';
                    var selectedSegmentDiv = document.createElement('div');
                    selectedSegmentDiv.className = 'selected-item';
                    selectedSegmentDiv.id = 'selectedSegment_' + segmentId; // Set the id attribute
                    selectedSegmentDiv.setAttribute('data-id', segmentId); // Set the data-id attribute
                    selectedSegmentDiv.innerHTML = selectedSegment.description + cancelIcon;
                    selectedSegmentsContainer.appendChild(selectedSegmentDiv);
                }
            }
            // Update hidden input value
            var selectedSegmentsInput = document.getElementById('offerSelectedSegments');
            selectedSegmentsInput.value = selectedDescriptions.join(',');
        }

        function findSegmentById(segmentId) {
            var decodedString = $("<div/>").html('${segmentsJson}').text(); // Decode HTML-encoded string
            var segments = JSON.parse(decodedString); // Parse JSON string
            for (var i = 0; i < segments.length; i++) {
                if (segments[i].id === segmentId) {
                    return segments[i];
                }
            }
            return null;
        }

        function updatePromotionDescriptionOnLoading() {
            var selectedPromotionId = document.getElementById('offerPromotionAssignedId').value;
            if(${promotionsJson != null}){
                var decodedString = $("<div/>").html('${promotionsJson}').text(); // Decode HTML-encoded string
                var promotions = JSON.parse(decodedString); // Parse JSON string
                var selectedPromotionDescription = "";
                for (var i = 0; i < promotions.length; i++) { // Use promotions.length to iterate over promotions
                    var promotion = promotions[i];
                    if (promotion.id == selectedPromotionId) {
                        selectedPromotionDescription = promotion.description;
                        break;
                    }
                }
                document.getElementById('offerPromotionAssignedInput').value = selectedPromotionDescription;
            }
        }

        function parseDate(dateString) {
            /* Required due to american defaultism and not being able to read a calender */
            var parts = dateString.split('/');
            return new Date(parts[2], parts[1] - 1, parts[0]);
        }

        function updatePromotionInput(selectedValue) {
            <g:each in="${promotions}" var="promotion">
                if ('${promotion.description}' === selectedValue) {

                    $('#offerStartDateId').datepicker('destroy');
                    $('#offerEndDateId').datepicker('destroy');

                    <%
                        def currentDate = new DateTime(DateTimeZone.UTC)
                        def startDate = promotion.startDate.isBefore(currentDate) ? currentDate : promotion.startDate
                        def formattedStartDate = startDate.toString("dd/MM/yyyy")

                        def endDate = promotion.endDate
                        def formattedEndDate = endDate ? endDate.toString("dd/MM/yyyy") : startDate.plusDays(7).toString("dd/MM/yyyy")
                    %>

                    var startDatePoint = parseDate('${formattedStartDate}');
                    var endDatePoint = parseDate('${formattedEndDate}');

                    $('#offerStartDateId').datepicker({
                        format: "dd/mm/yyyy",
                        weekStart: 1,
                        todayHighlight: true,
                        autoclose: true,
                        todayBtn: "linked",
                        orientation: "bottom auto",
                        startDate: startDatePoint,
                        endDate: endDatePoint
                    });

                    $('#offerEndDateId').datepicker({
                        format: "dd/mm/yyyy",
                        weekStart: 1,
                        todayHighlight: true,
                        autoclose: true,
                        todayBtn: "linked",
                        orientation: "bottom auto",
                        startDate: startDatePoint,
                        endDate: endDatePoint
                    });

                    $('#offerStartDateId').datepicker('setDate',  startDatePoint);
                    $('#offerEndDateId').datepicker('setDate',  endDatePoint);
                }
            </g:each>

            document.getElementById('offerPromotionAssignedInput').value = selectedValue;

            /* Promotion has been selected so other values can be edited */
            document.getElementById('offerSegmentAssignedInput').readOnly = false;
            document.getElementById('offerSegmentAssignedId').disabled = false;

            document.getElementById('offerDescriptionId').readOnly = false;
            document.getElementById('offerStatusId').disabled = false;

            document.getElementById('offerMaxRedemptionsId').readOnly = false;
            document.getElementById('offerMaxBudgetId').readOnly = false;

            document.getElementById('offerMarketingTextId').readOnly = false;
            document.getElementById('offerTermsTextId').readOnly = false;
        }

        function filterDropdown(inputId, selectId, inputElement) {
            // Filter the dropdown options based on the input value
            filter(inputId, selectId, inputElement.value);
        }

        function filter(inputId, selectId, filterValue) {
            var select = document.getElementById(selectId);
            var options = select.getElementsByTagName('option');
            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                var txtValue = option.textContent || option.innerText;
                if (txtValue.toUpperCase().indexOf(filterValue.toUpperCase()) > -1) {
                    option.style.display = "";
                } else {
                    option.style.display = "none";
                }
            }
        }

        function updateSegmentInput() {
            var selectedOptions = document.getElementById('offerSegmentAssignedId').selectedOptions;
            var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
            var selectedSegmentsInput = document.getElementById('offerSelectedSegments');

            // Store selected descriptions
            var selectedDescriptions = selectedSegmentsInput.value.split(',');

            // Add or update selected items
            for (var i = 0; i < selectedOptions.length; i++) {
                var description = selectedOptions[i].text;

                // Check if description already exists
                if (!selectedDescriptions.includes(description)) {
                    var id = selectedOptions[i].value;
                    var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(event)">&#10006;</span>';
                    var selectedSegment = document.createElement('div');
                    selectedSegment.className = 'selected-item';
                    selectedSegment.setAttribute('data-id', id);
                    selectedSegment.innerHTML = description + cancelIcon;
                    selectedSegmentsContainer.appendChild(selectedSegment);
                    selectedDescriptions.push(description);
                }
            }

            // Update hidden input value
            selectedSegmentsInput.value = selectedDescriptions.join(',');
        }

        function findExistingItem(container, description) {
            var items = container.getElementsByClassName('selected-item');
            for (var i = 0; i < items.length; i++) {
                if (items[i].innerText.includes(description)) {
                    return items[i];
                }
            }
            return null;
        }

        function removeSelectedItem(event) {
            var container = event.target.closest('.selected-item'); // Find the closest parent container with the class 'selected-item'
            if (container && !isUpdate) {
                container.remove(); // Remove the found container
            }
            // Update the hidden input value after removing the selected item
            updateHiddenInput();
        }

        function updateHiddenInput() {
            var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
            var selectedSegmentsInput = document.getElementById('offerSelectedSegments');

            // Get all remaining selected items
            var selectedItems = selectedSegmentsContainer.querySelectorAll('.selected-item');

            // Store selected descriptions
            var selectedDescriptions = [];

            // Extract and store the descriptions of remaining selected items
            selectedItems.forEach(function(item) {
                selectedDescriptions.push(item.textContent.trim());
            });

            // Update hidden input value
            selectedSegmentsInput.value = selectedDescriptions.join(',');
        }

        function saveLoyaltyOffer() {
            if(validateMandatoryFields()) {
                return
            }

            var offerId = $('#offerId').val();
            var offerDescription = $('#offerDescriptionId').val();
            var offerMarketingText = $('#offerMarketingTextId').val();
            var offerTermsText = $('#offerTermsTextId').val();
            var offerStartDate = $('#offerStartDateId').val();
            var offerEndDate = $('#offerEndDateId').val();
            var offerMaxRedemption = $('#offerMaxRedemptionsId').val();
            var offerMaxBudget = $('#offerMaxBudgetId').val();
            var offerStatus = $('#offerStatusId').val();
            var selectedPromotion = null

            //Get promotion id
            var selectElement = document.getElementById('offerPromotionAssignedId');
            var selectedOption = selectElement.options[selectElement.selectedIndex];
            if(selectedOption != null) {
                selectedPromotion = selectedOption.value;
            }
            //Prepare parameter map
            var params = {
                id: offerId,
                offerDescription: offerDescription,
                offerMarketingText: offerMarketingText,
                offerTermsText: offerTermsText,
                startDate: offerStartDate,
                endDate: offerEndDate,
                maxBudget: offerMaxBudget,
                maxRedemptions: offerMaxRedemption,
                retailerOfferId: selectedPromotion,
                status: offerStatus
            };

            //Get selected segment items id list
            loyaltySegmentIndex = 0
            var selectedSegments = document.querySelectorAll('#offerSelectedSegmentsContainer .selected-item');
            selectedSegments.forEach(function(segment) {
                var dataId = segment.getAttribute('data-id');
                params["loyaltyOfferSegments[" + loyaltySegmentIndex + "].offerId"] = offerId;
                params["loyaltyOfferSegments[" + loyaltySegmentIndex + "].segmentId"] = dataId;
                loyaltySegmentIndex++;
            });

            $.ajax({
                url: "${createLink(controller: 'loyalty', action: 'ajaxSaveLoyaltyOffers')}",
                method: "POST",
                data: params,
                statusCode: {
                    500: function (response) {
                        var errorList = response.responseJSON.error;

                        if (errorList && errorList.length > 0) {
                            let errorHeader = "An error occured when attempting to save the offer."
                            let errorString = "";

                            errorList.forEach(function(errorMessage) {
                                errorString = errorString.concat("<li>" + errorMessage + "</li>");
                            });

                            $('#validation-errors').html("<ul class='no-bullets'>" + errorHeader + errorString + "\n</ul>");
                            $('#validation-errors').prop("hidden", false);
                        }
                    },
                    200: function (response) {
                        var successMessage = "Loyalty Offer Saved Successfully";
                        var redirectUrl = '${createLink(controller: 'loyalty', action:'loyaltyOffers')}';
                        // Append success message as a query parameter
                        redirectUrl += '?successMessage=' + encodeURIComponent(successMessage);
                        // Redirect to the loyaltyOffers page with the success message
                        window.location.href = redirectUrl;
                    }
                }
            });
        }

        function validateMandatoryFields() {
            let error = false;
            let errorString = "";

            if ($('#offerDescriptionId').val() == "")  {
                errorString = errorString.concat("<li>Please enter an offer description</li>");
                error = true;
            }

            if ($('#offerPromotionAssignedInput').val() == "")  {
                errorString = errorString.concat("<li>Please select a promotion</li>");
                error = true;
            }

            if ($('#offerSelectedSegmentsContainer .selected-item').length === 0) {
                errorString = errorString.concat("<li>At least one segment must be selected</li>");
                error = true;
            }

            if ($('#offerMarketingTextId').val().length > 200) {
                errorString = errorString.concat("<li>Marketing text - max 200 characters allowed</li>");
                error = true;
            }

            if ($('#offerTermsTextId').val().length > 600) {
                errorString = errorString.concat("<li>Terms & conditions - max 600 characters allowed</li>");
                error = true;
            }

            var startDate = parseDate($('#offerStartDateId').val());
            var endDate = parseDate($('#offerEndDateId').val());


            if (endDate < startDate) {
                errorString = errorString.concat("<li>Offer end date cannot be set before the start date</li>");
                error = true;
            }

            if (error) {
                let errorHeader = "All mandatory fields must be present before data can be saved."
                let errorMessage = "<ul  class='no-bullets'>" + errorHeader + errorString + "\n</ul>"

                $('#validation-errors').html("<ul class='no-bullets'>" + errorHeader + errorString + "\n</ul>");
                $('#validation-errors').prop("hidden", false);
            }

            return error
        }
    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="loyalty" action="loyaltyOffers">Loyalty Offer Management</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${isUpdate ? 'Edit' : 'Add'} Loyalty Offer</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="add-loyalty-offer-section" class="container-fluid">
        <div class="row header-wl mt-3" id="add-loyalty-offer-header">
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">${isUpdate ? 'Edit' : 'Add'} Loyalty Offer</h2>
            </div>

            <div class="col-2 text-right">
                <g:link id="cancel" class="btn btn-wl" name="cancel" action="loyaltyOffers" onclick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                <button id="save" class="btn btn-success" name="save" onclick="saveLoyaltyOffer();">Save</button>
            </div>
        </div>

        <div id="validation-errors" class="alert alert-danger alert-wl mx-0" role="alert" hidden></div>

        <g:form name="add-loyalty-offer-form" id="add-loyalty-offer-form-id" action="save" novalidate="novalidate" class="mt-4">
            <g:hiddenField name="offerId" value="${loyaltyOffer?.id ?: 0}" />

            <div class="row mt-5 mb-3">
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerPromotionAssigned" for="offerPromotionAssignedInput" class="col-4 col-form-label text-right pr-4">Promotion Assigned</label>
                    <div class="dropdown-content col-6">
                        <div class="input-group-append">
                            <input id="offerPromotionAssignedInput" type="text" class="form-control bottom-border" placeholder="Search For Promotion.." oninput="filterDropdown('offerPromotionAssignedInput', 'offerPromotionAssignedId', this)" ${isUpdate ? 'disabled' : ''}>
                        </div>
                        <g:select id="offerPromotionAssignedId" name="offerPromotionAssigned" size="6" style="overflow-y: scroll; overflow-x: hidden;"
                                  from="${promotions}"
                                  optionValue="description"
                                  value="${loyaltyOffer?.retailerOfferId}"
                                  optionKey="id"
                                  class="form-control select-border"
                                  disabled="${isUpdate ? true : false}"
                                  onchange="updatePromotionInput(this.options[this.selectedIndex].text)"/>
                    </div>
                </div>
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerSegmentAssigned" for="offerSegmentAssignedInput" class="col-4 col-form-label text-right pr-4">Segment Assigned</label>
                    <div class="dropdown-content col-6">
                        <div class="input-group-append">
                            <input id="offerSegmentAssignedInput" type="text" class="form-control bottom-border" placeholder="Search For Segment.." oninput="filterDropdown('offerSegmentAssignedInput', 'offerSegmentAssignedId', this)" readonly="true">
                        </div>
                        <div id="offerSelectedSegmentsContainer" style="height: 100px; overflow-y: auto; border: 1px solid #ccc; margin-top: 5px; border-top: 0; border-bottom: 1px solid #ccc;"></div>
                        <input type="hidden" id="offerSelectedSegments" name="offerSelectedSegments" readonly = "${isUpdate ? true : false}">
                        <g:select id="offerSegmentAssignedId" name="offerSegmentAssigned" multiple="multiple"
                                  from="${segments}"
                                  optionValue="description"
                                  value="${selectedSegmentIds}"
                                  optionKey="id"
                                  class="form-control select-border"
                                  disabled="true"
                                  onchange="updateSegmentInput()"/>
                    </div>
                </div>
            </div>

            <div class="row mt-5 mb-3">
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerDescription" for="offerDescriptionId" class="col-4 col-form-label text-right pr-4">Offer Description</label>
                    <g:textField id="offerDescriptionId" name="offerDescription" class="col-6 form-control bottom-border" value="${loyaltyOffer?.offerDescription}" readonly="${isUpdate ? 'false' : 'true'}" />
                </div>
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerStatus" for="offerStatusId" class="col-4 col-form-label text-right pr-4">Status</label>
                    <g:select id="offerStatusId" name="status" class="col-6 form-control select-border" from="${LoyaltyOfferStatus.values()}" value="${loyaltyOffer?.status ? loyaltyOffer?.status : LoyaltyOfferStatus.PENDING}" disabled="${isUpdate ? 'false' : 'true'}" />
                </div>
            </div>

            <div class="row mt-2 mb-3">
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerStartDate" for="offerStartDateId" class="col-4 col-form-label text-right pr-4">Start date</label>
                    <g:textField id="offerStartDateId" name="offerStartDate" class="col-6 form-control bottom-border" value="${startDate?.toString("dd/MM/yyyy")}" readonly="true"/>
                </div>
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerEndDate" for="offerEndDateId" class="col-4 col-form-label text-right pr-4">End date</label>
                    <g:textField id="offerEndDateId" name="offerEndDate" class="col-6 form-control bottom-border" value="${endDate?.toString("dd/MM/yyyy")}" readonly="true"/>
                </div>
            </div>

            <div class="row mt-2 mb-5">
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerMaxRedemptions" for="offerMaxRedemptionsId" class="col-4 col-form-label text-right pr-4">Max Redemptions</label>
                    <g:textField id="offerMaxRedemptionsId" name="offerMaxRedemptions" class="col-6 form-control bottom-border numeric-field" value="${loyaltyOffer?.maxRedemptions}" readonly="${isUpdate ? 'false' : 'true'}" />
                </div>
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerMaxBudget" for="offerMaxBudgetId" class="col-4 col-form-label text-right pr-4">Max budget</label>
                    <g:textField id="offerMaxBudgetId" name="offerMaxBudget" class="col-6 form-control bottom-border numeric-field" value="${loyaltyOffer?.maxBudget}" readonly="${isUpdate ? 'false' : 'true'}" />
                </div>
            </div>

            <div class="row mt-2 mb-5">
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerMarketingText" for="offerMarketingTextId" class="col-4 col-form-label text-right pr-4">Marketing Text</label>
                    <g:textArea id="offerMarketingTextId" name="offerMarketingText" class="col-6 form-control select-border" value="${loyaltyOffer?.marketingText}" rows="5" readonly="${isUpdate ? 'false' : 'true'}" />
                </div>
                <div class="form-group row col-12 col-sm-6">
                    <label id="offerTermsText" for="offerTermsTextId" class="col-4 col-form-label text-right pr-4">Terms &amp; Conditions</label>
                    <g:textArea id="offerTermsTextId" name="offerTermsText" class="col-6 form-control select-border" value="${loyaltyOffer?.termsText}" rows="5" readonly="${isUpdate ? 'false' : 'true'}" />
                </div>
            </div>
        </g:form>
    </section>

</body>
</html>