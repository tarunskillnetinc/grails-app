package uk.co.wonderlane.wlpos


import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.charity.CharitySortParams

@Secured(['ROLE_ENGINEER'])
class CharityOrganisationsController {
    def springSecurityService
    def financialWeekService
    def charityService

    private static final CHARITY_SORT_COLUMNS = [ "id", "organisationName" , "type", "memberNumber" , "active" ]

    @Secured(['ROLE_ENGINEER'])
    def index() {}

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddCharity() {
        render(template: "addCharity", model: [enableSave : true])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetSearchCharity(CharitySortParams sortParams) {
        session.CHARITY_TYPE_SEARCH_TERM = params.organisationTypeTerm
        session.CHARITY_MEMBER_NUMBER_SEARCH_TERM = params.charityMemberNumberTerm
        session.CHARITY_DESCRIPTION_SEARCH_TERM = params.charityGroupDescriptionTerm
        session.INCLUDE_DELETED_CHARITIES = params.includeDeletedCharitiesTerm

        sortParams.validateParams(CHARITY_SORT_COLUMNS) //pre process supplier sorting column list

        def charities = [] //declare charity list
        def suppliersResponse = supplierService.getSuppliers(params.supplierNameTerm, params.supplierReferenceTerm, params.customerReferenceTerm, params.includeDeletedSuppliers,
                sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
        def returnedSuppliers = suppliersResponse?.suppliers
        def totalCount = suppliersResponse?.totalCount
        if (returnedSuppliers != null && returnedSuppliers.size() > 0){
            suppliers = returnedSuppliers
        }
        render(template: "supplierSearchResults",
                model: [ suppliers: suppliers,
                         searchTerm: params.searchTerm,
                         max: sortParams.max ?: 50,
                         offset: sortParams.offset,
                         sortParams  : sortParams,
                         totalCount : totalCount
                ])
    }

/*
    def ajaxGetSearchSupplier(SupplierSortParams sortParams) {
        session.SUPPLIER_CUSTOMER_REFERENCE_SEARCH_TERM = params.customerReferenceTerm
        session.SUPPLIER_REFERENCE_SEARCH_TERM = params.supplierReferenceTerm
        session.SUPPLIER_NAME_SEARCH_TERM = params.supplierNameTerm
        session.INCLUDE_DELETED_SUPPLIERS = params.includeDeletedSuppliers

        sortParams.validateParams(SUPPLIER_SORT_COLUMNS) //pre process supplier sorting column list

        def suppliers = [] //declare supplier list
        def suppliersResponse = supplierService.getSuppliers(params.supplierNameTerm, params.supplierReferenceTerm, params.customerReferenceTerm, params.includeDeletedSuppliers,
                sortParams.offset ? sortParams.offset : 0, sortParams.max ? sortParams.max : 50, sortParams.sortColumn, sortParams.getSortOrder())
        def returnedSuppliers = suppliersResponse?.suppliers
        def totalCount = suppliersResponse?.totalCount
        if (returnedSuppliers != null && returnedSuppliers.size() > 0){
            suppliers = returnedSuppliers
        }
        render(template: "supplierSearchResults",
                model: [ suppliers: suppliers,
                         searchTerm: params.searchTerm,
                         max: sortParams.max ?: 50,
                         offset: sortParams.offset,
                         sortParams  : sortParams,
                         totalCount : totalCount
                ])
    }
    */

}