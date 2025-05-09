class TerminologyTagLib {

    def springSecurityService

    def terminology = { attrs, body ->
        switch (attrs.case?.toLowerCase()) {
            case "lower":
                out << springSecurityService.principal?.retailer?.config?.retailerTerminologyConfig?."${attrs.term}"?.toLowerCase()
                break
            case "upper":
                out << springSecurityService.principal?.retailer?.config?.retailerTerminologyConfig?."${attrs.term}"?.toUpperCase()
                break
            default:
                out << springSecurityService.principal?.retailer?.config?.retailerTerminologyConfig?."${attrs.term}"
                break
        }
    }
}