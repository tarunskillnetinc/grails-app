<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />
    <title>Inventory Management</title>

      <asset:javascript src="validators/input-validator.js" />
      <asset:javascript src="jquery-ui.js" />
      <asset:stylesheet src="jquery-ui.css" />
<script>

    function productSelected(id, itemCode, description) {
        var addProductUrl = "${createLink(controller: 'inventory', action: 'ajaxAddProduct')}";

        $.ajax({
            url: addProductUrl,
            data: { productId: id },
            success: function(resp) {
                let productList = $("#productList")
                let warningMessage = $('#warning-message')

                for (const element of productList.children()) {
                    if (element.id.toUpperCase() === "PRODUCTVARIANT" + id) {
                        if (warningMessage.length) {
                            warningMessage.text("Product has already been added.")
                            warningMessage.removeClass("hidden")
                        }
                        return
                    }
                }

                if (warningMessage.length) {
                    warningMessage.addClass("hidden")
                }

                productList.append(resp);

                $('#noResultsRow').hide();

                let i = productList.children().length - 1;
                let row = $('#productVariant' +id);
                row.addClass("wl-striped" +((i-1) % 2));
                row.find('#prod-0-id').attr("id", "prod-" + i + "-id");
                row.find('#prod-0-sku').attr("id", "prod-" + i + "-sku");
                row.find('#prod-0-description').attr("id", "prod-" + i + "-description");
                row.find('#prod-0-colour').attr("id", "prod-" + i + "-colour");
                row.find('#prod-0-size').attr("id", "prod-" + i + "-size");
                row.find('#prod-0-remove-btn').attr("id", "prod-" + i + "-remove-btn");

                // Enable store button after adding product
                checkProductsAndEnableStoreButton();
            }
        });
    }


    function removeProduct(productId) {
        $('#product' +productId).remove();

        var productList = $('#productList');

        if (productList.children().length === 1) {
            var noResultsRow = $('#noResultsRow');

            noResultsRow.removeClass("wl-striped0");
            noResultsRow.removeClass("wl-striped1");
            noResultsRow.addClass("wl-striped0");
            noResultsRow.show();
        } else {
            for (var i = 1 ; i <= productList.children().length ; i++) {
                var child = $('#productList>div:nth-child(' +i +')');

                child.removeClass("wl-striped0");
                child.removeClass("wl-striped1");

                child.addClass("wl-striped" +(i % 2));
            }
        }
    }


      var getStoresUrl = "${createLink(controller: 'inventory', action: 'ajaxGetStores')}"
      var deleteStoreUrl = "${createLink(controller: 'store', action: 'ajaxDeleteStore')}"
      var addStoreAdditionalDetails = "${createLink(controller: 'store', action: 'ajaxAddStoreAdditionalDetail')}"

      var globalSortParams = null;

      $(function() {
          var sort = "${sort}";
          var order = "${order}"

          getStores({max: ${max ?: 'null'}, offset: ${offset ?: 'null'}, sort: (sort !== "" ? sort : null), order: (order !== "" ? order : null)});
          $('#ExitButton').on('click', function() {
              if (confirm('Are you sure you want to exit?')) {
                  window.location.href = "${createLink(uri: '/')}";
              }
          });

          $(document).on('click', '#cancelButton', function() {
              if (confirm('Are you sure you want to cancel?')) {
                  window.location.href = "${createLink(uri: '/')}";
              }
          });
      });

      function searchStores(sortParams) {
          getStores(sortParams);
      }

      function getStores(sortParams) {
          $('#search-results').html("");
          $("#loading-indicator").show();

          var filterParams = {};

          $("#filtersForm input").each(function() {
              filterParams[$(this).attr("name")] = $(this).val();
          }).get();

          $("#filtersForm :checkbox:checked").each(function() {
              filterParams[$(this).attr("name")] = true;
          }).get();

          globalSortParams = sortParams;

          $.extend(filterParams, globalSortParams);

          $.ajax({
              url: getStoresUrl,
              data: filterParams,
              success: function(resp) {
                  $('#results-container').html(resp);
                  $("#loading-indicator").hide();
              },
              error: function(xhr, status, error) {
                  $("#loading-indicator").hide();
                  $('#errors-container').html('<div class="alert alert-danger mt-3">Error loading stores: ' + error + '</div>');
                  console.error("AJAX Error:", status, error);
              }
          });
      }

      function clearFilters() {
          $("#filtersForm input").each(function() {
              $(this).val("");
          }).get();

          $("#filtersForm :checkbox:checked").each(function() {
              $(this).prop("checked", false);
          }).get();

          getStores();
      }

      function filter(inputName, dropDownName) {
          var keyword = document.getElementById(inputName).value.toLowerCase();
          var select = document.getElementById(dropDownName);
          for (var i = 0; i < select.length; i++) {
              var txt = select.options[i].text.toLowerCase();
              if (!txt.match(keyword)) {
                  $(select.options[i]).attr('disabled', 'disabled').hide();
              } else {
                  $(select.options[i]).removeAttr('disabled').show();
              }
          }
      }

      function clearStoreFilters() {
          $('#modalStoreNumberFilter').val('');
          $('#modalStoreNameFilter').val('');
          $('#selectAllStores').prop('checked', false);
      }

      function getStores() {
          $('#store-results-container').html("");
          $("#store-loading-indicator").show();

          var filterParams = {
              storeNumberFilter: $('#modalStoreNumberFilter').val(),
              storeNameFilter: $('#modalStoreNameFilter').val(),
              max: 50,
              offset: 0
          };

          $.ajax({
              url: getStoresUrl,
              data: filterParams,
              success: function(resp) {
                  $('#store-results-container').html(resp);
                  $("#store-loading-indicator").hide();

                  $('#selectAllStores').on('change', function() {
                      $('.store-checkbox').prop('checked', $(this).is(':checked'));
                  });

                  $(document).on('change', '.store-checkbox', function() {
                      updateSelectAllCheckbox();
                  });
              },
              error: function(xhr, status, error) {
                  $("#store-loading-indicator").hide();
                  $('#store-results-container').html('<div class="alert alert-danger mt-3">Error loading stores: ' + error + '</div>');
                  console.error("AJAX Error:", status, error);
              }
          });
      }

      function updateSelectAllCheckbox() {
          var allChecked = true;
          var anyChecked = false;

          $('.store-checkbox').each(function() {
              if($(this).is(':checked')) {
                  anyChecked = true;
              } else {
                  allChecked = false;
              }
          });

          $('#selectAllStores').prop('checked', allChecked);
      }


     var currentProductId = null;

     function setCurrentProductId(productId) {
         currentProductId = productId;
         // Load existing stores if any
         if (productId) {
             var storesData = $('#product' + productId + ' .selected-stores').val();
             if (storesData) {
                 try {
                     var stores = JSON.parse(storesData);
                     // Uncheck all checkboxes first
                     $('.store-checkbox').prop('checked', false);
                     // Check the boxes for stores already selected
                     stores.forEach(function(store) {
                         $('.store-checkbox[data-store-id="' + store.id + '"]').prop('checked', true);
                     });
                     updateSelectAllCheckbox();
                 } catch (e) {
                     console.error("Error parsing stores data:", e);
                 }
             }
         }
     }

     function addSelectedStores() {
         var selectedStores = [];
         var selectedStoreNames = [];
         var selectedStoreData = [];

         $('.store-checkbox:checked').each(function() {
             var storeId = $(this).data('store-id');
             var storeNumber = $(this).data('store-number');
             var storeName = $(this).data('store-name');

             selectedStores.push(storeId);
             selectedStoreNames.push(storeName);
             selectedStoreData.push({
                 id: storeId,
                 number: storeNumber,
                 name: storeName
             });
         });

         if (selectedStores.length === 0) {
             alert('Please select at least one store.');
             return;
         }

         // Update only the current product if specified, otherwise update all
         if (currentProductId) {
             updateProductStores(currentProductId, selectedStoreData, selectedStores.length, selectedStoreNames.join(', '));
             currentProductId = null; // Reset after update
         } else {
             // Update all products (if you want this functionality)
             $('.selected-stores').val(JSON.stringify(selectedStoreData));
             $('.stores-count').text(selectedStores.length);
             $('.stores-list').text(selectedStoreNames.join(', '));
         }

         $('#addStoresModal').modal('hide');
         $('#successMessage').text('Successfully added ' + selectedStores.length + ' store(s)').show();
         setTimeout(function() {
             $('#successMessage').hide();
         }, 5000);
     }

     function updateProductStores(productId, storeData, count, names) {
         $('#product' + productId + ' .selected-stores').val(JSON.stringify(storeData));
         $('#product' + productId + ' .stores-count').text(count);
         $('#product' + productId + ' .stores-list').text(names);
     }


      $(document).ready(function() {
          $('#addStoresModal').on('shown.bs.modal', function() {
              clearStoreFilters();
              getStores();
          });

          $('#addStoresModal').on('hidden.bs.modal', function() {
              $(document).off('change', '.store-checkbox');
          });
      });


    function checkProductsAndEnableStoreButton() {
        var hasProducts = $('#productList').children().length > 1 ||
                         ($('#productList').children().length === 1 && !$('#noResultsRow').is(':visible'));

        $('#addStoresButton').prop('disabled', !hasProducts);

        if (!hasProducts) {
            $('#addStoresButton').attr('title', 'Please add products first');
        } else {
            $('#addStoresButton').removeAttr('title');
        }
    }

    function removeProduct(productId) {
        $('#product' +productId).remove();

        var productList = $('#productList');

        if (productList.children().length === 1) {
            var noResultsRow = $('#noResultsRow');

            noResultsRow.removeClass("wl-striped0");
            noResultsRow.removeClass("wl-striped1");
            noResultsRow.addClass("wl-striped0");
            noResultsRow.show();
        } else {
            for (var i = 1 ; i <= productList.children().length ; i++) {
                var child = $('#productList>div:nth-child(' +i +')');

                child.removeClass("wl-striped0");
                child.removeClass("wl-striped1");

                child.addClass("wl-striped" +(i % 2));
            }
        }

        // Check if we need to disable store button
        checkProductsAndEnableStoreButton();
    }

        $(document).ready(function() {
            // Initialize store button state
            checkProductsAndEnableStoreButton();

            $('#addStoresModal').on('shown.bs.modal', function() {
                clearStoreFilters();
                getStores();
            });

            $('#addStoresModal').on('hidden.bs.modal', function() {
                $(document).off('change', '.store-checkbox');
            });
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
                        <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link uri="/inventory/index">Inventory Management</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Create Stock Adjustment</li>
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
                <button id="cancelButton" type="button" class="btn btn-wl mt-1" onclick="">Cancel</button>
                <button id="saveButton" type="button" class="btn btn-wl mt-1" onclick="">Save</button>
            </div>
        </div>
    </section>

    <section id="alerts-container" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert" id="successMessage" style="display: none"></div>
            <div class="alert alert-danger alert-wl mx-0" role="alert" id="failureMessage" style="display: none"></div>
    </section>
    </section>

     <div class="row mx-5 pt-3 pb-2" style="display: flex; align-items: center;">
          <div class="col-12 text-right">
            <button id="addProductButton" type="button" class="btn btn-wl mt-1" data-toggle="modal" data-target="#productSearchModal">Add Product(s)</button>
          <!--   <button type="button" class="btn btn-wl" data-toggle="modal" data-target="#addStoresModal">Add Store(s)</button>-->
        <!--  <button type="button" class="btn btn-wl" data-toggle="modal" data-target="#addStoresModal" onclick="setCurrentProductId('${product?.id}')">Add Store(s)</button>-->
       <!--  <button type="button" class="btn btn-sm btn-wl mt-1" data-toggle="modal" data-target="#addStoresModal" onclick="setCurrentProductId('${product?.id}')">Add Stores</button>-->
        <button type="button" class="btn btn-sm btn-wl mt-1" id="addStoresButton" data-toggle="modal" data-target="#addStoresModal" onclick="setCurrentProductId('${product?.id}')" disabled>Add Stores</button>
          </div>
          </div>

     <div class="row mt-4 ml-0 mr-0 bottom-border">
           <div class="col my-auto font-weight-bold">Item Code</div>
               <div class="col my-auto font-weight-bold">Barcode</div>
               <div class="col my-auto font-weight-bold">Description</div>
               <div class="col my-auto font-weight-bold">Category</div>
               <div class="col my-auto  font-weight-bold">Current Quantity</div>
               <div class="col my-auto  font-weight-bold">Current Quantity</div>
               <div class="col my-auto  font-weight-bold">Amended Quantity</div>
               <div class="col my-auto font-weight-bold">Total Number of Stores</div>
               <div class="col my-auto font-weight-bold">Action</div>
           </div>
           <div id="productList" class="align-content-center mb-5">
                               <g:if test="${(!productList?.productListItems || productList?.productListItems?.size() == 0) && (!unsavedVariants || unsavedVariants?.size() == 0) }">
                                   <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                               </g:if>

                               <g:each in="${productList?.productListItems}" var="productListItem" status="i">
                                   <g:render template="stockSearchResults" model="[productVariant: productListItem.productVariant, i: i]" />
                               </g:each>

                               <g:each in="${unsavedVariants}" var="productVariant" status="i">
                                   <g:render template="stockSearchResults" model="[productVariant: productVariant, i: i]" />
                               </g:each>
                           </div>

             <!-- Product search modal -->
             <g:render template="/product/productSearch" />


<div class="modal fade" id="addStoresModal" tabindex="-1" role="dialog" aria-labelledby="addStoresModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Add Store(s)</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <section id="filters-section" class="container-fluid">
                    <!-- Search filters section -->
                    <div class="row mt-3">
                        <div class="col-12">
                            <div id="filters" class="card bg-light border-wl">
                                <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                                    <div class="row">
                                        <div class="col-10">Store Search</div>
                                        <div class="col-2 text-right">
                                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                                <div class="card-body collapse show" id="filterCollapse">
                                    <div class="form-group row">
                                        <label for="modalStoreNumberFilter" class="col-3 col-form-label-sm text-right">Store Number</label>
                                        <div class="col-4">
                                            <input id="modalStoreNumberFilter" type="number" min="0" max="2147483647" name="storeNumberFilter" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                                        </div>
                                        <div class="col-5 text-right">
                                            <button id="filter-clear-button" type="button" class="btn btn-danger" onclick="clearStoreFilters();">Reset Filter</button>
                                            <button type="button" class="btn btn-success" onclick="addSelectedStores()">Save</button>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <label for="modalStoreNameFilter" class="col-3 col-form-label-sm text-right">Store Name</label>
                                        <div class="col-4">
                                            <input id="modalStoreNameFilter" name="storeNameFilter" class="form-control bottom-border" />
                                        </div>
                                        <div class="col-5 text-right">
                                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getStores();">Search</button>
                                            <button type="button" class="btn btn-wl ml-2" data-dismiss="modal">Cancel</button>
                                        </div>
                                    </div>
                                    <div class="form-group row">
                                        <div class="col-7 offset-3">
                                            <div class="form-check">
                                                <input class="form-check-input" type="checkbox" id="selectAllStores">
                                                <label class="form-check-label" for="selectAllStores">
                                                    All Stores
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Results Table Section -->
                <section id="store-results-section" class="container-fluid mt-3">
                    <div class="card bg-light border-wl">
                        <div class="card-body p-0">
                            <div id="store-loading-indicator" class="text-center py-3" style="display: none;">
                                <div class="spinner-border" role="status">
                                    <span class="sr-only">Loading...</span>
                                </div>
                            </div>
                            <div id="store-results-container">
                                <!-- Store results will be loaded here via AJAX -->
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </div>
</div>



</body>
</html>