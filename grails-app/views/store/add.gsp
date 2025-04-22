<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Add Store</title>
    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="storeCommonUtils.js" />
    <asset:javascript src="inert.min.js" />

    <link rel="stylesheet" href="/assets/bootstrap-datepicker3.min.css?compile=false"/>
    <script type="text/javascript" src="/assets/bootstrap-datepicker.min.js?compile=false"></script>

    <script type="text/javascript">

        var addStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxAddStoreAdditionalDetail')}"
        var saveStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxSaveStoreAdditionalDetail')}"
        var addStoreOtherRestrictionsValues = "${createLink(controller: 'store', action: 'ajaxAddStoreOtherRestrictions')}"
        var saveStoreOtherRestrictions = "${createLink(controller: 'store', action: 'ajaxSaveStoreOtherRestrictions')}"
        var addAmenity = "${createLink(controller: 'store', action: 'ajaxAddAmenities')}"
        var addStoreAmenities = "${createLink(controller: 'store', action: 'ajaxAddStoreAmenity')}"
        var getStoreAmenities = "${createLink(controller: "store", action: 'ajaxGetAllAmenities')}"

        function typeChanged() {
            var selectedType = $("#type option:selected").val();

            // Reset the parent store to None.
            $("#parentStoreId").prop("selectedIndex", 0);

            // Show or hide the parent select div accordingly.
            var parentStoreSelectDiv = $("#parent-store-select");

            if (selectedType !== "STORE") {
                parentStoreSelectDiv.show();
            } else {
                parentStoreSelectDiv.hide();
            }
        }

        function copyConfigFromChanged() {
            var selectedCopyConfigFrom = $("#copyConfigFrom option:selected").val();

            // Reset the range and price band selected to the top value (it won't be used, just resetting it).
            $("#range\\.id").prop("selectedIndex", 0);
            $("#priceBand\\.id").prop("selectedIndex", 0);

            var rangeSelectDiv = $("#range-select-div");
            var priceBandSelectDiv = $("#priceBand-select-div");

            if (selectedCopyConfigFrom === '') {
                rangeSelectDiv.show();
                priceBandSelectDiv.show();
            } else {
                rangeSelectDiv.hide();
                priceBandSelectDiv.hide();
            }
        }

        function updateAlcoholLicensingVisibility() {
            if($('#alcoholLicensingCommand\\.licensedToSellAlcohol').is(':checked')) {
                $('#alcoholHoursContainer').show();
            } else {
                $('#alcoholHoursContainer').hide();
            }
        }

        $(document).ready(function() {
            $(document).on('click', '#addSpecialOpeningHoursModal', function(event) {
                event.stopPropagation();
            });

            $('#alcoholLicensingCommand\\.licensedToSellAlcohol').change(function() {
                updateAlcoholLicensingVisibility();
            });

            updateAlcoholLicensingVisibility();
        });
    </script>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link controller="store" action="index">Store Management</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add Store</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="header-container" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto my-auto">Add Store</h2>
            </div>

            <div class="col-2 text-right">
                <g:link elementId="cancel-btn" controller="store" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
                <button id="save-btn" class="btn btn-success" name="save" onclick="$('#add-store-form').submit();">Save</button>
            </div>
        </div>
    </section>

    <g:hasErrors bean="${store}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${store}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:if test="${flash.message}">
        <section id="errors-container2" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

    <section id="add-store-section" class="container-fluid mt-4">
        <g:form name="add-store-form" action="saveNewStore">
            <div id="accordion">
                <!-- Store information. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="productDetails" data-toggle="collapse" data-target="#collapseProductDetails" aria-expanded="true" aria-controls="collapseProductDetails">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Store Information</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseProductDetails" class="collapse show" aria-labelledby="productDetails" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="type" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Type*</label>
                                    <div class="col-7 col-lg-4 col-xl-2">
                                        <g:select name="type" from="${storeTypes}" value="${store?.type}" valueMessagePrefix="StoreType" class="form-control select-border" onchange="typeChanged();" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="storeNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Number*</label>
                                    <div class="col-7 col-lg-2">
                                        <g:field type="number" min="0" max="999999" maxlength="6" name="storeNumber" value="${store?.storeNumber}"  class="form-control bottom-border" onkeydown="acceptMaxNumberValue(event, 999999);" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="storeName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Name*</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="storeName" maxlength="30" value="${store?.storeName}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row mt-4">
                                    <label for="copyConfigFrom" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Copy Configuration From*</label>

                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:select name="copyConfigFrom"
                                                  from="${parentStores}"
                                                  noSelection="['': 'None / Manually Configure']"
                                                  value="${store?.copyConfigFrom}"
                                                  optionValue="${{it?.config?.storeNumber +' - ' +it?.config?.storeName}}"
                                                  optionKey="id"
                                                  class="form-control select-border"
                                                  onchange="copyConfigFromChanged();" />
                                    </div>
                                </div>

                                <div id="range-select-div" class="form-group row" style="display: ${store?.copyConfigFrom != null ? 'none' : ''};">
                                    <label for="range.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Product Range*</label>

                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:select name="range.id" from="${ranges}" value="${store?.range?.id}" optionValue="description" optionKey="id" class="form-control select-border" />
                                    </div>
                                </div>

                                <div id="priceBand-select-div" class="form-group row" style="display: ${store?.copyConfigFrom != null ? 'none' : ''};">
                                    <label for="priceBand.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Price Band*</label>

                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:select name="priceBand.id" from="${priceBands}" value="${store?.priceBand?.id}" optionValue="description" optionKey="id" class="form-control select-border" />
                                    </div>
                                </div>

                                <div class="form-group row mt-5">
                                    <label for="addressBuildingNumberOrName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Building Name / Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressBuildingNumberOrName" maxlength="30" value="${store?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressLine1" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 1</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressLine1" maxlength="20" value="${store?.addressLine1}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressLine2" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 2</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressLine2" maxlength="20" value="${store?.addressLine2}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressLine3" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 3</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressLine3" maxlength="20" value="${store?.addressLine3}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressTown" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Town / City</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressTown" maxlength="20" value="${store?.addressTown}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressCounty" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">County</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressCounty" maxlength="20" value="${store?.addressCounty}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressCountry" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Country</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressCountry" maxlength="20" value="${store?.addressCountry}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="addressPostCode" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Post Code</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="addressPostCode" maxlength="8" value="${store?.addressPostCode}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="phoneNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Phone Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="phoneNumber" maxlength="12" value="${store?.phoneNumber}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="alternativePhoneNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Alternative Phone Number</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="alternativePhoneNumber" maxlength="12" value="${store?.phoneNumber}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="emailAddress" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Email Address</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="emailAddress" maxlength="254" value="${store?.emailAddress}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="netSalesArea" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Net Sales Area</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="netSalesArea" maxlength="20" value="${store?.netSalesArea}" class="form-control bottom-border"
                                                     onkeydown="acceptFloat(event)"
                                                     onkeyup="validateFloatQuantity(this, 0, 999999.9999, 4)" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="anaCode" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">ANA Code</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="anaCode" maxlength="30" value="${store?.anaCode}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="longitude" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Longitude</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="longitude" maxlength="20" value="${store?.longitude}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="latitude" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Latitude</label>
                                    <div class="col-7 col-lg-4">
                                        <g:textField name="latitude" maxlength="20" value="${store?.latitude}" class="form-control bottom-border" />
                                    </div>
                                </div>

                                <div id="parent-store-select" class="form-group row" style="display: none;">
                                    <label for="parentStoreId" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Parent Store</label>

                                    <div class="col-7 col-lg-4 col-xl-3">
                                        <g:select name="parentStoreId"
                                                  from="${parentStores}"
                                                  noSelection="['': 'None']"
                                                  value="${store?.parentStoreId}"
                                                  optionValue="${{it?.config?.storeNumber + ' - ' +it?.config?.storeName}}"
                                                  optionKey="id"
                                                  class="form-control select-border" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <g:render template="sharedStoreConfigurationAccordionsTop" model="[storeAdditionalDetails: storeAdditionalDetails, storeOpeningHoursCommand: storeOpeningHoursCommand]"/>
                <g:render template="sharedStoreConfigurationAccordionsBottom"/>
            </div>
        </g:form>
    </section>


</body>
</html>