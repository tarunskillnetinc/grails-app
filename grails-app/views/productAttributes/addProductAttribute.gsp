<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />
  <title>Add Product Attribute</title>

  <%@ page import="org.joda.time.DateTime" %>
  <%@ page import="org.joda.time.DateTimeZone" %>
  <%@ page import="org.joda.time.format.DateTimeFormat" %>


  <style>
    .form-container {
      max-width: 800px;
      margin: 0 auto;
    }
    .form-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 1rem;
    }
    .form-group {
      flex: 0 0 48%;
    }
    .form-control {
      width: 100%;
      max-width: 300px;
    }
    .checkbox-container {
      padding: 0;
      height: 38px; /* Adjust this value to match your input height */
    }
    .wl-checkbox {
      width: 20px;
      height: 20px;
      margin-left: 0;
    }
    .form-row.align-items-end {
      margin-bottom: 1rem;
    }
  </style>

  <script type="text/javascript">

    // Call the function when the page loads
    window.onload = function() {
      typeChanged();
    };

    $(document).ready(function() {
      $("#add-product-attribute-form :input").on("input", clearErrors);
    });

    function acceptDefaultNumeric(e, maxValue) {
      // Allow digits, backspace, and arrow keys without further checks
      if (e.key === 'Backspace' || e.key === 'Delete') {
        return;
      } else if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
        return;
      }

      // Check if the key pressed is a digit
      if (e.key >= '0' && e.key <= '9') {
        // Construct the potential new value by adding the typed digit
        const newValue = parseInt(e.target.value + e.key, 10);

        // Check if the new value exceeds the maximum allowed value
        if (newValue > maxValue) {
          e.preventDefault(); // Prevent the key press if it exceeds the maximum value
        }
      } else {
        e.preventDefault(); // Prevent non-digit characters
      }
    }


    function typeChanged() {
      var selectedType = $("#type option:selected").val();
      var dynamicInputContainer = document.getElementById("dynamicDefaultValueContainer");

      if (selectedType === "BOOLEAN") {
        $('#defaultValueLbl').show();
        dynamicInputContainer.innerHTML = `<g:select id="active" name="defaultValue" class="form-control" from="${['True','False']}" keys="${["true","false"]}" value="True" />`;
      } else if (selectedType === "LIST" || selectedType === "DATE") {
        dynamicInputContainer.innerHTML = ``;
        $('#defaultValueLbl').hide();
      } else if (selectedType === "NUMERIC") {
        dynamicInputContainer.innerHTML = `<g:field name="defaultValue" class="form-control" type="number" min="0" max="999999999" step="1" onkeydown="acceptDefaultNumeric(event,999999999);"/>`;
        $('#defaultValueLbl').show();
      } else if (selectedType === "TEXT") {
        dynamicInputContainer.innerHTML = `<g:textField name="defaultValue" maxlength="50" class="form-control" />`;
        $('#defaultValueLbl').show();
      }
    }

    function saveProductAttribute() {
      var formValues = $("#add-product-attribute-form").serialize();
      $.ajax({
        url: "${createLink(controller: 'productAttributes', action: 'saveProductAttribute')}",
        method: "POST",
        data: formValues,
        headers: {
          'X-CSRF-Token': $('meta[name="csrf-token"]').attr('content')
        },
        success: function(response) {
          if (response === "OK") {
            // Redirect on success
            window.location.href = "${createLink(controller: 'productAttributes', action: 'productAttributes')}";
          } else {
            showGenericError();
          }
        },
        error: function(xhr, status, error) {
          if (xhr.status === 400) {
            $("#error-container").html(xhr.responseText);
          } else {
            showGenericError();
          }
        }
      });
    }

    function showGenericError() {
      $("#error-container").html("<div class='alert alert-danger'>An error occurred while saving the product attribute. Please try again.</div>");
    }

    function clearErrors() {
      $("#error-container").empty();
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
          <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="productAttributes" action="productAttributes">Retailer Product Attributes</g:link></li>
          <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add Product Attribute</li>
        </ol>
      </div>
    </div>
  </nav>
</section>
<section id="header-container" class="container-fluid">
  <div class="row header-wl mt-0 mb-1">
    <div class="col-8 offset-2">
      <h2 id="page-title" class="mx-auto my-auto">Add Product Attribute</h2>
    </div>

    <div class="col-12 text-right mt-3">
      <div class="d-flex justify-content-end align-items-center">
        <g:link elementId="cancel-btn" controller="productAttributes" action="productAttributes" tabindex="-1" role="button" class="btn btn-wl ml-1">Cancel</g:link>
        <button id="save-btn" class="btn btn-success ml-1" name="save" onclick="saveProductAttribute();">Save</button>
      </div>
    </div>
  </div>
</section>
<section id="error-container">
</section>
<section id="add-product-attribute-section" class="container-fluid mt-4">
  <g:form name="add-product-attribute-form" id="add-product-attribute-form" action="saveProductAttribute">
    <div id="accordion">
      <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
        <div class="card-header pointer" id="attributeDetails" data-target="#collapseAttributeDetails" aria-expanded="true" aria-controls="collapseAttributeDetails">
          <div class="row">
            <div class="col-10 font-weight-bold">Attribute Information</div>
          </div>
        </div>

        <div id="collapseAttributeDetails" class="collapse show" aria-labelledby="attributeDetails" data-parent="#accordion">
          <div class="card-body py-5">
            <div class="form-container">

              <div class="form-row align-items-end">
                <div class="form-group col-md-6">
                  <label for="attributeName">Attribute Name</label>
                  <g:textField name="attributeName" maxlength="50" class="form-control " />
                </div>
                <div class="form-group col-md-6">
                  <label for="displayAttribute">Display Attribute</label>
                  <div class="checkbox-container">
                      <g:checkBox name="displayAttribute" class="form-check-input wl-checkbox" checked="${false}" />
                  </div>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group col-md-6">
                  <label for="type">Attribute Type</label>
                  <div>
                    <g:select name="type" from="${attributeTypes}" valueMessagePrefix="ProductAttributeType"
                              optionKey="${{it}}"
                              class="form-control"
                              optionValue="${{message(code: "ProductAttributeType.${it}")}}"
                              onchange="typeChanged();"
                    />
                  </div>
                </div>
                <div class="form-group col-md-6">
                  <label id="defaultValueLbl" for="defaultValue">Default Value</label>
                  <div id="dynamicDefaultValueContainer">
                    <!-- Dynamic content will be inserted here -->
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </g:form>
</section>
</body>
</html>