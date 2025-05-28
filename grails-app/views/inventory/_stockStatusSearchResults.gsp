<div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border align-content-center">
    <div class="col-1 font-weight-bold">ID</div>
    <div class="col-2 font-weight-bold">Status</div>
    <div class="col-3 font-weight-bold">Total Number of Stores</div>
    <div class="col-3 font-weight-bold">Total Number of Products</div>
    <div class="col-2 font-weight-bold">Completed</div>
</div>

<g:if test="${stockAdjustmentResults && !stockAdjustmentResults.isEmpty()}">
    <g:each in="${stockAdjustmentResults}" var="result" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border align-content-center wl-striped${i%2} clickable-row"
             data-id="${result.id}"
             data-status="${result.status}"
             data-total-stores="${result.totalStores}"
             data-total-products="${result.totalProducts}"
             data-date-actioned="${result.dateActioned ? formatDate(date: result.dateActioned, format: 'dd/MM/yyyy HH:mm:ss') : '-'}">
            <div class="col-1">${result.id}</div>
            <div class="col-2">${result.status}</div>
            <div class="col-3">${result.totalStores}</div>
            <div class="col-3">${result.totalProducts}</div>
            <div class="col-2">
                <g:if test="${result.dateActioned}">
                    <g:formatDate date="${result.dateActioned}" format="dd/MM/yyyy HH:mm:ss" />
                </g:if>
                <g:else>
                    -
                </g:else>
            </div>
        </div>
    </g:each>
</g:if>
<g:else>
    <div class="row ml-0 mr-0 pt-4 pb-4 table-wl bottom-border text-center align-content-center">
        <div class="col-12">No stock adjustments found</div>
    </div>
</g:else>

<!-- Adjustment Details Modal -->
<div class="modal fade" id="adjustmentDetailsModal" tabindex="-1" role="dialog" aria-labelledby="adjustmentDetailsModalLabel" aria-hidden="true">
    <<div class="modal-dialog modal-lg" role="document">
     			<div class="modal-content">
     				<div class="modal-header">
     					<h5 class="modal-title">Stock Adjustments</h5>
     					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
     						<span aria-hidden="true">&times;</span>
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
</div>