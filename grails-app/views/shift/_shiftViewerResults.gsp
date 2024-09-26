<style>
    /* Shift details border styling */
    .shift-card-body {
        /*background-color: #fff;*/
        border: 2px  solid black;
        padding: 15px;
        margin-bottom: 15px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    }

    .shift-card-header {
        border: 2px  solid black;
        padding: 15px;
        margin-bottom: 15px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    /* Button styling (can be customized more if needed) */
    button.btn {
        margin-left: 5px;
    }

    .card{
        margin-top: 5px;
    }


</style>

<div class="container-fluid mt-3 mb-3">
    <div class="row justify-content-end align-items-center">
        <div class="col-auto pr-0">
            <div class="d-flex align-items-center">
                <strong class="mr-2 h5 mb-0">Data retrieved at:</strong>
                <span id="lastRefreshTime" class="h5 mb-0 mr-3"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${lastRefreshDate?.toDate() ?: new Date()}" timeZone="Europe/London"/></span>
            </div>
        </div>
        <div class="col-auto pr-0"><a id="refresh" href="#" class="btn btn-wl" onclick="getShifts();">Refresh</a></div>
    </div>
</div>


<div class="container-fluid p-0">
    <div class="row mb-2 no-gutters" id="shift-menu-headers">
        <g:if test="${isFinancialWeekExists}">
            <div class="col-1 text-center font-weight-bold">Financial Week</div>
        </g:if>
        <div class="col-1 text-center font-weight-bold pl-2">Till Number</div>
        <div class="col-1 text-center font-weight-bold">Shift Number</div>
        <div class="col-2 text-center font-weight-bold">Shift Start</div>
        <div class="col-2 text-center font-weight-bold">Shift End</div>
        <div class="col-2 text-center font-weight-bold">Shift Status</div>
        <div class="${isFinancialWeekExists ? 'col-3' : 'col-4'} text-right font-weight-bold pr-4"></div>
    </div>

    <div class="card">
        <!-- Each Shift as a Boxed Card -->
        <g:if test="${shiftMap?.isEmpty()}">
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <h3 class="text-muted">No results found</h3>
            </div>
        </g:if>
        <g:else>
            <!-- Each Shift as a Boxed Card -->
            <g:each in="${shiftMap}" var="entry" status="i">
                <div class="shift-card-body border rounded mb-2 shift-info-container pt-2 pb-2 wl-striped${i%2}">
                    <g:each in="${entry.value}" var="shift">
                        <div class="row mb-2 align-items-center">
                            <g:if test="${isFinancialWeekExists}">
                                <div class="col-1 text-center">${shift?.financialWeek?.id}</div>
                            </g:if>
                            <div class="col-1 text-center">${shift.tillId}</div>
                            <div class="col-1 text-center">${shift.shiftNumber}</div>
                            <div class="col-2 text-center">
                                <g:formatStringDate date="${shift?.shiftOpenTime}" inputFormat="yyyy-MM-dd HH:mm:ss" outputFormat="dd/MM/yyyy HH:mm" timeZone="Europe/London"/>
                            </div>
                            <div class="col-2 text-center">
                                <g:formatStringDate date="${shift?.shiftCloseTime}" inputFormat="yyyy-MM-dd HH:mm" outputFormat="dd/MM/yyyy HH:mm" timeZone="Europe/London"/>
                            </div>
                            <div class="col-2 text-center">${shift.shiftStatus}</div>
                            <div class="${isFinancialWeekExists ? 'col-3' : 'col-4'}">
                                <div class="button-container d-flex justify-content-end align-items-center">
                                    <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.OPEN}">
                                        <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;">Close</button>
                                    </g:if>
                                    <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.UNRECONCILED}">
                                        <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, ${shift.reconciledDate != null});">Reconcile</button>
                                    </g:if>
                                    <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.RECONCILED}">
                                        <button class="btn btn-danger p-1 me-1" style="min-width: 70px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, ${shift.reconciledDate != null});">Recount</button>
                                        <button class="btn btn-success p-1 me-1" style="min-width: 70px; font-size: 0.9rem;">Finalise</button>
                                    </g:if>
                                    <button class="btn btn-wl p-1" style="min-width: 70px; font-size: 0.9rem;">Spot check</button>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </div>
            </g:each>
        </g:else>
    </div>
</div>