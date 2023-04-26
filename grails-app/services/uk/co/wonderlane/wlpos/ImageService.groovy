package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class ImageService implements IImageService {

    def springSecurityService

    private final String customerDisplayFileLocation
    private final String receiptImagesFileLocation
    private final String buttonImagesFileLocation

    ImageService(String customerDisplayFileLocation, String receiptImagesFileLocation, String buttonImagesFileLocation) {
        this.customerDisplayFileLocation = customerDisplayFileLocation
        this.receiptImagesFileLocation = receiptImagesFileLocation
        this.buttonImagesFileLocation = buttonImagesFileLocation
    }

    @Override
    def getButtonImage(int buttonId) throws Exception {
        def filePath = Path.of(buttonImagesFileLocation, File.separator, String.valueOf(springSecurityService.principal.retailerId), File.separator, buttonId + ".png")

        return getImageFromFile(filePath.toString())
    }

    @Override
    def saveButtonImage(int buttonId, byte[] imageBytes) throws Exception {
        def directory = Path.of(buttonImagesFileLocation, File.separator, String.valueOf(springSecurityService.principal.retailerId))
        def fileName = buttonId + ".png"

        saveImageToFile(directory.toString(), fileName, imageBytes)
    }

    @Override
    def deleteButtonImage(int buttonId) throws Exception {
        def filePath = Path.of(buttonImagesFileLocation, File.separator, String.valueOf(springSecurityService.principal.retailerId), File.separator, buttonId + ".png")

        deleteFile(filePath.toString())
    }

    @Override
    def getCustomerDisplayImages() throws Exception {
        return null
    }

    @Override
    def getReceiptImage() throws Exception {
        return null
    }

    def getCfdImagesFromFile(int storeId, String directory) {
        if (directory != null) {
            File dir = new File(directory + storeId)

            if (dir.exists() && dir.listFiles().length != 0) {
                if (dir.list(new FilenameFilter() {
                    @Override
                    boolean accept(File file, String name) {
                        return name.equals("0.png")
                    }
                }).length == 0) {
                    return new ArrayList<byte[]>()
                }

                int index = 0;
                List<byte[]> result = new ArrayList<>()
                // sort using file names so the result of following block is same regardless of the OS returned file order
                for (File file : dir.listFiles().sort{it.name}) {
                    if (file.getName().equals(String.valueOf(index) + ".png")) {
                        result.add(file.getBytes())
                        index++
                    } else {
                        return result
                    }
                }
                return result
            } else {
                if (Files.createDirectories(dir.toPath())) {
                    System.println("Successfully created new base folder at: " + dir.toPath().toString())
                }
                return new ArrayList<byte[]>()
            }
        }
    }

    def saveCfdImagesToFile(int storeId, String directory, List<byte[]> images) {
        if (directory != null) {
            File dir = new File(directory + storeId)
            if (dir.exists()) {
                dir.deleteDir()
            }

            Files.createDirectories(dir.toPath())

            try {
                int index = 0;
                for (byte[] image : images) {
                    File file = new File(dir.toPath().toString() + File.separator + index + ".png")
                    OutputStream outputStream
                    try {
                        outputStream = new FileOutputStream(file)
                        outputStream.write(image)
                        index++;
                    } finally {
                        if(outputStream) {
                            outputStream.close()
                        }
                    }

                }
                return true
            } catch (IOException e) {
                e.printStackTrace()
                return false
            }
        }
    }

    def getImageFromFile(String filename) {
        if (filename != null && !filename.isEmpty()) {
            File file = new File(filename)

            if (file.exists()) {
                return file.getBytes()
            } else {
                return null
            }
        }

        return null
    }

    def saveImageToFile(String directory, String filename, byte[] image) {
        if (directory != null && !directory.isEmpty() && filename != null && !filename.isEmpty()) {
            File file = new File(directory + File.separator + filename)
            if (file.exists()) {
                Files.delete(file.toPath())
            }

            Files.createDirectories(Paths.get(directory))

            try {
                OutputStream outputStream = new FileOutputStream(file)
                outputStream.write(image)
                outputStream.close()
                return true
            } catch (IOException e) {
                e.printStackTrace()
                return false
            }
        }
    }

    def deleteFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath)

            if (file.exists()) {
                Files.delete(file.toPath())
            }
        }
    }
}
