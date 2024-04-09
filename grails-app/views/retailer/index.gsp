<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <script type='text/javascript'>
        var getBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxGetBrandLogo')}";
        var resetBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxResetBrandLogo')}";

        $(document).ready(function () {
            $('input[name=brandLogo]').change(function() {
                if (this.files[0].size < 1048576 /* 1MB */) {
                    if (this.files[0].type === "image/png") {
                        const fileData = this.files[0];
                        if (FileReader && fileData) {
                            var urlFileReader = new FileReader();
                            urlFileReader.onload = function () {
                                var brandingImage = $(".branding-image");
                                brandingImage.attr("src", urlFileReader.result);
                                brandingImage.removeAttr("hidden");
                            }
                            urlFileReader.readAsDataURL(fileData);
                        } else {
                            // fallback?
                        }
                    } else {
                        alert('${message(code:'button.error.incompatible.message', default:"Image incorrect file type. Please use .png.")}')
                    }
                } else {
                    alert('${message(code:'button.error.fileSize.message', default:"Image file size too large")}')
                }
            });

            $.ajax({
                url: getBrandLogoUrl,
                success: function(resp) {
                    if (resp === '') {
                        var brandingImage = $(".branding-image");
                        brandingImage.attr("src", "");
                        brandingImage.attr("hidden", "");
                    } else {
                        var brandingImage = $(".branding-image");
                        brandingImage.attr("src", "data:image/png;base64," + resp);
                        brandingImage.removeAttr("hidden");
                    }
                    // Iterate over each element with the class "item-label" and update its content
                    $(".item-label").each(function() {
                        const item = $(this).text(); // Get the text content of the current div
                        const readableItem = camelToReadable(item); // Convert to readable format
                        $(this).text(readableItem); // Update the div's content
                    });
                }
            });
        });

        function resetBrandLogo() {
            if (confirm("This will reset your brand logo to Trust retail default.")) {
                $.ajax({
                    url: resetBrandLogoUrl,
                    success: function (resp) {
                        var brandingImage = $(".branding-image");
                        brandingImage.attr("src", "");
                        brandingImage.attr("hidden", "");
                    }
                });
            }
        }
        function camelToReadable(camelCaseString) {
            // Use a regular expression to split the string at capital letters
            const words = camelCaseString.split(/(?=[A-Z])/);
            // Capitalize the first letter of each word and join with spaces
            const readableString = words.map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
            return readableString;
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Retailer Settings</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="header-container" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto my-auto">Retailer Settings</h2>
            </div>

            <div class="col-2 text-right">
                <g:link elementId="cancel-btn" controller="retailer" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
                <button id="save-btn" class="btn btn-success" name="save" onclick="$('#save-button').submit();">Save</button>
            </div>
        </div>
    </section>

    <g:hasErrors bean="${retailer}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${retailer}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:if test="${flash.error}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        </section>
    </g:if>

    <g:if test="${flash.message}">
        <section id="errors-container2" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert">
                <g:each in="${flash.message}" var="message" status="i">
                    ${message}<br/>
                </g:each>
            </div>
        </section>
    </g:if>



    <section id="addProduct-section" class="container-fluid mt-4">
        <g:uploadForm name="save-button" action="save">
            <div id="accordionGeneral">
                <!-- General information. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="generalDetails" data-toggle="collapse" data-target="#collapseGeneralDetails" aria-expanded="true" aria-controls="collapseGeneralDetails">
                        <div class="row">
                            <div class="col-10 font-weight-bold">General Settings</div>
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
                                    <label for="productLookupVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">None</label>
                                    <div class="col-10 col-lg-1">
                                        <input type="radio" class="col-2 form-check-input wl-radio" name="locationsType" id="locationTypeNone" value="NONE" ${retailer?.config?.locationsType.toString() === 'NONE' ? 'checked' : '' }/>
                                    </div>
                                    <label for="productLookupVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Simple</label>
                                    <div class="col-10 col-lg-1">
                                        <input type="radio" class="col-2 form-check-input wl-radio" name="locationsType" id="locationTypeSimple" value="SIMPLE" ${retailer?.config?.locationsType.toString() === 'SIMPLE' ? 'checked' : '' }/>
                                    </div>
                                    <label for="productLookupVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Advanced</label>
                                    <div class="col-10 col-lg-1">
                                        <input type="radio" class="col-2 form-check-input wl-radio" name="locationsType" id="locationTypeAdvanvced" value="ADVANCED" ${retailer?.config?.locationsType.toString() === 'ADVANCED' ? 'checked' : '' }/>
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
            <div id="accordion">
                <!-- Brand information. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="brandDetails" data-toggle="collapse" data-target="#collapseBrandDetails" aria-expanded="true" aria-controls="collapseBrandDetails">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Brand Settings</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseBrandDetails" class="collapse " aria-labelledby="brandDetails" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="brandLogo" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Brand Logo</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="file" class="col-7 form-control-file bottom-border bg-transparent p-0" name="brandLogo" accept="image/png" id="brandLogo" hidden />
                                    </div>
                                </div>

                                <div class="form-group row margin-top-1rem">
                                    <div class="col-12 d-flex justify-content-center">
                                        <div class="branding-container">
                                            <img class="justify-content-center branding-image" hidden />
                                        </div>
                                    </div>

                                </div>

                                <div class="form-group row">
                                    <div class="col-12 offset-5">
                                        <div class="btn btn-dark" id="upload-branding-button" onclick="$('#brandLogo').click();"><g:message code="button.upload.button"/></div>
                                        <div class="btn btn-danger" id="reset-branding-button" onclick="resetBrandLogo();">Reset</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="accordionTerminology">
                <!-- Terminology information. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="terminologyDetails" data-toggle="collapse" data-target="#collapseTerminologyDetails" aria-expanded="true" aria-controls="collapseTerminologyDetails">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Terminology Settings</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseTerminologyDetails" class="collapse " aria-labelledby="terminologyDetails" data-parent="#accordionTerminology">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="productTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Product Term</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.productTerm" id="productTerm" value="${retailer?.config?.retailerTerminologyConfig.productTerm}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-product-term-button" onclick="$('#productTerm').val('Product')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="packTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Pack</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.packTerm" id="packTerm" value="${retailer?.config?.retailerTerminologyConfig.packTerm}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-pack-term-button" onclick="$('#packTerm').val('Pack')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qisTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QIS (Quantity In Stock)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.quantityInStockTerm" id="qisTerm" value="${retailer?.config?.retailerTerminologyConfig.quantityInStockTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-qis-term-button" onclick="$('#qisTerm').val('Quantity In Stock')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qooTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QOO (Quantity On Order)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.quantityOnOrderTerm" id="qooTerm" value="${retailer?.config?.retailerTerminologyConfig.quantityOnOrderTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-qoo-term-button"onclick="$('#qooTerm').val('Quantity On Order')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="userTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">User</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.userTerm" id="userTerm" value="${retailer?.config?.retailerTerminologyConfig.userTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-user-term-button"onclick="$('#userTerm').val('User')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="storeTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.storeTerm" id="storeTerm" value="${retailer?.config?.retailerTerminologyConfig.storeTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-store-term-button"onclick="$('#storeTerm').val('Store')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="itemCodeTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Item Code</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.itemCodeTerm" id="itemCodeTerm" value="${retailer?.config?.retailerTerminologyConfig?.itemCodeTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-itemCode-term-button"onclick="$('#itemCodeTerm').val('Item Code')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="storeHoldingsTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Holdings</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.storeHoldingsTerm" id="storeHoldingsTerm" value="${retailer?.config?.retailerTerminologyConfig?.storeHoldingsTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-storeHoldings-term-button"onclick="$('#storeHoldingsTerm').val('Store Holdings')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="inStockTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">In Stock</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.inStockTerm" id="inStockTerm" value="${retailer?.config?.retailerTerminologyConfig?.inStockTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-inStock-term-button"onclick="$('#inStockTerm').val('In Stock')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="deliveredTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Delivered</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.deliveredTerm" id="deliveredTerm" value="${retailer?.config?.retailerTerminologyConfig?.deliveredTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-delivered-term-button"onclick="$('#deliveredTerm').val('Delivered')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="accentBarStoreTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store (Accent Bar)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.accentBarStoreTerm" id="accentBarStoreTerm" value="${retailer?.config?.retailerTerminologyConfig?.accentBarStoreTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-accent-bar-store-term-button"onclick="$('#accentBarStoreTerm').val('Store')">Reset</div>
                                    </div>
                                </div>
                                <!-- Locations Table Terminology -->
                                <div class="form-group row">
                                    <label for="stockLocationsTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Stock Locations (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.stockLocationsTerm" id="stockLocationsTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-stockLocations-term-button"onclick="$('#stockLocationsTerm').val('Stock Locations')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="descriptionTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Description (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.descriptionTerm" id="descriptionTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-description-term-button"onclick="$('#descriptionTerm').val('Description')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="bayTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Bay (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.bayTerm" id="bayTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-bay-term-button"onclick="$('#bayTerm').val('Bay')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="shelfTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Shelf (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.shelfTerm" id="shelfTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-shelf-term-button"onclick="$('#shelfTerm').val('Shelf')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="positionTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Position (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.positionTerm" id="positionTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-position-term-button"onclick="$('#positionTerm').val('Position')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="aisleTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Aisle (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.aisleTerm" id="aisleTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-aisle-term-button"onclick="$('#aisleTerm').val('Aisle')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="shelfCapacityTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Shelf Capacity (Locations Table)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.locationsTableConfig.shelfCapacityTerm" id="shelfCapacityTerm" value="${retailer?.config?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-shelfCapacity-term-button"onclick="$('#shelfCapacityTerm').val('Shelf Capacity')">Reset</div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="accordionFunctionModification">
                <!-- Function Modification. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="functionModification" data-toggle="collapse" data-target="#collapseFunctionModification" aria-expanded="true" aria-controls="collapseFunctionModification">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Function Modification</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseFunctionModification" class="collapse " aria-labelledby="functionModificationDetails" data-parent="#accordionFunctionModification">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                                    <div class="card-header pointer" id="productLookup" data-toggle="collapse" data-target="#collapseProductLookup" aria-expanded="true" aria-controls="collapseProductLookup">
                                        <div class="row">
                                            <div class="col-10 font-weight-bold">Product Lookup</div>
                                            <div class="col-2 text-right">
                                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                                </svg>
                                            </div>
                                        </div>
                                    </div>

                                    <div id="collapseProductLookup" class="collapse" aria-labelledby="productLookup" data-parent="#productLookup">
                                        <div class="card-body py-5">
                                            <div class="col-12">
                                                <div class="form-group row">
                                                    <label for="productLookupName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Name</label>
                                                    <div class="col-7 col-lg-4">
                                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.functionMenuItems[productLookup].name" id="productLookupName" value="${retailer?.config?.retailerFunctionConfig.functionMenuItems['productLookup'].name}"/>
                                                    </div>
                                                    <div class="form-group row">
                                                        <div class="btn btn-danger" id="reset-product-lookup-name-button"onclick="$('#productLookupName').val('')">Reset</div>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">SEL & RTC Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="selVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.shelfEdgeVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="selVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.shelfEdgeVisibility.toString() === "DISABLED" ? 'checked' : ''} />
                                                    </div>
                                                    <label for="selVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.shelfEdgeVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">VAT Rates Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="vatRatesVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.vatRatesVisibility" id="vatRatesVisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.vatRatesVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="vatRatesVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.vatRatesVisibility" id="vatRatesVisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.vatRatesVisibility.toString() === "DISABLED" ? 'checked' : ''} />
                                                    </div>
                                                    <label for="vatRatesVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.vatRatesVisibility" id="vatRatesVisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.vatRatesVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">Style Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="styleVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.styleVisibility" id="styleVisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.styleVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="styleVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.styleVisibility" id="styleVisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.styleVisibility.toString() === "DISABLED" ? 'checked' : ''} />
                                                    </div>
                                                    <label for="styleVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.styleVisibility" id="styleVisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.styleVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">Category Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="categoryVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.categoryVisibility" id="categoryVisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.categoryVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="categoryVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.categoryVisibility" id="categoryVisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.categoryVisibility.toString() === "DISABLED" ? 'checked' : ''} />
                                                    </div>
                                                    <label for="categoryVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.categoryVisibility" id="categoryVisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.categoryVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="productLookupVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="productLookupVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'DISABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="productLookupVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <%
                                    var itemList = [
                                            "gapCheck",
                                            "stockCount",
                                            "replenishment",
                                            "shelfAudit",
                                            "excessCount",
                                            "pickList",
                                            "centralCounts",
                                            "dynamicReplenishment",
                                            "fitToShelf",
                                            "orders",
                                            "deliveries",
                                            "releaseItem",
                                            "inventoryAdjustment",
                                            "transfersIn",
                                            "transfersOut",
                                            "shelfEdgeLabels",
                                            "priceCheck",
                                            "storeSales",
                                            "storeReports",
                                            "varianceReport",
                                            "bottomHomeButton",
                                            "bottomProductButton",
                                            "bottomFileButton",
                                            "bottomSettingsButton",
                                    ]
                                %>
                                <g:each in="${itemList}" var="item" status="index">
                                    <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                                        <div class="card-header pointer" id="${item}" data-toggle="collapse" data-target="#collapse${item}" aria-expanded="true" aria-controls="collapse${item}">
                                            <div class="row">
                                                <div class="col-10 font-weight-bold item-label">${item}</div>
                                                <div class="col-2 text-right">
                                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>

                                        <div id="collapse${item}" class="collapse" aria-labelledby="${item}" data-parent="#${item}">
                                            <div class="card-body py-5">
                                                <div class="col-12">
                                                    <div class="form-group row">
                                                        <label for="${item}Name" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Name</label>
                                                        <div class="col-7 col-lg-4">
                                                            <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.functionMenuItems[${item}].name" id="${item}Name" value="${retailer?.config?.retailerFunctionConfig.functionMenuItems[item].name}"/>
                                                        </div>
                                                        <div class="form-group row">
                                                            <div class="btn btn-danger" id="reset-${item}-name-button"onclick="$('#${item}Name').val('')">Reset</div>
                                                        </div>
                                                    </div>
                                                    <h5 class="text-center">Visibility Status</h5>
                                                    <div class="form-group row justify-content-center">
                                                        <label for="productLookupVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityEnabled" value="ENABLED" ${retailer?.config?.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                        </div>
                                                        <label for="productLookupVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityDisabled" value="DISABLED" ${retailer?.config?.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'DISABLED' ? 'checked' : '' }/>
                                                        </div>
                                                        <label for="productLookupVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityInvisible" value="INVISIBLE" ${retailer?.config?.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </g:uploadForm>
    </section>
</body>
</html>