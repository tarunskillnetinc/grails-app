<div id="modal-header" class="modal-header">
    <h2>Cash Management</h2>
</div>

<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">

        <g:set var="reconciliationTotals" value="${isShiftFinalizeMode ? shift.reconciliationTotals : shift.pendingReconciliationTotals}" />

        <div class="row">
            <p class="mx-auto">Reconciliation for shift number ${shift.shiftNumber}(<g:formatStringDate date="${shift?.shiftOpenTime}" inputFormat="yyyy-MM-dd HH:mm:ss" outputFormat="dd/MM/yyyy" timeZone="Europe/London"/>)</p>
        </div>

        <div class="row mt-3 mb-2 ml-0 mr-0 table-wl">
            <div class="col-2 font-weight-bold text-right">&nbsp;</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Expected</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Counted</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Diff</div>
        </div>

        <g:each in="${reconciliationTotals}" var="reconciliationTotal">
            <div class="row ml-0 mr-0 pt-2 pb-2">
                <div class="col-2 my-auto text-right"><g:message code="TenderType.${reconciliationTotal.tenderType}" /></div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value - reconciliationTotal.variance}" type="currency" /></div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value}" type="currency" /></div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.variance}" type="currency" /></div>
            </div>
        </g:each>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Total</div>
            <div class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${reconciliationTotals.sum { it.value }}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Debit / credit card</div>
            <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${shift.tenderTotals.find { it.tenderType.name() == 'CARD' }?.value ?: 0}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Coupons</div>
            <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${BigDecimal.ZERO}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Other</div>
            <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${BigDecimal.ZERO}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row ml-0 mr-0 pt-4 pb-2">
            <div class="col-5 my-auto font-weight-bold text-right">Shift Total</div>
            <div class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${(reconciliationTotals.sum { it.value } ?: 0) + (shift.tenderTotals.find { it.tenderType.name() == 'CARD' }?.value ?: 0)}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <g:if test="${!isShiftFinalizeMode}">
            <g:form name="shiftVarianceForm">
                <g:hiddenField name="shiftId" value="${shift.id}" />

                <g:if test="${reconciliationTotals.sum { it.variance.abs() } >  tillShiftVarianceLimit}">
                    <div class="container">
                        <div class="row pt-5 pb-2">
                            <div class="col-10 offset-1">
                                <p>You are about to declare a shift variance of <g:formatNumber number="${reconciliationTotals.sum { it.variance.abs() }}" type="currency" /></p>
                            </div>
                        </div>

                        <div class="row pt-2 pb-2 align-items-center">
                            <div class="col-5 offset-1">
                                <p class="mb-0">Please select a reason:</p>
                            </div>
                            <div class="col-4">
                                <g:select name="tenderReconciliationVarianceReason"
                                          from="${varianceReasons}"
                                          optionKey="code"
                                          optionValue="description"
                                          value="${reconciliationTotals.find { it.varianceReason != null }?.varianceReason}"
                                          class="form-control select-border form-control-sm" />
                            </div>
                        </div>

                        <div class="row pt-1 pb-2">
                            <div class="col-9 offset-1">
                                <g:textField name="tenderReconciliationVarianceReasonText" class="form-control bottom-border form-control-sm" placeholder="Additional reason (optional)."
                                             value="${reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}" />
                            </div>
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
            <g:if test="${reconciliationTotals.find { it.varianceReason != null }}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance reason:</div>
                    <g:set var="varianceReasonSelected" value="${reconciliationTotals.find { it.varianceReason != null }?.varianceReason}" />
                    <div class="col-7"><g:message code="${varianceReasons.find { it.varianceReason != null }?.description}" /></div>
                </div>
            </g:if>

            <g:if test="${reconciliationTotals.find { it.varianceReasonText != null }}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance additional reason:</div>
                    <div class="col-7">${reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}</div>
                </div>
            </g:if>

            <div class="row mb-2 ml-0 mr-0">
                <div class="col-5 text-right">Reconciled by:</div>
                <div class="col-7">${shift.reconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${shift?.reconciledDate?.toDate()}" timeZone="Europe/London"/>.</div>
            </div>

            <g:if test="${shift.reReconciledDate != null}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Re-reconciled by:</div>
                    <div class="col-7">${shift.reReconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${shift?.reReconciledDate?.toDate()}" timeZone="Europe/London"/>.</div>
                </div>
            </g:if>

            <g:if test="${safeLocations?.collect()?.size() > 1}">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Please select a safe location:</div>
                    <div class="col-4">
                        <g:select name="safeLocationId"
                                  from="${safeLocations}"
                                  optionKey="id"
                                  optionValue="description"
                                  class="form-control select-border form-control-sm"/>
                    </div>
                </div>
            </g:if>
            <g:elseif test="${safeLocations?.collect()?.size() == 1}">
                <g:hiddenField name="safeLocationId" value="${safeLocations?.collect()[0].id}"/>
            </g:elseif>

        </g:else>
    </div>

    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="shiftReport" model="[shift: shift]" />
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">${shift.reconciledDate == null ? "Cancel" : "Close"}</button>
    <g:if test="${!isShiftFinalizeMode}">
        <button type="button" id="saveShiftButton" class="btn btn-success" onclick="submitShift(${shift.id}, ${shift.reconciledDate != null}, false)" >Save</button>
    </g:if>
    <g:else>
        <button type="button" id="finalizeButton" class="btn btn-success" onclick="submitShift(${shift.id}, false, true)">Finalise</button>
    </g:else>
</div>