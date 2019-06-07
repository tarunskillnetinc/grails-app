package uk.co.wonderlane.wlpos

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

class WellAuthenticationProvider extends DaoAuthenticationProvider {

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication)

        Object details = authentication.details

        if (!(details instanceof WellAuthenticationDetails)) {
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        def wellAuthenticationDetails = details as WellAuthenticationDetails

        if (userDetails instanceof WellUserDetails) {
            ((WellUserDetails)userDetails).storeId = Integer.parseInt(wellAuthenticationDetails.storeId)
        }
    }
}