<style>
    #add-float-form-container .dropdown-menu {
        display: none;
    }
    #add-float-form-container .dropdown-menu.show {
        display: block;
    }
    #add-float-form-container .dropdown {
        position: relative;
    }
    #add-float-form-container .dropdown-menu {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 1000;
        float: left;
        min-width: 10rem;
        padding: .5rem 0;
        margin: .125rem 0 0;
        font-size: 1rem;
        color: #212529;
        text-align: left;
        list-style: none;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid rgba(0,0,0,.15);
        border-radius: .25rem;
    }
    #add-float-form-container .caret {
        display: inline-block;
        width: 0;
        height: 0;
        margin-left: 0.255em;
        vertical-align: 0.255em;
        content: "";
        border-top: 0.3em solid;
        border-right: 0.3em solid transparent;
        border-bottom: 0;
        border-left: 0.3em solid transparent;
    }

    #add-float-form-container .dropdown.show .caret {
        transform: rotate(180deg);
    }
</style>

<script type="text/javascript">

    var successMessage = "${success}";
    var errorMessage = "${error}";

    $(document).ready(function () {
        addMoneyMaskLogic();
        processAddFloatActionButton();
        handleResponseMessages("${success}", "${error}");
        initializeMultiSelect();
    });

    function processAddFloatActionButton() {
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#add-float-save');

        // Add the click handler once
        $(document).on('click', '#add-float-save', function(e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);

            // Call the processTenderLift function
            processAddFloat();

            // Re-enable the button after a short delay
            setTimeout(function() {
                $button.prop('disabled', false);
            }, 1000); // Adjust the delay as needed
        });
    }

    function handleResponseMessages(successMessage, errorMessage) {
        $("#messages-container").empty();

        if (successMessage && successMessage.trim() !== '') {
            var decodedSuccessMessage = $("<textarea/>").html(successMessage).text(); // Decode escaped HTML
            var successHtml = $('<div class="alert alert-success alert-wl mx-0" role="alert"></div>');
            successHtml.html(decodedSuccessMessage); // Render decoded HTML
            $("#messages-container").append(successHtml);
        }

        if (errorMessage && errorMessage.trim() !== '') {
            var decodedErrorMessage = $("<textarea/>").html(errorMessage).text(); // Decode escaped HTML
            var errorHtml = $('<div class="alert alert-danger alert-wl mx-0" role="alert"></div>');
            errorHtml.html(decodedErrorMessage); // Render decoded HTML
            $("#messages-container").append(errorHtml);
        }
    }

    function initializeMultiSelect() {
        // Attach event listener for dynamically generated checkboxes
        $(document).on('change', 'input[name="tillNos"]', function() {
            updateSelectedTills();
        });

        // Update selected tills on page load
        updateSelectedTills();

        $('#tillNo').on('click', function () {
            $(this).parent().toggleClass('show');
            $(this).next('.dropdown-menu').toggleClass('show');
        });

        $(document).on('click', function (e) {
            if (!$(e.target).closest('.dropdown').length) {
                $('.dropdown-menu').removeClass('show');
                $('.dropdown').removeClass('show');
            }
        });

        function updateSelectedTills() {
            const selected = $('input[name="tillNos"]:checked').map(function () {
                return $(this).val();
            }).get();

            if (selected.length > 0) {
                $('#selectedTills').text(selected.join(', '));
            } else {
                $('#selectedTills').text('Select Till Numbers');
            }
        }
    }

</script>

<div class="centered-content">
    <div class="form-container">
        <section id="segment-details">
            <div id="messages-container"></div>
        </section>

        <section class="mt-1">
            <g:form method="post" action="processAddFloat" class="mt-1" name="processAddFloat">
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="safe" class="col-form-label">Safe</label>
                            <div class="flex-grow-1">
                                <g:select name="safeId" from="${safeLocations}" optionKey="id" optionValue="description" value="${primarySafe?.id}" class="form-control select-border"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="tillNo" class="col-form-label">Till No</label>
                            <div class="dropdown">
                                <div class="form-control" id="tillNo">
                                    <span id="selectedTills">Select Till Numbers</span>
                                    <span class="caret"></span>
                                </div>
                                <div class="dropdown-menu">
                                    <g:each in="${tills}" var="till">
                                        <label class="mb-0 dropdown-item">
                                            <input type="checkbox" name="tillNos" value="${till.tillId}"> ${till.tillId}
                                        </label>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-col">
                        <div class="form-group">
                            <label for="tenders" class="col-form-label">Tender</label>
                            <div class="flex-grow-1">
                                <g:select name="tenderTypeId" from="${tenders}" optionKey="id" optionValue="name" class="form-control select-border" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="amount" class="col-form-label">Amount</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="9999.99" class="form-control mask-money"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="buttons-container">
                    <button id="add-float-cancel" type="button" name="safe-save-button" onclick="handleCancelTenderUpdate('${createLink(action:'/home')}')" class="btn btn-wl mr-2">Cancel</button>
                    <button id="add-float-save" type="button" class="btn btn-success">Save</button>
                </div>
            </g:form>
        </section>
    </div>
</div>

