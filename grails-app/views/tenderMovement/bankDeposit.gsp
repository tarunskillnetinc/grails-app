<script type="text/javascript">
    $(document).ready(function() {
        $('.date-picker').datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            endDate: new Date(),  // Set end date to today
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto"
        });
    });
</script>
<div id="bankDeposit" class="centered-content">
    <div class="form-container">
        <section id="bank-deposit-details">
            <div id="messages-container"></div>
        </section>
        <section class="mt-1">
            <g:form method="post" action="processBankDeposit" class="mt-1" name="processBankDeposit">
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="safe" class="col-form-label">Safe</label>
                            <g:select name="safeId"
                                      from="${safes}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${selectedSafe?.id}"
                                      class="form-control select-border"/>
                        </div>
                        <div class="form-group">
                            <label for="bankingDate" class="col-form-label">Date</label>
                            <g:textField name="bankingDate"
                                         value="${currentDate}"
                                         class="form-control date-picker"/>
                        </div>
                    </div>
                    <div class="form-col">
                        <div class="form-group">
                            <label for="tender" class="col-form-label">Tender</label>
                            <g:select
                                    name="tempTenderField"
                                    from="${tenders}"
                                    disabled="disabled"
                                    optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                                    class="form-control select-border"/>
                            <g:hiddenField name="tender" value="${tenders.get(0).toString()}"/>
                        </div>
                        <div class="form-group">
                            <label for="bank" class="col-form-label">Bank</label>
                            <g:textField name="bank"
                                         value="${bank?.name}"
                                         class="form-control"/>
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="bagReferenceNumber" class="col-form-label">Bag Reference Number</label>
                            <g:textField name="bagReferenceNumber"
                                         value="${bagReferenceNumber}"
                                         class="form-control"/>
                        </div>
                    </div>
                    <div class="form-col">
                        <div class="form-group">
                            <label for="amount" class="col-form-label">Amount</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField id="amount"
                                             name="amount"
                                             value="${amount}"
                                             min="0.01"
                                             max="999999.99"
                                             class="form-control mask-money"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="comments" class="col-form-label">Comments</label>
                            <textarea name="comments" class="form-control">${comments}</textarea>
                        </div>
                    </div>
                </div>
                <div class="buttons-container">
                    <button id="bank-deposit-cancel" type="button" name="cancel-button"
                            onclick="handleCancel('${createLink(action:'/home')}')"
                            class="btn btn-wl mr-2">Cancel</button>
                    <button id="bank-deposit-save" type="submit" name="save-button"
                            class="btn btn-success">Save</button>
                </div>
            </g:form>
        </section>
    </div>
</div>