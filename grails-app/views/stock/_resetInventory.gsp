<script>
    $(document).ready(function () {
        $('input[name="storeNumber"], input[name="username"]').on('input', function () {
            const store = $('input[name="storeNumber"]').val().trim();
            const user = $('input[name="username"]').val().trim();
            $('#continueBtn').prop('disabled', !(store && user));
        });
    });

    $(document).ready(function () {
        $('#resetInventoryForm').on('submit', function(e) {
            e.preventDefault();
            $.ajax({
                url: $(this).attr('action'),
                type: 'POST',
                data: $(this).serialize(),
                success: function(response) {
                    // This will handle the redirect from the controller
                    window.location.href = "${createLink(controller:'stock', action:'index')}";
                },
                error: function(xhr) {
                    alert('Error: ' + xhr.responseText);
                }
            });
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a id="product-list-store-number" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeNumber',
        order: ${sortParams?.sort == 'storeNumber' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Number</a></div>
    <div class="col-6 font-weight-bold"><a id="product-list-store-name" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeName',
        order: ${sortParams?.sort == 'storeName' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });">Store Name</a></div>
    <div class="col-2 font-weight-bold"><a id="product-list-store-type" href="#" onclick="getStores({
        max: ${sortParams?.max},
        offset: ${sortParams?.offset},
        sort: 'storeType',
        order: ${sortParams?.sort == 'storeType' ? sortParams?.order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}
    });"></a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
        <div id="store-result" class="row ml-0 mr-0 pt-2 pb-2 wl-striped hoverable"  style="cursor: pointer;" ">
            <div id="store-number" class="col-2 my-auto">${storeNumber}</div>
            <div id="store-name" class="col-2 my-auto">${storeName}</div>
            <div class="col-3 my-auto text-right">
               <button id="resetInventoryButton" type="button" class="btn btn-danger text-right" data-toggle="modal" data-target="#confirmModal">Reset ALL Stock Figures</button>
            </div>
        </div>


</div>
<div class="modal fade" id="confirmModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content modal-confirm">
            <div class="modal-header badge-danger">WARNING!</div>
            <div class="modal-body" style="word-break: break-all; word-wrap: break-word; margin-left: 15px">
                <p><strong>Warning You have selected to reset the stock levels for all products within<br>
                    ${storeNumber}, ${storeName}</strong>.</p>
                <p><strong>To action the 'Reset Store Inventory' please confirm you wish to continue by entering :</strong></p>

                <g:form controller="stock" action="resetInventory" method="POST" id="resetInventoryForm">
                    <div class="form-group">
                        <label>The Store Number:</label>
                        <g:textField name="storeNumber" class="form-control" required="true"/>
                    </div>
                    <div class="form-group">
                        <label>Enter Your Username:</label>
                        <g:textField name="username" class="form-control" required="true"/>
                    </div>
                    <button type="submit" class="btn btn-secondary" style="margin-left: 15px; float: left;" id="continueBtn" disabled>Continue</button>
                </g:form>
            </div>
        </div>
    </div>
</div>
