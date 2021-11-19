package uk.co.wonderlane.wlpos

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

class WonderLaneAuthenticationProvider extends DaoAuthenticationProvider {

    def storeNumberValidator

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // If we reach here then a user with the username was found.
        // This line will include the password check.
        super.additionalAuthenticationChecks(userDetails, authentication)

        // This is all further custom checking.
        Object details = authentication.details

        if (!(details instanceof WonderLaneAuthenticationDetails)) {
            throw new BadCredentialsException(messages.getMessage("WonderLaneAuthenticationProvider.badCredentials", "Invalid username or password."));
        }

        def wonderLaneAuthenticationDetails = details as WonderLaneAuthenticationDetails

        // Do our store number check.
        if (!wonderLaneAuthenticationDetails.storeId) {
            def roles = userDetails.authorities?.collect {it.authority }

            if (!roles.contains("ROLE_ENGINEER") && !roles.contains("ROLE_HEAD_OFFICE")) {
                throw new BadCredentialsException(messages.getMessage("WonderLaneAuthenticationProvider.notHeadOffice", "You are not permitted to log in at head office level."))
            }
        } else if (!(userDetails instanceof WonderLaneUserDetails) && !wonderLaneAuthenticationDetails.storeId || wonderLaneAuthenticationDetails.storeId.length() > 10 || !wonderLaneAuthenticationDetails.storeId.isNumber() || ((userDetails instanceof WonderLaneUserDetails) && !storeNumberValidator.isValidStoreNumber(((WonderLaneUserDetails)userDetails).retailerId, Integer.parseInt(wonderLaneAuthenticationDetails.storeId)))) {
            throw new BadCredentialsException(messages.getMessage("WonderLaneAuthenticationProvider.badStoreId", "Store number not recognised."))
        }

        // Add the store number to our user details object.
        if (userDetails instanceof WonderLaneUserDetails && wonderLaneAuthenticationDetails.storeId) {
            ((WonderLaneUserDetails)userDetails).storeId = Integer.parseInt(wonderLaneAuthenticationDetails.storeId)
        }
    }
}