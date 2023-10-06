package uk.co.wonderlane.wlpos

interface IBrandAssetsService {

    def getBrandLogo() throws Exception
    def saveBrandLogo(byte[] brandLogo) throws Exception
    def resetBrandLogo() throws Exception
}