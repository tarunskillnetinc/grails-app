<%@ page import="uk.co.wonderlane.wlpos.enums.SymbolGroupSubscriptionStatus" %>
<div class="modal-header">
    <h2>Add Supplier Affiliation</h2>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new supplier affiliation.</div>

    <g:form name="addSymbolGroupSubscriptionForm">
        <g:hiddenField name="id" value="${symbolGroupSubscription?.id}"/>

        <div class="row form-group mb-4">
            <label for="symbolGroup.id" class="col-3 offset-1 col-form-label text-right">Supplier</label>

            <div class="input-group col-4">
                <g:select name="symbolGroup.id" from="${symbolGroups}"
                          value="${symbolGroupSubscription?.symbolGroup?.id}"
                          optionKey="id" optionValue="name" class="form-control select-border"
                          noSelection="[null: 'Please select']"
                          onChange="symbolGroupSubscripionSupplierChanged(this.value);"/>
            </div>
        </div>

        <div id="affiliationForm">
            <g:if test="${symbolGroupSubscription != null}">
                <g:if test="${symbolGroupSubscription.getSymbolGroup().getId() == 1}">
                    <g:render template="addSymbolGroupSubscriptionNisa"
                              model="[symbolGroupSubscription: symbolGroupSubscription]"/>
                </g:if>
                <g:elseif test="${symbolGroupSubscription.getSymbolGroup().getId() == 4}">
                    <g:render template="addSymbolGroupSubscriptionSnappy"
                              model="[symbolGroupSubscription: symbolGroupSubscription]"/>
                </g:elseif>
            </g:if>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddSymbolGroupSubscriptionButton" class="btn btn-secondary"
            data-dismiss="modal" onclick="getSymbolGroupSubscriptions()">Cancel</button>
    <% def specifiedValues = [SymbolGroupSubscriptionStatus.PENDING, SymbolGroupSubscriptionStatus.IN_PROGRESS, SymbolGroupSubscriptionStatus.DELETED] %>
    <g:if test="${!specifiedValues.contains(symbolGroupSubscription.status)}">
        <button type="button" id="saveSynbolGroupSubscriptionButton" class="btn btn-success"
                onclick="saveSymbolGroupSubscription();">Save</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSynbolGroupSubscriptionButton" class="btn btn-wl disabled" title="Download in progress or deleted can not save">Save</button>
    </g:else>
</div>