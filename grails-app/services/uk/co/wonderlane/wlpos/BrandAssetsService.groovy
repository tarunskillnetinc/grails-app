package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.dataaccess.FileSystemFileDal

class BrandAssetsService implements IBrandAssetsService {

    def springSecurityService

    private final String brandAssetsDirectory

    BrandAssetsService(String brandAssetsDirectory) {
        this.brandAssetsDirectory = brandAssetsDirectory
    }

    def getBrandLogo() {
        FileSystemFileDal fileDal = new FileSystemFileDal(brandAssetsDirectory + File.separator + springSecurityService.principal.retailerId + File.separator, new BackOfficeLogger())

        return fileDal.getFile("BrandLogo.png")
    }

    @Override
    def saveBrandLogo(byte[] brandLogo) throws Exception {
        FileSystemFileDal fileDal = new FileSystemFileDal(brandAssetsDirectory + File.separator + springSecurityService.principal.retailerId + File.separator, new BackOfficeLogger())

        fileDal.writeFile("BrandLogo.png", brandLogo)
    }

    @Override
    def resetBrandLogo() throws Exception {
        FileSystemFileDal fileDal = new FileSystemFileDal(brandAssetsDirectory + File.separator + springSecurityService.principal.retailerId + File.separator, new BackOfficeLogger())

        fileDal.deleteFile("BrandLogo.png")
    }
}