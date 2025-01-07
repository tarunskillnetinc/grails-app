<section id="modal-header">
  <div class="modal-header">
      <h2 id="page-title" class="mx-auto my-auto">Add List Item</h2>
  </div>
</section>
<section id="modal-error">
</section>
<section id="modal-form">
<g:form name="addListItemForm">
  <!-- Hidden field for id -->
  <g:hiddenField name="attributeId" value="${attributeId}" />

  <!-- Integer field for startIndex -->
  <div class="row form-group mb-4 mt-4">
    <label for="itemName" class="col-3 offset-1 col-form-label-mandatory text-right">Name</label>
    <div class="col-4">
      <div class="input-group number-box">
        <g:field type="text" id="itemName" name="itemName" class="form-control select-border" maxlength="50" />
      </div>
    </div>
  </div>
</g:form>
</div>

<div class="modal-footer">
  <button type="button" id="closeListItemModal" class="btn btn-wl" onclick="closeModal();">Cancel</button>
  <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveListItem();">Save</button>
</div>
</section>
