package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class ImageService {

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
                for (File file : dir.listFiles()) {
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
                    OutputStream outputStream = new FileOutputStream(file)
                    outputStream.write(image)
                    index++;
                }
                return true
            } catch (IOException e) {
                e.printStackTrace()
                return false
            }
        }
    }

    def getImageFromFile(String directory, String filename) {
        if (directory != null && !directory.isEmpty() && filename != null && !filename.isEmpty()) {
            File file = new File(directory + filename)
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
            File file = new File(directory + filename)
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

    def deleteFile(String directory, String filename) {
        if (directory != null && !directory.isEmpty() && filename != null && !filename.isEmpty()) {
            File file = new File(directory + filename)
            if (file.exists()) {
                Files.delete(file.toPath())
            }
        }
    }
}
