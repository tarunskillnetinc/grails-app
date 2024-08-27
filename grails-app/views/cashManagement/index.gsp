<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="popper.min.js" />
    <asset:javascript src="multi-select-checks.js" />
    <style>
        .multiselect-container {
            position: relative;
            display: inline-block;
            width: 100%;
        }
        .multiselect-display {
            width: 100%;
            height: 40px;
            padding: 8px;
            border: 1px solid #ced4da;
            border-radius: 0.25rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            cursor: pointer;
            background-color: #fff;
        }
        .dropdown-arrow {
            margin-left: 10px;
        }
        .multiselect-items {
            display: none;
            position: absolute;
            width: 100%;
            background-color: #fff;
            border: 1px solid #ced4da;
            max-height: 200px;
            overflow-y: auto;
            z-index: 1;
        }
        .multiselect-items .dropdown-item {
            display: flex;
            align-items: center;
            padding: 10px;
        }
        .multiselect-items .dropdown-item input {
            margin-right: 10px;
        }
        .multiselect-items.show {
            display: block;
        }
    </style>
    <script type='text/javascript'>
        %{--var getBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxGetBrandLogo')}";--}%
        %{--var resetBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxResetBrandLogo')}";--}%

        %{--function validateImg(input) {--}%
        %{--    if (input.files[0].size >= 1048576 /* 1MB */) {--}%
        %{--        return '${message(code:'retailer.logo.maxsize', default:"Image file size too large")}'--}%
        %{--    }--}%
        %{--    if (input.files[0].type !== "image/png") {--}%
        %{--        return '${message(code:'button.error.incompatible.message', default:"Image incorrect file type. Please use .png.")}'--}%
        %{--    }--}%
        %{--}--}%

        $(document).ready(function () {
            // $('input[name=brandLogo]').change(function() {
            //     const error = validateImg(this);
            //     if (error) {
            //         $('input[name=brandLogo]').val(null);
            //         alert(error);
            //         return
            //     }
            //
            //     const fileData = this.files[0];
            //     if (FileReader && fileData) {
            //         const urlFileReader = new FileReader();
            //         urlFileReader.onload = function () {
            //             const brandingImage = $(".branding-image");
            //             brandingImage.attr("src", urlFileReader.result);
            //             brandingImage.removeAttr("hidden");
            //         }
            //         urlFileReader.readAsDataURL(fileData);
            //     }
            // });
            //
            // $.ajax({
            //     url: getBrandLogoUrl,
            //     success: function(resp) {
            //         if (resp === '') {
            //             var brandingImage = $(".branding-image");
            //             brandingImage.attr("src", "");
            //             brandingImage.attr("hidden", "");
            //         } else {
            //             var brandingImage = $(".branding-image");
            //             brandingImage.attr("src", "data:image/png;base64," + resp);
            //             brandingImage.removeAttr("hidden");
            //         }
            //         // Iterate over each element with the class "item-label" and update its content
            //         $(".item-label").each(function() {
            //             const item = $(this).text(); // Get the text content of the current div
            //             const readableItem = camelToReadable(item); // Convert to readable format
            //             $(this).text(readableItem); // Update the div's content
            //         });
            //     }
            // });
            $('#selectedItemsDisplay').click(function() {
                $('#weekdayDropdown').toggleClass('show');
            });

            $('.weekday-checkbox').change(function() {
                updateSelectedItems();
            });

            function updateSelectedItems() {
                var selectedItems = [];
                $('.weekday-checkbox:checked').each(function() {
                    selectedItems.push($(this).val());
                });
                $('#selectedItemsDisplay > span:first-child').html(selectedItems.join(', ') || 'Select Weekdays');
            }

            $(document).click(function(event) {
                if(!$(event.target).closest('.multiselect-container').length) {
                    if($('#weekdayDropdown').hasClass('show')) {
                        $('#weekdayDropdown').removeClass('show');
                    }
                }
            });

            const weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
            createMultiSelectorChecks('automaticCloseDaysSelector', 'automaticCloseDays', weekdays, "Select Days");
            createMultiSelectorChecks('tillAutoSnapshotDaysSelector', 'tillAutoSnapshotDays', weekdays, "Select Days");
        });

        %{--function resetBrandLogo() {--}%
        %{--    if (confirm("This will reset your brand logo to Trust retail default.")) {--}%
        %{--        $.ajax({--}%
        %{--            url: resetBrandLogoUrl,--}%
        %{--            success: function (resp) {--}%
        %{--                var brandingImage = $(".branding-image");--}%
        %{--                brandingImage.attr("src", "");--}%
        %{--                brandingImage.attr("hidden", "");--}%
        %{--            }--}%
        %{--        });--}%
        %{--    }--}%
        %{--}--}%
        %{--function camelToReadable(camelCaseString) {--}%
        %{--    // Use a regular expression to split the string at capital letters--}%
        %{--    const words = splitCamelCaseString(camelCaseString);--}%
        %{--    // Capitalize the first letter of each word and join with spaces--}%
        %{--    const readableString = words.map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');--}%
        %{--    return readableString;--}%
        %{--}--}%

        %{--function renameBottomButtonName(camelCaseString) {--}%
        %{--    const words = splitCamelCaseString(camelCaseString);--}%
        %{--    words[1].charAt(0).toUpperCase();--}%
        %{--    return words[1];--}%
        %{--}--}%

        %{--function splitCamelCaseString(camelCaseString) {--}%
        %{--    return camelCaseString.split(/(?=[A-Z])/);--}%
        %{--}--}%
    </script>
</head>
<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Cash Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Cash Management</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel-btn" controller="retailer" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
            <button id="save-btn" class="btn btn-success" name="save" onclick="$('#save-button').submit();">Save</button>
        </div>
    </div>
</section>

%{--<g:hasErrors bean="${retailer}">--}%
%{--    <section id="errors-container" class="container-fluid">--}%
%{--        <div class="alert alert-danger alert-wl mx-0" role="alert">--}%
%{--            <g:renderErrors bean="${retailer}" as="list"/>--}%
%{--        </div>--}%
%{--    </section>--}%
%{--</g:hasErrors>--}%

%{--<g:if test="${flash.error}">--}%
%{--    <section id="errors-container2" class="container-fluid">--}%
%{--        <div class="alert alert-danger alert-wl mx-0" role="alert">--}%
%{--            <ul>--}%
%{--                <g:each in="${flash.error}" var="error" status="i">--}%
%{--                    <li>${error}</li>--}%
%{--                </g:each>--}%
%{--            </ul>--}%
%{--        </div>--}%
%{--    </section>--}%
%{--</g:if>--}%

%{--<g:if test="${flash.message}">--}%
%{--    <section id="errors-container2" class="container-fluid">--}%
%{--        <div class="alert alert-success alert-wl mx-0" role="alert">--}%
%{--            <g:each in="${flash.message}" var="message" status="i">--}%
%{--                ${message}<br/>--}%
%{--            </g:each>--}%
%{--        </div>--}%
%{--    </section>--}%
%{--</g:if>--}%


<section id="addProduct-section" class="container-fluid mt-4">
    <g:uploadForm name="save-button" action="save" method="POST" enctype="multipart/form-data">
        <div id="accordion">
            <!-- General information. -->
            <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                <div class="card-header pointer" id="generalDetails" data-toggle="collapse" data-target="#collapseGeneralDetails" aria-expanded="true" aria-controls="collapseGeneralDetails">
                    <div class="row">
                        <div class="col-10 font-weight-bold">Cash Management</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div id="collapseGeneralDetails" class="collapse show" aria-labelledby="generalDetails" data-parent="#accordion">
                    <div class="card-body py-5">
                        <div class="col-12">
                            <h5 class="text-center">Till Shifts</h5>
                            <div class="form-group row">
                                <label for="isManualOpen" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Manual Open</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isManualOpen" id="isManualOpen" ${cashManagement?.config?.isManualOpen ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="isManualClose" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Manual Close</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isManualClose" id="isManualClose" ${cashManagement?.config?.isManualClose ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="automaticCloseDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Automatic Close Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="automaticCloseDays" name="automaticCloseDays"/>
                                    <div id="automaticCloseDaysSelector"></div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="automaticCloseTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Automatic Close Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="automaticCloseTime" id="automaticCloseTime" value="${cashManagement?.config?.automaticCloseTime}" placeholder="HH:MM"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Rolling Float</h5>
                            <div class="form-group row">
                                <label for="isRollingFloatEnable" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Rolling Float Enable</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isRollingFloatEnable" id="isRollingFloatEnable" ${cashManagement?.config?.isRollingFloatEnable ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="rollingFloatValue" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Rolling Float Value</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="rollingFloatValue" id="rollingFloatValue" step="0.01" value="${cashManagement?.config?.rollingFloatValue}" placeholder="0.00"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Reconciliation</h5>
                            <div class="form-group row">
                                <label for="tillShiftRecountLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Shift Recount Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillShiftRecountLimit" id="tillShiftRecountLimit" value="${cashManagement?.config?.tillShiftRecountLimit}"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="tillShiftVarianceLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Shift Variance Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillShiftVarianceLimit" id="tillShiftVarianceLimit" value="${cashManagement?.config?.tillShiftVarianceLimit}" step="0.01" placeholder="0.00"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeRecountLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Recount Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="safeRecountLimit" id="safeRecountLimit" value="${cashManagement?.config?.safeRecountLimit}"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeVarianceLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Variance Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="safeVarianceLimit" id="safeVarianceLimit" value="${cashManagement?.config?.safeVarianceLimit}" step="0.01" placeholder="0.00"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="isOpenShiftWithoutFloat" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Open Shift Without Float</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="isOpenShiftWithoutFloat" id="isOpenShiftWithoutFloat" ${cashManagement?.config?.isOpenShiftWithoutFloat ? 'checked' : ''} />
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Snapshots</h5>
                            <div class="form-group row">
                                <label for="tillAutoSnapshotDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Auto Snapshot Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="tillAutoSnapshotDays" name="tillAutoSnapshotDays"/>
                                    <div id="tillAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="tillAutoSnapshotTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Auto Snapshot Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="tillAutoSnapshotTime" id="tillAutoSnapshotTime" value="${cashManagement?.config?.tillAutoSnapshotTime}" placeholder="HH:MM"/>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeAutoSnapshotDays" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Auto Snapshot Days</label>
                                <div class="col-7 col-lg-4">
                                    <input type="hidden" id="safeAutoSnapshotDays" name="safeAutoSnapshotDays"/>
                                    <div id="safeAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="safeAutoSnapshotTime" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Safe Auto Snapshot Time</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="safeAutoSnapshotTime" id="safeAutoSnapshotTime" value="${cashManagement?.config?.safeAutoSnapshotTime}" placeholder="HH:MM"/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Holding Limit</h5>
                            <div class="form-group row">
                                <label for="tillCashHoldingLimit" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Till Cash Holding Limit</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillCashHoldingLimit" id="tillCashHoldingLimit" value="${cashManagement?.config?.tillCashHoldingLimit}" step="0.01" placeholder="0.00"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:uploadForm>
</section>
</body>
</html>