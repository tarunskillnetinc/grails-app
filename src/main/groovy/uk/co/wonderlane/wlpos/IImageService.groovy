package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.entities.ImageRecord

interface IImageService {

    def getButtonImage(int buttonId) throws Exception
    def saveButtonImage(int buttonId, byte[] imageBytes) throws Exception
    def deleteButtonImage(int buttonId) throws Exception
    def getCustomerDisplayImages() throws Exception
    def getReceiptImage() throws Exception
}