package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

class WellUserDetails extends GrailsUser {

    int storeId
    int retailerId
    String usersName

    public WellUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
                           boolean accountNonLocked, Collection<GrantedAuthority> authorities, id, int retailerId, int storeId, String usersName) {

        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)

        this.retailerId = retailerId
        this.storeId = storeId
        this.usersName = usersName
    }
}