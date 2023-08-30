<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
        <div class="row">
            <p class="mx-auto">Safe Count reconciliation</p>
        </div>

        <div class="row mt-3 mb-2 ml-0 mr-0 table-wl">
            <div class="col-2 font-weight-bold text-right">&nbsp;</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Expected</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Counted</div>
            <div class="col-3 font-weight-bold text-right" style="border-bottom: 1px solid black;">Diff</div>
        </div>

        <g:each in="${snapshot.totals}" var="reconciliationTotal" status="i">
            <div class="row ml-0 mr-0 pt-2 pb-2">
                <div class="col-2 my-auto text-right"><g:message code="TenderType.${reconciliationTotal.tenderType}" /></div>
                <div id="expected-${i + 1}" class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value - reconciliationTotal.variance}" type="currency" /></div>
                <div id="counted-${i + 1}" class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value}" type="currency" /></div>
                <div id="diff-${i + 1}" class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.variance}" type="currency" /></div>
            </div>
        </g:each>

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Total</div>
            <div id="total" class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${snapshot.totals.sum { it.value }}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <g:if test="${snapshot.countDate == null}">
            <g:form name="snapshotVarianceForm">
                <g:hiddenField name="snapshotId" value="${snapshot.id}" />

                <g:if test="${snapshot.totals.sum { it.variance } ?: 0 != 0}">
                    <div class="row ml-0 mr-0 pt-5 pb-2">
                        <p id="safe-variance-message" class="mx-auto text-truncate">You are about to declare a safe variance of <g:formatNumber number="${snapshot.totals.sum { it.variance.abs() }}" type="currency" />.</p>
                    </div>
                    <div class="row ml-0 mr-0 pt-2 pb-2">
                        <p class="mx-auto">Please select a reason:</p>
                    </div>

                    <div class="row ml-0 mr-0 pt-1 pb-2 form-group">
                        <div class="col-6 offset-3">
                            <g:select name="varianceReason" from="${varianceReasons}" valueMessagePrefix="TenderReconciliationVarianceReason" class="form-control select-border" />
                        </div>
                    </div>
                    <div class="row ml-0 mr-0 pt-1 pb-2 form-group">
                        <div class="col-6 offset-3">
                            <g:textField name="varianceReasonText" class="form-control bottom-border" placeholder="Additional reason (optional)." />
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
            <g:if test="${snapshot.varianceReason != null }">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance reason:</div>
                    <div id="variance-reason-after-save" class="col-7"><g:message code="TenderReconciliationVarianceReason.${snapshot.varianceReason}" /></div>
                </div>
            </g:if>

            <g:if test="${snapshot.varianceReasonText != null }">
                <div class="row mb-2 ml-0 mr-0">
                    <div class="col-5 text-right">Variance additional reason:</div>
                    <div id="variance-additional-reason-after-save" class="col-7">${snapshot.varianceReasonText}</div>
                </div>
            </g:if>

            <div class="row mb-2 ml-0 mr-0">
                <div id="reconciled-by" class="col-5 text-right">Reconciled by:</div>
                <div class="col-7">${snapshot.countedByUsersName} on <g:formatDate format="dd/MM/yyyy 'at' HH:mm:ss" date="${snapshot.countDate.toDate()}" /></div>
            </div>
        </g:else>
    </div>

    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="safeReport" model="[snapshot: snapshot]" />
    </div>
</div>