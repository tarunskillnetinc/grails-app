%{--<div id="modal-header" class="modal-header">--}%
%{--    <h2>Spot Check</h2>--}%
%{--</div>--}%

%{--<!-- Adjusting the height and adding space between rows -->--}%
%{--<div class="row mt-3 mb-2" style="height: 400px;"> <!-- Increased height here -->--}%
%{--    <div class="col-8 pr-0 mx-auto" style="flex: 0 0 60%; max-width: 60%;">--}%
%{--        <!-- Centered header for till and shift info -->--}%
%{--        <div class="row text-center">--}%
%{--            <p class="mx-auto">--}%
%{--                Spot check for till ${shift?.tillId} shift ${shift?.shiftNumber}--}%
%{--            </p>--}%
%{--        </div>--}%

%{--    <!-- Aligned layout for Tender and Expected amount with increased row spacing -->--}%
%{--    <div class="spot-check-list" style="padding: 10px; border: 1px solid #000; text-align: center;">--}%
%{--        <div class="spot-check-header d-flex justify-content-between" style="margin-bottom: 15px; font-weight: bold;">--}%
%{--            <span style="width: 50%; text-align: left;">Tender</span>--}%
%{--            <span style="width: 50%; text-align: right;">Expected amount</span>--}%
%{--        </div>--}%

%{--        <!-- Loop through the filtered tender types and display them -->--}%
%{--        <g:each var="tenderType" in="${tenderTypes}">--}%
%{--            <div class="spot-check-item d-flex justify-content-between" style="margin-bottom: 20px;">--}%
%{--                <span style="width: 50%; text-align: left;">${tenderType.name().toLowerCase().capitalize()}</span>--}%
%{--                <span style="width: 50%; text-align: right;"><g:formatNumber number="${expectedAmounts[tenderType] ?: BigDecimal.ZERO}" type="currency" /></span>--}%
%{--            </div>--}%
%{--        </g:each>--}%
%{--    </div>--}%


%{--    </div>--}%
%{--</div>--}%

%{--<div class="modal-footer">--}%
%{--    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Cancel</button>--}%
%{--</div>--}%

<div id="modal-header" class="modal-header">
    <h2>Spot Check</h2>
</div>

<style>
.spot-check-list {
    width: 80%;
    margin: 0 auto;
    text-align: center;
}
.spot-check-row {
    display: flex;
    justify-content: center;
    margin-bottom: 10px;
}
.spot-check-row span {
    width: 50%;
}
</style>

<div class="row mt-3 mb-2" style="height: 400px;">
    <div class="col-8 pr-0 mx-auto" style="flex: 0 0 70%; max-width: 70%;">
        <div class="row text-center">
            <p class="mx-auto">
                Spot check for till ${shift?.tillId} Shift ${shift?.shiftNumber}
            </p>
        </div>

        <div class="spot-check-list" style="padding: 10px; border: 1px solid #000;">
            <div class="spot-check-row" style="font-weight: bold;">
                <span>Tender</span>
                <span>Expected amount</span>
            </div>

            <g:each var="tenderType" in="${tenderTypes}">
                <div class="spot-check-row">
                    <span>${tenderType.name().toLowerCase().capitalize()}</span>
                    <span><g:formatNumber number="${expectedAmounts[tenderType] ?: BigDecimal.ZERO}" type="currency" /></span>
                </div>
            </g:each>
        </div>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Cancel</button>
</div>