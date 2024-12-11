<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />
  <title>Add Product Attribute</title>

  <%@ page import="org.joda.time.DateTime" %>
  <%@ page import="org.joda.time.DateTimeZone" %>
  <%@ page import="org.joda.time.format.DateTimeFormat" %>

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
        dynamicInputContainer.innerHTML = `<g:select id="active" name="defaultValue" from="${['True','False']}" keys="${["true","false"]}" value="True" />`;
      } else if (selectedType == "DATE") {
        console.log("Input type DATE");
        dynamicInputContainer.innerHTML = `<g:textField name="date" id="datepicker" value="${new Date().format('yyyy-MM-dd')}"/>`;
      } else if (selectedType == "LIST") {
        console.log("Input type LIST");
        dynamicInputContainer.innerHTML = ``;
      } else if (selectedType == "NUMERIC") {
        console.log("Input type NUMERIC");
        dynamicInputContainer.innerHTML = `list entry stuff <g:textField name="defaultValue" maxlength="5" class="form-control select-border" min="0"
                         onkeypress="return numericOnly(event);" ondrop="return false;"
                         onpaste="return false;" oncontextmenu="return false;"/>`;
      } else if (selectedType == "TEXT") {
        console.log("Input type TEXT");
        dynamicInputContainer.innerHTML = `<g:textField name="defaultValue" maxlength="30" class="form-control bottom-border" />`;
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
          <div class="col-12">
            <div class="form-group row">
              <label for="name" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Attribute Name</label>
              <div class="col-7 col-lg-4">
                <g:textField name="attributeName" maxlength="30" class="form-control bottom-border" />
              </div>
            </div>

            <div class="form-group row">
              <label for="type" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Attribute Type</label>
              <div class="col-7 col-lg-4 col-xl-2">
                <g:select name="type" from="${attributeTypes}" valueMessagePrefix="AttributeType" class="form-control select-border" onchange="typeChanged();" />
              </div>
            </div>

            <div class="form-group row">
              <label for="defaultValue" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Default Value</label>
              <div class="col-7 col-lg-4" id="dynamicDefaultValueContainer">

              </div>
            </div>

            <div class="form-group row">
              <label for="displayAttribute" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Display Attribute</label>
              <div class="col-7 col-lg-4">
                <g:checkBox name="displayAttribute" class="form-check-input" value="${false}"/>
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

%{--

<section id="add-store-section" class="container-fluid mt-4">
  <g:form name="add-store-form" action="saveNewStore">
    <div id="accordion">
      <!-- Store information. -->
      <div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
        <div class="card-header pointer" id="productDetails" data-toggle="collapse" data-target="#collapseProductDetails" aria-expanded="true" aria-controls="collapseProductDetails">
          <div class="row">
            <div class="col-10 font-weight-bold">Store Information</div>
            <div class="col-2 text-right">
              <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
              </svg>
            </div>
          </div>
        </div>

        <div id="collapseProductDetails" class="collapse show" aria-labelledby="productDetails" data-parent="#accordion">
          <div class="card-body py-5">
            <div class="col-12">
              <div class="form-group row">
                <label for="type" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Type*</label>
                <div class="col-7 col-lg-4 col-xl-2">
                  <g:select name="type" from="${storeTypes}" value="${store?.type}" valueMessagePrefix="StoreType" class="form-control select-border" onchange="typeChanged();" />
                </div>
              </div>

              <div class="form-group row">
                <label for="storeNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Number*</label>
                <div class="col-7 col-lg-2">
                  <g:field type="number" min="0" max="999999999" maxlength="9" name="storeNumber" value="${store?.storeNumber}"  class="form-control bottom-border" onkeydown="acceptMaxNumberValue(event, 999999999);" />
                </div>
              </div>

              <div class="form-group row">
                <label for="storeName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Store Name*</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="storeName" maxlength="30" value="${store?.storeName}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row mt-4">
                <label for="copyConfigFrom" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Copy Configuration From*</label>

                <div class="col-7 col-lg-4 col-xl-3">
                  <g:select name="copyConfigFrom"
                            from="${parentStores}"
                            noSelection="['': 'None / Manually Configure']"
                            value="${store?.copyConfigFrom}"
                            optionValue="${{it?.config?.storeNumber +' - ' +it?.config?.storeName}}"
                            optionKey="id"
                            class="form-control select-border"
                            onchange="copyConfigFromChanged();" />
                </div>
              </div>

              <div id="range-select-div" class="form-group row" style="display: ${store?.copyConfigFrom != null ? 'none' : ''};">
                <label for="range.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Product Range*</label>

                <div class="col-7 col-lg-4 col-xl-3">
                  <g:select name="range.id" from="${ranges}" value="${store?.range?.id}" optionValue="description" optionKey="id" class="form-control select-border" />
                </div>
              </div>

              <div id="priceBand-select-div" class="form-group row" style="display: ${store?.copyConfigFrom != null ? 'none' : ''};">
                <label for="priceBand.id" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Price Band*</label>

                <div class="col-7 col-lg-4 col-xl-3">
                  <g:select name="priceBand.id" from="${priceBands}" value="${store?.priceBand?.id}" optionValue="description" optionKey="id" class="form-control select-border" />
                </div>
              </div>

              <div class="form-group row mt-5">
                <label for="addressBuildingNumberOrName" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Building Name / Number</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressBuildingNumberOrName" maxlength="30" value="${store?.addressBuildingNumberOrName}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressLine1" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 1</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressLine1" maxlength="20" value="${store?.addressLine1}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressLine2" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Address Line 2</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressLine2" maxlength="20" value="${store?.addressLine2}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressTown" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Town / City</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressTown" maxlength="20" value="${store?.addressTown}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressCounty" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">County</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressCounty" maxlength="20" value="${store?.addressCounty}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressCountry" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Country</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressCountry" maxlength="20" value="${store?.addressCountry}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="addressPostCode" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Post Code</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="addressPostCode" maxlength="8" value="${store?.addressPostCode}" class="form-control bottom-border" />
                </div>
              </div>

              <div class="form-group row">
                <label for="phoneNumber" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Phone Number</label>
                <div class="col-7 col-lg-4">
                  <g:textField name="phoneNumber" maxlength="12" value="${store?.phoneNumber}" class="form-control bottom-border" />
                </div>
              </div>

              <div id="parent-store-select" class="form-group row" style="display: none;">
                <label for="parentStoreId" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">Parent Store</label>

                <div class="col-7 col-lg-4 col-xl-3">
                  <g:select name="parentStoreId"
                            from="${parentStores}"
                            noSelection="['': 'None']"
                            value="${store?.parentStoreId}"
                            optionValue="${{it?.config?.storeNumber + ' - ' +it?.config?.storeName}}"
                            optionKey="id"
                            class="form-control select-border" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </g:form>
</section>
--}%
