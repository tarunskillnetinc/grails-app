package uk.co.wonderlane.wlpos

import org.springframework.security.web.authentication.WebAuthenticationDetails

import javax.servlet.http.HttpServletRequest

class WonderLaneAuthenticationDetails extends WebAuthenticationDetails {

    String storeId

    WonderLaneAuthenticationDetails(HttpServletRequest request) {
        super(request)
    }
}