<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Button Grids</title>

    <asset:javascript src="button.js" />
    <script type="text/javascript">
        $(function() {
            if ("${button?.imageDisplay}" === "false") {
                $("input[id*=displayTextInput]").attr("disabled", true)
            }

            $("#image").on("change", function() {
                if (this.files[0].size < 26214400) {
                    if (this.files[0].type === "image/png") {
                        const fileData = this.files[0];
                        if (FileReader && fileData) {
                            var urlFileReader = new FileReader();
                            urlFileReader.onload = function () {
                                var buttonImage = $(".button-image");
                                buttonImage.attr("src", urlFileReader.result);
                                buttonImage.removeAttr("hidden");

                                $("input[id*=textDisplayInput]").removeAttr("disabled");
                                $("#removeImage").val("");
                                $("div[id*=imageRemoveBtn]").removeAttr("disabled");
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
            })

            $("div[id*=imageRemoveBtn]").on("click", function () {
                var buttonImage = $(".button-image");
                buttonImage.attr("src", "");
                buttonImage.attr("hidden", true);

                var displayTextCheck = $("input[id*=textDisplayInput]");
                displayTextCheck.prop("checked", true);
                displayTextCheck.prop("value", true);
                displayTextCheck.attr("disabled", true);

                var imageInput = $("#image");
                imageInput.files = null;
                imageInput.prop("value", "");

                $(".button-example-text").removeAttr("hidden");
                $("#removeImage").val(true);
                $("div[id*=imageRemoveBtn]").attr("disabled", true);
                $("#textDisplay").val(true);
            })

            $("input[id*=textDisplayInput]").on("change", function () {
                $("#textDisplay").val(this.checked);
                $("input[id*=textDisplayInput]").prop("checked", this.checked);
                if (this.checked === true) {
                    $(".button-example-text").removeAttr("hidden");
                    $("div[id*=imageRemoveBtn]").removeAttr("disabled");
                } else {
                    $(".button-example-text").attr("hidden", true)
                    $("div[id*=imageRemoveBtn]").attr("disabled", true);
                }
            })

            $("input[id*=descriptionInput]").on("change", function() {
                $("input[id*=descriptionInput]").val(this.value);
                $("#description").val($(this).val());
            })

            $("input[id*=quantityInput]").on("change", function() {
                $("#quantity").val($(this).val());
            })

            $("select[id*=subPageIdInput]").on("change", function() {
                $("#subPageId").val($(this).val());
            })

            $("select[id*=processInput]").on("change", function() {
                $("#process").val($(this).val());
            })

            $("input[id*=amountInput]").on("change", function() {
                $("#amount").val($(this).val());
            })

            $("select[id*=tenderTypeInput]").on("change", function() {
                $("#tenderType").val($(this).val());
            })

            $(".button-example").css("backgroundColor", $("#bgColour").val());
            $(".button-example").css("color", $("#textColour").val());

            $("input[id*=bgColourInput]").on('change', function () {
                $("input[id*=bgColourInput]").val(this.value);
                $("#bgColour").val(this.value);
                $(".button-example").css("backgroundColor", this.value);
            })
            $("input[id*=textColourInput]").on('change', function () {
                $("input[id*=textColourInput]").val(this.value);
                $("#textColour").val(this.value);
                $(".button-example").css("color", this.value);
            })
        })

        function saveOverride(btnId, storeId) {
            $("input[id*=btnId]").val(-1);
            $("input[id*=btnStoreId]").val(storeId);
            $("input[id*=overrideId]").val(btnId);
            document.querySelector('#submission-form').submit();
        }

        function onTypeChange(newType) {
            var type = $("#type");

            if (newType !== type.val()) {
                $("#quantity").val("");
                $("#buttonProductId").val("");
                $("#itemCode").html("");
                $("#productDescription").html("");
                $("#subpageId").val("");
                $("#process").val("");
                $("#amount").val("");
                $("#tenderType").val("");

                $("input[id*=quantityInput]").val("");
                $("input[id*=subPageIdInput]").val("");
                $("input[id*=processInput]").val("");
                $("input[id*=amountInput]").val("");
                $("input[id*=tenderTypeInput]").val("");

                type.val(newType);
            } else if (newType === "${button?.type}") {
                $("#quantity").val("${button?.quantity}");
                $("#buttonProductId").val("${button?.sku}");
                $("#itemCode").html("${productSku}");
                $("#productDescription").html("${productDescription}");
                $("#subpageId").val("${button?.subPageId}");
                $("#process").val("${button?.process}");
                $("#amount").val("${button?.amount}");
                $("#tenderType").val("${button?.tenderType}");

                $("input[id*=quantityInput]").val("${button?.quantity}");
                $("input[id*=subPageIdInput]").val("${button?.subPageId}");
                $("input[id*=processInput]").val("${button?.process}");
                $("input[id*=amountInput]").val("${button?.amount}");
                $("input[id*=tenderTypeInput]").val("${button?.tenderType}");

                type.val(newType);
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Button Grids</li>
                        <li id="breadcrumb-3" class="breadcrumb-item" aria-current="page">
                            <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}">
                                <g:if test="${button?.buttonGrid?.type?.name() == 'OTHER'}">
                                    ${button?.buttonGrid?.description}
                                </g:if>
                                <g:else>
                                    <g:message code="ButtonGridType.${button?.buttonGrid?.type?.name()}" />
                                </g:else>
                            </g:link>
                        </li>
                        <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${button?.description ? button.description : "New Button"}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <div class="header-wl mt-3">
        <h2 class="mx-auto">Edit Button</h2>
    </div>

    <g:hasErrors bean="${button}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${button}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <section id="addProduct-section" class="container-fluid mt-4">
        <div id="accordion">
            <g:if test="${button.buttonGrid?.type?.name() != 'TENDER'}">
                <!-- Product button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="productButton" data-toggle="collapse" data-target="#collapseProductButton" aria-expanded="true" aria-controls="collapseProductButton" onclick="onTypeChange('PRODUCT')">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Product Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseProductButton" class="collapse ${!button?.type || button?.type?.name() == 'PRODUCT' ? 'show' : ''}" aria-labelledby="productButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="form-group row">
                                <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                <div class="col-8 col-sm-4">
                                    <g:textField name="descriptionInput" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="quantity" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Quantity</label>
                                <div class="col-4 col-sm-2">
                                    <g:field name="quantityInput" type="number" min="1" max="999" value="${button.quantity}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="product-select-box">
                                <div class="form-group row" style="padding-top: 15px;">
                                    <label for="sku" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">SKU</label>
                                    <div class="col-4 my-auto">
                                        <span id="sku">${productSku}</span>
                                    </div>
                                    <div class="col-4">
                                        <!-- Button trigger modal -->
                                        <a id="add-product-btn" href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                                            Select Product
                                        </a>
                                    </div>
                                </div>

                                <div class="form-group row" style="margin-top: 0px; padding-bottom: 10px;">
                                    <label for="productDescription" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                    <div class="col-8 my-auto">
                                        <span id="productDescription">${productDescription}</span>
                                    </div>
                                </div>
                            </div>

                            <g:render template="buttonVisualControls" model="[button: button, buttonImage: buttonImage, notFixed: true]"/>
                            <g:render template="saveCancelButtons" model="${[button: button]}" />
                        </div>
                    </div>
                </div>

                <!-- Navigation button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="navigationButton" data-toggle="collapse" data-target="#collapseNavigationButton" aria-expanded="true" aria-controls="collapseNavigationButton" onclick="onTypeChange('SUB_PAGE')">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Navigation Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseNavigationButton" class="collapse ${button?.type?.name() == 'SUB_PAGE' ? 'show' : ''}" aria-labelledby="navigationButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="form-group row">
                                <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                <div class="col-8 col-sm-6 col-lg-4">
                                    <g:textField name="descriptionInput" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="subPageId" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Sub page</label>
                                <div class="col-8 col-sm-5 col-lg-3">
                                    <g:select name="subPageIdInput" from="${availableSubPages}" value="${button?.subPageId}" optionKey="id" optionValue="description" noSelection="['':'']" class="form-control select-border" />
                                </div>
                            </div>

                            <g:render template="buttonVisualControls" model="[button: button, buttonImage: buttonImage, notFixed: true]"/>
                            <g:render template="saveCancelButtons" model="${[button: button]}" />
                        </div>
                    </div>
                </div>

                <!-- Action/process button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="actionButton" data-toggle="collapse" data-target="#collapseActionButton" aria-expanded="true" aria-controls="collapseActionButton" onclick="onTypeChange('PROCESS')">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Action Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseActionButton" class="collapse ${button?.type?.name() == 'PROCESS' ? 'show' : ''}" aria-labelledby="actionButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="form-group row">
                                <label for="description" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Description</label>
                                <div class="col-8 col-sm-6 col-lg-4">
                                    <g:textField name="descriptionInput" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="process" class="col-4 col-sm-2 offset-sm-2 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Action</label>
                                <div class="col-8 col-sm-5 col-lg-3">
                                    <g:select name="processInput" from="${availableProcesses}" valueMessagePrefix="ProcessType" value="${button.process}" noSelection="['':'']" class="form-control select-border" />
                                </div>
                            </div>

                            <g:render template="buttonVisualControls" model="[button: button, buttonImage: buttonImage, notFixed: true]"/>
                            <g:render template="saveCancelButtons" model="${[button: button]}" />
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${button.buttonGrid?.type?.name() == 'TENDER'}">
                <!-- Tender button. -->
                <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                    <div class="card-header pointer" id="tenderButton" data-toggle="collapse" data-target="#collapseTenderButton" aria-expanded="true" aria-controls="collapseTenderButton" onclick="onTypeChange('TENDER')">
                        <div class="row">
                            <div class="col-10 font-weight-bold">Tender Button</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div id="collapseTenderButton" class="collapse show" aria-labelledby="tenderButton" data-parent="#accordion">
                        <div class="card-body py-5">
                            <div class="form-group row">
                                <label for="description" class="col-4 col-sm-2 offset-sm-2 col-form-label">Description</label>
                                <div class="col-8 col-sm-6">
                                    <g:textField name="descriptionInput" maxlength="50" value="${button?.description}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="amount" class="col-4 col-sm-2 offset-sm-2 col-form-label">Amount</label>
                                <div class="col-4 col-sm-2">
                                    <g:field name="amountInput" type="number" min="0" max="9999" step=".01" value="${button.amount}" class="form-control bottom-border" />
                                </div>
                                <div class="col-4 col-sm-4" style="margin-top: 7px;"><small class="text-muted">Leave blank for manual entry.</small></div>
                            </div>

                            <div class="form-group row">
                                <label for="tenderType" class="col-4 col-sm-2 offset-sm-2 col-form-label">Tender type:</label>
                                <div class="col-6 col-sm-4">
                                    <g:select name="tenderTypeInput" from="${availableTenderTypes}" valueMessagePrefix="TenderType" value="${button.tenderType}" noSelection="['':'']" class="form-control select-border" />
                                </div>
                            </div>

                            <g:render template="buttonVisualControls" model="[button: button, buttonImage: buttonImage, notFixed: true]"/>
                            <g:render template="saveCancelButtons" model="${[button: button]}" />
                        </div>
                    </div>
                </div>
            </g:if>
        </div>

        <div class="col-12">
            <g:form name="submission-form" action="save" novalidate="novalidate" enctype="multipart/form-data">
                <g:hiddenField id="btnId" name="id" value="${button?.id}" />
                <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
                <g:hiddenField name="retailerId" value="${button?.buttonGrid?.retailerId}" />
                <g:hiddenField name="storeId" value="${button?.buttonGrid?.storeId}" />
                <g:hiddenField name="row" value="${button?.row}" />
                <g:hiddenField name="column" value="${button?.column}" />
                <g:hiddenField name="description" value="${button?.description}"/>
                <g:hiddenField name="type" value="${button?.type?.name()}" />
                <g:hiddenField name="quantity" value="${button?.quantity}" />
                <g:hiddenField name="sku" id="buttonSku" value="${button?.sku}" />
                <g:hiddenField name="subPageId" value="${button?.subPageId}"/>
                <g:hiddenField name="process" value="${button?.process}" />
                <g:hiddenField name="amount" value="${button?.amount}" />
                <g:hiddenField name="tenderType" value="${button?.tenderType}" />
                <g:hiddenField name="bgColour" value="${button?.bgColour}" />
                <g:hiddenField name="textColour" value="${button?.textColour}" />
                <g:hiddenField name="imageDisplay" value="${button?.imageDisplay}" />
                <g:hiddenField name="textDisplay" value="${button?.textDisplay}" />
                <g:hiddenField id="btnStoreId" name="storeId" value="${button?.storeId}" />
                <g:hiddenField id="overrideId" name="overrideId" value="${button?.overrideId}" />
                <g:hiddenField name="removeImage" value=""/>

                <input id="image" name="image" type="file" accept="image/png" hidden/>
            </g:form>
        </div>
    </section>

    <!-- Product search modal -->
    <g:render template="/product/productSearch" />
</body>
</html>