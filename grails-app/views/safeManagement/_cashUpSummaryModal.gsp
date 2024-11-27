<div class="modal-header">
    <h2>Safe Session Management</h2>
</div>

<div class="modal-body">
    <div class="row mt-3 mb-2">
        <div class="col-12 pr-0">
            <g:set var="reconciliationTotals" value="${isSafeSessionFinalizeMode ? safeSession.reconciliationTotals : safeSession.pendingReconciliationTotals}" />

            <div class="row">
                <p class="mx-auto"><strong>${safeDescription}</strong> count reconciliation</p>
            </div>

            <div class="row mt-3 mb-2 table-wl">
                <div class="col-2 font-weight-bold text-right">&nbsp;</div>
                <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Expected</div>
                <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Counted</div>
                <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Diff</div>
            </div>

            <g:each in="${reconciliationTotals}" var="reconciliationTotal" status="i">
                <div class="row pt-2 pb-2">
                    <div class="col-2 text-right"><g:message code="TenderType.${reconciliationTotal.tenderType}" /></div>
                    <div id="expected-${i + 1}" class="col-3 text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value - reconciliationTotal.variance}" type="currency" /></div>
                    <div id="counted-${i + 1}" class="col-3 text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value}" type="currency" /></div>
                    <div id="diff-${i + 1}" class="col-3 text-right text-truncate">
                        <g:if test="${reconciliationTotal.variance >= 0}">
                            <span style="font-weight: bold; color: black;">
                                <g:formatNumber number="${reconciliationTotal.variance}" type="currency" />
                            </span>
                        </g:if>
                        <g:else>
                            <span style="font-weight: bold; color: red;">
                                <g:formatNumber number="${reconciliationTotal.variance}" type="currency" />
                            </span>
                        </g:else>
                    </div>
                </div>
            </g:each>

            <div class="row pt-2 pb-2">
                <div class="col-5 text-right">Total</div>
                <div id="total" class="col-3 text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;">
                    <g:formatNumber number="${reconciliationTotals.sum { it.value }}" type="currency" />
                </div>
                <div class="col-3 text-right">&nbsp;</div>
            </div>

            <g:if test="${!isSafeSessionFinalizeMode}">
                <g:form name="safeSessionVarianceForm">
                    <g:hiddenField name="safeSessionId" value="${safeSession.id}" />
                    <g:hiddenField name="safeDescription" value="${safeDescription}" />
                    <g:set var="reconciliationTotalsSum" value="${reconciliationTotals.sum { it.variance.abs() } }" />

                    <g:if test="${reconciliationTotals.sum { it.variance } ?: 0 != 0}">
                        <div class="row pt-5 pb-2">
                            <p id="safe-variance-message" class="mx-auto">
                                You are about to declare a safe variance of
                                <g:if test="${reconciliationTotals.sum { it.variance } < 0}">
                                    <span style="font-weight: bold; color: red;">
                                        <g:formatNumber number="${reconciliationTotals.sum { it.variance }}" type="currency" />
                                    </span>
                                </g:if>
                                <g:else>
                                    <span style="font-weight: bold; color: black;">
                                        <g:formatNumber number="${reconciliationTotals.sum { it.variance }}" type="currency" />
                                    </span>
                                </g:else>
                                .
                            </p>
                        </div>


                        <g:if test="${reconciliationTotalsSum >  tillSafeSessionVarianceLimit}">
                            <g:if test="${varianceReasons.size() > 0}">
                                <div class="row pt-2 pb-2">
                                    <p class="mx-auto">Please select a reason:</p>
                                </div>
                                <div class="row pt-1 pb-2 form-group">
                                    <div class="col-6 mx-auto">
                                        <g:select name="tenderReconciliationVarianceReason"
                                                  from="${varianceReasons}"
                                                  optionKey="code"
                                                  optionValue="description"
                                                  value="${reconciliationTotals.find { it.varianceReason != null }?.varianceReason}"
                                                  class="form-control select-border form-control-sm" />
                                    </div>
                                </div>
                            </g:if>
                            <div class="row pt-1 pb-2 form-group">
                                <div class="col-6 mx-auto">
                                    <g:textField name="tenderReconciliationVarianceReasonText" class="form-control bottom-border" placeholder="Additional reason (optional)."
                                                 maxLength="40" size="40" value="${reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}"/>
                                </div>
                            </div>
                        </g:if>

                    </g:if>
                </g:form>
            </g:if>
            <g:else>
                <div class="row mt-5">&nbsp;</div>
                <g:if test="${reconciliationTotals.find { it.varianceReason != null }}">
                    <div class="row mb-2">
                        <div class="col-5 text-right">Variance reason:</div>
                        <div class="col-7">
                            <g:message code="${varianceReasons.find {  it.getCode() == reconciliationTotals.find { it.varianceReason != null }?.varianceReason }?.description}" />
                        </div>
                    </div>
                </g:if>

                <g:if test="${reconciliationTotals.find { it.varianceReasonText != null }}">
                    <div class="row mb-2">
                        <div class="col-5 text-right">Variance additional reason:</div>
                        <div id="variance-additional-reason-after-save" class="col-7">${reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}</div>
                    </div>
                </g:if>

                <div class="row mb-2">
                    <div id="reconciled-by" class="col-5 text-right">Reconciled by:</div>
                    <div class="col-7">${safeSession.reconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${safeSession?.reconciledDate?.toDate()}" timeZone="Europe/London"/></div>
                </div>

                <g:if test="${safeSession.reReconciledDate != null}">
                    <div class="row mb-2">
                        <div id="re-reconciled-by" class="col-5 text-right">Re-reconciled by:</div>
                        <div class="col-7">${safeSession.reReconciledByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${safeSession?.reReconciledDate?.toDate()}" timeZone="Europe/London" /></div>
                    </div>
                </g:if>

            </g:else>
        </div>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelSafeSessionButton" class="btn btn-secondary" data-dismiss="modal" onclick="getSafeSessions()">${safeSession.reconciledDate == null ? 'Cancel' : 'Close'}</button>
    <g:if test="${!isSafeSessionFinalizeMode}">
        <button type="button" id="saveSafeSessionButton" class="btn btn-success" onclick="submitSafeSession(${safeSession.id}, ${safeSession.reconciledDate != null}, false, '${safeDescription}', ${isSafeFinalisingWarningRequired})" >Save</button>
    </g:if>
    <g:else>
        %{-- Here can use same `submitSafeSession` action--}%
        <button type="button" id="finalizeSafeSessionButton" class="btn btn-success" onclick="submitSafeSession(${safeSession.id}, false, true, '${safeDescription}', ${isSafeFinalisingWarningRequired})">Finalise</button>
    </g:else>
</div>
