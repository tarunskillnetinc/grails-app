<!doctype html>

<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Button Grids</title>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li class="breadcrumb-item active" aria-current="page">Button Grids</li>
                        <li class="breadcrumb-item active" aria-current="page">Add Button Grid</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <g:hasErrors bean="${buttonGrid}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${buttonGrid}" as="list" />
        </div>
    </g:hasErrors>

    <div class="row header-wl mt-3">
        <h2 class="mx-auto">Add Button Grid</h2>
    </div>

    <div class="col-12 col-lg-6 offset-lg-3">
        <g:form name="save-button" action="save">
            <g:hiddenField name="type" value="OTHER" />

            <div class="form-group row margin-top-2rem">
                <label for="description" class="col-3 col-form-label">Description</label>
                <div class="col-7">
                    <g:textField name="description" maxlength="45" value="${buttonGrid?.description}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row margin-top-2rem">
                <label for="rows" class="col-3 col-form-label">Number of rows</label>
                <div class="col-2">
                    <g:field type="number" min="1" max="4" maxlength="1" name="rows" value="${buttonGrid?.rows ?: 4}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row margin-top-2rem">
                <label for="columns" class="col-3 col-form-label">Number of columns</label>
                <div class="col-2">
                    <g:field type="number" min="1" max="4" maxlength="1" name="columns" value="${buttonGrid?.columns ?: 2}" class="form-control bottom-border" />
                </div>
            </div>

            <div class="form-group row margin-top-2rem">
                <div class="col-8 offset-3">
                    <g:link controller="storeSettings" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                    <g:submitButton class="btn btn-success" name="save" value="Save" />
                </div>
            </div>
        </g:form>
    </div>
</body>
</html>