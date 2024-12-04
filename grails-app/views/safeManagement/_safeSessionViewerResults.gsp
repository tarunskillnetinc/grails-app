<script>
    var successMessage = "${successMessage}";
    var errorMessage = "${errorMessage}";

    $(document).ready(function () {
        if(successMessage != null && successMessage !== ''){
            $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
        } else if (errorMessage != null && errorMessage !== '') {
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        } else {
            $("#messages-container").html('');
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

    /* Button styling (can be customized more if needed) */
    button.btn {
        margin-left: 5px;
    }

    .card{
        margin-top: 5px;
    }

</style>


<div class="container-fluid p-0 mt-5 mb-3">
    <div class="row mb-2 no-gutters" id="shift-menu-headers">
        <g:if test="${isFinancialWeekExists}">
            <div class="col-1 text-center font-weight-bold">Financial Week</div>
        </g:if>
        <div class="col-2 text-center font-weight-bold pl-2">Safe</div>
        <div class="col-1 text-center font-weight-bold">Session Number</div>
        <div class="col-2 text-center font-weight-bold">Safe Started<br>Date and Time</div>
        <div class="col-2 text-center font-weight-bold">Safe Last Counted<br>Date and Time</div>
        <div class="col-1 text-center font-weight-bold">Safe Status</div>
    </div>

    <div class="card">
        <!-- Each Shift as a Boxed Card -->
        <g:if test="${safeSessions?.isEmpty()}">
            <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                <h3 class="text-muted">No results found</h3>
            </div>
        </g:if>
        <g:else>
            <g:each in="${safeSessions}" var="safeSession" status="i">
                <div class="shift-card-body border rounded mb-2 shift-info-container pt-2 pb-2 wl-striped${i%2}">
                    <g:set var="safe" value="${safeList.find { it.id == safeSession.safeId }}" />
                    <div class="row mb-2 align-items-center">
                        <g:if test="${isFinancialWeekExists}">
                            <div class="col-1 text-center">${safeSession?.financialWeek?.weekNumber}</div>
                        </g:if>
                        <div class="col-2 text-center" style="word-break: break-word;"><g:renderSafeInfo safe="${safe}" /></div>
                        <div class="col-1 text-center">${safeSession.sessionNumber}</div>
                        <div class="col-2 text-center">
                            <g:formatStringDate date="${safeSession?.openTime}" inputFormat="yyyy-MM-dd HH:mm:ss" outputFormat="dd/MM/yyyy HH:mm" timeZone="Europe/London"/>
                        </div>
                        <div class="col-2 text-center">
                            <g:if test="${safeSession?.reReconciledDate}">
                                <g:formatDate format="dd/MM/yyyy HH:mm" date="${safeSession.reReconciledDate.toDate()}" timeZone="Europe/London"/>
                            </g:if>
                            <g:elseif test="${safeSession?.reconciledDate}">
                                <g:formatDate format="dd/MM/yyyy HH:mm" date="${safeSession.reconciledDate.toDate()}" timeZone="Europe/London"/>
                            </g:elseif>
                        </div>
                        <div class="col-1 text-center" style="white-space: nowrap; overflow: visible; text-overflow: ellipsis;">
                            ${safeSession.sessionStatus == uk.co.wonderlane.wlpos.enums.SafeSessionStatus.OPEN ? 'In-progress' : 'Reconciled'}
                        </div>

                        <div class="${isFinancialWeekExists ? 'col-3' : 'col-4'}">
                            <div class="button-container d-flex justify-content-end align-items-center">
                                <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem;" onclick="safeSpotCheck(${safeSession.id});">Spot check</button>
                                <g:if test="${safeSession.sessionStatus == uk.co.wonderlane.wlpos.enums.SafeSessionStatus.OPEN}">
                                    <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9em;"
                                            onclick="showSafeSessionReconcileModal(${safeSession.id}, false, false, `${safe.description}`, ${configuredRecountLimit}, ${safeSession.totalRecountAttempts ?: 0});">Reconcile</button>
                                </g:if>
                                <g:if test="${safeSession.sessionStatus == uk.co.wonderlane.wlpos.enums.SafeSessionStatus.RECONCILED}">
                                    <g:if test="${configuredRecountLimit > (safeSession.totalRecountAttempts ?: 0)}">
                                        <button class="btn btn-danger p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                            onclick="showSafeSessionReconcileModal(${safeSession.id}, true, false, `${safe.description}`, ${configuredRecountLimit}, ${safeSession.totalRecountAttempts ?: 0});">Recount</button>
                                    </g:if>
                                    <button class="btn btn-success p-1 me-1" style="min-width: 80px; font-size: 0.9rem;"
                                            onclick="showSafeSessionReconcileModal(${safeSession.id}, false, true, `${safe.description}`, ${configuredRecountLimit}, ${safeSession.totalRecountAttempts ?: 0});">Finalise</button>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>
            </g:each>
        </g:else>
    </div>
</div>