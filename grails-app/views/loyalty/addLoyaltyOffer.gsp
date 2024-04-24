<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Add Loyalty Offer</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type='text/javascript'>
        let isUpdate = false

        $(function() {
            // Set start date to today and initialize datepicker
            $('#offerStartDateId').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            // Set end date to one week from today and initialize datepicker
            $('#offerEndDateId').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: new Date(), // Set start date to today
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            //This is for multi select
            $('#PromotionAssignedId').attr('multiple', 'multiple');

            //By using the changingDate flag, ensure that the function is only called once per change event
            var changingDate = false;

            // Add change event listener to offer end date to validate and correct dates
            $('#offerEndDateId').change(function() {
                if (!changingDate) {
                    changingDate = true;
                    validateAndCorrectDates();
                    changingDate = false;
                }
            });

            // Add change event listener to offer start date to validate and correct dates
            $('#offerStartDateId').change(function() {
                if (!changingDate) {
                    changingDate = true;
                    validateAndCorrectDates();
                    changingDate = false;
                }
            });

        });

        //Adding event listener to load existing multi selected segments  + promotions
        window.addEventListener('load', function() {
            updatePromotionDescriptionOnLoading()
            updateSegmentInputOnLoading()
            isUpdate = ${isUpdate}
        })

        function validateAndCorrectDates() {
            var startDate = $('#offerStartDateId').datepicker('getDate');
            var endDate = $('#offerEndDateId').datepicker('getDate');

            // Check if end date is before start date
            if (endDate < startDate) {
                // Set start date to today
                $('#offerStartDateId').datepicker('setDate', new Date());

                // Set end date to 1 week from start date
                var newEndDate = new Date();
                newEndDate.setDate(newEndDate.getDate() + 7);
                $('#offerEndDateId').datepicker('setDate', newEndDate);
            }
        }

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

        function updatePromotionInput(selectedValue) {
            document.getElementById('offerPromotionAssignedInput').value = selectedValue;
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


        function saveLoyaltyOffer(){

            if(validateMandatoryFields()){//Validate for mandatory fields
                return
            }

            var offerId = $('#offerId').val();
            var offerDescription = $('#offerDescriptionId').val();
            var offerStartDate = $('#offerStartDateId').val();
            var offerEndDate = $('#offerEndDateId').val();
            var offerMaxRedemption = $('#offerMaxRedemptionsId').val();
            var offerMaxBudget = $('#offerMaxBudgetId').val();
            var offerStatus = $('#offerStatusId').val();
            var selectedPromotion = null

            //Get promotion id
            var selectElement = document.getElementById('offerPromotionAssignedId');
            var selectedOption = selectElement.options[selectElement.selectedIndex];
            if(selectedOption != null){
                selectedPromotion = selectedOption.value;
            }
            //Prepare parameter map
            var params = {
                id: offerId,
                offerDescription: offerDescription,
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
                            var errorDiv = $('<div class="alert alert-danger alert-wl mx-0" role="alert"></div>');

                            errorList.forEach(function(errorMessage) {
                                var errorMessageSpan = $('<span>' + errorMessage + '</span>');
                                errorDiv.append(errorMessageSpan);
                                errorDiv.append($('<br>'));
                            });

                            var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');

                            closeIcon.click(function () {
                                errorDiv.remove(); // Remove the error message div when the cancel icon is clicked
                            });

                            errorDiv.append(closeIcon);
                            $('#errors-container').html(errorDiv);

                            // Adjust icon position to top-right corner
                            closeIcon.css({
                                "position": "absolute",
                                "top": "-10px",
                                "right": "1px",
                                "margin": "0.5rem"
                            });
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

        function validateMandatoryFields(){
            var offerDescription = $('#offerDescriptionId').val();
            var offerStartDate = $('#offerStartDateId').val();
            var offerEndDate = $('#offerEndDateId').val();
            var offerStatus = $('#offerStatusId').val();
            var selectedSegments = $('#offerSelectedSegmentsContainer .selected-item');

            // Perform form validation
            if (offerDescription.trim() === "" || offerStartDate.trim() === "" || offerEndDate.trim() === "" || offerStatus.trim() === "") {
                var errorMessage = "All mandatory fields must be present before data can be saved.";
                createErrorAlert(errorMessage)
                return true; // Stop further execution of saveLoyaltyOffer() if form validation fails
            }else if (selectedSegments.length === 0) {
                var errorMessage = "At least one segment must be selected.";
                createErrorAlert(errorMessage)
                return true; // Stop further execution of saveLoyaltyOffer() if segment validation fails
            }

            return false
        }

        function createErrorAlert(errorMessage){
            var errorDiv = $('<div class="alert alert-danger alert-wl mx-0" role="alert"></div>');
            var errorMessageSpan = $('<span id="error-message">' + errorMessage + '</span>');
            var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');

            closeIcon.click(function () {
                errorDiv.remove(); // Remove the error message div when the cancel icon is clicked
            });

            errorDiv.append(closeIcon);
            errorDiv.append(errorMessageSpan);
            $('#errors-container').html(errorDiv);

            // Adjust icon position to top-right corner
            closeIcon.css({
                "position": "absolute",
                "top": "-10px",
                "right": "1px",
                "margin": "0.5rem"
            });

        }

    </script>

    <style>
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

        #errors-container {
            margin-top: 20px; /* Adjust the value as needed */
            margin-bottom: 20px; /* Adjust the value as needed */
        }

    </style>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="loyalty" action="loyaltyOffers">Loyalty Offer</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add Loyalty Offer</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="add-loyalty-offer-section" class="container-fluid">
        <div class="row header-wl mt-3" id="add-loyalty-offer-header">
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Add Loyalty Offer</h2>
            </div>

            <div class="col-2 text-right">
                <g:link id="cancel" class="btn btn-warning" name="cancel" action="loyaltyOffers" onclick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                <button id="save" class="btn btn-success" name="save" onclick="saveLoyaltyOffer();">Save</button>
            </div>
        </div>

        <section id="errors-container" class="container-fluid mb-20"></section>

        <g:form name="add-loyalty-offer-form" id="add-loyalty-offer-form-id" action="save" novalidate="novalidate" class="mt-4">
            <g:hiddenField name="offerId" value="${loyaltyOffer?.id ?: 0}" />

            <div class="row mt-5 mb-3">
                <div class="form-group row col-12 col-sm-6 offset-sm-1">
                    <label for="offerDescription" class="col-4 col-form-label text-right pr-4">Offer Description</label>
                    <g:textField name="offerDescription" id="offerDescriptionId" class="col-5 form-control bottom-border" value="${loyaltyOffer?.offerDescription}" autocomplete="off" />
                </div>
                <div class="form-group row col-12 col-12 col-sm-5">
                    <label for="role" class="col-4 col-form-label text-right pr-4">Status</label>
                    <g:select name="role" id="offerStatusId" class="col-3 form-control select-border" from="${eligibleOfferStatus}" value="${loyaltyOffer?.status ? loyaltyOffer?.status : defaultStatus}" valueMessagePrefix="Role" />
                </div>
            </div>

            <div class="row mt-2 mb-3">
                <div class="form-group row col-12 col-sm-6 offset-sm-1">
                    <label for="offerStartDate" class="col-4 col-form-label text-right pr-4">Start date</label>
                    <g:textField name="offerStartDate" id="offerStartDateId" class="col-5 form-control bottom-border" value="${startDate?.toString("dd/MM/yyyy")}" readonly="false"/>
                </div>
                <div class="form-group row col-12 col-12 col-sm-5">
                    <label for="offerEndDate" class="col-4 col-form-label text-right pr-4">End date</label>
                    <g:textField name="offerEndDate" id="offerEndDateId" class="col-5 form-control bottom-border" value="${endDate?.toString("dd/MM/yyyy")}" readonly="false"/>
                </div>
            </div>

            <div class="row mt-2 mb-5">
                <div class="form-group row col-12 col-sm-6 offset-sm-1">
                    <label for="offerMaxRedemptions" class="col-4 col-form-label text-right pr-4">Max Redemptions</label>
                    <g:textField name="offerMaxRedemptions" id="offerMaxRedemptionsId" class="col-5 form-control bottom-border" value="${loyaltyOffer?.maxRedemptions}" autocomplete="off" />
                </div>
                <div class="form-group row col-12 col-12 col-sm-5">
                    <label for="offerMaxBudget" class="col-4 col-form-label text-right pr-4">Max budget</label>
                    <g:textField name="offerMaxBudget" id="offerMaxBudgetId" class="col-5 form-control bottom-border" value="${loyaltyOffer?.maxBudget}" />
                </div>
            </div>


            <div class="row mt-5 mb-3">
                <div class="form-group row col-12 col-sm-6 offset-sm-1">
                    <label for="offerPromotionAssigned" class="col-4 col-form-label text-right pr-4">Promotion Assigned</label>
                    <div class="dropdown-content col-5">
                        <div class="input-group-append">
                            <asset:image src="search.png" id="offerPromotionSearchButton" name="offerPromotionSearchButton" onclick="searchProduct()" class="wl-search-button" />
                            <input type="text" class="form-control bottom-border" placeholder="Search For Promotion.." id="offerPromotionAssignedInput"
                                   oninput="filterDropdown('offerPromotionAssignedInput', 'offerPromotionAssignedId', this)" ${isUpdate ? 'disabled' : ''}>
                        </div>
                        <g:select id="offerPromotionAssignedId" name="offerPromotionAssigned" size="6" style="overflow-y: scroll; overflow-x: hidden; display: true;" from="${promotions}" optionValue="description"
                                  value="${loyaltyOffer?.retailerOfferId}"
                                  optionKey="id"
                                  class="form-control select-border"
                                  disabled="${isUpdate ? true : false}"
                                  onchange="updatePromotionInput(this.options[this.selectedIndex].text)"/>
                    </div>
                </div>

                <div class="form-group row col-12 col-12 col-sm-5">
                    <label for="offerSegmentAssigned" class="col-4 col-form-label text-right pr-4">Segment Assigned</label>
                    <div class="dropdown-content col-5">
                        <div class="input-group-append">
                            <asset:image src="search.png" id="offerSegmentSearchButton" name="offerSegmentSearchButton" onclick="searchProduct()" class="wl-search-button" />
                            <input type="text" class="form-control bottom-border" placeholder="Search For Segment.." id="offerSegmentAssignedInput" ${isUpdate ? 'disabled' : ''}>
                        </div>
                        <div id="offerSelectedSegmentsContainer" style="height: 100px; overflow-y: auto; border: 1px solid #ccc; margin-top: 5px; border-top: 0; border-bottom: 1px solid #ccc;"></div>
                        <input type="hidden" id="offerSelectedSegments" name="offerSelectedSegments" readonly = "${isUpdate ? true : false}">
                        <g:select id="offerSegmentAssignedId" name="offerSegmentAssigned" multiple="multiple" style="display: true;" from="${segments}" optionValue="description"
                                  value="${selectedSegmentIds}" optionKey="id" class="form-control select-border" disabled="${isUpdate ? true : false}" onchange="updateSegmentInput()"/>
                    </div>
                </div>

            </div>
        </g:form>
    </section>

    <section id="addLoyaltyOffers-modal" class="container-fluid" >
        <div class="modal fade" id="addLoyaltyOffersModal" tabindex="-1" role="dialog" aria-labelledby="addLoyaltyOffersModalLabel" data-backdrop="false" aria-hidden="true" style="margin-top: 120px">
            <div class="modal-dialog modal-lg" style="border: 2px black solid ; margin-top: 120px" role="document" >
                <div id="addLoyaltyOffersContent" class="modal-content" ></div>
            </div>
        </div>
    </section>

</body>
</html>