<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />
    <title>Inventory Management</title>

      <asset:javascript src="validators/input-validator.js" />
      <asset:javascript src="jquery-ui.js" />
      <asset:stylesheet src="jquery-ui.css" />
     <script type="application/javascript">
     $(document).ready(function () {
       $('#uploadBtn').click(function () {
           $('#csvFile').click();
         });

         $('#csvFile').on('change', function () {
           if (this.files.length > 0) {

                     let fileInput = $("#csvFile")[0];
                     if (!fileInput.files.length) {
                         alert("Please select a CSV file.");
                         return;
                     }

                     let formData = new FormData();
                     formData.append("csvFile", fileInput.files[0]);

                     $.ajax({
                         url: "${createLink(controller: 'inventory', action: 'ajaxImportCSV')}",
                         type: "POST",
                         data: formData,
                         processData: false,
                         contentType: false,
                         success: function (response) {
                             $("#fileName").text(response.file);
                             $("#totalLines").text(response.total);
                             $("#successLines").text(response.success);
                             $("#errorLines").text(response.errors);
                             $("#errorLog").val(response.errorLog.join('\n'));
                             $("#csvDialog").dialog({
                                 modal: true,
                                 width: 600
                             });
                         },
                         error: function (xhr) {
                             alert("Upload failed: " + xhr.statusText);
                         }
                     });
           }
         });

         });

                 function handleCancel() {
                     window.location.href = "${createLink(controller: 'inventory', action: 'index')}";
                 }
                 function createStockAdjustmentClicked() {
                                 $('#showReasonCodeModal').modal('show');
                 }
                  function selectReasonCode(reasonCodeId) {
                        window.location.href = '${createLink(controller: 'inventory', action:'createStockAdjustment')}' + '?reasonCodeId=' +reasonCodeId;
                  }

                  // Add this to the $(document).ready function in index.gsp
                  $('#statusSelect').change(function() {
                      let selectedStatus = $(this).val();
                      $.ajax({
                          url: "${createLink(controller: 'inventory', action: 'ajaxFilterAdjustments')}",
                          type: "GET",
                          data: {
                              status: selectedStatus
                          },
                          success: function(response) {
                              $("#results-container").html(response);
                          },
                          error: function(xhr) {
                              console.error("Filter failed:", xhr.responseText);
                              if (xhr.status === 500) {
                                  alert("An error occurred while filtering stock adjustments. Please check the server logs.");
                              } else {
                                  alert("Filter failed: " + xhr.statusText);
                              }
                          }
                      });
                  });

                  $(document).ready(function () {
                    $('#uploadBtn').click(function () {
                      $('#csvFile').click();
                    });

                    // Explicitly call the stock adjustments action on page load
                    $.ajax({
                        url: "<g:createLink controller='inventory' action='ajaxGetStockAdjustments'/>",
                        success: function(resp) {
                            $('#results-container').html(resp); // Load results into the existing container
                        }
                    });

                    $('#statusSelect').change(function() {
                      let selectedStatus = $(this).val();
                      $.ajax({
                        url: "${createLink(controller: 'inventory', action: 'ajaxFilterAdjustments')}",
                        type: "GET",
                        data: {
                          status: selectedStatus
                        },
                        success: function(response) {
                          $("#results-container").html(response);
                        },
                        error: function(xhr) {
                          console.error("Filter failed:", xhr.responseText);
                          if (xhr.status === 500) {
                            alert("An error occurred while filtering stock adjustments. Please check the server logs.");
                          } else {
                            alert("Filter failed: " + xhr.statusText);
                          }
                        }
                      });
                    });
                    // Rest of your existing code...
                  });

         $(document).ready(function() {
             // Check for success message in URL
             const urlParams = new URLSearchParams(window.location.search);
             const successMessage = urlParams.get('successMessage');

             if (successMessage) {
                 $('#successMessage').text(successMessage).show();
                 setTimeout(function() {
                     $('#successMessage').hide();
                 }, 5000);

                 // Clean URL
                 window.history.replaceState({}, document.title, window.location.pathname);
             }
         });

             </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Inventory Management</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="store-management" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Store Stock Adjustment</h2>
            </div>

            <div class="col-3 text-right">
                <button id="createAdjustmentButton" type="button" class="btn btn-wl mt-1" onclick="createStockAdjustmentClicked()">Create Stock Adjustment</button>
                <button id="cancelButton" type="button" class="btn btn-wl mt-1" onclick="">Cancel</button>
            </div>

    </section>

    <section id="alerts-container" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert" id="successMessage" style="display: none"></div>
            <div class="alert alert-danger alert-wl mx-0" role="alert" id="failureMessage" style="display: none"></div>
    </section>
    </section>

     <div class="row mx-5 pt-3 pb-2" style="display: flex; align-items: center;">
         <label for="status" class="col-1 col-form-label-sm text-right">Status</label>
         <div class="input-group-append">
             <g:select id="statusSelect" name="statusSelect" from="${['All', 'Pending', 'In progress', 'Complete']}" value="" valueMessagePrefix="reasonCodeSelect" class="form-control select-border" style="z-index: 0;" />
         </div>
         <div class="col-12 text-right" style="max-width:71%">
             <button id="uploadBtn" type="button" class="btn btn-wl mt-1">Upload CSV</button>
             <input type="file" id="csvFile" name="csvFile" accept=".csv,.CSV"
                    style="display:none" oncancel="resetExclusionInput()">
         </div>
     </div>
        <div id="results-container" class="align-content-center">
          <g:render template="stockStatusSearchResults" />
        </div>

     <div id="csvDialog" title="" style="display:none;text-align: center;">
         <p>Import file <span id="fileName">[File Name]</span> contains <span id="totalLines">0</span>
           Inventory Adjustment Lines
         </p>
         <p><span id="successLines">0</span> lines read in  successfully</p>
         <p><span id="errorLines">0</span> lines reported error</p>

         <div>
             <p><strong>Error Log:</strong></p>
             <textarea id="errorLog" rows="5" style="width: 100%;" readonly></textarea>
         </div>

         <p style="font-weight:bold;">You are about to adjust/overwrite stock levels of the product(s) selected within the store(s) assigned.</p>
         <p style="font-weight:bold;">Do you wish to continue</p>

         <button id="cancelBtn" class="btn btn-danger" onclick="handleCancel()" >Cancel</button>
         <button id="confirmBtn" class="btn btn-success">Continue</button>
     </div>

             <!-- Product search modal -->
             <g:render template="/product/productSearch" />

              <section id="showPopUp-modal" class="container-fluid">
                         <div class="modal fade" id="showReasonCodeModal" tabindex="-1"  role="dialog" aria-labelledby="showReasonCodeLabel" data-backdrop="false" aria-hidden="true" >
                             <div class="modal-dialog modal-lg" role="document"style="border: 2px black solid; width: 350px; margin-top: 120px">
                                 <div id="showReasonCodeContent" class="modal-content">
                                     <g:render template="/inventory/showReasonCode" model="[reasonCodes: reasonCodes]" />
                                 </div>
                             </div>
                         </div>
                     </section>




</body>
</html>