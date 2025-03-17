<div class="modal-header">
    <h2>${isUpdate ? "Edit Supplier" : "Add Supplier"}</h2>
</div>
<div class="modal-body">
    <script>
        $("#caserateeffectivedate").datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            todayHighlight: true,
            autoclose: true,
            startDate: new Date(),
            todayBtn: "linked",
            orientation: "top auto"
        }).on('changeDate', function (selected) {
            let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
            let formatted = formatDate(selected.date, options, '-');

            $("#caserateeffectivedate").val(formatted);
        });

        $(".mask-money").maskMoney({allowZero: true});
        $(".mask-money").maskMoney('mask');
        $(".denomination").focusout(function () {
            if (!this.value || this.value < 0) {
                this.value = 0;
            }
        })
            .keypress(function (e) {
                if (["e", "E", "+", "-"].includes(e.key)) {
                    e.preventDefault();
                }
            });
    </script>

    <section id="errors-container" class="container-fluid">
        <g:hasErrors bean="${supplier}">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${supplier}" as="list"/>
            </div>
        </g:hasErrors>
        <g:hasErrors bean="${supplierCaseRate}">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${supplierCaseRate}" as="list"/>
            </div>
        </g:hasErrors>
    </section>

    <div id="js-errors-container" class="container-fluid" hidden>
        <div class="alert alert-danger alert-wl mx-0" role="alert">
            <div id="validation-errors"></div>
        </div>
    </div>

    <g:form name="addSupplierForm">
        <g:hiddenField name="id" value="${supplier?.id}" />

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="name" class="col-5 m-0 offset-1 col-form-label text-right">Supplier Name</label>

                    <div class="input-group col-6">
                        <g:textField id="suppliername" name="name" value="${supplier?.name}"
                                     class="form-control bottom-border"
                                     maxlength="60"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressBuildingNumberOrName"
                           class="col-5 m-0 offset-1 col-form-label text-right">Building Number or Name</label>

                    <div class="input-group col-6">
                        <g:textField name="addressBuildingNumberOrName" value="${supplier?.addressBuildingNumberOrName}"
                                     maxlength="40"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="reference"
                           class="col-5 m-0 offset-1 col-form-label text-right">Supplier Reference</label>

                    <div class="input-group col-6">
                        <g:textField id="supplierreference" name="reference" value="${supplier?.reference}"
                                     maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressLine1"
                           class="col-5 m-0 offset-1 col-form-label text-right">Address Line 1</label>

                    <div class="input-group col-6">
                        <g:textField name="addressLine1" value="${supplier?.addressLine1}" maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="contactName" class="col-5 m-0 offset-1 col-form-label text-right">Contact Name</label>

                    <div class="input-group col-6">
                        <g:textField name="contactName" value="${supplier?.contactName}" maxlength="40"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressLine2"
                           class="col-5 m-0 offset-1 col-form-label text-right">Address Line 2</label>

                    <div class="input-group col-6">
                        <g:textField name="addressLine2" value="${supplier?.addressLine2}" maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="phoneNumber"
                           class="col-5 m-0 offset-1 col-form-label text-right">Contact Telephone</label>

                    <div class="input-group col-6">
                        <g:textField name="phoneNumber" id="phoneNumber" value="${supplier?.phoneNumber}" maxlength="12"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressTown" class="col-5 m-0 offset-1 col-form-label text-right">Town</label>

                    <div class="input-group col-6">
                        <g:textField name="addressTown" value="${supplier?.addressTown}" maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="email" class="col-5 m-0 offset-1 col-form-label text-right">Contact Email</label>

                    <div class="input-group col-6">
                        <g:textField id="email" name="email" type="email" value="${supplier?.email}" maxlength="50"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressCounty" class="col-5 m-0 offset-1 col-form-label text-right">County</label>

                    <div class="input-group col-6">
                        <g:textField name="addressCounty" value="${supplier?.addressCounty}" maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="customerReference"
                           class="col-5 m-0 offset-1 col-form-label text-right">Customer Reference</label>

                    <div class="input-group col-6">
                        <g:textField name="customerReference" value="${supplier?.customerReference}" maxlength="40"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressCountry" class="col-5 m-0 offset-1 col-form-label text-right">Country</label>

                    <div class="input-group col-6">
                        <g:textField name="addressCountry" value="${supplier?.addressCountry}" maxlength="20"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="addressPostCode" class="col-5 m-0 offset-1 col-form-label text-right">Postcode</label>

                    <div class="input-group col-6">
                        <g:textField name="addressPostCode" value="${supplier?.addressPostCode}" maxlength="8"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>
        </div>

        <hr/>

        <h3>Case Rates</h3>
        <g:set var="firstCaseRate" value="${supplierCaseRates?.size() > 0 ? supplierCaseRates.first() : null}"/>

        <div class="row">
            <div class="col-md-6">
                <div class="form-group row">
                    <label for="caserateeffectivedate"
                           class="col-5 m-0 offset-1 col-form-label text-right">Case Rate Effective Date</label>

                    <div class="input-group col-6">
                        <g:textField name="caserateeffectivedate"
                                     value="${firstCaseRate?.caseRateEffectiveDate?.format("dd/MM/yyyy")}"
                                     id="caserateeffectivedate"
                                     class="form-control bottom-border"/>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group row">
                    <label for="caserate"
                           class="col-5 m-0 offset-1 col-form-label text-right">Case Rate</label>

                    <div class="input-group col-6">
                        <div class="input-group-prepend">
                            <span class="input-group-text">&pound;</span>
                        </div>
                        <g:textField name="caserate" value="${firstCaseRate?.caseRate}"
                                     class="form-control bottom-border mask-money"/>
                    </div>
                </div>
            </div>
        </div>

    </g:form>

    <g:if test="${isUpdate}">
        <div id="accordion">
            <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
                <div class="card-header pointer collapsed" id="historicalCaseRates" data-toggle="collapse"
                     data-target="#collapseHistoricalCaseRates" aria-expanded="false"
                     aria-controls="collapseHistoricalCaseRates">
                    <div class="row">
                        <div class="col-10 font-weight-bold">Historical Case Rates</div>

                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                                 fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"></path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div id="collapseHistoricalCaseRates" class="collapse" aria-labelledby="historicalCaseRates"
                     data-parent="#accordion" style="">
                    <div class="col-12">
                        <g:if test="${supplierCaseRates == null || supplierCaseRates?.size() == 0 || supplierCaseRates?.first() == null || supplierCaseRates?.size() == 1}">
                            <div class="m-5">No supplier historical case rates found.</div>
                        </g:if>
                        <g:else>
                            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                                <div class=" col-md-6 font-weight-bold">
                                    Case Rate Effective Date
                                </div>

                                <div class="col-md-6 font-weight-bold">
                                    Case Rate
                                </div>
                            </div>

                            <g:each in="${supplierCaseRates}" var="supplierCaseRate" status="index">
                                <g:if test="${index > 0}">
                                    <div id="suppliercaserate-${index - 1}"
                                         class="row ml-0 mr-0 pt-2 pb-2 wl-striped${index - 1}">
                                        <div class="col-md-6">
                                            ${supplierCaseRate?.caseRateEffectiveDate?.format("dd/MM/yyyy")}
                                        </div>

                                        <div class="col-md-6">
                                            <div class="form-group row">
                                                <div class="input-group col-6 text-right">
                                                    &pound;${String.format("%.2f", supplierCaseRate?.caseRate)}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </g:each>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddSupplierButton" class="btn btn-secondary"
            data-dismiss="modal">Cancel</button>
    <g:if test="${enableSave}">
        <button type="button" id="saveSupplierButton" class="btn btn-success"
                onclick="saveSupplier();">Save</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSupplierButton" class="btn btn-success" disabled
                onclick="saveSupplier();">Save</button>
    </g:else>
</div>
