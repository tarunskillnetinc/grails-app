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
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                endDate: new Date().toString(),
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#endDate').datepicker({
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

        function updateSerialNumberInput(selectedValue) {
            document.getElementById('offerPromotionAssignedInput').value = selectedValue;
        }

        function togglePromotionSelectVisibility() {
            var select = document.getElementById("offerPromotionAssignedId");
            if (select.style.display === "none") {
                select.style.display = "block";
            } else {
                select.style.display = "none";
            }
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

        // function updateSegmentInput() {
        //     var selectedDescriptions = [];
        //     var selectedOptions = document.getElementById('segmentAssignedId').selectedOptions;
        //     for (var i = 0; i < selectedOptions.length; i++) {
        //         selectedDescriptions.push(selectedOptions[i].text);
        //     }
        //     document.getElementById('segmentAssignedInput').value = selectedDescriptions.join(', ');
        //     updateCancelIcons();
        // }

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
        //     var selectedOptions = document.getElementById('segmentAssignedId').selectedOptions;
        //     var selectedSegmentsContainer = document.getElementById('selectedSegmentsContainer');
        //     var selectedSegmentsInput = document.getElementById('selectedSegments');
        //     selectedSegmentsContainer.innerHTML = ''; // Clear previous content
        //     var selectedSegments = [];
        //     for (var i = 0; i < selectedOptions.length; i++) {
        //         var description = selectedOptions[i].text;
        //         var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(' + i + ')">&#10006;</span>';
        //         var selectedSegment = document.createElement('div');
        //         selectedSegment.className = 'selected-item';
        //         selectedSegment.innerHTML = description + cancelIcon;
        //         selectedSegmentsContainer.appendChild(selectedSegment);
        //         selectedSegments.push(description);
        //     }
        //     selectedSegmentsInput.value = selectedSegments.join(',');
        // }
        //
        // function removeSelectedItem(index) {
        //     var selectElement = document.getElementById('segmentAssignedId');
        //     selectElement.remove(index);
        //     updateSegmentInput();
        // }

        function updateSegmentInput() {
            var selectedOptions = document.getElementById('offerSegmentAssignedId').selectedOptions;
            var selectedSegmentsContainer = document.getElementById('offerSelectedSegmentsContainer');
            var selectedSegmentsInput = document.getElementById('offerSelectedSegments');

            // Store selected descriptions
            var selectedDescriptions = [];

            // Add or update selected items
            for (var i = 0; i < selectedOptions.length; i++) {
                var description = selectedOptions[i].text;
                var cancelIcon = '<span class="cancel-icon" onclick="removeSelectedItem(' + i + ')">&#10006;</span>';
                var existingItem = findExistingItem(selectedSegmentsContainer, description);

                if (existingItem) {
                    existingItem.innerHTML = description + cancelIcon;
                } else {
                    var selectedSegment = document.createElement('div');
                    selectedSegment.className = 'selected-item';
                    selectedSegment.innerHTML = description + cancelIcon;
                    selectedSegmentsContainer.appendChild(selectedSegment);
                }

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

        function removeSelectedItem(index) {
            var selectElement = document.getElementById('offerSegmentAssignedId');
            selectElement.remove(index);
            updateSegmentInput();
        }


        function saveLoyaltyOffer(){
            var offerDescription = $('#offerDescriptionId').val();
            var offerStartDate = $('#offerStartDateId').val();
            var offerEndDate = $('#offerEndDateId').val();
            var offerMaxRedemption = $('#offerMaxRedemptionsId').val();
            var offerMaxBudget = $('#offerMaxBudgetId').val();

            //Get selected segment items id list
            var selectElement = document.getElementById('offerSegmentAssignedId');
            var selectedIds = [];
            for (var i = 0; i < selectElement.options.length; i++) {
                if (selectElement.options[i].selected) {
                    selectedIds.push(selectElement.options[i].value);
                }
            }

            console.log(selectedIds)
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

    <g:hasErrors bean="${user}">
        <section id="errors-container">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${user}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="add-loyalty-offer-form" action="save" novalidate="novalidate" class="mt-4">
        <g:hiddenField name="id" value="${offer?.id ?: 0}" />

        <div class="form-group row col-12 col-lg-6">
            <label for="offerDescription" class="col-4 col-form-label text-right pr-4">Offer Description</label>
            <g:textField name="offerDescription" id="offerDescriptionId" class="col-5 form-control bottom-border" value="${user?.username}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="offerStartDate" class="col-4 col-form-label text-right pr-4">Start date</label>
            <g:textField name="offerStartDate" id="offerStartDateId" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="offerEndDate" class="col-4 col-form-label text-right pr-4">End date</label>
            <g:textField name="offerEndDate" id="offerEndDateId" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="offerMaxRedemptions" class="col-4 col-form-label text-right pr-4">Max Redemptions</label>
            <g:textField name="offerMaxRedemptions" id="offerMaxRedemptionsId" class="col-5 form-control bottom-border" value="${user?.name}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="offerMaxBudget" class="col-4 col-form-label text-right pr-4">Max budget</label>
            <g:passwordField name="offerMaxBudget" id="offerMaxBudgetId" class="col-5 form-control bottom-border" value="${user?.password}" />
        </div>

        <div class="row form-group row col-12 col-lg-6">
            <label for="offerPromotionAssigned" class="col-4 col-form-label text-right pr-4">Promotion Assigned</label>
            <div class="dropdown-content col-5">
                <div class="input-group-append">
                    <asset:image src="search.png" id="offerPromotionSearchButton" name="offerPromotionSearchButton" onclick="searchProduct()" class="wl-search-button" />
                    <input type="text" class="form-control bottom-border" placeholder="Search For Promotion.." id="offerPromotionAssignedInput" onclick="togglePromotionSelectVisibility()"
                           oninput="filterDropdown('offerPromotionAssignedInput', 'offerPromotionAssignedId', this)">
                </div>
                <g:select id="offerPromotionAssignedId" name="offerPromotionAssigned" size="6" style="overflow-y: scroll; overflow-x: hidden; display: none;" from="${promotionList}" optionValue="description"
                          value="${till?.serialNumber}"
                          optionKey="id"
                          class="form-control select-border"
                          disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
                          onchange="updateSerialNumberInput(this.options[this.selectedIndex].text)"/>
            </div>
        </div>

        <div class="row form-group row col-12 col-lg-6">
            <label for="offerSegmentAssigned" class="col-4 col-form-label text-right pr-4">Segment Assigned</label>
            <div class="dropdown-content col-5">
                <div class="input-group-append">
                    <asset:image src="search.png" id="offerSegmentSearchButton" name="offerSegmentSearchButton" onclick="searchProduct()" class="wl-search-button" />
                    <input type="text" class="form-control bottom-border" placeholder="Search For Segment.." id="offerSegmentAssignedInput" >
                </div>
                <div id="offerSelectedSegmentsContainer" style="height: 100px; overflow-y: auto; border: 1px solid #ccc; margin-top: 5px;; border-top: 0; border-bottom: 1px solid #ccc;"></div>
                <input type="hidden" id="offerSelectedSegments" name="offerSelectedSegments">
                <g:select id="offerSegmentAssignedId" name="offerSegmentAssigned" multiple="multiple" style="display: true;" from="${promotionList}" optionValue="description"
                          value="${till?.serialNumber}" optionKey="id"
                          class="form-control select-border"
                          disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"
                          onchange="updateSegmentInput()"/>
            </div>
        </div>


    </g:form>
</section>
</body>
</html>