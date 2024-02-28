<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Add Loyalty Offer</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type='text/javascript'>
        $(function() {
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                endDate: new Date().toString(),
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            $('#endDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                endDate: new Date().toString(),
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });

            const currentDate = new Date();
            const oneWeekFromToday = new Date(currentDate);
            oneWeekFromToday.setDate(currentDate.getDate() + 7);

            const formattedDate = oneWeekFromToday.toLocaleDateString('en-GB');
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
                    <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="loyalty" action="loyaltyOffers">Loyalty Offer</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add Loyalty Offer</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="add-user-section" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 class="mx-auto my-auto">Add Loyalty Offer</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel" controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>

            <button id="save" class="btn btn-success" name="save" onclick="$('#add-user-form').submit();">Save</button>
        </div>
    </div>

    <g:if test="${flash.message}">
        <section id="errors-container">
            <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

    <g:hasErrors bean="${user}">
        <section id="errors-container">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${user}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="add-user-form" action="save" novalidate="novalidate" class="mt-4">
        <g:hiddenField name="id" value="${offer?.id ?: 0}" />

        <div class="form-group row col-12 col-lg-6">
            <label for="description" class="col-4 col-form-label text-right pr-4">Offer Description</label>
            <g:textField name="description" class="col-5 form-control bottom-border" value="${user?.username}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="startDate" class="col-4 col-form-label text-right pr-4">Start date</label>
            <g:textField name="startDate" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="endDate" class="col-4 col-form-label text-right pr-4">End date</label>

            <g:textField name="endDate" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="maxRedemptions" class="col-4 col-form-label text-right pr-4">Max Redemptions</label>
            <g:textField name="maxRedemptions" class="col-5 form-control bottom-border" value="${user?.name}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="maxBudget" class="col-4 col-form-label text-right pr-4">Max budget</label>
            <g:passwordField name="maxBudget" class="col-5 form-control bottom-border" value="${user?.password}" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="promotionAssigned" class="col-4 col-form-label text-right pr-4">Promotion assigned</label>
            <g:passwordField name="promotionAssigned" class="col-5 form-control bottom-border" value="${user?.confirmPassword}" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="segmentAssigned" class="col-4 col-form-label text-right pr-4">Segment assigned</label>
            <g:textField name="segmentAssigned" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="status" class="col-4 col-form-label text-right pr-4">Status</label>
            <g:select name="status" class="col-3 form-control select-border" from="${promotions.entrySet()}"
                      valueMessagePrefix="Role" optionKey="key" optionValue="value" />
        </div>


    </g:form>
</section>
</body>
</html>