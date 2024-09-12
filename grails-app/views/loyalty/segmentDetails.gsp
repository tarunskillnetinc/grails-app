<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />
  <title>${session.edit ? "Edit" : "Add"} Segment</title>

  <script type='text/javascript'>
    window.onload = function() {
      var numericFields = document.querySelectorAll('.numeric-field');
      numericFields.forEach(function(field) {
        field.addEventListener('input', function(event) {
          var value = event.target.value;
          var regex = /^\d*$/;

          if (!regex.test(value)) {
            // Remove any invalid characters
            value = value.replace(/[^\d]/g, '');
            event.target.value = value;
          }
        });
      });

      var alphaFields = document.querySelectorAll('.alpha-field');
      alphaFields.forEach(function(field) {
        field.addEventListener('input', function(event) {
          var value = event.target.value;
          var regex = /^[a-zA-Z0-9]*$/;

          if (!regex.test(value)) {
            // Remove any invalid characters
            value = value.replace(/[^a-zA-Z0-9]/g, '');
            event.target.value = value;
          }
        });
      });
    };

    function validateEditedFields() {
      let error = false;
      let errorString = "";

      let name = $('#name').val();
      let description = $('#description').val();
      let min = parseFloat($('#min').val());
      let max = parseFloat($('#max').val());

      if (name.length == 0) {
        errorString = errorString.concat("<li>A name is required</li>");
        error = true;
      }

      if (description.length == 0) {
        errorString = errorString.concat("<li>A description is required</li>");
        error = true;
      }

      if (isNaN(min)) {
        errorString = errorString.concat("<li>A minimum value is required</li>");
        error = true;
      }

      if (isNaN(max)) {
        errorString = errorString.concat("<li>A maximum value is required</li>");
        error = true;
      }

      if (max < min) {
        errorString = errorString.concat("<li>Segment max value cannot be less than Segment min value</li>");
        error = true;
      }
      
      if (error) {
        let errorHeader = "All mandatory fields must be present before data can be saved."
        let errorMessage = "<ul  class='no-bullets'>" + errorHeader + errorString + "\n</ul>"

        $('#error-message').html(errorMessage);
        $('#error-message').prop("hidden", false);
      } else {
        if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {
          $('#segmentDetails').submit();
        }
      }
    }
  </script>
</head>
<body>
<section id="breadcrumb-container" class="container-fluid">
  <nav aria-label="breadcrumb">
    <div class="row mt-4">
      <div class="col">
        <ol class="breadcrumb">
          <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
          <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link action="loyaltySegment">Loyalty Segment Management</g:link></li>
          <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${segment?.name ?: "Segment"}</li>
        </ol>
      </div>
    </div>
  </nav>
</section>

<section id="segment-details" class="container-fluid">
  <div class="alert alert-danger alert-wl mx-0" role="alert" id="error-message" hidden></div>
  <div class="row header-wl mt-3">
    <div class="col-6 offset-3">
      <h2 id="page-title" class="mx-auto my-auto">Segment Details</h2>
    </div>
  </div>
</section>

<section>

  <g:form method="post" action="${session.edit ? 'updateLoyaltySegment' : 'addLoyaltySegment'}" class="mt-4" name="segmentDetails">
    <g:hiddenField name="id" value="${segment?.id}" />

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment Name</label>
      <div class="input-group col-3">
        <g:textField id="name" name="name" value="${segment?.name}" class="form-control bottom-border alpha-field" maxLength="20" />
      </div>
    </div>

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment Description</label>
      <div class="input-group col-3">
        <g:textField id="description" name="description" value="${segment?.description}" class="form-control bottom-border" maxLength="45" />
      </div>
    </div>

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment Minimum</label>
      <div class="input-group col-2">
        <g:textField id="min" name="min" value="${segment?.min}" class="form-control bottom-border numeric-field" maxLength="9" />
      </div>
    </div>

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment maximum</label>
      <div class="input-group col-2">
        <g:textField id="max" name="max" value="${segment?.max}" class="form-control bottom-border numeric-field"  maxLength="9" />
      </div>
    </div>

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment Type</label>
      <div class="input-group col-2">
        <g:select id="type" name="type" from="${['AGE', 'POINTS', 'SPEND']}" value="${segment?.type}"  class="form-control select-border"/>
      </div>
    </div>

    <div class="row form-group mb-4">
      <label class="col-3 offset-1 col-form-label text-right">Segment Status</label>
      <div class="input-group col-2">
        <g:select id="status" name="status" from="${['ACTIVE', 'INACTIVE']}" value="${segment?.status}"  class="form-control select-border"/>
      </div>
    </div>

    <div class="tab-content">
      <div class="row my-5">
        <g:link elementId="editSegment-cancel" action="loyaltySegment" class="btn btn-wl col-1 offset-2" onClick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
        <button id="editSegmentSave" type="button" name="editSegment-save-button" onclick="validateEditedFields()" class="btn btn-success col-1 offset-6">Save</button>
      </div>
    </div>
  </g:form>
</section>
</body>
</html>