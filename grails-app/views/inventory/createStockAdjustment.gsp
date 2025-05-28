	<%@ page contentType="text/html;charset=UTF-8" %>
	<html>
	<head>

		<meta name="layout" content="main" />
		<title>Inventory Management</title>

		  <asset:javascript src="validators/input-validator.js" />
		  <asset:javascript src="jquery-ui.js" />
		  <asset:stylesheet src="jquery-ui.css" />
	<script>
		var lastSelectedStores = [];

		function productSelected(id, itemCode, description) {
            var addProductUrl = "${createLink(controller: 'inventory', action: 'ajaxAddProduct')}";
            let productList = $("#productList");
            let warningMessage = $('#warning-message');

            // First check if product already exists in the list
            if ($('#product' + id).length > 0) {
                if (warningMessage.length) {
                    warningMessage.text("This product has already been added to the adjustment list.");
                    warningMessage.removeClass("hidden").addClass("show");

                    // Hide the warning after 3 seconds
                    setTimeout(function() {
                        warningMessage.removeClass("show").addClass("hidden");
                    }, 3000);
                }
                return false; // Exit function
            }

            // If we get here, product isn't in the list yet
            $.ajax({
                url: addProductUrl,
                method: 'POST',
                data: {
                    productId: id,
                    itemCode: itemCode,
                    description: description
                },
                success: function(resp) {
                    // Hide any existing warning
                    if (warningMessage.length) {
                        warningMessage.addClass("hidden").removeClass("show");
                    }

                    // Add the new product
                    productList.append(resp);
                    $('#noResultsRow').hide();

                    // Update row styling and IDs
                    let i = productList.children().length - 1;
                    let row = $('#product' + id);
                    row.addClass("wl-striped" + ((i-1) % 2));

                    // Update element IDs to maintain uniqueness
                    row.find('#prod-0-id').attr("id", "prod-" + i + "-id");
                    row.find('#prod-0-sku').attr("id", "prod-" + i + "-sku");
                    row.find('#prod-0-description').attr("id", "prod-" + i + "-description");
                    row.find('#prod-0-colour').attr("id", "prod-" + i + "-colour");
                    row.find('#prod-0-size').attr("id", "prod-" + i + "-size");
                    row.find('#prod-0-remove-btn').attr("id", "prod-" + i + "-remove-btn");

                    // Apply last selected stores if any exist
                    if (lastSelectedStores.length > 0) {
                        var storeNames = lastSelectedStores.map(store => store.name).join(', ');
                        updateProductStores(id, lastSelectedStores, lastSelectedStores.length, storeNames);
                    }

                    // Enable store button if needed
                    checkProductsAndEnableStoreButton();
                },
                error: function(xhr) {
                    if (warningMessage.length) {
                        warningMessage.text("Failed to add product: " + xhr.responseText);
                        warningMessage.removeClass("hidden").addClass("show");
                    }
                    console.error("Error adding product:", xhr.responseText);
                }
            });
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

                     // ADDED: Restore previously selected stores after AJAX completes
                     if (currentProductId) {
                         var storesData = $('#product' + currentProductId + ' .selected-stores').val();
                         if (storesData) {
                             try {
                                 var stores = JSON.parse(storesData);
                                 // Check the boxes for stores already selected
                                 stores.forEach(function(store) {
                                     $('.store-checkbox[data-store-id="' + store.id + '"]').prop('checked', true);
                                 });
                                 updateSelectAllCheckbox();
                             } catch (e) {
                                 console.error("Error parsing stores data:", e);
                             }
                         }
                     } else if (lastSelectedStores && lastSelectedStores.length > 0) {
                         // If no specific product, use last selected stores
                         lastSelectedStores.forEach(function(store) {
                             $('.store-checkbox[data-store-id="' + store.id + '"]').prop('checked', true);
                         });
                         updateSelectAllCheckbox();
                     }
                 },
                 error: function(xhr, status, error) {
                     $("#store-loading-indicator").hide();
                     $('#store-results-container').html('<div class="alert alert-danger mt-3">Error loading stores: ' + error + '</div>');
                     console.error("AJAX Error:", status, error);
                 }
             });
         }

        var currentProductId = null;

        function setCurrentProductId(productId) {
            currentProductId = productId;
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




		$(document).ready(function() {
			// Initialize view adjustment buttons based on store assignments
			$('.view-adjustment-btn').each(function() {
				var productId = $(this).closest('.row').attr('id').replace('product', '');
				var storesData = $('#product' + productId + ' .selected-stores').val();
				$(this).toggle(storesData && storesData !== '' && JSON.parse(storesData).length > 0);
			});
		});


		 function updateProductStores(productId, storeData, count, names) {
             $('#product' + productId + ' .selected-stores').val(JSON.stringify(storeData));
             $('#product' + productId + ' .stores-count').text(count);
             $('#product' + productId + ' .stores-list').text(names);

             // Show/hide the view adjustment button
             var viewBtn = $('#product' + productId + ' .view-adjustment-btn');
             viewBtn.toggle(storeData && storeData.length > 0);

             // Check if any product has stores assigned
             var hasStores = false;
             $('.selected-stores').each(function() {
                 if ($(this).val() && JSON.parse($(this).val()).length > 0) {
                     hasStores = true;
                     return false; // break out of the loop
                 }
             });

             // Show/hide the Save button based on whether any product has stores
             if (hasStores) {
                 $('#saveButton').removeClass('hidden');
             } else {
                 $('#saveButton').addClass('hidden');
             }
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

            if (hasProducts) {
                $('#addStoresButton').removeClass('hidden');
            } else {
                $('#addStoresButton').addClass('hidden');
            }
        }

		function removeProduct(productId) {
            var removeUrl = "${createLink(controller: 'inventory', action: 'ajaxRemoveProduct')}";

            $.ajax({
                url: removeUrl,
                method: 'POST',
                data: { productId: productId },
                success: function() {
                    $('#product' + productId).remove();
                    var productList = $('#productList');

                    if (productList.children().length === 1) {
                        var noResultsRow = $('#noResultsRow');
                        noResultsRow.removeClass("wl-striped0");
                        noResultsRow.removeClass("wl-striped1");
                        noResultsRow.addClass("wl-striped0");
                        noResultsRow.show();
                        // Hide Save button when no products left
                        $('#saveButton').addClass('hidden');
                    } else {
                        for (var i = 1; i <= productList.children().length; i++) {
                            var child = $('#productList>div:nth-child(' + i + ')');
                            child.removeClass("wl-striped0");
                            child.removeClass("wl-striped1");
                            child.addClass("wl-striped" + (i % 2));
                        }
                        // Check if any remaining products have stores
                        var hasStores = false;
                        $('.selected-stores').each(function() {
                            if ($(this).val() && JSON.parse($(this).val()).length > 0) {
                                hasStores = true;
                                return false;
                            }
                        });
                        $('#saveButton').toggleClass('hidden', !hasStores);
                    }
                    checkProductsAndEnableStoreButton();
                },
                error: function(xhr) {
                    alert('Failed to remove product: ' + xhr.responseText);
                }
            });
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

			$(document).ready(function() {
				// Handle save button click
				$('#saveButton').click(function() {
					$('#saveConfirmationModal').modal('show');
				});

				// Handle confirmation - save stores then redirect
				$('#confirmSaveYes').click(function() {
					$('#saveConfirmationModal').modal('hide');

					var amendedQuantities = {};
					$('.amended-quantity-input').each(function() {
						var productId = $(this).attr('name').replace('amendedQuantity_', '');
						var amendedQty = $(this).val();
						if (amendedQty !== '') {
							amendedQuantities[productId] = amendedQty;
						}
					});

					$.ajax({
						url: "${createLink(controller: 'inventory', action: 'finalizeStockAdjustment')}",
						method: "POST",
						data: {
							amendedQuantities: JSON.stringify(amendedQuantities)
						},
						success: function() {
							window.location.href = "${createLink(controller: 'inventory', action: 'index')}?successMessage=Stock+adjustment+has+been+scheduled";
						},
						error: function(xhr) {
							alert("Failed to schedule stock adjustment: " + xhr.responseText);
						}
					});
				});
			});

		   $(document).ready(function() {
				   // Handle search button click
				   $('#inventoryProductSearchButton').click(function() {
					   searchProducts();
				   });

				   // Handle Enter key in search field
				   $('#inventoryProductSearchTerm').keypress(function(e) {
					   if (e.which == 13) {
						   searchProducts();
					   }
				   });
			   });

		   function searchProducts() {
			   var searchTerm = $('#inventoryProductSearchTerm').val();
			   var searchBy = $('#inventoryProductSearchBy').val();



			   $.ajax({
				   url: "${createLink(controller: 'inventory', action: 'ajaxSearchProducts')}",
				   data: {
					   searchTerm: searchTerm,
					   searchBy: searchBy,
					   max: 50,
					   offset: 0
				   },
				   success: function(resp) {
					   $('#inventoryProductSearchResults').html(resp);
				   },
				   error: function(xhr, status, error) {
					   console.error("Error searching products:", error);
					   alert('Error searching products. Please try again.');
				   }
			   });
		   }

		  $(document).ready(function() {
			  // Handle search button click
			  $('#inventoryProductSearchButton').click(function() {
				  searchProducts();
			  });

			  // Handle Enter key in search field
			  $('#inventoryProductSearchTerm').keypress(function(e) {
				  if (e.which == 13) {
					  searchProducts();
				  }
			  });

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

		  $(document).ready(function() {
			  // Handle reset button click
			  $('#inventoryResetFilterButton').click(function() {
				  resetProductSearch();
			  });
		  });

		  function resetProductSearch() {
			  // Clear search term
			  $('#inventoryProductSearchTerm').val('');

			  // Reset search by dropdown to default
			  $('#inventoryProductSearchBy').val('everything');

			  // Clear search results
			  $('#inventoryProductSearchResults').html('');

			  // Optional: You could also trigger an empty search if you want to show all products
			  // searchProducts();
		  }

        function viewAdjustments(productId) {
                     // Get the selected stores data
                     var storesData = $('#product' + productId + ' .selected-stores').val();

                     if (!storesData || storesData === '') {
                         alert('No stores have been assigned to this product.');
                         return;
                     }

                     try {
                         var stores = JSON.parse(storesData);
                         var productRow = $('#product' + productId);

                         // Debug: Log the entire product row HTML
                         console.log("Product Row HTML:", productRow.html());

                         // Find all elements with IDs starting with 'prod-' in this row
                         var prodElements = productRow.find('[id^="prod-"]');
                         console.log("Product Elements:", prodElements);

                         // Get the product data - use more flexible selectors
                         var itemCode = productRow.find('[id$="-id"]').text().trim();
                         var barcode = productRow.find('[id$="-sku"]').text().trim();
                         var description = productRow.find('[id$="-description"]').text().trim();
                         var amendedQty = productRow.find('input[type="number"]').val() || '0';

                         // Debug: Log the retrieved values
                         console.log("Retrieved values:", {
                             itemCode: itemCode,
                             barcode: barcode,
                             description: description,
                             amendedQty: amendedQty
                         });

                         // Clear previous content
                         $('#adjustmentsTableBody').empty();

                         // Add rows for each store
                         stores.forEach(function(store) {
                             $('#adjustmentsTableBody').append(
                                 '<tr>' +
                                 '   <td>' + store.number + '</td>' +
                                 '   <td>' + store.name + '</td>' +
                                 '   <td>' + itemCode + '</td>' +
                                 '   <td>' + barcode + '</td>' +
                                 '   <td>' + description + '</td>' +
                                 '   <td>20</td>' + // Assuming current quantity is always 20 as shown in your template
                                 '   <td>' + amendedQty + '</td>' +
                                 '</tr>'
                             );
                         });

                         // Show the modal
                         $('#viewAdjustmentsModal').modal('show');
                     } catch (e) {
                         console.error("Error parsing stores data:", e);
                         alert('Error loading store data. Please try again.');
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
                $('#failureMessage').text('Please select at least one store.').show();
                setTimeout(function() {
                    $('#failureMessage').hide();
                }, 5000);
                return;
            }

            // Update last selected stores
            lastSelectedStores = selectedStoreData;

            // Update only the current product if specified, otherwise update all
            if (currentProductId) {
                updateProductStores(currentProductId, selectedStoreData, selectedStores.length, selectedStoreNames.join(', '));
            } else {
                // Update all products
                $('.selected-stores').val(JSON.stringify(selectedStoreData));
                $('.stores-count').text(selectedStores.length);
                $('.stores-list').text(selectedStoreNames.join(', '));

                // Show the Save button since we've added stores
                $('#saveButton').removeClass('hidden');
            }

            // Collect selected stores
            if (selectedStores.length > 0) {
                $.ajax({
                    url: '${createLink(controller: 'inventory', action: 'saveProductListStores')}',
                    method: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({stores: selectedStores}),
                    error: function(xhr) {
                        $('#failureMessage').text('Failed to save stores: ' + xhr.responseText).show();
                        setTimeout(function() {
                            $('#failureMessage').hide();
                        }, 5000);
                    }
                });
            } else {
                window.location.href = '${createLink(controller: 'inventory', action: 'index')}';
            }

            // Show/hide view adjustment button based on store count
            $('.view-adjustment-btn').each(function() {
                var productId = $(this).closest('.row').attr('id').replace('product', '');
                var storesData = $('#product' + productId + ' .selected-stores').val();
                $(this).toggle(storesData && storesData !== '' && JSON.parse(storesData).length > 0);
            });

            $('#addStoresModal').modal('hide');
            $('#successMessage').text('Successfully added ' + selectedStores.length + ' store(s)').show();
            setTimeout(function() {
                $('#successMessage').hide();
            }, 5000);
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
					 <button id="addProductButton" type="button" class="btn btn-wl mt-1" data-toggle="modal" data-target="#inventoryProductSearchModal">Add Product(s)</button>
					 <button id="cancelButton" type="button" class="btn btn-wl mt-1" onclick="window.location.href='${createLink(controller: 'inventory', action: 'index')}'">Cancel</button>

				</div>
			</div>
		</section>

		<section id="alerts-container" class="container-fluid">
				<div class="alert alert-success alert-wl mx-0" role="alert" id="successMessage" style="display: none"></div>
				<div class="alert alert-danger alert-wl mx-0" role="alert" id="failureMessage" style="display: none"></div>
		</section>
		</section>

		 <div class="row mx-3 pt-3 pb-2" style="display: flex; justify-content: flex-end;">
			  <div class="col-5 text-right">
                    <button type="button" class="btn btn-wl mt-1 hidden " id="addStoresButton" data-toggle="modal" data-target="#addStoresModal" onclick="setCurrentProductId('${product?.id}')" >Add Stores(s)</button>			  </div>
                    <button id="saveButton" type="button" class="btn btn-success mt-1 hidden">Save</button>
              </div>

		 <div class="row mt-4 ml-0 mr-0 bottom-border">
			   <div class="col my-auto font-weight-bold">Item Code</div>
				   <div class="col my-auto font-weight-bold">Barcode</div>
				   <div class="col my-auto font-weight-bold">Description</div>
				   <div class="col my-auto font-weight-bold">Category</div>
				   <div class="col my-auto  font-weight-bold">Aggregated Quantity</div>
				   <div class="col my-auto  font-weight-bold">Amended Quantity</div>
				   <div class="col my-auto font-weight-bold">Total Number of Stores</div>
				   <div class="col my-auto font-weight-bold"></div>
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



	<div class="modal fade" id="addStoresModal" tabindex="-1" role="dialog" aria-labelledby="addStoresModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg" role="document">
			<div class="modal-content">
				<div class="modal-header" style="display: flex; justify-content: space-between; align-items: center;">
                    <h5 class="modal-title" style="flex: 1; text-align: center; margin: 0;">Add Store(s)</h5>
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

	<!-- Confirmation Modal -->
	<div class="modal fade" id="saveConfirmationModal" tabindex="-1" role="dialog" aria-labelledby="saveConfirmationModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered" role="document">
			<div class="modal-content">
				<div class="modal-header bg-danger text-white text-center"> <!-- Added text-center -->
					<h5 class="modal-title w-100" id="saveConfirmationModalLabel">WARNING!</h5> <!-- Added w-100 for full width -->
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					</button>
				</div>
				<div class="modal-body text-center">
					<p>You are about to adjust/overwrite stock levels of the product selected within the stores assigned</p>
					<p>Do you wish to continue?</p>
				</div>
				<div class="modal-footer justify-content-center">
					<button type="button" class="btn btn-success" id="confirmSaveYes">YES</button>
					<button type="button" class="btn btn-danger" data-dismiss="modal">NO</button>
				</div>
			</div>
		</div>
	</div>


	<!---add product Model--->

	<div class="modal fade header-wl" id="inventoryProductSearchModal" tabindex="-1" role="dialog" aria-labelledby="inventoryProductSearchModalTitle" aria-hidden="true">
		<div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
			<div class="modal-content">
				<div class="modal-body">
					<h3 class="product-search-header">Select product</h3>

					<div class="row product-search-filters">
						<div class="input-group col-8">
							<g:textField id="inventoryProductSearchTerm" name="inventoryProductSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

							<div class="input-group-append">
								<g:select id="inventoryProductSearchBy" name="inventoryProductSearchBy" from="${['everything', 'description', 'itemCode', 'barcode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
							</div>
						</div>

						<div class="col-2">
							<div class="d-flex flex-column">
								<button id="inventoryProductSearchButton" name="inventoryProductSearchButton" class="btn btn-wl mt-1 w-100" style="min-width: 100px;">
									<i class="fas fa-filter"></i> Search
								</button>
								<button id="inventoryResetFilterButton" class="btn btn-danger mt-1 w-100" style="min-width: 100px;">
									<i class="fas fa-undo"></i> Reset
								</button>
							</div>
						</div>
					</div>

					<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
						<div class="col-2 font-weight-bold">Item Code</div>
						<div class="col-4 font-weight-bold">Description</div>
						<div class="col-2 font-weight-bold">SKU</div>
						<div class="col-2 font-weight-bold">Category</div>
						<div class="col-2 font-weight-bold">Actions</div>
					</div>

					<div id="inventoryProductSearchResults">
						<!-- Initial content - can be empty or loading message -->
					</div>
					<div class="row mt-3">
						<span class="col-12 text-right">
							<button class="btn btn-danger" data-dismiss="modal">Cancel</button>
						</span>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- View Adjustments Modal -->
	<div class="modal fade" id="viewAdjustmentsModal" tabindex="-1" role="dialog" aria-labelledby="viewAdjustmentsModalLabel" aria-hidden="true">
		<div class="modal-dialog modal-lg" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">Stock Adjustments</h5>
					<button type="button" class="btn btn-danger" data-dismiss="modal" aria-label="Close">
                        Exit
                    </button>
				</div>
				<div class="modal-body">
					<table class="table table-bordered">
						<thead class="thead-light">
							<tr>
								<th>Store ID</th>
								<th>Store Name</th>
								<th>Item Code</th>
								<th>Barcode</th>
								<th>Description</th>
								<th>Old Quantity</th>
								<th>New Quantity</th>
							</tr>
						</thead>
						<tbody id="adjustmentsTableBody">
							<!-- Content will be populated by JavaScript -->
						</tbody>
					</table>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
				</div>
			</div>
		</div>
	</div>

	</body>
	</html>