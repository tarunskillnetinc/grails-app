<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <asset:javascript src="validators/input-validator.js" />
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

        %{--$(document).ready(function () {--}%
        %{--    $('input[name=brandLogo]').change(function() {--}%
        %{--        const error = validateImg(this);--}%
        %{--        if (error) {--}%
        %{--            $('input[name=brandLogo]').val(null);--}%
        %{--            alert(error);--}%
        %{--            return--}%
        %{--        }--}%

        %{--        const fileData = this.files[0];--}%
        %{--        if (FileReader && fileData) {--}%
        %{--            const urlFileReader = new FileReader();--}%
        %{--            urlFileReader.onload = function () {--}%
        %{--                const brandingImage = $(".branding-image");--}%
        %{--                brandingImage.attr("src", urlFileReader.result);--}%
        %{--                brandingImage.removeAttr("hidden");--}%
        %{--            }--}%
        %{--            urlFileReader.readAsDataURL(fileData);--}%
        %{--        }--}%
        %{--    });--}%

        %{--    $.ajax({--}%
        %{--        url: getBrandLogoUrl,--}%
        %{--        success: function(resp) {--}%
        %{--            if (resp === '') {--}%
        %{--                var brandingImage = $(".branding-image");--}%
        %{--                brandingImage.attr("src", "");--}%
        %{--                brandingImage.attr("hidden", "");--}%
        %{--            } else {--}%
        %{--                var brandingImage = $(".branding-image");--}%
        %{--                brandingImage.attr("src", "data:image/png;base64," + resp);--}%
        %{--                brandingImage.removeAttr("hidden");--}%
        %{--            }--}%
        %{--            // Iterate over each element with the class "item-label" and update its content--}%
        %{--            $(".item-label").each(function() {--}%
        %{--                const item = $(this).text(); // Get the text content of the current div--}%
        %{--                const readableItem = camelToReadable(item); // Convert to readable format--}%
        %{--                $(this).text(readableItem); // Update the div's content--}%
        %{--            });--}%
        %{--        }--}%
        %{--    });--}%
        %{--});--}%

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
                            <h5 class="text-center">Location Type</h5>
                            <div class="form-group row justify-content-center">
                                <div class="col-10 col-lg-2 form-check form-check-inline justify-content-center">
                                    <label for="productLookupVisibilityEnabled" class="form-check-label">None</label>
                                    <input type="radio" class="form-check-input wl-radio ml-2" name="locationsType" id="locationTypeNone" value="NONE" ${retailer?.config?.locationsType.toString() === 'NONE' ? 'checked' : '' }/>
                                </div>

                                <div class="col-10 col-lg-2 form-check form-check-inline justify-content-center">
                                    <label for="productLookupVisibilityDisabled" class="form-check-label">Simple</label>
                                    <input type="radio" class="form-check-input wl-radio ml-2" name="locationsType" id="locationTypeSimple" value="SIMPLE" ${retailer?.config?.locationsType.toString() === 'SIMPLE' ? 'checked' : '' }/>
                                </div>

                                <div class="col-10 col-lg-2 form-check form-check-inline justify-content-center">
                                    <label for="productLookupVisibilityInvisible" class="form-check-label">Advanced</label>
                                    <input type="radio" class="form-check-input wl-radio ml-2" name="locationsType" id="locationTypeAdvanvced" value="ADVANCED" ${retailer?.config?.locationsType.toString() === 'ADVANCED' ? 'checked' : '' }/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <g:hasErrors bean="${config}" field="locationsType">
                                    <span class="text-danger">${g.message(error: 'default.required.message', default: 'Field is required')}</span>
                                </g:hasErrors>
                            </div>

                            <div class="form-group row">
                                <label for="headOfficeProductMaintenance" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Head Office Product Maintenance</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="headOfficeProductMaintenance" id="headOfficeProductMaintenance" ${retailer?.config?.headOfficeProductMaintenance ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="snappyShopperEnabled" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Snappy Shopper Enabled</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="snappyShopperEnabled" id="snappyShopperEnabled" ${retailer?.config?.snappyShopperEnabled ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="twoStageSel" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Two-Stage SEL</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="twoStageSel" id="twoStageSel" ${retailer?.config?.twoStageSel ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="averyEnabled" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Avery Enabled</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="averyEnabled" id="averyEnabled" ${retailer?.config?.averyEnabled ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="2DBarcodesEnabled" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">2D Barcodes Enabled</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="twoDimensionalBarcodesEnabled" id="2DBarcodesEnabled" ${retailer?.config?.twoDimensionalBarcodesEnabled ? 'checked' : ''} />
                                </div>
                            </div>
                            <div class="form-group row">
                                <label for="scoEnabled" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">SCO Enabled</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="scoEnabled" id="scoEnabled" ${retailer?.config?.scoEnabled ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqSslEnabled" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ SSL Enabled</label>
                                <div class="col-7 col-lg-4">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="rabbitMqSslEnabled" id="rabbitMqSslEnabled" ${retailer?.config?.rabbitMqSslEnabled ? 'checked' : ''} />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqUrl" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ URL</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqUrl" id="rabbitMqUrl" value="${retailer?.config?.rabbitMqUrl}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-url-button" onclick="$('#rabbitMqUrl').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqPort" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Port</label>
                                <div class="col-7 col-lg-4">
                                    <input type="number" class="col-5 form-control bottom-border" name="rabbitMqPort" id="rabbitMqPort" value="${retailer?.config?.rabbitMqPort}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-port-button" onclick="$('#rabbitMqPort').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqVirtualHost" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Virtual Host</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqVirtualHost" id="rabbitMqVirtualHost" value="${retailer?.config?.rabbitMqVirtualHost}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-vhost-button" onclick="$('#rabbitMqVirtualHost').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqUsername" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Username</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqUsername" id="rabbitMqUsername" value="${retailer?.config?.rabbitMqUsername}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-username-button" onclick="$('#rabbitMqUsername').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqPassword" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Password</label>
                                <div class="col-7 col-lg-4">
                                    <input type="password" class="col-5 form-control bottom-border" name="rabbitMqPassword" id="rabbitMqPassword" value="${retailer?.config?.rabbitMqPassword}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-password-button" onclick="$('#rabbitMqPassword').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqTransactionsExchange" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Transactions Exchange</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqTransactionsExchange" id="rabbitMqTransactionsExchange" value="${retailer?.config?.rabbitMqTransactionsExchange}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-transactions-exchange-button" onclick="$('#rabbitMqTransactionsExchange').val('')">Reset</div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="rabbitMqDataSyncExchange" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Data Sync Exchange</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqDataSyncExchange" id="rabbitMqDataSyncExchange" value="${retailer?.config?.rabbitMqDataSyncExchange}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-data-sync-exchange-button" onclick="$('#rabbitMqDataSyncExchange').val('')">Reset</div>
                                </div>
                            </div>


                            <div class="form-group row">
                                <label for="rabbitMqReceiptsExchange" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">RabbitMQ Receipts Exchange</label>
                                <div class="col-7 col-lg-4">
                                    <input type="text" class="col-5 form-control bottom-border" name="rabbitMqReceiptsExchange" id="rabbitMqReceiptsExchange" value="${retailer?.config?.rabbitMqReceiptsExchange}" />
                                </div>
                                <div class="form-group row">
                                    <div class="btn btn-danger" id="reset-rabbitmq-receipts-exchange-button" onclick="$('#rabbitMqReceiptsExchange').val('')">Reset</div>
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