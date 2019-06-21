<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy</title>
</head>
<body>
    <g:if test="${flash.message}">
        <div class="alert alert-danger alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <form action="${postUrl ?: '/login/authenticate'}" method="POST" id="loginForm" class="cssform" autocomplete="off">
        <div class="row">
            <div class="col-12 col-md-6 offset-md-3">
                <div class="header-wl">
                    <h2>Please log in</h2>
                </div>

                <div class="form-group row">
                    <label for="storeId" class="col-5 col-md-2 offset-md-2 col-form-label">Store number</label>
                    <div class="col-7 col-md-4">
                        <input type="number" class="text_ form-control bottom-border no-number-arrows" max="9999999999" maxlength="10" name="storeId" id="storeId" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="username" class="col-5 col-md-2 offset-md-2 col-form-label">Username</label>
                    <div class="col-7 col-md-5">
                        <input type="text" class="text_ form-control bottom-border" maxlength="45" name="${usernameParameter ?: 'username'}" id="username" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="password" class="col-5 col-md-2 offset-md-2 col-form-label">Password</label>
                    <div class="col-7 col-md-5">
                        <input type="password" class="text_ form-control bottom-border" maxlength="45" name="${passwordParameter ?: 'password'}" id="password"/>
                    </div>
                </div>

                <div class="form-group row">
                    <div class="col-12 col-md-5 offset-md-4">
                        <g:submitButton class="btn btn-success" name="save" value="Log in" />
                    </div>
                </div>
            </div>
        </div>
    </form>
</body>
</html>