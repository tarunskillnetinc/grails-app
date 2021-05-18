<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane</title>
</head>
<body>
    <g:if test="${flash.message}">
        <div class="alert alert-danger alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <form action="${postUrl ?: '/login/authenticate'}" method="POST" id="loginForm" class="cssform" autocomplete="off">
        <div class="row">
            <div class="col-12 col-md-6 offset-md-3">
                <div class="header-wl mt-5">
                    <h2>Please log in</h2>
                </div>

                <div class="form-group row mt-4">
                    <label for="storeId" class="col-5 col-md-3 offset-md-1 col-form-label text-right">Store Number</label>
                    <div class="col-7 col-md-4">
                        <input type="number" class="text_ form-control bottom-border no-number-arrows" max="9999999999" maxlength="10" name="storeId" id="storeId" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="username" class="col-5 col-md-3 offset-md-1 col-form-label text-right">Username</label>
                    <div class="col-7 col-md-4">
                        <input type="text" class="text_ form-control bottom-border" maxlength="45" name="${usernameParameter ?: 'username'}" id="username" />
                    </div>
                </div>

                <div class="form-group row">
                    <label for="password" class="col-5 col-md-3 offset-md-1 col-form-label text-right">Password</label>
                    <div class="col-7 col-md-4">
                        <input type="password" class="text_ form-control bottom-border" maxlength="45" name="${passwordParameter ?: 'password'}" id="password"/>
                    </div>
                </div>

                <div class="form-group row mt-4">
                    <div class="col-12 col-md-4 offset-md-4">
                        <g:submitButton class="btn btn-wl" name="save" value="Log in" />
                    </div>
                </div>
            </div>
        </div>
    </form>
</body>
</html>