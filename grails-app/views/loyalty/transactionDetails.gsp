<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Transaction Details</title>

        <asset:javascript src="jquery.js" />
        <asset:javascript src="jquery-ui.js" />
    </head>
    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link action="loyaltyMembers">Transaction Details</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${transaction?.transactionId ?: "Transaction Details"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="details-header" class="container-fluid">
            <div class="row header-wl mt-3">

                <div class="col-6 offset-3">
                    <h2 id="page-title" class="mx-auto my-auto">Transaction Details</h2>
                </div>
            </div>
        </section>

        <section>
            <div class="card-body pt-5">
                <div class="row">
                    <div class="col-12 col-lg-5 offset-lg-1">
                        <div class="row form-group mb-3">
                            <label for="storeId" class="col-4 col-form-label text-right pr-4">Store Id</label>
                            <g:textField name="storeId" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.storeId ?: 0}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="storeName" class="col-4 col-form-label text-right pr-4">Store Name</label>
                            <g:textField name="storeName" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.store?.name ?: ''}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="offerDescription" class="col-4 col-form-label text-right pr-4">Offer Description</label>
                            <g:textField name="offerDescription" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.redeemedOffer?.loyaltyOffer?.offerDescription ?: ''}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="transactionDate" class="col-4 col-form-label text-right pr-4">Transaction Date</label>
                            <g:textField name="transactionDate" class="col-5 form-control bottom-border add-product-desc" value="${transaction?.transactionTimestamp ? DateTimeFormat.forPattern('hh:mm:ss dd/MM/yyyy').print(transaction.transactionTimestamp) : ''}" readonly="true" />
                        </div>

                    </div>
                    <div class="col-12 col-lg-6">
                        <div class="row form-group mb-3">
                            <label for="transactionTotal" class="col-4 col-form-label text-right pr-4">Transaction Total</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="transactionTotal" class="col-3 form-control bottom-border add-product-desc" value="${String.format("%.2f", transaction?.transactionTotal ?: 0.00)}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="totalDiscount" class="col-4 col-form-label text-right pr-4">Total Discount</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="totalDiscount" class="col-3 form-control bottom-border add-product-desc" value="${String.format("%.2f", transaction?.transactionDiscount ?: 0.00)}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="amountSaved" class="col-4 col-form-label text-right pr-4">Amount Saved Per Offer</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="amountSaved" class="col-3 form-control bottom-border add-product-desc" value="${transaction?.redeemedOffer?.awardValue != null ? String.format("%.2f", transaction.redeemedOffer.awardValue) : ''}" readonly="true" />
                        </div>

                    </div>
                </div>
            </div>
        </section>
    </body>
</html>