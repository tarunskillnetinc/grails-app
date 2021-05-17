<div class="row form-group mb-4">
    <label for="storeIdentifier" class="col-3 offset-1 col-form-label text-right">Store Identifier</label>

    <div class="input-group col-4">
        <g:textField name="storeIdentifier" value="${symbolGroupSubscription?.storeIdentifier}" class="form-control bottom-border" />
    </div>
</div>

<g:hiddenField name="organisationIdentifier" value="${symbolGroupSubscription?.organisationIdentifier}" />

<div class="row form-group mb-4">
    <label for="username" class="col-3 offset-1 col-form-label text-right">Username</label>

    <div class="input-group col-4">
        <g:textField name="username" value="${symbolGroupSubscription?.username}" class="form-control bottom-border" />
    </div>
</div>

<div class="row form-group mb-4">
    <label for="password" class="col-3 offset-1 col-form-label text-right">Password</label>

    <div class="input-group col-4">
        <g:passwordField name="password" value="${symbolGroupSubscription?.password}" class="form-control bottom-border" />
    </div>
</div>

<g:hiddenField name="lastProductDownload" value="${symbolGroupSubscription?.lastProductDownload}" />
<g:hiddenField name="lastPromotionDownload" value="${symbolGroupSubscription?.lastPromotionDownload}" />

<div class="row form-group mb-4">
    <label for="additionalPassword" class="col-3 offset-1 col-form-label text-right">FTP Password</label>

    <div class="input-group col-4">
        <g:passwordField name="additionalPassword" value="${symbolGroupSubscription?.additionalPassword}" class="form-control bottom-border" placeholder="(Optional)" />
    </div>
</div>

<g:hiddenField name="status" value="${symbolGroupSubscription?.status}" />
<g:hiddenField name="error" value="${symbolGroupSubscription?.error}" />

<div class="row mt-5 col-10 offset-1">
    <div><strong>What is a Nisa supplier affiliation?</strong></div>
    <div>A supplier affiliation allows you to receive product and promotion files from Nisa which is updated by them on an ongoing basis. You can also place orders directly with Nisa using the WonderLane inventory management app.</div>
</div>

<div class="row mt-3 col-10 offset-1">
    <div><strong>How do I sign up to a Nisa supplier affiliation?</strong></div>
    <div>You need to have an existing affiliation with Nisa and valid Nisa login credentials to enter on this page. Save these and your affiliation will be added.</div>
</div>

<div class="row mt-3 col-10 offset-1">
    <div><strong>How much does a Nisa supplier affiliation cost?</strong></div>
    <div>Nothing! Adding your credentials and signing up to receive product and promotion updates are included in your WonderLane POS package at no extra cost.</div>
</div>

<div class="row mt-3 col-10 offset-1">
    <div><strong>How do I cancel a Nisa supplier affiliation?</strong></div>
    <div>You can cancel your Nisa supplier affiliation by pressing the "Remove" button.</div>
</div>

<div class="row mt-3 col-10 offset-1">
    <div><strong>Notes</strong></div>
    <ul class="mt-2">
        <li>
            <div>Placing an order with Nisa using the WonderLane inventory management app and receiving the "Order Confirmed" message means that your order has been sent, but you still need to proceed to the Nisa portal to submit your order. All orders you place with Nisa will be subject to Nisa’s own terms and charges. For more information on these terms, please consult your own Nisa documentation, available via <a href="https://www.ntorder.com" target="_blank">www.ntorder.com</a> or as otherwise notified to you directly by Nisa. Otherwise, if you have any queries regarding your order, please contact Nisa.</div>
        </li>
        <li>
            <div>If you have a specific sharing arrangement with Nisa, your sales data will be passed to Nisa where you have a Nisa supplier affiliation running, and you have added the appropriate sales sign on credentials provided to you by Nisa (FTP password) on this screen.</div>
        </li>
    </ul>
</div>