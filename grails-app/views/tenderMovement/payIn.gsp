<script type="text/javascript">
	var successMessage = "${success}";
    var errorMessage = "${error}";
    $(document).ready(function () {
        addMoneyMaskLogic()
        processPayInActionButton();
        handlePayInResponseMessages("${success}", "${error}");
    });

    function processPayInActionButton(){
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#payin-save');

        // Add the click handler once
        $(document).on('click', '#payin-save', function(e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);

            processPayIn();

            // Re-enable the button after a short delay
            setTimeout(function() {
                $button.prop('disabled', false);
            }, 1000); // Adjust the delay as needed
        });
    }

    function handlePayInResponseMessages(successMessage, errorMessage){
        if(successMessage != null && successMessage !== ''){
            $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
        } else if (errorMessage != null && errorMessage !== '') {
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        } else {
            $("#messages-container").html('');
        }
    }
</script>

<style>
#payin.form-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    max-width: 800px;
    margin: 0 auto;
}

.form-row {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin-bottom: 20px;
}

.form-group {
    width: 48%;
}

.button-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>

<section id="segment-details" class="container-fluid">
    <div id="messages-container"></div>
</section>
<section class="mt-1 pt-5">
    <g:form method="post" action="processPayIn" class="mt-1" name="processPayIn">
        <div id="payin" class="form-container">
            <div class="form-row">                
                    <div class="form-group">                        
                        <label for="safeId">Safe</label>                            
                        <g:select name="safeId"
	                              from="${safeLocations}"
	                              optionKey="id"
	                              optionValue="description"
	                              value="${primarySafe?.id}"
	                              class="form-control select-border"
	                            title="list of safes"
	                            />                            
                    </div>

                    <div class="form-group">                        
                            <label for="tender">Tender</label>                            
                            <g:select name="tender"
                                          from="${tenders}"
                                          optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                                          class="form-control select-border"
                                            title="list of tenders"
                                            readonly="true"
                                            />                            
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="reasoncodeId" >Reason Code</label>                            
                            <g:select name="reasoncodeId"
                                               from="${reasonCodes}"
                                               optionKey="id"
                                               optionValue="${{ it.description.toLowerCase().capitalize() }}"
                                               class="form-control select-border"
                                                title="cash reasoncode"
                                />                            
                        </div>

                        <div class="form-group">
                                <label for="amount" >Amount</label>
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <span class="input-group-text">&pound;</span>
                                        </div>
                                        <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="9999.99"
                                                class="form-control mask-money"/>
                                    </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="button-container">
                        <button id="payin-cancel" type="button" name="safe-save-button" onclick="handleCancelPayIn('${createLink(action: '/home')}')" class="btn btn-wl mr-2">Cancel</button>
                        <button id="payin-save" type="button" name="safe-save-button" class="btn btn-success">Save</button> <!-- Event Delegation button action added for this in function-processTenderLiftActionButton-->
                    </div>
                </div>
            </div>        
    </g:form>
</section>