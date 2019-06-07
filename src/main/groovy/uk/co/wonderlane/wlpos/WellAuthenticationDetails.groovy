package uk.co.wonderlane.wlpos

import org.springframework.security.web.authentication.WebAuthenticationDetails

import javax.servlet.http.HttpServletRequest

class WellAuthenticationDetails extends WebAuthenticationDetails {

    String storeId

    WellAuthenticationDetails(HttpServletRequest request) {
        super(request)
    }
}