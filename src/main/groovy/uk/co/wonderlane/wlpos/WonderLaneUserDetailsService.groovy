package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.gorm.transactions.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import uk.co.wonderlane.wlpos.enums.Role

class WonderLaneUserDetailsService implements GrailsUserDetailsService {

    static final List ENGINEER = [ new SimpleGrantedAuthority("ROLE_ENGINEER") ]
    static final List HEAD_OFFICE = [ new SimpleGrantedAuthority("ROLE_HEAD_OFFICE") ]
    static final List STORE_MANAGER = [ new SimpleGrantedAuthority("ROLE_STORE_MANAGER") ]
    static final List NO_ROLE = [ new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE) ]

    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        return loadUserByUsername(username)
    }

    @Transactional(readOnly=true, noRollbackFor=[IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = User.findByUsername(username)

        if (!user) {
            throw new NoStackUsernameNotFoundException()
        }

        return new WonderLaneUserDetails(user.username, user.password, user.active, true, true, true, getRole(user.role), user.id, user.retailerId, null, null, user.name, null)
    }

    static def getRole(Role role) {
        switch (role) {
            case Role.ENGINEER:
                return ENGINEER
            case Role.HEAD_OFFICE:
                return HEAD_OFFICE
            case Role.STORE_MANAGER:
                return STORE_MANAGER
            default:
                return NO_ROLE
        }
    }
}