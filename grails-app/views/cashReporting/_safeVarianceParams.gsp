<div class="col-12">
    <div class="card bg-light border-wl">
        <div id="reportParams" class="card-body">
            <g:form id="reportParams" name="reportParams" >
                <div class="form-group row">
                    <div class="row col-xl-7 col-12 mb-4">
                        <label for="storeIdSelect" class="col-2 col-form-label-sm text-right">Store ID:</label>
                        <div class="col-2">
                            <g:select id="storeIdSelect"
                                      name="storeIdSelect"
                                      from="${stores}"
                                      optionValue="${{it.config.storeNumber}}"
                                      optionKey="${{it.config.storeNumber}}"
                                      noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': 'Please Select'] : ['': 'Please Select']}"
                                      value="${sec.loggedInUserInfo(field: 'storeId') ? sec.loggedInUserInfo(field: 'storeNumber') : ''}"
                                      class="form-control select-border text-truncate"
                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}">
                            </g:select>
                        </div>
                        <label for="safeId" class="col-3 col-form-label-sm text-right">Safe:</label>
                        <div class="dropdown col-5">
                            <div class="form-control" id="safes">
                                <span id="selectedSafes" style="display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">Select Safes</span>
                                <span class="caret"></span>
                            </div>
                            <div id="safeIdSelect" class="dropdown-menu">
                                <g:each in="${safes}" var="safe">
                                    <div class="dropdown-item">
                                        <label class="mb-0">
                                            <input id="${safe.id}" type="checkbox" name="safes" value="${safe.description}"> ${safe.description}${safe.active ? '' : ' - (Inactive Safe)'}</input>
                                        </label>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                    <div class="row col-xl-5 col-12 mb-4">
                        <label for="startDate" class="col-3 col-form-label-sm text-right">From Date:</label>
                        <div class="col-4">
                            <g:textField id="startDate" name="startDate" onkeydown="return false" class="form-control bottom-border text-center" value="${startDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                        </div>

                        <label for="endDate" class="col-2 col-form-label-sm text-right">To Date:</label>
                        <div class="col-3">
                            <g:textField id="endDate" name="endDate" onkeydown="return false" class="form-control bottom-border text-center" value="${endDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
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