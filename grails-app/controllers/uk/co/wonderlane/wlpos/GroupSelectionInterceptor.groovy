package uk.co.wonderlane.wlpos


class GroupSelectionInterceptor {

    GroupSelectionInterceptor() {
        // Just matching the actions available from the nav bar as a token gesture.
        match(controller:"user", action:"index")
        match(controller:"storeSettings", action:"index")
        match(controller:"storeSettings", action:"copy")
        match(controller:"group", action:"index")
        match(controller:"admin", action:"copystore")
    }

    boolean before() {
        if (session.LOGIN_TYPE == "GROUP" && !session.GROUP_NAME) {
            redirect (controller: "group", action: "select")
        }

        return true
    }

    boolean after() {
        return true
    }

    void afterView() {

    }
}