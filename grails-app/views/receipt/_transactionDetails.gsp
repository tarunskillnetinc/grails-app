<div class="modal-header">
    <h2>Transaction Details</h2>
</div>
<div class="container">
    <div class="row">
        <div class="col-md-4">
            <h4>Transaction Reference:</h4>

            <div>
                <div><span>Transaction Number:</span> <span>${receipt.transactionId}</span></div>

                <div><span>Transaction Date &amp; Time:</span> <span>${receipt.dateGenerated}</span></div>

                <div><span>Store:</span> <span>${store.config.storeName} - ${store.config.storeNumber}</span></div>

                <div><span>Till:</span> <span>${receipt.tillId}</span></div>
            </div>
        </div>

        <div class="col-md-4">
            <h4>Operator Details:</h4>

            <div>
                <div><span>Operator:</span> <span>${user.retailerUserId}</span></div>

                <div><span>Operator Name:</span> <span>${user.name}</span></div>

                <div><span>Operator Role:</span> <span>${user.transactionId}</span></div>
            </div>
        </div>

        <div class="col-md-4">
            <h4>Transaction:</h4>

            <div>
                <div><span>Transaction Type:</span> <span>${receipt.paymentMethod}</span></div>

                <div><span>Transaction Status:</span> <span>UNKNOWN</span></div>

                <div><span>Transaction Total:</span> <span>${receipt.transactionAmount}</span></div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Transaction Additional Details</h4>

            <div><span>Loyalty Number:</span> <span>${user.name}</span></div>

            <div><span>Discount Card:</span> <span>${user.name}</span></div>
        </div>
    </div>

    <div class="row">
        <h4>Transaction Details:</h4>
        <!-- TODO individual lines go here -->
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Transaction Discount Totals</h4>

            <div><span>Discount:</span> <span>${user.name}</span></div>

            <div><span>Promotions Savings:</span> <span>${user.name}</span></div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4>Tender Details</h4>
            <!-- TODO individual lines go here -->
        </div>
    </div>
</div>

%{--<div class="receipt">--}%
%{--    <g:each in="${receipt.receiptLines?.sort { it.id }}" var="receiptLine">--}%
%{--        <g:receiptLine receiptLine="${receiptLine}" containsModifiers="${containsModifiers}"--}%
%{--                       duplicate="${receipt.isPrinted()}"--}%
%{--                       firstHorizontalLine="${firstHorizontalLineId == receiptLine.id}"--}%
%{--                       maxTotalLength="${maxTotalLength}" maxVatLength="${maxVatLength}"/>--}%
%{--    </g:each>--}%
%{--</div>--}%

<div class="modal-footer">
    <button type="button" id="cancelButton" class="btn btn-secondary" data-dismiss="modal"
            style="margin-left: 15px; float: left;">Close</button>
</div>