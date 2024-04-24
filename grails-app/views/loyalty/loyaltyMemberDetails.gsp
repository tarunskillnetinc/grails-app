<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Member Details</title>

        <asset:javascript src="jquery.js" />
        <asset:javascript src="jquery-ui.js" />
        <asset:javascript src="money-mask.js" />
        <asset:stylesheet src="jquery-ui.css" />

        <script type='text/javascript'>
            $(document).ready(function () {
                $(".mask-money").maskMoney({ allowZero: true });
                $(".mask-money").maskMoney('mask');
                $('.mask-money').maskMoney('destroy');
            });

            function validateMobileNumber(number) {
                const regex = /^(07\d{9})$/;

                if (regex.test(number)) {
                    return true;
                } else {
                    return false;
                }
            }

            function validateEmail(email) {
                const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

                if (regex.test(email)) {
                    return true;
                } else {
                    return false;
                }
            }

            function validateUpdates() {
                 if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {        
                    let error = false;
                    let errorString = "";

                    if ($('#email').val() !== "")  {
                        let email = $('#email').val();

                        if (!validateEmail(email)) {
                            error = true;
                            errorString = errorString.concat("\n<li>Please enter a valid email address</li>");
                        }
                    }

                    if ($('#mobile_no').val() !== "")  {
                        let mobile = $('#mobile_no').val();

                        if (!validateMobileNumber(mobile)) {
                            error = true;
                            errorString = errorString.concat("\n<li>Please enter a valid mobile number</li>");
                        }
                    }

                    if (!error) {
                        $('#memberDetails').submit();
                    } else {
                        $('#validation-errors').html("<ul>" + errorString + "\n</ul>");
                        $('#validation-errors').prop("hidden", false);
                    }
                }
            }
        </script>
    </head>
    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link action="loyaltyMembers">Membership Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${member?.cardNumber ?: "Member Details"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <g:if test="${flash.message}">
            <section id="alerts-container" class="container-fluid">
                <div id="alerts-container-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section>
        </g:if>

        <section id="maintenance-search" class="container-fluid">
            <div class="row header-wl mt-3">

                <div class="col-6 offset-3">
                    <h2 id="page-title" class="mx-auto my-auto">Member Details</h2>
                </div>

                <div class="col-3 text-right d-inline-flex flex-row justify-content-end">
                    <g:link elementId="add-new-product-btn" action="transactions" params="[cardNumber: member?.cardNumber]" class="btn btn-wl p-2 ml-2">Member Transactions</g:link>
                    <g:link elementId="add-new-product-btn" action="offers" params="[cardNumber: member?.cardNumber]" class="btn btn-wl p-2 ml-2">Available Offers</g:link>
                </div>
            </div>
        </section>

        <section>
            <div id="validation-errors" class="alert alert-danger alert-wl mx-0" role="alert" hidden></div>

            <g:form method="post" action="memberUpdateSave" class="mt-5" name="memberDetails">
                <div class="card-body pt-5">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group mb-3">
                                <label for="firstName" class="col-4 col-form-label text-right pr-4">First Name</label>
                                <g:textField name="firstName" type="text" maxLength="50" nullable="true" class="col-5 form-control bottom-border" value="${member?.firstName}" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="lastName" class="col-4 col-form-label text-right pr-4">Last Name</label>
                                <g:textField name="lastName" type="text" maxLength="50" nullable="true" class="col-5 form-control bottom-border" value="${member?.lastName}" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="email" class="col-4 col-form-label text-right pr-4">Email Address</label>
                                <g:textField name="email" class="col-7 form-control bottom-border add-product-desc" value="${member?.email}" required="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="mobile_no" class="col-4 col-form-label text-right pr-4">Mobile Number</label>
                                <g:textField name="mobile_no" class="col-7 form-control bottom-border add-product-desc" value="${member?.mobile_no}" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="cardNumber" class="col-4 col-form-label text-right pr-4">Membership Number</label>
                                <g:field name="cardNumber" type="text" minLength="8" maxLength="25" class="col-5 form-control bottom-border" value="${member?.cardNumber}" readonly="true" />
                            </div>
                        </div>
                        <div class="col-12 col-lg-6">
                            <div class="row form-group mb-3">
                                <label for="offersAvailable" class="col-4 col-form-label text-right pr-4">Offers Available</label>
                                <g:field name="offersAvailable" type="text" class="col-2 form-control bottom-border" value="${member?.offersAvailable}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="currentSpend" class="col-4 col-form-label text-right pr-4">Current Spend This Year</label>
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:field name="currentSpend" type="text" class="col-2 form-control bottom-border mask-money" value="${member?.currentSpend}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="currentSavings" class="col-4 col-form-label text-right pr-4">Current Savings This Year</label>
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:field name="currentSavings" type="text" class="col-2 form-control bottom-border mask-money" value="${member?.currentSavings}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="currentPoints" class="col-4 col-form-label text-right pr-4">Current Points Balance</label>
                                <g:field name="currentPoints" type="text" class="col-2 form-control bottom-border" value="${member?.currentPoints}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label for="lastActivity" class="col-4 col-form-label text-right pr-4">Last Activity</label>
                                <g:field name="lastActivity" type="text" class="col-3 form-control bottom-border" value="${member?.lastTransaction}" readonly="true" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="tab-content">
                    <div class="row my-5">
                        <g:link elementId="memberUpdate-cancel" action="loyaltyMembers" class="btn btn-wl col-1 offset-1" onClick="return confirm('Any unsaved changes will be lost, are you sure you wish to continue?');">Cancel</g:link>
                        <button id="memberUpdateSave" type="button" name="memberUpdate-save-button" onclick="validateUpdates()" class="btn btn-success col-1 offset-8">Save</button>
                    </div>
                </div>
            </g:form>
        </section>
    </body>
</html>