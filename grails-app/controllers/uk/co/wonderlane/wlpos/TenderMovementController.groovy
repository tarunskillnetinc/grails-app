package uk.co.wonderlane.wlpos

class TenderMovementController {

    def index() {}

    def issueFloat(){
        [pageName : "Issue Float"]
    }

    def tenderLift(){
        [pageName : "Tender Lift"]
    }

    def payIn(){
        [pageName : "Pay In"]
    }

    def payOut(){
        [pageName : "Pay Out"]
    }

    def bankDeposit(){
        [pageName : "Bank Deposit"]
    }

    def bankReceipt(){
        [pageName : "Bank Receipt"]
    }


}
