<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />

  <title>Barcode Configuration</title>
  <asset:javascript src="co-utils.js" />
  <asset:javascript src="validators/input-validator.js" />

  <style>
  .custom-checkbox-align .form-check-input {
    width: 1.5em;
    height: 1.5em;
    margin-left: 0;
  }
  .field-error {
    font-size: 0.7em; /* Adjust the size as needed */
  }
  .form-container {
    border: 1px solid #cccccc; /* Add gray border */
    padding: 20px; /* Add padding for better spacing */
    width: 50%; /* Make the form half of the page size */
    margin: 0 auto; /* Center the form */
  }
  .number-box {
    width: 80px;
  }
  </style>

  <script type="text/javascript">

    var saveSignifierURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxSaveSignifier')}"
    var updateRestrictedSignifierURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxRestrictedSaveSignifier')}"
    var addEmbeddedDataURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxAddEmbeddedData')}?signifierId=${signifier?.id}"
    var editEmbeddedDataURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxEditEmbeddedData')}?signifierId=${signifier?.id}"
    var saveEmbeddedDataURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxSaveEmbeddedData')}?signifierId=${signifier?.id}"
    var loadEmbeddedDataURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxShowEmbeddedDataList')}"
    var deleteEmbeddedDataURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxDeleteEmbeddedData')}"

    $(function() {
      getEmbeddedData();
    });

    function saveSignifier() {
      var formValues = $("#editSignifierForm").serialize();
      $("#loading-indicator").show();
      hideBtns();
      $.ajax({
        url: saveSignifierURL,
        method: "POST",
        data: formValues,
        success: function (resp) {
          if (resp === "OK") {
            $("#loading-indicator").hide();
            window.location.href = '<g:createLink controller="barcodeConfig" action="index"/>';
          } else {
            document.open();
            document.write(resp);
            document.close();
          }
        }
      });
    }

    function updateRestrictedSignifier() {
      var formValues = $("#editSignifierForm").serialize();
      $("#loading-indicator").show();
      hideBtns();
      $.ajax({
        url: updateRestrictedSignifierURL,
        method: "POST",
        data: formValues,
        success: function (resp) {
          if (resp === "OK") {
            $("#loading-indicator").hide();
            window.location.href = '<g:createLink controller="barcodeConfig" action="index"/>';
          } else {
            document.open();
            document.write(resp);
            document.close();
          }
        }
      });
    }

    function saveEmbeddedData() {
      var formValues = $("#addEmbeddedDataForm").serialize();
      $("#loading-indicator").show();
      hideBtns();
      $.ajax({
        url: saveEmbeddedDataURL,
        method: "POST",
        data: formValues,
        success: function (resp) {
          $("#loading-indicator").hide();
          if (resp === "OK") {
            $('#editSignifierModal').modal('hide')
            showBtns();
            getEmbeddedData();
          } else {
            showBtns();
            $("#editSignifierContent").html(resp);
          }
        }
      });
    }

    function addEmbeddedData() {
      $("#editSignifierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
      $('#editSignifierModal').modal({show: true, backdrop: 'static', keyboard: false});
      $.ajax({
        url: addEmbeddedDataURL,
        method: "GET",
        success: function (resp) {
          $("#editSignifierContent").html(resp);
        }
      });
    }

    function editEmbeddedData(embeddedDataId) {
      $("#editSignifierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
      $('#editSignifierModal').modal({show: true, backdrop: 'static', keyboard: false});
      var urlParams = {}
      urlParams['embeddedDataId'] = embeddedDataId;

      $.ajax({
        url: editEmbeddedDataURL,
        method: "GET",
        data: urlParams,
        success: function (resp) {
          $("#editSignifierContent").html(resp);
        }
      });
    }


    function hideBtns() {
      $('#btn-container').hide()
      $('#addEmbeddedData').hide()
    }

    function showBtns() {
      $('#btn-container').show()
      $('#addEmbeddedData').show()
    }

    function cancelEmbeddedData() {
      if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
        $('#editSignifierModal').modal('hide')
      }
    }

    function getEmbeddedData() {
      $('#results-container').html("");
      $("#loading-indicator").show();

      var filterParams = {}
      filterParams['barcodeSignifierId'] = "${signifier?.id}";

      $.ajax({
        url: loadEmbeddedDataURL,
        data: filterParams,
        success: function(resp) {
          $('#results-container').html(resp);
        }
      });
    }

    function deleteEmbeddedData(embeddedDataId) {
      if (confirm("This will delete the selected Embedded Data.")) {
        $.ajax({
          url: deleteEmbeddedDataURL,
          method: "DELETE",
          data: {embeddedDataId: embeddedDataId},
          success: function (data, textStatus, resp) {
            $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
            getEmbeddedData()
          },
          error: function (resp) {
            $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
            getEmbeddedData()
          }
        });
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
          <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Edit Barcode Signifier</li>
        </ol>
      </div>
    </div>
  </nav>
</section>

<section id="editSignifierTtl" class="container-fluid">
  <div class="row header-wl mt-3">
    <div class="col-6 offset-3">
      <h2 id="page-title" class="mx-auto my-auto">Edit Barcode Signifier</h2>
    </div>
  </div>
</section>

<g:render template="/errors/errorMessage" model="[errorMessages: errorMessages, error: error]" />

<section id="form-section" class="container-fluid">
  <div class="row mt-3">
    <div class="col-12">
      <div class="form-container">
        <form name="editSignifierForm" id="editSignifierForm">
          <g:hiddenField name="id" value="${signifier?.id}" />
          <div class="form-group row">
            <label for="descriptionValue" class="col-2 col-form-label-sm text-right">Description</label>
            <div class="col-4">
              <sec:ifAnyGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                <g:textField name="descriptionValue" value="${signifier?.description}" class="form-control bottom-border" />
              </sec:ifAnyGranted>
              <sec:ifNotGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                <g:textField name="descriptionValue" value="${signifier?.description}" class="form-control bottom-border" readonly="true"/>
              </sec:ifNotGranted>
            </div>
            <label for="receiptDescriptionValue" class="col-2 col-form-label-sm text-right">Receipt Description</label>
            <div class="col-4">
              <sec:ifAnyGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                <g:textField name="receiptDescriptionValue" value="${signifier?.receiptDescription}" class="form-control bottom-border" />
              </sec:ifAnyGranted>
              <sec:ifNotGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                <g:textField name="receiptDescriptionValue" value="${signifier?.receiptDescription}" class="form-control bottom-border" readonly="true"/>
              </sec:ifNotGranted>
            </div>
          </div>
          <div class="form-group row">
            <label for="typeValue" class="col-2 col-form-label-mandatory text-right">Type</label>
            <div class="col-4">
              <div class="input-group">
                <sec:ifAnyGranted roles="ROLE_ENGINEER">
                  <g:select name="typeValue" from="${signifierTypes}" valueMessagePrefix="BarcodeSignifierType"
                            optionKey="${{it}}"
                            noSelection="['': 'Select Type']"
                            class="form-control select-border"
                            value="${signifier?.type}" />
                </sec:ifAnyGranted>
                <sec:ifNotGranted roles="ROLE_ENGINEER">
                  <g:select name="typeValue" from="${signifierTypes}" valueMessagePrefix="BarcodeSignifierType"
                            optionKey="${{it}}"
                            noSelection="['': 'Select Type']"
                            class="form-control select-border"
                            value="${signifier?.type}"
                            disabled="true"
                  />
                </sec:ifNotGranted>
              </div>
            </div>
            <label for="checkDigitValue" class="col-2 col-form-label-sm text-right">Check Digit</label>
            <div class="col-4 custom-checkbox-align">
              <sec:ifAnyGranted roles="ROLE_ENGINEER">
                <g:checkBox name="checkDigitValue" value="${signifier?.checkDigit}" class="form-check-input" />
              </sec:ifAnyGranted>
              <sec:ifNotGranted roles="ROLE_ENGINEER">
                <g:checkBox name="checkDigitValue" value="${signifier?.checkDigit}" class="form-check-input" disabled="true"/>
              </sec:ifNotGranted>
            </div>
          </div>
          <div class="form-group row">
            <label for="patternValue" class="col-2 col-form-label text-right">Pattern</label>
            <div class="col-4">
              <div class="input-group">
                <sec:ifAnyGranted roles="ROLE_ENGINEER">
                  <g:field type="text" id="pattern" name="patternValue" value="${signifier?.pattern}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                </sec:ifAnyGranted>
                <sec:ifNotGranted roles="ROLE_ENGINEER">
                  <g:field type="text" id="pattern" name="patternValue" value="${signifier?.pattern}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" readonly="true"/>
                </sec:ifNotGranted>
              </div>
            </div>
            <label for="lengthValue" class="col-2 col-form-label-mandatory text-right">Length</label>
            <div class="col-4">
              <div class="input-group number-box">
                <sec:ifAnyGranted roles="ROLE_ENGINEER">
                  <g:field type="number" id="length" name="lengthValue" value="${signifier?.length}" class="form-control bottom-border" oninput="validateInput(this);" min="0" max="45" onkeydown="acceptMaxNumberValue(event, 45);" />
                </sec:ifAnyGranted>
                <sec:ifNotGranted roles="ROLE_ENGINEER">
                  <g:field type="number" id="length" name="lengthValue" value="${signifier?.length}" class="form-control bottom-border" oninput="validateInput(this);" min="0" max="45" onkeydown="acceptMaxNumberValue(event, 45);" readonly="true"/>
                </sec:ifNotGranted>
              </div>
            </div>
          </div>
          <div class="form-group row">
            <label for="discountPercentageValue" class="col-2 col-form-label-sm text-right">Discount Percentage</label>
            <div class="col-4">
              <div class="input-group number-box">
                <sec:ifAnyGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                  <g:field type="number" name="discountPercentageValue" value="${signifier?.discountPercentage}" class="form-control bottom-border" min="0" max="100" onkeydown="acceptMaxNumberValue(event, 100);"/>
                </sec:ifAnyGranted>
                <sec:ifNotGranted roles="ROLE_ENGINEER,ROLE_HEAD_OFFICE">
                  <g:field type="number" name="discountPercentageValue" value="${signifier?.discountPercentage}" class="form-control bottom-border" min="0" max="100" onkeydown="acceptMaxNumberValue(event, 100);" readonly="true"/>
                </sec:ifNotGranted>
              </div>
            </div>
          </div>
        </form>
        <div id="btn-container" class="form-group row">
          <div class="col-12 text-right">
            <sec:ifAnyGranted roles='ROLE_ENGINEER'>
              <button id="form-submit-button" type="submit" class="btn btn-success text-right" onclick="saveSignifier()">Save</button>
            </sec:ifAnyGranted>
            <sec:ifAnyGranted roles='ROLE_HEAD_OFFICE'>
              <button id="form-submit-button" type="submit" class="btn btn-success text-right" onclick="updateRestrictedSignifier()">Save</button>
            </sec:ifAnyGranted>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<section id="signifiers-container" class="container-fluid mb-1">
  <div class="row mt-5">
    <div class="col-12 mt-5 d-flex justify-content-center">
      <h2 id="embedded-data-title" class="mx-auto my-auto">Embedded Data</h2>
    </div>
    <div class="col-12 text-right">
      <sec:ifAnyGranted roles='ROLE_ENGINEER'>
        <a id="addEmbeddedData" href="#" class="btn btn-wl mt-1" onclick="addEmbeddedData()">Add Embedded Data</a>
      </sec:ifAnyGranted>
    </div>
  </div>
  <div id="results-container" class="mt-1">
    <g:render template="embeddedData/embeddedDataSearchResults"/>
  </div>
</section>

<section id="editSignifier-modal" class="container-fluid">
  <!-- Add Signifier modal -->
  <div class="modal fade" id="editSignifierModal" tabindex="-1" role="dialog" aria-labelledby="addSignifierModalLabel"
       aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
      <div id="editSignifierContent" class="modal-content"></div>
    </div>
  </div>
</section>

<script type="text/javascript">
  function clearForm() {
    document.getElementById('editSignifierForm').reset();
  }
</script>
</body>
</html>
