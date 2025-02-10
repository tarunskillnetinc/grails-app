<div id="modal-header" class="modal-header">
    <h2>Cash Management</h2>
</div>

<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">

        <g:set var="reconciliationTotals" value="${shift.reconciliationTotals}" />

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
                <div class="col-2 my-auto text-right">${reconciliationTotal.tenderTypeName}</div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value - reconciliationTotal.variance}" type="currency" /></div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${reconciliationTotal.value}" type="currency" /></div>
                <div class="col-3 my-auto text-right text-truncate">
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

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto text-right">Total</div>
            <div class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${reconciliationTotals.sum { it.value }}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <%
            def autoReconcileTotal = BigDecimal.ZERO
        %>
        <g:each in="${tenderTypes?.findAll { it.autoReconcile }}" var="tenderType">
            <%
                autoReconcileTotal = autoReconcileTotal.add(shift.tenderTotals.find { it.tenderTypeId == tenderType.id }?.value ?: 0)
            %>
            <div class="row ml-0 mr-0 pt-2 pb-2">
                <div class="col-5 my-auto text-right">${tenderType.name}</div>
                <div class="col-3 my-auto text-right text-truncate"><g:formatNumber number="${shift.tenderTotals.find { it.tenderTypeId == tenderType.id }?.value ?: 0}" type="currency" /></div>
                <div class="col-3 my-auto text-right">&nbsp;</div>
            </div>
        </g:each>

        <div class="row ml-0 mr-0 pt-4 pb-2">
            <div class="col-5 my-auto font-weight-bold text-right">Shift Total</div>
            <div class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${(reconciliationTotals.sum { it.value } ?: 0) + autoReconcileTotal}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <div class="row mt-5">&nbsp;</div>
        <g:if test="${reconciliationTotals.find { it.varianceReason != null }}">
            <div class="row mb-2 ml-0 mr-0">
                <div class="col-5 text-right">Variance reason:</div>
                <g:set var="varianceReasonSelected" value="${reconciliationTotals.find { it.varianceReason != null }?.varianceReason}" />
                <div class="col-7"><g:message code="${varianceReasons.find { it.getCode() == varianceReasonSelected }?.description}" /></div>
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

        <g:if test="${safes?.collect()?.size() > 1}">
            <div class="row mb-2 ml-0 mr-0">
                <div class="col-5 text-right">Please select a safe location:</div>
                <div class="col-4">
                    <g:select name="safeId"
                              from="${safes}"
                              optionKey="id"
                              optionValue="description"
                              class="form-control select-border form-control-sm"/>
                </div>
            </div>
        </g:if>
        <g:elseif test="${safes?.collect()?.size() == 1}">
            <g:hiddenField name="safeId" value="${safes?.collect()[0].id}"/>
        </g:elseif>
    </div>

    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="shiftReport" model="[shift: shift]" />
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">${shift.reconciledDate == null ? "Cancel" : "Close"}</button>
    <button type="button" id="finalizeButton" class="btn btn-success" onclick="submitShift(${shift.id}, false, true)">Finalise</button>
</div>