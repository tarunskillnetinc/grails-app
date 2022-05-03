<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
        <div class="row">
            <p class="mx-auto">Reconciliation for shift number ${shift.shiftNumber} (<g:formatDate format="dd/MM/yyyy" date="${shift.firstTransactionDate.toDate()}" />)</p>
        </div>

        <div class="row mt-3 mb-2 ml-0 mr-0 table-wl">
            <div class="col-2 font-weight-bold text-right">&nbsp;</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Expected</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Counted</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Diff</div>
        </div>

        <g:each in="${shift.reconciliationTotals}" var="reconciliationTotal">
            <div class="row ml-0 mr-0 pt-2 pb-2">
                <div class="col-2 my-auto text-right"><g:message code="TenderType.${reconciliationTotal.tenderType}" /></div>
                <div class="col-3 my-auto text-right"><g:formatNumber number="${reconciliationTotal.value - reconciliationTotal.variance}" type="currency" /></div>
                <div class="col-3 my-auto text-right"><g:formatNumber number="${reconciliationTotal.value}" type="currency" /></div>
                <div class="col-3 my-auto text-right"><g:formatNumber number="${reconciliationTotal.variance}" type="currency" /></div>
            </div>
        </g:each>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Total</div>
            <div class="col-3 my-auto text-right" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${shift.reconciliationTotals.sum { it.value }}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Debit / credit card</div>
            <div class="col-3 my-auto text-right"><g:formatNumber number="${shift.tenderTotals.find { it.tenderType.name() == 'CARD' }?.value ?: 0}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Coupons</div>
            <div class="col-3 my-auto text-right"><g:formatNumber number="${BigDecimal.ZERO}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Other</div>
            <div class="col-3 my-auto text-right"><g:formatNumber number="${BigDecimal.ZERO}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-4 pb-2">
            <div class="col-5 my-auto font-weight-bold text-right">Shift Total</div>
            <div class="col-3 my-auto text-right" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${(shift.reconciliationTotals.sum { it.value } ?: 0) + (shift.tenderTotals.find { it.tenderType.name() == 'CARD' }?.value ?: 0)}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <g:if test="${shift.reconciledDate == null}">
            <g:form name="shiftVarianceForm">
                <g:hiddenField name="shiftId" value="${shift.id}" />

                <g:if test="${shift.reconciliationTotals.sum { it.variance } ?: 0 != 0}">
                    <div class="row ml-0 mr-0 pt-5 pb-2">
                        <p class="mx-auto">You are about to declare a shift variance of <g:formatNumber number="${shift.reconciliationTotals.sum { it.variance.abs() }}" type="currency" /></p>
                    </div>
                    <div class="row ml-0 mr-0 pt-2 pb-2">
                        <p class="mx-auto">Please select a reason:</p>
                    </div>

                    <div class="row ml-0 mr-0 pt-1 pb-2 form-group">
                        <div class="col-6 offset-3">
                            <g:select name="tenderReconciliationVarianceReason" from="${varianceReasons}" valueMessagePrefix="TenderReconciliationVarianceReason" class="form-control select-border" />
                        </div>
                    </div>
                    <div class="row ml-0 mr-0 pt-1 pb-2 form-group">
                        <div class="col-6 offset-3">
                            <g:textField name="tenderReconciliationVarianceReasonText" class="form-control bottom-border" placeholder="Additional reason (optional)." />
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <g:hiddenField name="tenderReconciliationVarianceReason" value="null" />
                    <g:hiddenField name="tenderReconciliationVarianceReasonText" value="null" />
                </g:else>
            </g:form>
        </g:if>
        <g:else>
            <div class="row mt-5">&nbsp;</div>
            <g:if test="${shift.reconciliationTotals.find { it.varianceReason != null }}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance reason:</div>
                    <div class="col-7"><g:message code="TenderReconciliationVarianceReason.${shift.reconciliationTotals.find { it.varianceReason != null }?.varianceReason}" /></div>
                </div>
            </g:if>

            <g:if test="${shift.reconciliationTotals.find { it.varianceReasonText != null }}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance additional reason:</div>
                    <div class="col-7">${shift.reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}</div>
                </div>
            </g:if>

            <div class="row mb-2 ml-0 mr-0">
                <div class="col-5 text-right">Reconciled by:</div>
                <div class="col-7">${shift.reconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${shift.reconciledDate.toDate()}" />.</div>
            </div>

            <g:if test="${shift.reReconciledDate != null}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Re-reconciled by:</div>
                    <div class="col-7">${shift.reReconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${shift.reReconciledDate.toDate()}" />.</div>
                </div>
            </g:if>
        </g:else>
    </div>

    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="shiftReport" model="[shift: shift]" />
    </div>
</div>