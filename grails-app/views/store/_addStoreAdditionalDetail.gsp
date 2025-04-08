<section id="modal-header">
  <div class="modal-header">
    <h2 id="page-title" class="mx-auto my-auto">Add Additional Details</h2>
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

        // Add event listener for the description field
        $("#addStoreAdditionalDetailDescription").on('input', function() {
          // Clear error message when user starts typing
          $("#additional-details-errors-container").empty().hide();
          // Remove invalid class from the input
          $(this).removeClass("is-invalid");
        });

      });
    </script>

    <section id="additional-details-errors-container" class="container-fluid"></section>

    <div class="row form-group mb-4 mt-4">
      <g:hiddenField name="addStoreAdditionalDetailIndex" value="${index}" />
      <div class="col-5">
        <div class="row">
          <label for="addStoreAdditionalDetailDescription" class="col-4 col-form-label-mandatory text-right">Description</label>
          <div class="col-8">
            <g:field type="text" id="addStoreAdditionalDetailDescription" name="addStoreAdditionalDetailDescription" class="form-control select-border" maxlength="20" value="${description}"/>
          </div>
        </div>
      </div>

      <div class="col-7">
        <div class="row">
          <label id="value" for="addStoreAdditionalDetailValue" class="col-3 col-form-label text-right pr-4">Value</label>
          <div class="col-9 pr-5">
            <g:textArea id="addStoreAdditionalDetailValue" name="addStoreAdditionalDetailValue" class="form-control select-border" maxlength="240" value="${value}" rows="6" style="min-height: 150px;" />
          </div>
        </div>
      </div>
    </div>
  </g:form>

  <div class="modal-footer">
    <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeStoreAdditionalDetailAddModal();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveStoreAdditionalDetail();">Save</button>
  </div>
</section>