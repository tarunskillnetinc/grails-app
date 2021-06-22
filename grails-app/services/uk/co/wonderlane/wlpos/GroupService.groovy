package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class GroupService {

    def springSecurityService

    def getStoresInGroup(int groupId) {
        Group group = Group.get(groupId)

        return getStoresInGroup(group)
    }

    def getStoresInGroup(Group group) {
        def stores = []

        stores.addAll(group.stores)

        def childGroups = Group.findAllByParentGroup(group)

        childGroups?.each {
            stores.addAll(getStoresInGroup(it))
        }

        return stores
    }

    def getFirstStoreInGroupHierarchy(int groupId) {
        Group group = Group.get(groupId)

        return getFirstStoreInGroupHierarchy(group)
    }

    def getFirstStoreInGroupHierarchy(Group group) {
        if (group.stores) {
            return group.stores?.sort{ it.id }?.getAt(0)
        }

        def childGroups = Group.findAllByParentGroup(group, [sort: "id"])

        if (childGroups) {
            for (Group childGroup : childGroups) {
                def store = getFirstStoreInGroupHierarchy(childGroup)

                if (store) {
                    return store
                }
            }
        }

        return null
    }

    def getGroup(int id) {
        return Group.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def getGroupsByLevel(int groupLevel) {
        def groupCriteria = Group.createCriteria()

        return groupCriteria.list ([sort: "name", order: "ASC"]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            level {
                eq ("level", groupLevel)
            }
        }
    }

    def saveGroup(Group group) {
        group.save()
    }
}
