<div class="modal-header">
    <h2>Suppliers &amp; Packs</h2>
</div>

<div class="modal-body">


    <g:if test="${variant?.hasErrors}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:each in="${variant?.packs}" var="pack" status="i">
                    <g:hasErrors bean="${pack}">
                        <div class="ml-3 pl-3">
                            Pack ${i+1}
                            <g:renderErrors bean="${pack}" as="list" />
                        </div>
                    </g:hasErrors>
                </g:each>
            </div>
        </section>
    </g:if>



    <div id="defaultSupplierForm">
        <div class="row mx-4 pt-3 pb-2">
            <label for="defaultSupplier" class="col-10 col-form-label text-right">Default Supplier</label>

            <div class="col-2 px-0">
                <g:select name="defaultSupplier" from="${suppliers}"
                          noSelection="['': '']" value="${defaultSupplier}"
                          optionValue="name" optionKey="id"
                          class="form-control select-border"/>
            </div>
        </div>
    </div>

    <div class="row mx-4 pt-5 pb-2 table-wl bottom-border">
        <div class="col-3 font-weight-bold">Supplier</div>
        <div class="col-2 font-weight-bold">Pack Quantity</div>
        <div class="col-2 font-weight-bold">Cost Price</div>
        <div class="col-2 font-weight-bold">Order Code</div>
        <div class="col-2 font-weight-bold">Outer Barcode</div>
        <div class="col-1 font-weight-bold">&nbsp;</div>
    </div>

    <div id="addPacksContainer-${variantIndex}">
        <g:each in="${variant.packs}" var="pack" status="i">
            <div id="addPackContainer-${variantIndex}-${i}">
                <g:render template="addPack" model="[variantIndex: variantIndex, productVariantId: variant.productVariantId, packIndex: i, pack: pack, isNewPack: pack?.isNewPack, suppliers: suppliers]" />
            </div>
        </g:each>
    </div>

    <div class="row mx-4 mt-3">
        <a id="add-pack-btn" href="#" onclick="addPack(${variantIndex}, null, ${variant.productVariantId});" class="btn btn-wl">Add Pack</a>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddVariantButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="savePacks(${variantIndex});">Ok</button>
</div>

<asset:javascript src="validators/input-validator.js" />