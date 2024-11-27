<script>
    var successMessage = "${successMessage}";
    var errorMessage = "${errorMessage}";
    var isRollingFloatSuccess = "${isRollingFloatSuccess}";
    var rollingFloatMessage = "${rollingFloatMessage}";

    $(document).ready(function () {
        if(successMessage != null && successMessage !== ''){
            $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
        } else if (errorMessage != null && errorMessage !== '') {
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        } else {
            $("#messages-container").html('');
        }

        if(isRollingFloatSuccess && rollingFloatMessage != null && rollingFloatMessage !== ''){
            alert(rollingFloatMessage)
        }

    });
</script>


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
            <g:each in="${shiftMap}" var="entry" status="i">
                <div class="shift-card-body border rounded mb-2 shift-info-container pt-2 pb-2 wl-striped${i%2}">
                    <g:each in="${entry.value}" var="shift">
                        <div class="row mb-2 align-items-center">
                            <g:if test="${isFinancialWeekExists}">
                                <div class="col-1 text-center">${shift?.financialWeek?.weekNumber}</div>
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

                            <g:if test="${shift.shiftStatus && shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.OPEN}">
                                <!-- Column for Add Float and Cash Lift buttons (horizontally aligned and centered) -->
                                <div class="col-1">
                                    <div class="button-container d-flex justify-content-center align-items-center">
                                        <button class="btn btn-wl p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="cashUpdateModal(${true}, ${shift.retailerId}, ${shift.storeId}, ${shift.tillId}, ${shift.id});">Add Float</button>
                                        <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;" onclick="cashUpdateModal(${false}, ${shift.retailerId}, ${shift.storeId}, ${shift.tillId},  ${shift.id});">Cash Lift</button>
                                    </div>
                                </div>

                                <!-- Adjusted column for other buttons -->
                                <div class="${isFinancialWeekExists ? 'col-2' : 'col-3'}">
                                    <div class="button-container d-flex justify-content-end align-items-center">
                                        <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;" onclick="spotCheck(${shift.retailerId}, ${shift.storeId}, ${shift.tillId}, ${shift.id});">Spot check</button>
                                        <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="closeShifts(${shift.retailerId}, ${shift.storeId}, ${shift.tillId}, ${shift.id});">Close</button>
                                    </div>
                                </div>
                            </g:if>
                            <g:else>
                                <!-- If shift status is not OPEN, allocate full width to the last column -->
                                <div class="${isFinancialWeekExists ? 'col-3' : 'col-4'}">
                                    <div class="button-container d-flex justify-content-end align-items-center">
                                        <g:if test="${!shift.shiftStatus}">
                                            <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="openShifts(${shift.retailerId}, ${shift.storeId}, ${shift.tillId});">Open</button>
                                        </g:if>
                                        <g:else>
                                            <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;" onclick="spotCheck(${shift.retailerId}, ${shift.storeId}, ${shift.tillId}, ${shift.id});">Spot check</button>
                                            <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.UNRECONCILED}">
                                                <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, false, false);">Reconcile</button>
                                            </g:if>
                                            <g:if test="${shift.shiftStatus == uk.co.wonderlane.wlpos.enums.ShiftStatus.RECONCILED}">
                                                <%int currentTotalRecountAttempts = shift.totalRecountAttempts != null ? shift.totalRecountAttempts : 0 %>
                                                <g:if test="${currentTotalRecountAttempts < configuredRetryAttempts}">
                                                    <button class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, true, false);">Recount</button>
                                                </g:if>
                                                <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;" onclick="showCashModal(${shift.id}, false, true);">Finalise</button>
                                            </g:if>
                                        </g:else>
                                    </div>
                                </div>
                            </g:else>
                        </div>
                    </g:each>
                </div>
            </g:each>


        </g:else>
    </div>
</div>