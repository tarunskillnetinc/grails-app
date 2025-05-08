package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.Role

class GroupController {

    def springSecurityService
    def userService
    def groupService

    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect (view:"/")
            return
        }

        def groups = groupService.getGroupsByLevel(1)
        def groupLevels = GroupLevel.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "level", order: "ASC"])

        [groups: groups, lowestLevel: groupLevels.last().level]
    }

    def ajaxGetChildGroups(int parentId) {
        if (parentId > 0) {
            def parentGroup = Group.findByRetailerIdAndId(springSecurityService.principal.retailerId, parentId)
            def groupLevels = GroupLevel.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "level", order: "ASC"])

            if (parentGroup.level.level == groupLevels.last().level) {
                render(template: "storeResults", model: [parentId: parentId, group: parentGroup])
            } else {
                render(template: "groupResults", model: [parentId: parentId, groups: Group.findAllByParentGroup(parentGroup, [sort: "name"])])
            }
        } else {
            render(template: "groupResults", model: [parentId: parentId, groups: groupService.getGroupsByLevel(1)])
        }
    }

    def ajaxGetAvailableStores(int groupId) {
        def group = Group.get(groupId)
        def storeSettings = Store.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "id", order: "ASC"])

        storeSettings.removeAll(group.stores)

        render (template: "availableStores", model: [groupId: groupId, stores: storeSettings])
    }

    def ajaxAddStoreToGroup(int storeId, int groupId) {
        def store = Store.findByIdAndRetailerId(storeId, springSecurityService.principal.retailerId)
        def group = Group.findByIdAndRetailerId(groupId, springSecurityService.principal.retailerId)

        group.addToStores(store)

        groupService.saveGroup(group)

        response.status = 204
    }

    def ajaxRemoveStoreFromGroup(int storeId, int groupId) {
        def store = Store.findByIdAndRetailerId(storeId, springSecurityService.principal.retailerId)
        def group = Group.findByIdAndRetailerId(groupId, springSecurityService.principal.retailerId)

        group.removeFromStores(store)

        groupService.saveGroup(group)

        response.status = 204
    }

    def select() {
        if (springSecurityService.principal.storeId) {
            session.LOGIN_TYPE = "STORE"
            render (view: "/index")
            return
        } else {
            session.LOGIN_TYPE = "GROUP"
        }

        def user = userService.getUser(springSecurityService.principal.id)

        def groups = getGroupHierarchy(user.groups)

        [groups: flattenGroupHierarchy(groups)]
    }

    def selectGroup(int id) {
        Group group = Group.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)

        session.GROUP_ID = id
        session.GROUP_NAME = group.name
        // TODO CHANGE HERE
        session.STORE_ID = groupService.getFirstStoreInGroupHierarchy(group)?.storeId // Use the first store ID in the group to retrieve data.

        render (view: "/index")
    }

    def add() {
        [groupLevels: GroupLevel.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "level", order: "ASC"])]
    }

    def save() {
        def group = new Group()

        bindData(group, params)

        if (group.validate()) {
            // TODO Ability to add users to groups from the users page. For now just adding all users to new groups.
            def users = User.findAllByRetailerIdAndRole(springSecurityService.principal.retailerId, Role.ENGINEER)
            users.each {
                group.addToUsers(it)
            }

            group.retailerId = springSecurityService.principal.retailerId

            groupService.saveGroup(group)

            flash.message = "Group successfully added"

            redirect(action: "index")
        } else {
            render(view: "add", model: [group: group, groupLevels: GroupLevel.findAllByRetailerId(springSecurityService.principal.retailerId, [sort: "level", order: "ASC"]), groupValues: getGroupsForParentType(group.type)])
        }
    }

    def ajaxGetParentGroups(int level) {
        def groups = groupService.getGroupsByLevel(level - 1)

        render g.select(name: "parentGroup.id", class: "col-12 form-control select-border", optionKey: "id", optionValue: "name", from: groups)
    }

    private List<Group> getGroupHierarchy (def groups) {
        def idsToElements = [:]
        groups?.each {
            idsToElements[it.id] = it
        }

        def topLevel = []

        groups?.sort{ it.name }?.each {
            if (it.parentGroup)  {
                def parentGroup = idsToElements[it.parentGroup.id]
                if (!parentGroup) {
                    // Parent element missing in list; treat as top-level
                    topLevel.add(it)
                } else  {
                    parentGroup.children.add(it)
                }
            } else  {
                topLevel.push(it)
            }
        }

        return topLevel
    }

    private List<Group> flattenGroupHierarchy(def groups) {
        List<Group> flattenedGroups = []

        groups?.each {
            flattenedGroups.add(it)

            if (it.groups) {
                flattenedGroups.addAll(flattenGroupHierarchy(it.groups))
            }
        }

        return flattenedGroups
    }
}