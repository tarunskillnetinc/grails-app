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
        margin-top: 15px;
    }

    .shift-info-container {
        font-weight: bold;
    }

    .shift-info-container .btn {
        font-weight: bold;
    }

    .shift-info-container .small {
        font-weight: normal;
    }

    .shift-info-container h5 {
        font-weight: bolder;
    }

</style>

<div class="container-fluid mt-3 mb-3">
    <div class="row justify-content-end">
        <div class="col-auto">
            <div class="d-flex align-items-center">
                <strong class="mr-2 h5 mb-0 font-weight-bold">Data retrieved at:</strong>
                <span id="lastRefreshTime" class="mr-3 h5 mb-0 font-weight-bold"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${lastRefreshDate?.toDate() ?: new Date()}" timeZone="Europe/London"/></span>
                <a href="#" id="refreshIcon" class="text-primary" onclick="getShifts(); return false;" style="font-size: 24px; text-decoration: none;">&#x21bb;</a>
            </div>
        </div>
    </div>
</div>


<div class="card">
    <div class="shift-card-header bg-light">
        <div class="row">
            <g:if test="${isFinancialWeekExists}">
                <div class="col-1 text-center font-weight-bold">Financial Week</div>
            </g:if>
            <div class="col-1 text-center font-weight-bold">Till Number</div>
            <div class="col-1 text-center font-weight-bold">Shift Number</div>
            <div class="col-2 text-center font-weight-bold">
                <div>
                    <span>Shift Begin</span>
                    <span style="display: block;">Date & Time</span>
                </div>
            </div>
            <div class="col-2 text-center font-weight-bold">
                <div>
                    <span>Shift End</span>
                    <span style="display: block;">Date & Time</span>
                </div>
            </div>
            <div class="col-2 text-center font-weight-bold">Shift Status</div>
            <div class="col-3 text-right font-weight-bold">Actions</div>
        </div>
    </div>

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
                        <div class="col-1 text-center"><g:formatTillId tillId="${shift.tillId}"/></div>
                        <div class="col-1 text-center">${shift.shiftNumber}</div>
                        <div class="col-2 text-center">
                            <g:formatStringDate date="${shift?.shiftOpenTime}" inputFormat="yyyy-MM-dd HH:mm:ss" outputFormat="dd/MM/yyyy HH:mm" timeZone="Europe/London"/>
                        </div>
                        <div class="col-2 text-center">
                            <g:formatStringDate date="${shift?.shiftCloseTime}" inputFormat="yyyy-MM-dd HH:mm" outputFormat="dd/MM/yyyy HH:mm" timeZone="Europe/London"/>
                        </div>
                        <div class="col-2 text-center">${shift.shiftStatus}</div>
                        <div class="col-3 text-right">
                            <div class="button-container d-flex justify-content-end align-items-center">
                                <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.OPEN}">
                                    <button class="btn btn-wl btn-info p-1 me-1" style="min-width: 70px; font-size: 0.9rem;">Close</button>
                                </g:if>
                                <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.UNRECONCILED}">
                                    <button class="btn btn-primary p-1 me-1" style="min-width: 70px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, ${shift.reconciledDate != null});">Reconcile</button>
                                </g:if>
                                <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.RECONCILED}">
                                    <button class="btn btn-primary p-1 me-1" style="min-width: 70px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, ${shift.reconciledDate != null});">Recount</button>
                                    <button class="btn btn-success p-1 me-1" style="min-width: 70px; font-size: 0.9rem;">Finalise</button>
                                </g:if>
                                <button class="btn btn-info p-1" style="min-width: 70px; font-size: 0.9rem;">Spot check</button>
                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </g:each>
    </g:else>
</div>
