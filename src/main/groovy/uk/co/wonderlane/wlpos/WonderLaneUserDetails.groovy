package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

class WonderLaneUserDetails extends GrailsUser {

    int retailerId
    Integer storeNumber
    Integer storeId
    String usersName

    public WonderLaneUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
                                 boolean accountNonLocked, Collection<GrantedAuthority> authorities, id, int retailerId, Integer storeNumber, Integer storeId, String usersName) {

        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)

        this.retailerId = retailerId
        this.storeNumber = storeNumber
        this.storeId = storeId
        this.usersName = usersName
    }
}