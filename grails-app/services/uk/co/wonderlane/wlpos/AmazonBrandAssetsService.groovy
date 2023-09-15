package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.dataaccess.AmazonS3FileDal

class AmazonBrandAssetsService implements IBrandAssetsService {

    def springSecurityService

    private final AmazonS3FileDal fileDal

    AmazonBrandAssetsService(String brandAssetsBucket) {
        fileDal = new AmazonS3FileDal(brandAssetsBucket, new BackOfficeLogger())
    }

    def getBrandLogo() {
        return fileDal.getFile("${springSecurityService.principal.retailerId}/BrandLogo.png")
    }

    @Override
    def saveBrandLogo(byte[] brandLogo) throws Exception {
        fileDal.writeFile("${springSecurityService.principal.retailerId}/BrandLogo.png", brandLogo)
    }

    @Override
    def resetBrandLogo() throws Exception {
        fileDal.deleteFile("${springSecurityService.principal.retailerId}/BrandLogo.png")
    }
}
