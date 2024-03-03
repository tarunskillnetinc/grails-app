<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Add Loyalty Offer</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type='text/javascript'>
        let dropdownToggled = false;

        $(function() {
            $('#offerStartDateId').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                endDate: new Date().toString(),
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#offerEndDateId').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                endDate: new Date().toString(),
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#PromotionAssignedId').attr('multiple', 'multiple');

        });

        window.addEventListener('load', function() {
            updatePromotionDescriptionOnLoading()
            updateSegmentInputOnLoading()
        })

        function updateSegmentInputOnLoading() {
            console.log("hiii " +  ${selectedSegmentIds})
            var selectedSegmentIdList = ${selectedSegmentIds}; // Get the selected segment IDs from the server response

                var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
                selectedSegmentsContainer.innerHTML = ''; // Clear previous content

                // Retrieve selected segment descriptions based on IDs
                var selectedDescriptions = [];
                for (var i = 0; i < ('${selectedSegmentIds}').length; i++) {
                    var segmentId = selectedSegmentIdList[i];
                    var selectedSegment = findSegmentById(segmentId);
                    console.log("selected segment " + selectedSegment)
                    if (selectedSegment) {
                        selectedDescriptions.push(selectedSegment.description);
                    }
                }

                // Display selected segments in offerSelectedSegmentsContainer
                for (var j = 0; j < selectedDescriptions.length; j++) {
                    var description = selectedDescriptions[j];
                    var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(event)">&#10006;</span>';
                    var selectedSegment = document.createElement('div');
                    selectedSegment.className = 'selected-item';
                    selectedSegment.innerHTML = description + cancelIcon;
                    selectedSegmentsContainer.appendChild(selectedSegment);
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


        function togglePromotionSelectVisibility() {
            var select = document.getElementById("offerPromotionAssignedId");
            if (select.style.display === "none") {
                select.style.display = "block";
            } else {
                select.style.display = "none";
            }
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


        function toggleSegmentSelectVisibility() {
            var select = document.getElementById("offerSegmentAssignedId");
            if (select.style.display === "none") {
                select.style.display = "block";
            } else {
                select.style.display = "none";
            }
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

        function updateCancelIcons() {
            var selectedOptions = document.getElementById('offerSegmentAssignedId').selectedOptions;
            var inputField = document.getElementById('offerSegmentAssignedInput');
            inputField.innerHTML = '';
            for (var i = 0; i < selectedOptions.length; i++) {
                var span = document.createElement('span');
                span.innerHTML = selectedOptions[i].text;
                var cancelIcon = document.createElement('span');
                cancelIcon.className = 'cancel-icon';
                cancelIcon.innerHTML = '&#10006;';
                cancelIcon.setAttribute('data-index', i);
                cancelIcon.onclick = function() {
                    var index = this.getAttribute('data-index');
                    removeSelectedItem(index);
                };
                span.appendChild(cancelIcon);
                inputField.appendChild(span);
                inputField.appendChild(document.createTextNode(', '));
            }
        }



        // function updateSegmentInput() {
        //     var selectedOptions = document.getElementById('offerSegmentAssignedId').selectedOptions;
        //     var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
        //     var selectedSegmentsInput = document.getElementById('offerSelectedSegments');
        //
        //     // Store selected descriptions
        //     var selectedDescriptions = [];
        //
        //     // Add or update selected items
        //     for (var i = 0; i < selectedOptions.length; i++) {
        //         var description = selectedOptions[i].text;
        //         var cancelIcon = '<span class="cancel-icon" data-index="' + i + '" onclick="removeSelectedItem(event)">&#10006;</span>';
        //         var existingItem = findExistingItem(selectedSegmentsContainer, description);
        //
        //         if (existingItem) {
        //             existingItem.innerHTML = description + cancelIcon;
        //         } else {
        //             var selectedSegment = document.createElement('div');
        //             selectedSegment.className = 'selected-item';
        //             selectedSegment.innerHTML = description + cancelIcon;
        //             selectedSegmentsContainer.appendChild(selectedSegment);
        //         }
        //
        //         selectedDescriptions.push(description);
        //     }
        //
        //     // Update hidden input value
        //     selectedSegmentsInput.value = selectedDescriptions.join(',');
        // }

        function updateSegmentInput() {
            var selectedOptions = document.getElementById('offerSegmentAssignedId').selectedOptions;
            var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
            var selectedSegmentsInput = document.getElementById('offerSelectedSegments');

            // Clear the container before updating
            selectedSegmentsContainer.innerHTML = '';

            // Store selected descriptions
            var selectedDescriptions = [];

            // Add or update selected items
            for (var i = 0; i < selectedOptions.length; i++) {
                var description = selectedOptions[i].text;
                var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(event)">&#10006;</span>';
                var selectedSegment = document.createElement('div');
                selectedSegment.className = 'selected-item';
                selectedSegment.innerHTML = description + cancelIcon;
                selectedSegmentsContainer.appendChild(selectedSegment);

                selectedDescriptions.push(description);
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

        // function removeSelectedItem(index) {
        //     var selectElement = document.getElementById('offerSegmentAssignedId');
        //     selectElement.remove(index);
        //     updateSegmentInput();
        // }

        function removeSelectedItem(event) {
            var container = event.target.closest('.selected-item'); // Find the closest parent container with the class 'selected-item'
            if (container) {
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
            var offerDescription = $('#offerDescriptionId').val();
            var offerStartDate = $('#offerStartDateId').val();
            var offerEndDate = $('#offerEndDateId').val();
            var offerMaxRedemption = $('#offerMaxRedemptionsId').val();
            var offerMaxBudget = $('#offerMaxBudgetId').val();
            var offerStatus = $('#offerStatusId').val();

            //Get promotion id
            var selectElement = document.getElementById('offerPromotionAssignedId');
            var selectedOption = selectElement.options[selectElement.selectedIndex];
            var selectedPromotion = selectedOption.value;

            //Prepare parameter map
            var params = {
                offerDescription: offerDescription,
                startDate: offerStartDate,
                endDate: offerEndDate,
                maxBudget: offerMaxBudget,
                maxRedemptions: offerMaxRedemption,
                retailerOfferId: selectedPromotion,
                status: offerStatus
            };

            //Get selected segment items id list
            var selectElement = document.getElementById('offerSegmentAssignedId');
            loyaltySegmentIndex = 0
            for (var i = 0; i < selectElement.selectedOptions.length; i++) {
                params["loyaltyOfferSegments[" + loyaltySegmentIndex + "].segmentId"] = selectElement.selectedOptions[i].value;
                loyaltySegmentIndex++;
            }
            $.ajax({
                url: "${createLink(controller: 'loyalty', action: 'ajaxSaveLoyaltyOffers')}",
                method: "POST",
                data: params,
                statusCode: {
                    500: function (response) {
                        var errorMessage = response.responseJSON.error;
                        if (errorMessage) {
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
                    },
                    200: function (response) {
                        //window.location.href = window.location.href = '${createLink(controller: 'loyalty', action:'loyaltyOffers')}';
                    }
                }
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

<section id="add-user-section" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 class="mx-auto my-auto">Add Loyalty Offer</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel" controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>
            <button id="save" class="btn btn-success" name="save" onclick="saveLoyaltyOffer();">Save</button>
        </div>
    </div>

    <g:if test="${flash.message}">
        <section id="errors-container">
            <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

    <section id="errors-container" class="container-fluid"></section>


    <g:form name="add-loyalty-offer-form" action="save" novalidate="novalidate" class="mt-4">
        <g:hiddenField name="id" value="${offer?.id ?: 0}" />

        <div class="row mt-5 mb-3">
            <div class="form-group row col-12 col-sm-6 offset-sm-1">
                <label for="offerDescription" class="col-4 col-form-label text-right pr-4">Offer Description</label>
                <g:textField name="offerDescription" id="offerDescriptionId" class="col-5 form-control bottom-border" value="${loyaltyOffer?.offerDescription}" autocomplete="off" />
            </div>
            <div class="form-group row col-12 col-12 col-sm-5">
                <label for="role" class="col-4 col-form-label text-right pr-4">Status</label>
                <g:select name="role" id="offerStatusId" class="col-3 form-control select-border" from="${eligibleOfferStatus}" value="${loyaltyOffer?.status}" valueMessagePrefix="Role" />
            </div>
        </div>

        <div class="row mt-2 mb-3">
            <div class="form-group row col-12 col-sm-6 offset-sm-1">
                <label for="offerStartDate" class="col-4 col-form-label text-right pr-4">Start date</label>
                <g:textField name="offerStartDate" id="offerStartDateId" class="col-5 form-control bottom-border" value="${g.formatDate(format: "dd/MM/yyyy", date: loyaltyOffer?.startDate)}" readonly="false"/>
            </div>
            <div class="form-group row col-12 col-12 col-sm-5">
                <label for="offerEndDate" class="col-4 col-form-label text-right pr-4">End date</label>
                <g:textField name="offerEndDate" id="offerEndDateId" class="col-5 form-control bottom-border" value="${g.formatDate(format: "dd/MM/yyyy", date: loyaltyOffer?.endDate)}" readonly="false"/>
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
                        <input type="text" class="form-control bottom-border" placeholder="Search For Promotion.." id="offerPromotionAssignedInput" onclick="togglePromotionSelectVisibility()"
                               oninput="filterDropdown('offerPromotionAssignedInput', 'offerPromotionAssignedId', this)">
                    </div>
                    <g:select id="offerPromotionAssignedId" name="offerPromotionAssigned" size="6" style="overflow-y: scroll; overflow-x: hidden; display: true;" from="${promotions}" optionValue="description"
                              value="${loyaltyOffer?.retailerOfferId}"
                              optionKey="id"
                              class="form-control select-border"
                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
                              onchange="updatePromotionInput(this.options[this.selectedIndex].text)"/>
                </div>
            </div>

            <div class="form-group row col-12 col-12 col-sm-5">
                <label for="offerSegmentAssigned" class="col-4 col-form-label text-right pr-4">Segment Assigned</label>
                <div class="dropdown-content col-5">
                    <div class="input-group-append">
                        <asset:image src="search.png" id="offerSegmentSearchButton" name="offerSegmentSearchButton" onclick="searchProduct()" class="wl-search-button" />
                        <input type="text" class="form-control bottom-border" placeholder="Search For Segment.." id="offerSegmentAssignedInput" >
                    </div>
                    <div id="offerSelectedSegmentsContainer" style="height: 100px; overflow-y: auto; border: 1px solid #ccc; margin-top: 5px;; border-top: 0; border-bottom: 1px solid #ccc;"></div>
                    <input type="hidden" id="offerSelectedSegments" name="offerSelectedSegments">
                    <g:select id="offerSegmentAssignedId" name="offerSegmentAssigned" multiple="multiple" style="display: true;" from="${segments}" optionValue="description"
                              value="${selectedSegmentIds}" optionKey="id"
                              class="form-control select-border"
                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
                              onchange="updateSegmentInput()"/>
                </div>
            </div>

        </div>
    </g:form>
</section>
</body>
</html>