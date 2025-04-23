<div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
    <div class="card-header pointer" id="storeOpeningHours" data-toggle="collapse" data-target="#collapseStoreOpeningHours" aria-expanded="true" aria-controls="collapseStoreOpeningHours">
        <div class="row">
            <div class="col-10 font-weight-bold">Opening Hours</div>
            <div class="col-2 text-right">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                </svg>
            </div>
        </div>
    </div>

    <div id="collapseStoreOpeningHours" class="collapse" aria-labelledby="storeOpeningHours" data-parent="#accordion">
        <div class="card-body py-5">
            <div class="col-12">
                <g:render template="openingHours" model="[commandPrefix: 'storeOpeningHoursCommand', storeOpeningHoursCommand: storeOpeningHoursCommand,
                                                          titleRegularOpeningHours: 'Regular Store Opening Hours', titleSpecialOpeningHours:'Special Store Opening/Close Date & Time',
                                                          addSpecialHoursButtonText: 'Add special opening hours']" />
            </div>
        </div>
    </div>
</div>
<div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
    <div class="card-header pointer" id="storeLicense" data-toggle="collapse" data-target="#collapseStoreLicense" aria-expanded="true" aria-controls="collapseStoreLicense">
        <div class="row">
            <div class="col-10 font-weight-bold">Store License</div>
            <div class="col-2 text-right">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                </svg>
            </div>
        </div>
    </div>

    <div id="collapseStoreLicense" class="collapse" aria-labelledby="storeLicense" data-parent="#accordion">
        <div class="card-body py-5">
            <div class="col-12">
                <h4><b>Alcohol Licenses:</b></h4>
                <div class="checkbox-wrapper d-flex align-items-center justify-content-start mb-5">
                    <label for="alcoholLicensingCommand.licensedToSellAlcohol" class="mb-0 mr-2" style="position: relative; z-index: 2;">
                        Beers, Wines and Spirits (BWS) License
                    </label>
                    <g:checkBox name="alcoholLicensingCommand.licensedToSellAlcohol"
                           id="alcoholLicensingCommand.licensedToSellAlcohol"
                           checked="${alcoholLicensingCommand?.licensedToSellAlcohol}"
                           class="form-check-input wl-checkbox" style="position: relative; z-index: 1;"/>
                </div>
                <div id="alcoholHoursContainer">
                    <g:render template="openingHours" model="[commandPrefix: 'alcoholLicensingCommand', alcoholLicensingCommand: alcoholLicensingCommand,
                                                          titleRegularOpeningHours: 'Alcohol Licensing Hours', titleSpecialOpeningHours: 'Special Alcohol Licensing Date & Time',
                    addSpecialHoursButtonText: 'Add special licensing hours']" />
                </div>
            </div>
        </div>
    </div>
</div>

<div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
    <div class="card-header pointer" id="storeRestrictions" data-toggle="collapse" data-target="#collapseStoreRestrictions" aria-expanded="true" aria-controls="collapseStoreRestrictions">
        <div class="row">
            <div class="col-10 font-weight-bold">Restrictions</div>
            <div class="col-2 text-right">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                </svg>
            </div>
        </div>
    </div>

    <div id="collapseStoreRestrictions" class="collapse" aria-labelledby="storeRestrictions" data-parent="#accordion">
        <div class="card-body py-5">
            <div class="col-12" id="storeRestrictionsContainer">
                <g:render template="storeRestrictions" model="[storeRestrictions: storeRestrictions]" />
            </div>
        </div>
    </div>
</div>

<div class="card bg-light border-wl accordion-card col-12 col-lg-10 offset-lg-1 px-0">
    <div class="card-header pointer" id="storeAmenities" data-toggle="collapse" data-target="#collapseStoreAmenities" aria-expanded="true" aria-controls="collapseStoreAmenities">
        <div class="row">
            <div class="col-10 font-weight-bold">Amenities</div>
            <div class="col-2 text-right">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                </svg>
            </div>
        </div>
    </div>

    <div id="collapseStoreAmenities" class="collapse" aria-labelledby="storeAmenities" data-parent="#accordion">
        <div class="card-body py-5">
            <div class="col-12">
                <g:render template="storeAmenity" model="[storeAmenities: storeAmenities, storeSettings: storeSettings]" />
            </div>
        </div>
    </div>
</div>
