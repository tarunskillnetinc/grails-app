<div class="col-12">
    <div class="card bg-light border-wl">
        <div id="reportParams" class="card-body">
            <g:form id="reportParams" name="reportParams" >
                <div class="form-group row">

                    <div class="row col-xl-5 col-12 mb-4">
                        <label for="storeIdSelect" class="col-2 col-form-label-sm text-right">Store ID:</label>
                        <div class="col-4">
                            <g:select id="storeIdSelect" name="storeIdSelect" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                      optionKey="id"
                                      noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                      class="form-control select-border"
                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                        </div>

                        <label for="tillId" class="col-2 col-form-label-sm text-right">Till ID:</label>
                        <div class="col-4">
                            <g:select id="tillIdSelect" name="tillIdSelect" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                      optionKey="id"
                                      noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                      class="form-control select-border"
                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                        </div>
                    </div>

                    <div class="row col-xl-5 col-12 mb-4">
                        <label for="startDate" class="col-2 col-form-label-sm text-right">From Date:</label>
                        <div class="col-4">
                            <g:textField id="startDate" name="startDate" onkeydown="return false" class="form-control bottom-border text-center" value="${startDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                        </div>

                        <label for="endDate" class="col-2 col-form-label-sm text-right">To Date:</label>
                        <div class="col-4">
                            <g:textField id="endDate" name="endDate" onkeydown="return false" class="form-control bottom-border text-center" value="${endDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                        </div>
                    </div>

                    <div class="row col-xl-2 col-12 mb-4">
                        <label for="shiftNumber" class="col-xl-5 col-2 col-form-label-sm text-right">Shift no.:</label>
                        <div class="col-xl-7 col-4">
                            <g:select id="shiftNumberSelect" name="shiftNumberSelect" from="${stores}" optionValue="${{it.config.storeNumber}}" optionKey="id"
                                      noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                      class="form-control select-border"
                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                        </div>
                    </div>

                </div>

                <div class="form-group row">
                    <div class="col-12 text-right">
                        <button id="get-report-button" type="button" class="col-xl-3 col-5 btn btn-wl text-center" onclick="getReport()">Get Report</button>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
</div>