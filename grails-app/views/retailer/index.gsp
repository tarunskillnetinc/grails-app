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
                        $('#brand-logo-container').html('<img id="brand-logo" src="data:image/png;base64,' + resp + '" style="width: 100%;" />');
                        $('#reset-brand-logo-button').show();
                    }
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
                                        <input type="text" name="retailerTerminologyConfig.productTerm" id="productTerm" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-product-term-button" onclick="$('#productTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="packTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Pack</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" name="retailerTerminologyConfig.packTerm" id="packTerm" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-pack-term-button" onclick="$('#packTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qisTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QIS (Quantity In Stock)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" name="retailerTerminologyConfig.quantityInStockTerm" id="qisTerm" />
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-qis-term-button" onclick="$('#qisTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="qooTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">QOO (Quantity On Order)</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" name="retailerTerminologyConfig.quantityOnOrderTerm" id="qooTerm"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-qoo-term-button"onclick="$('#qooTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="userTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">User</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" name="retailerTerminologyConfig.userTerm" id="userTerm"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-user-term-button"onclick="$('#userTerm').val('')">Reset</div>
                                    </div>
                                </div>
                                <div class="form-group row">
                                    <label for="storeTerm" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store</label>
                                    <div class="col-7 col-lg-4">
                                        <input type="text" name="retailerTerminologyConfig.storeTerm" id="storeTerm"/>
                                    </div>
                                    <div class="form-group row">
                                        <div class="btn btn-danger offset-5" id="reset-store-term-button"onclick="$('#storeTerm').val('')">Reset</div>
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