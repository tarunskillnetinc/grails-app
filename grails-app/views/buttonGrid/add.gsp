<!doctype html>

<html>
<head>
    <meta name="layout" content="main" />

    <title>Button Grids</title>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active">Button Grids</li>
                        <g:if test="${buttonGrid?.id && buttonGrid?.type?.name() != 'OTHER'}">
                            <li id="breadcrumb-3" class="breadcrumb-item"><g:link action="show" params="[type: buttonGrid?.type]"><g:message code="ButtonGridType.${buttonGrid?.type}" /></g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${buttonGrid?.id ? 'Edit' : 'Add'} Button Grid</li>
                        </g:if>
                        <g:elseif test="${buttonGrid?.id && buttonGrid?.type?.name() == 'OTHER'}">
                            <li id="breadcrumb-3" class="breadcrumb-item"><g:link action="show" id="${buttonGrid?.id}">${buttonGrid?.description}</g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${buttonGrid?.id ? 'Edit' : 'Add'} Button Grid</li>
                        </g:elseif>
                        <g:else>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${buttonGrid?.id ? 'Edit' : 'Add'} Button Grid</li>
                        </g:else>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <g:hasErrors bean="${buttonGrid}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${buttonGrid}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <div class="header-wl mt-3">
        <h2 class="mx-auto">${buttonGrid?.id ? 'Edit' : 'Add'} Button Grid</h2>
    </div>

    <div class="col-12 col-lg-6 offset-lg-3">
        <g:form name="save-button" action="save">
            <g:hiddenField name="id" value="${buttonGrid?.id ?: ''}" />
            <g:hiddenField name="type" value="${buttonGrid?.type ?: 'OTHER'}" />

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
                    <g:link elementId="cancel-btn" controller="store" action="index" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

                    <g:submitButton class="btn btn-success" name="save" value="Save" />
                </div>
            </div>
        </g:form>
    </div>
</body>
</html>