<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Store Configuration</title>

    <asset:stylesheet src="multi-select-checks.css" />
    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="store-settings/color-pick.js" />
    <asset:javascript src="popper.min.js" />
    <asset:javascript src="multi-select-checks.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="numberHelper.js" />
    <asset:javascript src="storeCommonUtils.js" />

    <link rel="stylesheet" href="/assets/bootstrap-datepicker3.min.css?compile=false"/>
    <script type="text/javascript" src="/assets/bootstrap-datepicker.min.js?compile=false"></script>

    <script type="text/javascript">

        var addStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxAddStoreAdditionalDetail')}"
        var saveStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxSaveStoreAdditionalDetail')}"
        var addStoreOtherRestrictionsValues = "${createLink(controller: 'store', action: 'ajaxAddStoreOtherRestrictions')}"
        var saveStoreOtherRestrictions = "${createLink(controller: 'store', action: 'ajaxSaveStoreOtherRestrictions')}"
        var addAmenity = "${createLink(controller: 'store', action: 'ajaxAddAmenities')}"
        var addStoreAmenities = "${createLink(controller: 'store', action: 'ajaxAddStoreAmenity')}"

        function updateColorIndicator(color, indicatorId) {
            var colorPickerElement = document.getElementById(indicatorId);
            colorPickerElement.value = "#" + color; // Prepend "#" to the color value
        }

        function updateAlcoholLicensingVisibility() {
            if($('#alcoholLicensingCommand\\.licensedToSellAlcohol').is(':checked')) {
                $('#alcoholHoursContainer').show();
            } else {
                $('#alcoholHoursContainer').hide();
            }
        }

        $(document).ready(function() {
            // Select the first tab by default if none are active
            if (!$('.nav-link.active').length) {
                $('#store-tab').addClass('active');
                $('#store-container').addClass('active');
            }

            // Detect tab change event
            $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                var selectedTab = $(e.target).attr('id');  // Get the ID of the selected tab
                console.log("Selected tab: " + selectedTab);

            });

            $.get("${createLink(controller: 'cashManagement', action: 'index')}?storeId=" + ${storeSettings?.id} + "&onlyRetailerLevel=false&storeNumber=${storeSettings?.config?.storeNumber}&storeName=${storeSettings?.config?.storeName}", function(data) {
                $('#cash-container').html(data);
            });

            $('#alcoholLicensingCommand\\.licensedToSellAlcohol').change(function() {
                updateAlcoholLicensingVisibility();
            });

            updateAlcoholLicensingVisibility();

        });

    </script>

    <style>
        .tooltip-trigger {
            position: relative;
            cursor: pointer;
            display: inline-block;
            width: 25px; /* Adjust size as needed */
            height: 25px; /* Adjust size as needed */
            border-radius: 50%; /* Makes it round */
            background-color: lightblue; /* Light blue background color */
            text-align: center; /* Centers the '?' mark */
            line-height: 25px; /* Vertically centers the '?' mark */
        }

        .tooltip-trigger:hover .tooltip-content {
            display: inline-block;
        }

        #fake-tab div{
            border-bottom: 2px solid #575756;
            width: 100%;
            height: 100%;
            margin-bottom: 2px;
        }
        #tab-ul {
            border-bottom-color: white !important;
        }
        #tab-ul.tabs-wl li a.active {
            border-style: solid;
            border-width: 2px 2px 0 2px;
            border-color: #575756 !important;
            border-bottom-color: white !important;
            color: #575756 !important;
            background-color: white !important;
        }
        #tab-ul.tabs-wl li a {
            text-align: center;
            width: 300px;
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
                        <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link controller="store" action="index" params="[storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter, max: max, offset: offset, sort: sort, order: order]">Store Management</g:link></li>
                            </g:if>
                        </sec:ifAnyGranted>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Store ${storeSettings?.config?.storeNumber} Configuration</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>
    <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
        <section id="tab-container" class="container-fluid m-0">
            <div class="row m-0">
                <div class="col-12 pl-0">
                    <ul id="tab-ul" class="nav nav-tabs tabs-wl d-flex m-0" role="tablist">
                        <li class="nav-item m-0">
                            <a id="store-tab" data-toggle="tab" href="#store-container" aria-selected="true" role="tab" aria-controls="store-container" class="nav-link ${ tabType.equals('store' ? 'active' : 'disabled')}">Store Config</a>
                        </li>
                        <li class="nav-item m-0">
                            <a id="cash-tab" data-toggle="tab" href="#cash-container" role="tab" aria-controls="cash-container" class="nav-link ${tabType.equals('cash' ? 'active' : 'disabled')}">Cash Management</a>
                        </li>
                        <li id="fake-tab" class="nav-item flex-grow-1 m-0">
                            <div></div>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="tab-content mt-3">
                <div id="store-container" class="tab-pane ${tabType.equals('store') ? 'active' : ''}">
                    <g:render template="storeConfig" model='${pageScope}'/>
                </div>
                <div id="cash-container" class="tab-pane ${tabType.equals('cash') ? 'active' : ''}">

                </div>
            </div>
        </section>
    </g:if>
    <g:else>
        <g:render template="storeConfig" model='${pageScope}'/>
    </g:else>

    <section id="addAmenities-modal" class="container-fluid">
        <div class="modal fade" id="addAmenitiesModal" tabindex="-1" role="dialog" aria-labelledby="addAmenitiesModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-xl" role="document">
                <div id="addAmenitiesContent" class="modal-content"></div>
            </div>
        </div>
    </section>

</body>
</html>