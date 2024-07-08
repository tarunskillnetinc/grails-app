<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Member Offer Details</title>

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

            window.onload = function() {
                var numericField = document.querySelector('.numeric-field');
                numericField.addEventListener('input', function(event) {
                    if (!/^\d*$/.test(event.target.value)) {
                        event.target.value = event.target.value.replace(/[^\d]/g, '');
                    }
                });
            };

            function validateUpdates() {
                 if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {
                    $('#memberOfferDetails').submit();
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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link action="loyaltyMembers">Membership Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page"><g:link action="showMemberDetails" params="[cardNumber: cardNumber]">${cardNumber}</g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page"><g:link action="offers" params="[cardNumber: cardNumber]">Member Offers</g:link></li>
                            <li id="breadcrumb-5" class="breadcrumb-item active" aria-current="page">${offer?.offerDescription ?: "Offer Details"}</li>
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

        <section id="offer-details" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-6 offset-3">
                    <h2 id="page-title" class="mx-auto my-auto">Member Offer Details</h2>
                </div>
            </div>
        </section>

        <section>
            <div id="validation-errors" class="alert alert-danger alert-wl mx-0" role="alert" hidden></div>

            <g:form method="post" action="memberOfferUpdate" class="mt-4" name="memberOfferDetails">
                <g:hiddenField name="cardNumber" value="${cardNumber}" />
                <g:hiddenField name="offerId" value="${offer?.id}" />
                <div class="card-body pt-5">
                    <div class="row">
                        <div class="col-12 col-lg-5 offset-lg-1">
                            <div class="row form-group mb-3">
                                <label id = "description" for="description" class="col-5 col-form-label text-right pr-4">Description</label>
                                <g:textField name="description" type="text" nullable="true" class="col-3 form-control bottom-border" value="${offer?.offerDescription}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label  id = "currentRedemptions" for="currentRedemptions" class="col-5 col-form-label text-right pr-4">Number Of Times Redeemed</label>
                                <g:textField name="currentRedemptions" type="number" nullable="true" class="col-3 form-control bottom-border" value="${offer?.currentRedemptions}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label id = "remainingRedemptions" for="remainingRedemptions" class="col-5 col-form-label text-right pr-4">Remaining Redemptions</label>
                                <g:textField name="remainingRedemptions" type="number" nullable="true" class="col-3 form-control bottom-border numeric-field" value="${offer?.remainingRedemptions}" />
                            </div>
                            <div class="row form-group mb-3">
                                <label id = "savings" for="savings" class="col-5 col-form-label text-right pr-4">Savings</label>
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField name="savings" type="text" class="col-2 form-control bottom-border mask-money" value="${offer?.currentSavings}" readonly="true" />
                            </div>
                        </div>
                        <div class="col-12 col-lg-6">
                            <div class="row form-group mb-3">
                                <label id = "startDate" for="startDate" class="col-5 col-form-label text-right pr-4">Start Date</label>
                                <g:textField name="startDate" type="text" nullable="true" class="col-3 form-control bottom-border" value="${offer?.startDate ? DateTimeFormat.forPattern('hh:mm:ss dd/MM/yyyy').print(offer?.startDate) : ''}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label id = "endDate" for="endDate" class="col-5 col-form-label text-right pr-4">End Date</label>
                                <g:textField name="endDate" type="text" nullable="true" class="col-3 form-control bottom-border" value="${offer?.endDate ? DateTimeFormat.forPattern('hh:mm:ss dd/MM/yyyy').print(offer?.endDate) : ''}" readonly="true" />
                            </div>
                            <div class="row form-group mb-3">
                                <label id = "status" for="status" class="col-5 col-form-label text-right pr-4">Status</label>
                                <g:select id="status" name="status" from="${['ACTIVE', 'CLOSED', 'LIMITS', 'OPEN']}" valueMessagePrefix="MemberOfferStatus" value="${offer?.status}" class="col-3 form-control select-border" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="tab-content">
                    <div class="row my-5">
                        <g:link elementId="memberOfferUpdate-cancel" action="offers" params="[cardNumber: cardNumber]" class="btn btn-wl col-1 offset-1" onClick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                        <button id="memberOfferUpdateSave" type="button" name="memberOfferUpdate-save-button" onclick="validateUpdates()" class="btn btn-success col-1 offset-8">Save</button>
                    </div>
                </div>
            </g:form>
        </section>
    </body>
</html>