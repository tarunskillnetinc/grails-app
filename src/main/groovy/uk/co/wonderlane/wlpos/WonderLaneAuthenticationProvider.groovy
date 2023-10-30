package uk.co.wonderlane.wlpos

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails

class WonderLaneAuthenticationProvider extends DaoAuthenticationProvider {

    def storeNumberValidator

    def retailerProvider

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

            def store = storeNumberValidator.getStore(((WonderLaneUserDetails)userDetails).retailerId, null)
            ((WonderLaneUserDetails)userDetails).retailer = retailerProvider.getRetailer(((WonderLaneUserDetails)userDetails).retailerId)

            if (store) {
                ((WonderLaneUserDetails)userDetails).priceBand = store.priceBand
                ((WonderLaneUserDetails)userDetails).range = store.range
            }
        } else if (userDetails instanceof WonderLaneUserDetails && wonderLaneAuthenticationDetails.storeId.length() < 10 && wonderLaneAuthenticationDetails.storeId.isNumber()) {
            def store = storeNumberValidator.getStore(((WonderLaneUserDetails)userDetails).retailerId, Integer.parseInt(wonderLaneAuthenticationDetails.storeId))

            if (store && store.id > 0) {
                // Add the store number, store ID, price band, range and retailer objects to our user details object.
                ((WonderLaneUserDetails)userDetails).storeNumber = Integer.parseInt(wonderLaneAuthenticationDetails.storeId)
                ((WonderLaneUserDetails)userDetails).storeId = store.id
                ((WonderLaneUserDetails)userDetails).priceBand = store.priceBand
                ((WonderLaneUserDetails)userDetails).range = store.range
                ((WonderLaneUserDetails)userDetails).retailer = retailerProvider.getRetailer(((WonderLaneUserDetails)userDetails).retailerId)
            } else {
                throw new BadCredentialsException(messages.getMessage("WonderLaneAuthenticationProvider.storeNotFound", "Store number not found."))
            }
        } else {
            throw new BadCredentialsException(messages.getMessage("WonderLaneAuthenticationProvider.badStoreId", "Invalid store number."))
        }
    }
}