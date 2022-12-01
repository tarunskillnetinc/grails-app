package uk.co.wonderlane.wlpos


import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import java.nio.channels.FileLock
import java.nio.charset.StandardCharsets

class ImageServiceSpec extends Specification implements ServiceUnitTest<ImageService>, DataTest {

    def setup() {
        // delete the test directory if exists before testing
        File testDirectory = new File("test1")
        testDirectory.deleteDir()
    }

    def cleanup() {
        // clean up the test directory after testing
        File testDirectory = new File("test1")
        File testFile0 = new File("test1/0.png")
        testFile0.delete()
        File testFile1 = new File("test1/1.png")
        testFile1.delete()
        File testFile = new File("test_file.png")
        testDirectory.deleteDir()
        testFile.delete()
    }

    //-------------------------------getCfdImagesFromFile Unit tests----------------------------//

    def 'if read directory is not provided, do nothing'() {
        given:

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, null)

        then: 'getCfdImagesFromFile result is correct'
        result == null
    }

    def 'if directory not exists, create directory'() {
        given:

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, "test")

        then: 'getCfdImagesFromFile result is correct'
        result != null
        new File("test1").exists()
    }

    def 'if directory exists and empty, create directory'() {
        given:
        File testDir = new File("test1");
        testDir.mkdir()

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, "test")

        then: 'getCfdImagesFromFile result is correct'
        result != null
        testDir.exists()
        testDir.deleteDir()
    }

    def 'if directory exists and not empty but file doesnt exist, return empty array'() {
        given:
        File testDir = new File("test1");
        testDir.mkdir()
        File testFile = new File("test1/test0.png")
        FileWriter writer = new FileWriter(testFile);
        writer.write("TEST FILE")
        writer.flush()
        writer.close()

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, "test")

        then: 'getCfdImagesFromFile result is correct'
        result != null
        result.isEmpty()
        testFile.delete()
        testDir.deleteDir()
    }

    def 'if directory exists and not empty but file exists, return file'() {
        given:
        File testDir = new File("test1");
        testDir.mkdir()
        File testFile = new File("test1/0.png")
        FileWriter writer = new FileWriter(testFile);
        writer.write("TEST FILE")
        writer.flush()
        writer.close()

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, "test")

        then: 'getCfdImagesFromFile result is correct'
        result != null
        result.size() == 1
        testFile.delete()
        testDir.deleteDir()
    }

    def 'if directory exists and not empty but file exists, return files'() {
        given:
        File testDir = new File("test1");
        testDir.mkdir()
        File testFile0 = new File("test1/0.png")
        File testFile1 = new File("test1/1.png")
        File testFile2 = new File("test1/2.png")
        FileWriter writer0 = new FileWriter(testFile0);
        FileWriter writer1 = new FileWriter(testFile1);
        FileWriter writer2 = new FileWriter(testFile2);
        writer0.write("TEST FILE 0")
        writer1.write("TEST FILE 1")
        writer2.write("TEST FILE 2")
        writer0.flush()
        writer1.flush()
        writer2.flush()
        writer0.close()
        writer1.close()
        writer2.close()

        when: 'getCfdImagesFromFile action is executed'
        def result = service.getCfdImagesFromFile(1, "test")

        then: 'getCfdImagesFromFile result is correct'
        result != null
        result.size() == 3
        testFile0.delete()
        testFile1.delete()
        testFile2.delete()
        testDir.deleteDir()
    }

    //-------------------------------saveCfdImagesToFile Unit tests----------------------------//

    def 'if save directory is not provided, do nothing'() {
        given:

        when: 'saveCfdImagesToFile action is executed'
        def result = service.saveCfdImagesToFile(1, null, new ArrayList<byte[]>())

        then: 'saveCfdImagesToFile result is correct'
        result == null
    }

    def 'if save directory is provided, save files'() {
        given:
        String file1Txt = "TEST FILE 1"
        String file2Txt = "TEST FILE 2"
        List<byte[]> files = new ArrayList<>()
        files.add(file1Txt.getBytes(StandardCharsets.UTF_8))
        files.add(file2Txt.getBytes(StandardCharsets.UTF_8))

        when: 'saveCfdImagesToFile action is executed'
        def result = service.saveCfdImagesToFile(1, "test", files)

        then: 'saveCfdImagesToFile result is correct'
        result
        File file1 = new File("test1/0.png")
        File file2 = new File("test1/1.png")
        File file3 = new File("test1/2.png")
        file1.exists()
        file2.exists()
        !file3.exists()
        file1.delete()
        file2.delete()
    }

    def 'if IO exception is thrown when saving CfdImages, handle the exception'() {
        given:
        // create the file directory and file
        File directory = new File("test1")
        directory.mkdir()
        File testFile = new File("test1/0.png")
        testFile.createNewFile()
        String file1Txt = "TEST FILE 1"
        List<byte[]> files = new ArrayList<>()
        files.add(file1Txt.getBytes(StandardCharsets.UTF_8))

        //lock the file intentionally so it will throw IOException
        RandomAccessFile raFile = new RandomAccessFile(new File("test1/0.png"), "rw");
        FileLock lock = raFile.getChannel().lock();

        when: 'saveCfdImagesToFile action is executed'
        def result = service.saveCfdImagesToFile(1, "test", files)
        lock.release()
        raFile.getChannel().close()
        raFile.close()
        testFile.delete()
        directory.deleteDir()

        then: 'saveCfdImagesToFile result is correct'
        !result
    }

    //-------------------------------getImageFromFile Unit tests----------------------------//

    def 'if image file name is null, return null'() {
        given:

        when: 'getImageFromFile action is executed'
        def result = service.getImageFromFile(null)

        then: 'getImageFromFile result is correct'
        result == null
    }

    def 'if image file name is empty string, return null'() {
        given:

        when: 'getImageFromFile action is executed'
        def result = service.getImageFromFile("")

        then: 'getImageFromFile result is correct'
        result == null
    }

    def 'if image file doesnt exist, return null'() {
        given:

        when: 'getImageFromFile action is executed'
        def result = service.getImageFromFile("not_exist_file.png")

        then: 'getImageFromFile result is correct'
        result == null
    }

    def 'if image file exists, return image bytes'() {
        given:
        File testFile = new File("test_file.png")
        FileWriter testWriter = new FileWriter(testFile)
        testWriter.write("TEST DATA")
        testWriter.flush()
        testWriter.close()

        when: 'getImageFromFile action is executed'
        def result = service.getImageFromFile("test_file.png")

        then: 'getImageFromFile result is correct'
        result != null
        result.size() > 0
        testFile.delete()
    }

    //-------------------------------saveImageToFile Unit tests----------------------------//

    def 'if save directory and file information are incorrect, do nothing'() {
        given:

        when: 'saveImageToFile action is executed'
        def result = service.saveImageToFile(directoryName, fileName, new byte[10])

        then: 'saveImageToFile result is correct'
        result == null

        where:
        ID | directoryName | fileName
        1  | null          | "test_file.png"
        2  | ""            | "test_file.png"
        3  | "test_dir"    | null
        4  | "test_dir"    | ""
    }

    def 'if save directory and file information are correct and file exists, save file and return true'() {
        given:
        File directory = new File("test1")
        directory.mkdir()

        File testFile = new File("test1test_file.png")
        testFile.createNewFile()

        when: 'saveImageToFile action is executed'
        def result = service.saveImageToFile("test1", "test_file.png", new byte[10])

        then: 'saveImageToFile result is correct'
        result
        testFile.exists()
        testFile.delete()
        directory.deleteDir()
    }

    def 'if save directory and file information correct and file not exists, save file and return true'() {
        given:
        File testFile = new File("test1test_file.png")

        when: 'saveImageToFile action is executed'
        def result = service.saveImageToFile("test1", "test_file.png", new byte[10])

        then: 'saveImageToFile result is correct'
        result
        testFile.exists()
        testFile.delete()
    }

    //-------------------------------deleteFile Unit tests----------------------------//

    def 'if save directory and file information are incorrect or file not exists, do nothing'() {
        given:

        when: 'deleteFile action is executed'
        def result = service.deleteFile(directoryName, fileName)

        then: 'deleteFile result is correct'
        result == null

        where:
        ID | directoryName | fileName
        1  | null          | "test_file.png"
        2  | ""            | "test_file.png"
        3  | "test_dir"    | null
        4  | "test_dir"    | ""
        5  | "test_dir"    | "test_file.png"
    }

    def 'if file exists, delete file'() {
        given:
        File testFile = new File("test1test_file.png")
        testFile.createNewFile()

        when: 'saveImageToFile action is executed'
        service.deleteFile("test1", "test_file.png")

        then: 'saveImageToFile result is correct'
        !testFile.exists()
    }
}
