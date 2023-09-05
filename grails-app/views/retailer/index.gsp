<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <script type='text/javascript'>
        var getBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxGetBrandLogo')}";
        var resetBrandLogoUrl = "${createLink(controller: 'retailer', action: 'ajaxResetBrandLogo')}";
        $(document).ready(function () {
            $.ajax({
                url: getBrandLogoUrl,
                success: function(resp) {
                    if (resp === '') {
                        $('#brand-logo-container').empty();
                        $('#reset-brand-logo-button').hide();
                    } else {
                        $('#brand-logo-container').html('<img id="brand-logo" src="data:image/png;base64,' + resp + '" style="width: 100%; max-width: 10rem; border: black 1px solid; border-radius: 1rem" />');
                        $('#reset-brand-logo-button').show();
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
                        $('#brand-logo-container').empty();
                        $('#reset-brand-logo-button').hide();
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

    <g:hasErrors bean="${configErrors}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${configErrors}" as="list" />
            </div>
        </section>
    </g:hasErrors>

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
                                <div class="form-group row">
                                    <label for="locationsType" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Locations Type</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.locationsType" id="locationsType" value="${config?.locationsType}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-locations-type-button" onclick="$('#locationsType').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="headOfficeProductMaintenance" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Head Office Product Maintenance</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.headOfficeProductMaintenance" id="headOfficeProductMaintenance" ${config?.headOfficeProductMaintenance ? 'checked' : ''} />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="snappyShopperEnabled" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Snappy Shopper Enabled</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.snappyShopperEnabled" id="snappyShopperEnabled" ${config?.snappyShopperEnabled ? 'checked' : ''} />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="twoStageSel" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Two-Stage SEL</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.twoStageSel" id="twoStageSel" ${config?.twoStageSel ? 'checked' : ''} />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="averyEnabled" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Avery Enabled</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.averyEnabled" id="averyEnabled" ${config?.averyEnabled ? 'checked' : ''} />
                                    </div>
                                </div>


                                <div class="form-group row">
                                    <label for="scoEnabled" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">SCO Enabled</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.scoEnabled" id="scoEnabled" ${config?.scoEnabled ? 'checked' : ''} />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqSslEnabled" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ SSL Enabled</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="checkbox" class="col-1 form-check-input wl-checkbox" name="retailerFunctionConfig.rabbitMqSslEnabled" id="rabbitMqSslEnabled" ${config?.rabbitMqSslEnabled ? 'checked' : ''} />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqUrl" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ URL</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqUrl" id="rabbitMqUrl" value="${config?.rabbitMqUrl}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-url-button" onclick="$('#rabbitMqUrl').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqPort" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Port</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="number" class="col-2 form-control bottom-border" name="retailerFunctionConfig.rabbitMqPort" id="rabbitMqPort" value="${config?.rabbitMqPort}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-port-button" onclick="$('#rabbitMqPort').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqVirtualHost" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Virtual Host</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqVirtualHost" id="rabbitMqVirtualHost" value="${config?.rabbitMqVirtualHost}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-vhost-button" onclick="$('#rabbitMqVirtualHost').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqUsername" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Username</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqUsername" id="rabbitMqUsername" value="${config?.rabbitMqUsername}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-username-button" onclick="$('#rabbitMqUsername').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqPassword" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Password</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="password" class="col-2 form-control bottom-border" name="retailerFunctionConfig.rabbitMqPassword" id="rabbitMqPassword" value="${config?.rabbitMqPassword}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-password-button" onclick="$('#rabbitMqPassword').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqTransactionsExchange" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Transactions Exchange</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqTransactionsExchange" id="rabbitMqTransactionsExchange" value="${config?.rabbitMqTransactionsExchange}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-transactions-exchange-button" onclick="$('#rabbitMqTransactionsExchange').val('')">Reset</div>
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="rabbitMqDataSyncExchange" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Data Sync Exchange</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqDataSyncExchange" id="rabbitMqDataSyncExchange" value="${config?.rabbitMqDataSyncExchange}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-rabbitmq-data-sync-exchange-button" onclick="$('#rabbitMqDataSyncExchange').val('')">Reset</div>
                                    </div>
                                </div>


                                <div class="form-group row">
                                    <label for="rabbitMqReceiptsExchange" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">RabbitMQ Receipts Exchange</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.rabbitMqReceiptsExchange" id="rabbitMqReceiptsExchange" value="${config?.rabbitMqReceiptsExchange}" />
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

                    <div id="collapseBrandDetails" class="collapse show" aria-labelledby="brandDetails" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="brandLogo" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Brand Logo</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="file" name="brandLogo" accept=".png,.PNG" id="brandLogo" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <div id="brand-logo-container" class="col-7 offset-5"></div>
                                </div>

                                <div class="form-group row">
                                    <div class="btn btn-danger col-1 offset-5" id="reset-brand-logo-button" style="display: none;" onclick="resetBrandLogo();">Reset</div>
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

                    <div id="collapseTerminologyDetails" class="collapse show" aria-labelledby="terminologyDetails" data-parent="#accordionTerminology">
                        <div class="card-body py-5">
                            <div class="col-12">
                                <div class="form-group row">
                                    <label for="productTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Product Term</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.productTerm" id="productTerm" value="${config?.retailerTerminologyConfig.productTerm}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-product-term-button" onclick="$('#productTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="packTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Pack</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.packTerm" id="packTerm" value="${config?.retailerTerminologyConfig.packTerm}" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-pack-term-button" onclick="$('#packTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qisTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QIS (Quantity In Stock)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.quantityInStockTerm" id="qisTerm" value="${config?.retailerTerminologyConfig.quantityInStockTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-qis-term-button" onclick="$('#qisTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qooTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QOO (Quantity On Order)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.quantityOnOrderTerm" id="qooTerm" value="${config?.retailerTerminologyConfig.quantityOnOrderTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-qoo-term-button"onclick="$('#qooTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="userTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">User</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.userTerm" id="userTerm" value="${config?.retailerTerminologyConfig.userTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-user-term-button"onclick="$('#userTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="storeTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" class="col-5 form-control bottom-border" name="retailerTerminologyConfig.storeTerm" id="storeTerm" value="${config?.retailerTerminologyConfig.storeTerm}"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger" id="reset-store-term-button"onclick="$('#storeTerm').val('')">Reset</div>
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

                    <div id="collapseFunctionModification" class="collapse show" aria-labelledby="functionModificationDetails" data-parent="#accordionFunctionModification">
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
                                                    <label for="productLookupName" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Name</label>
                                                    <div class="col-7 col-lg-4">
                                                        <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.functionMenuItems[productLookup].name" id="productLookupName" value="${config?.retailerFunctionConfig.functionMenuItems['productLookup'].name}"/>
                                                    </div>
                                                    <div class="form-group row">
                                                        <div class="btn btn-danger" id="reset-product-lookup-name-button"onclick="$('#productLookupName').val('')">Reset</div>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">SEL & RTC Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="selVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityEnabled" value="ENABLED" ${config.retailerFunctionConfig.shelfEdgeVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="selVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityDisabled" value="DISABLED" ${config.retailerFunctionConfig.shelfEdgeVisibility.toString() === "DISABLED" ? 'checked' : ''} />
                                                    </div>
                                                    <label for="selVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.shelfEdgeVisibility" id="selVisibilityInvisible" value="INVISIBLE" ${config.retailerFunctionConfig.shelfEdgeVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
                                                    </div>
                                                </div>
                                                <h5 class="text-center">Visibility Status</h5>
                                                <div class="form-group row justify-content-center">
                                                    <label for="productLookupVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityEnabled" value="ENABLED" ${config.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="productLookupVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityDisabled" value="DISABLED" ${config.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'DISABLED' ? 'checked' : '' }/>
                                                    </div>
                                                    <label for="productLookupVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                    <div class="col-10 col-lg-1">
                                                        <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[productLookup].menuItemVisibility" id="productLookupVisibilityInvisible" value="INVISIBLE" ${config.retailerFunctionConfig.functionMenuItems['productLookup'].menuItemVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
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
                                            "bottomSettingsButton"
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
                                                        <label for="${item}Name" class="col-5 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Name</label>
                                                        <div class="col-7 col-lg-4">
                                                            <input type="text" class="col-5 form-control bottom-border" name="retailerFunctionConfig.functionMenuItems[${item}].name" id="${item}Name" value="${config.retailerFunctionConfig.functionMenuItems[item].name}"/>
                                                        </div>
                                                        <div class="form-group row">
                                                            <div class="btn btn-danger" id="reset-${item}-name-button"onclick="$('#${item}Name').val('')">Reset</div>
                                                        </div>
                                                    </div>
                                                    <h5 class="text-center">Visibility Status</h5>
                                                    <div class="form-group row justify-content-center">
                                                        <label for="productLookupVisibilityEnabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Enabled</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityEnabled" value="ENABLED" ${config.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'ENABLED' ? 'checked' : '' }/>
                                                        </div>
                                                        <label for="productLookupVisibilityDisabled" class="col-10 col-lg-1 col-form-label text-right pr-4">Disabled</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityDisabled" value="DISABLED" ${config.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'DISABLED' ? 'checked' : '' }/>
                                                        </div>
                                                        <label for="productLookupVisibilityInvisible" class="col-10 col-lg-1 col-form-label text-right pr-4">Invisible</label>
                                                        <div class="col-10 col-lg-1">
                                                            <input type="radio" class="col-2 form-check-input wl-radio" name="retailerFunctionConfig.functionMenuItems[${item}].menuItemVisibility" id="${item}VisibilityInvisible" value="INVISIBLE" ${config.retailerFunctionConfig.functionMenuItems[item].menuItemVisibility.toString() === 'INVISIBLE' ? 'checked' : '' }/>
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