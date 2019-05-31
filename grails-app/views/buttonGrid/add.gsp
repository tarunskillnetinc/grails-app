<!doctype html>

<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy Button Grids</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <g:hasErrors bean="${buttonGrid}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${buttonGrid}" as="list" />
        </div>
    </g:hasErrors>

    <div class="col-12 col-lg-6 offset-lg-3">
        <g:form name="save-button" action="save">
            <g:hiddenField name="type" value="OTHER" />

            <div class="form-group row">
                <label for="description" class="col-3 col-form-label">Description</label>
                <div class="col-7">
                    <g:textField name="description" maxlength="45" value="${buttonGrid?.description}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row">
                <label for="rows" class="col-3 col-form-label">Number of rows</label>
                <div class="col-2">
                    <g:field type="number" min="1" max="4" name="rows" value="${buttonGrid?.rows ?: 4}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row">
                <label for="columns" class="col-3 col-form-label">Number of columns</label>
                <div class="col-2">
                    <g:field type="number" min="1" max="4" name="columns" value="${buttonGrid?.columns ?: 2}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-8 offset-3">
                    <g:link controller="tillSettings" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                    <g:submitButton class="btn btn-success" name="save" value="Save" />
                </div>
            </div>
        </g:form>
    </div>
</body>
</html>