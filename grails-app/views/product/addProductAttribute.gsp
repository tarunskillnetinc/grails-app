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
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0;
    }
    .wl-checkbox {
      width: 20px;
      height: 20px;
    }
    .form-row.align-items-end {
      margin-bottom: 1rem;
    }

    .checkbox-container {
      display: flex;
      align-items: center;
      height: 38px; /* Adjust this value to match your input height */
    }

    .wl-checkbox {
      margin: 0;
    }
  </style>

  <script type="text/javascript">

    // Call the function when the page loads
    window.onload = function() {
      typeChanged();
    };

    $(function() {
      $("#datepicker").datepicker({
        dateFormat: "yy-mm-dd",
        defaultDate: new Date()
      });
    });

    function typeChanged() {
      var selectedType = $("#type option:selected").val();
      var dynamicInputContainer = document.getElementById("dynamicDefaultValueContainer");

      if (selectedType == "BOOLEAN") {
        console.log("Input type BOOLEAN");
        dynamicInputContainer.innerHTML = `<g:select id="active" name="defaultValue" class="form-control" from="${['True','False']}" keys="${["true","false"]}" value="True" />`;
      } else if (selectedType == "DATE") {
        console.log("Input type DATE");
        dynamicInputContainer.innerHTML = `<g:textField name="date" id="datepicker" value="${new Date().format('yyyy-MM-dd')}" class="form-control"/>`;
      } else if (selectedType == "LIST") {
        console.log("Input type LIST");
        dynamicInputContainer.innerHTML = ``;
      } else if (selectedType == "NUMERIC") {
        console.log("Input type NUMERIC");
        dynamicInputContainer.innerHTML = `<g:textField name="defaultValue" maxlength="5" class="form-control" min="0"
                         onkeypress="return numericOnly(event);" ondrop="return false;"
                         onpaste="return false;" oncontextmenu="return false;"/>`;
      } else if (selectedType == "TEXT") {
        console.log("Input type TEXT");
        dynamicInputContainer.innerHTML = `<g:textField name="defaultValue" maxlength="30" class="form-control" />`;
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
          <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="product" action="productAttributes">Retailer Product Attributes</g:link></li>
          <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add Product Attribute</li>

        </ol>
      </div>
    </div>
  </nav>
</section>
<section id="header-container" class="container-fluid">
  <div class="row header-wl mt-0">
    <div class="col-8 offset-2">
      <h2 id="page-title" class="mx-auto my-auto">Add Product Attribute</h2>
    </div>

    <div class="col-12 text-right mt-3">
      <div class="d-flex justify-content-end align-items-center">
        <g:link elementId="cancel-btn" controller="product" action="productAttributes" tabindex="-1" role="button" class="btn btn-wl ml-1">Cancel</g:link>
        <button id="save-btn" class="btn btn-success ml-1" name="save" onclick="$('#add-product-attribute-form').submit();">Save</button>
      </div>
    </div>
  </div>
</section>

<section id="add-product-attribute-section" class="container-fluid mt-4">
  <g:form name="add-product-attribute-form" action="saveProductAttribute">
    <div id="accordion">
      <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
        <div class="card-header pointer" id="attributeDetails" data-toggle="collapse" data-target="#collapseAttributeDetails" aria-expanded="true" aria-controls="collapseAttributeDetails">
          <div class="row">
            <div class="col-10 font-weight-bold">Attribute Information</div>
            <div class="col-2 text-right">
              <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
              </svg>
            </div>
          </div>
        </div>

        <div id="collapseAttributeDetails" class="collapse show" aria-labelledby="attributeDetails" data-parent="#accordion">
          <div class="card-body py-5">
            <div class="form-container">

              <div class="form-row align-items-end">
                <div class="form-group col-md-6">
                  <label for="attributeName">Attribute Name</label>
                  <g:textField name="attributeName" maxlength="30" class="form-control " />
                </div>
                <div class="form-group col-md-6">
                  <div class="d-flex align-items-center">
                    <span>Display Attribute</span>
                    <div class="checkbox-container ml-4">
                      <g:checkBox name="displayAttribute" class="form-check-input wl-checkbox" checked="${false}" />
                    </div>
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
                  <label for="defaultValue">Default Value</label>
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