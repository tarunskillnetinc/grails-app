<div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
    <div class="card-header pointer" id="storeAdditionalDetails" data-toggle="collapse" data-target="#collapseStoreAdditionalDetails" aria-expanded="true" aria-controls="collapseStoreAdditionalDetails">
        <div class="row">
            <div class="col-10 font-weight-bold">Store Additional Details</div>
            <div class="col-2 text-right">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                </svg>
            </div>
        </div>
    </div>

    <div id="collapseStoreAdditionalDetails" class="collapse" aria-labelledby="storeAdditionalDetails" data-parent="#accordion">
        <div class="card-body py-5">
            <div class="col-12" id="storeAdditionalDetailsContainer">
                <g:render template="storeAdditionalDetail" model="[storeAdditionalDetails: storeAdditionalDetails]" />
            </div>
            <div class="col-5 col-lg-4 offset-lg-3 col-form-label text-right pr-4 mr-3">
                <button type="button" class="btn btn-wl p-1" onclick="addStoreAdditionalDetail(null, null, null)">
                    Add additional store fields
                </button>
            </div>
        </div>
    </div>
</div>