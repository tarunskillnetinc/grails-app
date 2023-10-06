package uk.co.wonderlane.wlpos

interface IImageService {

    def getButtonImage(int buttonId) throws Exception
    def saveButtonImage(int buttonId, byte[] imageBytes) throws Exception
    def deleteButtonImage(int buttonId) throws Exception
    def getCustomerDisplayImages() throws Exception
    def getReceiptImage() throws Exception
}