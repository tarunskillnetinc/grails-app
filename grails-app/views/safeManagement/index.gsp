<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Safe Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />
        <asset:javascript src="safeCount.js"/>
        <asset:javascript src="date-pickers.js"/>
        <asset:javascript src="co-utils.js"/>
        <asset:javascript src="input-validator.js" />
        <asset:javascript src="safeManagement.js"/>
        <asset:javascript src="safeManagementUrls.js"/>
        <asset:javascript src="shiftUrls.js"/>
        <asset:javascript src="shiftManagement.js"/>

        <script type="text/javascript">
            $(function() {
                SafeManagementUrls.init(
                    "${createLink(controller: 'SafeManagement', action: 'ajaxGetSafeSessions')}",
                    "${createLink(controller: 'SafeManagement', action: 'ajaxGetSafeSessionCashUpModal')}",
                    "${createLink(controller: 'SafeManagement', action: 'ajaxUpdateSafeSessionReconcileData')}",
                    "${createLink(controller: 'SafeManagement', action: 'ajaxSaveSafeSessionCashData')}",
                );
                ShiftUrls.init("${createLink(controller: 'shift', action: 'ajaxGetShifts')}",
                    "${createLink(controller: 'shift', action: 'ajaxGetCashDetails')}",
                    "${createLink(controller: 'shift', action: 'ajaxChangeCashUpType')}",
                    "${createLink(controller: 'shift', action: 'ajaxSaveCash')}",
                    "${createLink(controller: 'shift', action: 'ajaxSaveShift')}",
                    "${createLink(controller: 'shift', action: 'ajaxOpenShift')}",
                    "${createLink(controller: 'shift', action: 'ajaxCloseShift')}",
                    "${createLink(controller: 'shift', action: 'ajaxSpotCheck')}",
                    "${createLink(controller: 'shift', action: 'ajaxCashUpdateModal')}",
                    "${createLink(controller: 'shift', action: 'ajaxSaveCashUpdate')}"
                );
                getSafeSessions();
                $("#messages-container").html('');
            });

        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Safe Management</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="shifts-container" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Safe Management</h2>
            </div>

            <div id="messages-container"></div>

            <div id="results-container" class="align-content-center">
                <g:render template="safeSessionViewerResults" />
            </div>
        </section>

        <section id="shift-modal" class="container-fluid">
            <!-- Cash modal -->
            <div class="modal fade" id="sessionModal" tabindex="-1" role="dialog" aria-labelledby="cashModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl" role="document">
                    <div id="modal-content" class="modal-content"></div>
                </div>
            </div>
        </section>
    </body>
</html>