<div class="header-wl mt-3">
    <h2 id="page-title" class="mx-auto">Pay In</h2>
</div>

<section id="segment-details" class="container-fluid">
    <div id="messages-container"></div>
</section>

<script type="text/javascript">
    $(document).ready(function () {
        addMoneyMaskLogic()
    });
</script>

<section class="mt-1">
    <g:form method="post" action="processTenderLift" class="mt-1" name="processTenderLift">
        <div class="container-fluid">
            <div class="row">
                <div class="col-md-6 col-lg-5 offset-md-1">
                    <div class="form-group mb-5">
                        <div class="d-flex align-items-center">
                            <label for="safe" class="col-form-label mb-0 mr-2" style="width: 5rem;">Safe</label>
                            <div class="flex-grow-1" style="max-width: 15rem;">
                            <g:select name="safeId"
                                      from="${safeLocations}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${primarySafe?.id}"
                                      class="form-control select-border"
                                    title="list of safes"
                                    />
                            </div>
                        </div>
                    </div>

                    <div class="form-group mb-5">
                        <div class="d-flex align-items-center">
                            <label class="col-form-label mb-0 mr-2" style="width: 5rem;">Tender</label>

                            <div class="flex-grow-1" style="max-width: 15rem;">
                                <g:select name="tender"
                                          from="${tenders}"
                                          optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                                          class="form-control select-border"
                                            title="list of tenders"
                                            disabled="true"
                                            />
                            </div>
                        </div>
                    </div>

                    <div class="form-group mb-5">
                        <div class="d-flex align-items-center">
                            <label for="reasoncode" class="col-form-label mb-0 mr-2" style="width: 5rem;">Reason Code</label>

                            <div class="flex-grow-1" style="max-width: 15rem;">
                                <g:select name="reasoncode"
                                               from="${reasonCodes}"
                                               optionKey="id"
                                               optionValue="${{ it.description.toLowerCase().capitalize() }}"
                                               class="form-control select-border"
                                                title="cash reasoncode"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="form-group mb-5">
                        <div class="d-flex align-items-center">
                            <label for="amount" class="col-form-label mb-0 mr-2" style="width: 5rem;">Amount</label>

                            <div class="flex-grow-1" style="max-width: 15rem;">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">&pound;</span>
                                    </div>
                                    <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="999999.99" class="form-control mask-money"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Buttons Row -->
                    <div class="mt-5 d-flex justify-content-end" style="max-width: 20.5rem;">  <!-- Increased margin-top -->
                        <button id="tender-lift-cancel" type="button" name="safe-save-button" onclick="handleCancelTenderLift('${createLink(action: '/home')}')" class="btn btn-wl mr-2">Cancel</button>
                        <button id="tender-lift-save" type="button" name="safe-save-button" class="btn btn-success">Save</button> <!-- Event Delegation button action added for this in function-processTenderLiftActionButton-->
                    </div>
                </div>
            </div>
        </div
    </g:form>
</section>