package uk.co.wonderlane.wlpos

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

class WellAuthenticationProvider extends DaoAuthenticationProvider {

    def storeNumberValidator

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // If we reach here then a user with the username was found.
        // This line will include the password check.
        super.additionalAuthenticationChecks(userDetails, authentication)

        // This is all further custom checking.
        Object details = authentication.details

        if (!(details instanceof WellAuthenticationDetails)) {
            throw new BadCredentialsException(messages.getMessage("WellAuthenticationProvider.badCredentials", "Invalid username or password."));
        }

        def wellAuthenticationDetails = details as WellAuthenticationDetails

        // Do our store number check.
        if (!(userDetails instanceof WellUserDetails) && !wellAuthenticationDetails.storeId || wellAuthenticationDetails.storeId.length() > 10 || !wellAuthenticationDetails.storeId.isNumber() || ((userDetails instanceof WellUserDetails) && !storeNumberValidator.isValidStoreNumber(((WellUserDetails)userDetails).retailerId, Integer.parseInt(wellAuthenticationDetails.storeId)))) {
            throw new BadCredentialsException(messages.getMessage("WellAuthenticationProvider.badStoreId", "Store number not recognised."))
        }

        // Add the store ID to our user details object.
        if (userDetails instanceof WellUserDetails) {
            ((WellUserDetails)userDetails).storeId = Integer.parseInt(wellAuthenticationDetails.storeId)
        }
    }
}