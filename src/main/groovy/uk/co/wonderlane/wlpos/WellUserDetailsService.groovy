package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.gorm.transactions.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

class WellUserDetailsService implements GrailsUserDetailsService {

    static final List NO_ROLES = [ new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE) ]

    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        return loadUserByUsername(username)
    }

    @Transactional(readOnly=true, noRollbackFor=[IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new NoStackUsernameNotFoundException()
        }

        return new WellUserDetails(user.username, user.password, user.active, true, true, true, NO_ROLES, user.id, user.retailerId, 0, user.name)
    }
}