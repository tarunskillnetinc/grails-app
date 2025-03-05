package uk.co.wonderlane.wlpos


import groovy.json.JsonOutput
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_ENGINEER'])
class CharityOrganisationsController {
    def springSecurityService
    def financialWeekService

    @Secured(['ROLE_ENGINEER'])
    def index() {}

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxAddCharity() {
        render(template: "addCharity", model: [enableSave : true])
    }

}