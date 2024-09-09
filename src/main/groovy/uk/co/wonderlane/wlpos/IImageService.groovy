package uk.co.wonderlane.wlpos

interface IImageService {

    def getImage(ImageRecord imageRecord) throws Exception
    def saveImage(ImageRecord imageRecord, byte[] imageBytes) throws Exception
    def deleteImage(ImageRecord imageRecord) throws Exception
}