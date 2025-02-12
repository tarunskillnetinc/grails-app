<div id="modal-header" class="modal-header">
    <h2>Cash Management</h2>
</div>

<div id="modal-body" class="modal-body row mt-3 mb-2">
    <div class="col-12 pr-0">

        <g:set var="reconciliationTotals" value="${shift.pendingReconciliationTotals}" />

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

        <div class="row ml-0 mr-0 pt-2 pb-4">
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

        <div class="row ml-0 mr-0 pt-2 pb-2">
            <div class="col-5 my-auto font-weight-bold text-right">Shift Total</div>
            <div class="col-3 my-auto text-right text-truncate" style="border-top: 1px solid black; border-bottom: 1px solid black;"><g:formatNumber number="${(reconciliationTotals.sum { it.value } ?: 0) + autoReconcileTotal}" type="currency" /></div>
            <div class="col-3 my-auto text-right">&nbsp;</div>
        </div>

        <g:form name="shiftVarianceForm">
            <g:hiddenField name="shiftId" value="${shift.id}" />
            <g:set var="reconciliationTotalsSum" value="${reconciliationTotals.sum { it.variance.abs() } }" />
            <g:if test="${reconciliationTotalsSum ?: 0 != 0}">
                <div class="container">
                    <div class="row pt-5 pb-2">
                        <p id="safe-variance-message" class="mx-auto">
                            You are about to declare a shift variance of
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

                    <g:if test="${reconciliationTotalsSum >  tillShiftVarianceLimit}">
                        <g:if test="${ varianceReasons.size() > 0}">
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
                        </g:if>
                        <div class="row pt-1 pb-2">
                            <div class="col-9 offset-1">
                                <g:textField name="tenderReconciliationVarianceReasonText" class="form-control bottom-border form-control-sm" placeholder="Additional reason (optional)."
                                             value="${reconciliationTotals.find { it.varianceReasonText != null }?.varianceReasonText}" />
                            </div>
                        </div>
                    </g:if>
                </div>
            </g:if>
        </g:form>
    </div>
</div>

<div id="modal-footer" class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">${shift.reconciledDate == null ? "Cancel" : "Close"}</button>
    <button type="button" id="saveShiftButton" class="btn btn-success" onclick="submitShift(${shift.id}, ${shift.reconciledDate != null}, false)" >Save</button>
</div>