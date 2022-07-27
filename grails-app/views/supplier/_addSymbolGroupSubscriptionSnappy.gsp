<div class="row form-group mb-4">
    <label for="username" class="col-3 offset-1 col-form-label text-right">Client ID</label>

    <div class="input-group col-4">
        <g:textField name="username" value="${symbolGroupSubscription?.username}" class="form-control bottom-border" />
    </div>
</div>

<div class="row form-group mb-4">
    <label for="password" class="col-3 offset-1 col-form-label text-right">Client Secret</label>

    <div class="input-group col-4">
        <g:passwordField name="password" value="${symbolGroupSubscription?.password}" class="form-control bottom-border" />
    </div>
</div>

<g:hiddenField name="status" value="${symbolGroupSubscription?.status}" />
<g:hiddenField name="error" value="${symbolGroupSubscription?.error}" />

<div class="row mt-3 col-10 offset-1">
    <div><strong>Notes</strong></div>
    <ul class="mt-2">
        <li>
            <div>By submitting this form, this will then proceed to sync all of your snappy enabled products to Snappy</div>
        </li>
    </ul>
</div>