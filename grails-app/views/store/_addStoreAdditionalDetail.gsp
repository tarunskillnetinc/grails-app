<section id="modal-header">
  <div class="modal-header">
    <h2 id="page-title" class="mx-auto my-auto">Store Additional Detail</h2>
  </div>
</section>
<section id="modal-error">
</section>
<section id="modal-form">
  <g:form name="addListItemForm">

    <script>
      $(document).ready(function () {
        $('#addListItemForm').bind('keydown', function (e) {
          if (e.keyCode == 13) {
            $('#saveAddSupplierButton').click();
            e.preventDefault()
          }
        });
      });
    </script>

    <div class="row form-group mb-4 mt-4">
      <div class="col-5">
        <div class="row">
          <label for="description" class="col-4 col-form-label-mandatory text-right">Description</label>
          <div class="col-8">
            <g:field type="text" id="description" name="description" class="form-control select-border" maxlength="20" />
          </div>
        </div>
      </div>

      <div class="col-7">
        <div class="row">
          <label id="value" for="value" class="col-3 col-form-label text-right pr-4">Value</label>
          <div class="col-9 pr-5">
            <g:textArea id="value" name="value" class="form-control select-border" value="" rows="6" style="min-height: 150px;" />
          </div>
        </div>
      </div>
    </div>
  </g:form>

  <div class="modal-footer">
    <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAdditionalDetailAddModal();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveListItem();">Save</button>
  </div>
</section>