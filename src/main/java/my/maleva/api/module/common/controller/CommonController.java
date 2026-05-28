package my.maleva.api.module.common.controller;

import my.maleva.api.common.dto.*;
import my.maleva.api.module.common.service.ICommonService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.ByteArrayContent;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Common Controller
 * Equivalent to .NET CommonController
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    private final ICommonService commonService;

    public CommonController(ICommonService commonService) {
        this.commonService = commonService;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestParam(value = "MyImages0", required = false) MultipartFile file0,
            @RequestParam(value = "MyImages1", required = false) MultipartFile file1,
            @RequestParam(value = "MyImages2", required = false) MultipartFile file2,
            @RequestParam(value = "MyImages3", required = false) MultipartFile file3,
            @RequestParam(value = "MyImages4", required = false) MultipartFile file4,
            @RequestParam(value = "MyImages5", required = false) MultipartFile file5,
            @RequestParam(value = "MyImages6", required = false) MultipartFile file6,
            @RequestParam(value = "MyImages7", required = false) MultipartFile file7,
            @RequestParam(value = "MyImages8", required = false) MultipartFile file8,
            @RequestParam(value = "MyImages9", required = false) MultipartFile file9) {
        try {
            comId = comId != null ? comId : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            fileName = fileName != null ? fileName : "";
            deleteFileName = deleteFileName != null ? deleteFileName : "";
            subFolderName = subFolderName != null ? subFolderName : "";

            String sub = subFolderName.isEmpty() ? "" : subFolderName + "/";
            List<MultipartFile> files = Arrays.asList(file0, file1, file2, file3, file4, file5, file6, file7, file8, file9);
            files = files.stream().filter(Objects::nonNull).toList();
            int fileCount = files.size();
            List<String> pathList = new ArrayList<>();

            if (fileCount != 0) {
                for (MultipartFile pic : files) {
                    if (!pic.isEmpty()) {
                        String fileNameOriginal = pic.getOriginalFilename();
                        String ext = getFileExtension(fileNameOriginal);
                        String imgName = fileName.isEmpty() ? UUID.randomUUID().toString() : fileName.replace(ext, "");

                        String relativePath = "uploads/" + comId + "/" + folderName + "/" + id + "/" + sub;
                        Files.createDirectories(Paths.get(relativePath));

                        String absolutePath = relativePath + imgName + ext;
                        String virtualPath = "/uploads/" + comId + "/" + folderName + "/" + id + "/" + sub + imgName + ext;

                        if (isImageFile(ext)) {
                            try (InputStream is = pic.getInputStream()) {
                                compressImage(is, absolutePath, fileNameOriginal);
                            }
                        } else {
                            pic.transferTo(Paths.get(absolutePath));
                        }
                        pathList.add(virtualPath);
                    }
                }
            }

            // Handle delete
            Path directory = Paths.get("uploads/" + comId + "/" + folderName + "/" + id + "/" + sub);
            String pathPrefix = "/uploads/" + comId + "/" + folderName + "/" + id + "/" + sub;
            if (Files.exists(directory)) {
                List<String> filesToDelete = deleteFileName.isEmpty() ? new ArrayList<>() :
                        Arrays.stream(deleteFileName.split(",")).map(s -> s.replace(pathPrefix, "")).toList();
                try (var stream = Files.list(directory)) {
                    stream.forEach(file -> {
                        String fileNameStr = file.getFileName().toString();
                        if (filesToDelete.contains(fileNameStr)) {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                logger.error("Error deleting file", e);
                            }
                        } else {
                            pathList.add(pathPrefix + fileNameStr);
                        }
                    });
                }
            }

            commonService.uploadFile(id, comId, folderName, String.join(",", pathList));
            if (fileCount != 0 || !deleteFileName.isEmpty()) {
                return ResponseEntity.ok(Map.of("ok", true, "data", pathList, "message", "Uploaded Successfully"));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", "Uploaded Failed"));
            }
        } catch (Exception ex) {
            logger.error("Error in uploadFile", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    // Similar for other upload methods, but for brevity, I'll implement one and note others

    @PostMapping("/fetchFile2")
    public ResponseEntity<Map<String, Object>> fetchFile2(
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName) {
        try {
            comid = comid != null ? comid : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            fileName = fileName != null ? fileName : "";
            subFolderName = subFolderName != null ? subFolderName : "";

            String imageDirectory = "/uploads/" + comid + "/" + folderName + "/" + id + "/" +
                    (subFolderName != null && !subFolderName.isEmpty() ? subFolderName + "/" : "");

            Path path = Paths.get("uploads" + imageDirectory);
            List<String> imageNames = new ArrayList<>();
            if (Files.exists(path) && Files.isDirectory(path)) {
                try (var stream = Files.list(path)) {
                    stream.forEach(p -> imageNames.add(imageDirectory + p.getFileName().toString()));
                }
            }

            return ResponseEntity.ok(Map.of("ok", true, "message", "Fetched Successfully", "Data", imageNames));
        } catch (Exception ex) {
            logger.error("Error in fetchFile2", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/deleteFile")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "FileName", required = false) String fileName) {
        try {
            comid = comid != null ? comid : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            subFolderName = subFolderName != null ? subFolderName : "";
            fileName = fileName != null ? fileName : "";

            String sub = subFolderName.isEmpty() ? "" : subFolderName + "/";
            Path directory = Paths.get("uploads/" + comid + "/" + folderName + "/" + id + "/" + sub);
            String pathPrefix = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
            List<String> patharray = new ArrayList<>();
            boolean deleted = false;

            if (Files.exists(directory)) {
                List<String> fileNameList = fileName.isEmpty() ? new ArrayList<>() : Arrays.asList(fileName.split(","));
                try (var stream = Files.list(directory)) {
                    for (Path file : (Iterable<Path>) stream::iterator) {
                        boolean toDelete = fileNameList.stream().anyMatch(f -> (pathPrefix + file.getFileName().toString()).equals(f));
                        if (toDelete) {
                            Files.delete(file);
                            deleted = true;
                        } else {
                            patharray.add(pathPrefix + file.getFileName().toString());
                        }
                    }
                }
            }

            if (deleted) {
                ResponseViewModel ro = commonService.uploadFile(id, comid, folderName, String.join(",", patharray));
                return ResponseEntity.ok(Map.of("ok", true));
            } else {
                return ResponseEntity.ok(Map.of("ok", false));
            }
        } catch (Exception ex) {
            logger.error("Error in deleteFile", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/fetchFiles")
    public ResponseEntity<Map<String, Object>> fetchFiles(@RequestParam String imageDirectory) {
        try {
            ResponseViewModel ro = commonService.fetchFiles(imageDirectory);
            if (ro.isSuccess()) {
                return ResponseEntity.ok(Map.of("ok", true, "message", ro.getMessage(), "data", ro.getData1()));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", ro.getMessage()));
            }
        } catch (Exception ex) {
            logger.error("Error in fetchFiles", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/checkFiles")
    public ResponseEntity<Map<String, Object>> checkFiles(@RequestParam String imageDirectory) {
        try {
            ResponseViewModel ro = commonService.checkFiles(imageDirectory);
            if (ro.isSuccess()) {
                return ResponseEntity.ok(Map.of("ok", true, "message", ro.getMessage(), "data", ro.getData1()));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", ro.getMessage()));
            }
        } catch (Exception ex) {
            logger.error("Error in checkFiles", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/uploadFile2")
    public ResponseEntity<Map<String, Object>> uploadFile2(
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestParam(value = "MyImages0", required = false) MultipartFile file0,
            @RequestParam(value = "MyImages1", required = false) MultipartFile file1,
            @RequestParam(value = "MyImages2", required = false) MultipartFile file2,
            @RequestParam(value = "MyImages3", required = false) MultipartFile file3,
            @RequestParam(value = "MyImages4", required = false) MultipartFile file4,
            @RequestParam(value = "MyImages5", required = false) MultipartFile file5,
            @RequestParam(value = "MyImages6", required = false) MultipartFile file6,
            @RequestParam(value = "MyImages7", required = false) MultipartFile file7,
            @RequestParam(value = "MyImages8", required = false) MultipartFile file8,
            @RequestParam(value = "MyImages9", required = false) MultipartFile file9) {
        try {
            comid = comid != null ? comid : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            fileName = fileName != null ? fileName : "";
            deleteFileName = deleteFileName != null ? deleteFileName : "";
            subFolderName = subFolderName != null ? subFolderName : "";

            String sub = subFolderName.isEmpty() ? "" : subFolderName + "/";
            List<MultipartFile> files = Arrays.asList(file0, file1, file2, file3, file4, file5, file6, file7, file8, file9);
            files = files.stream().filter(Objects::nonNull).toList();
            int filecount = files.size();
            List<String> patharray = new ArrayList<>();

            if (filecount != 0) {
                for (int i = 0; i < filecount; i++) {
                    MultipartFile pic = files.get(i);
                    if (pic != null && !pic.isEmpty()) {
                        String fileNameOriginal = pic.getOriginalFilename();
                        String ext = getFileExtension(fileNameOriginal);
                        String imgname = fileName.isEmpty() ? UUID.randomUUID().toString() : fileName.replace(ext, "");

                        String path1 = "uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
                        Path dirPath = Paths.get(path1);
                        Files.createDirectories(dirPath);

                        String comPath = path1 + imgname + ext;
                        imgname = imgname + ext;

                        if (isImageFile(ext)) {
                            try (InputStream strm = pic.getInputStream()) {
                                compressImage(strm, comPath, fileNameOriginal);
                            }
                        } else {
                            pic.transferTo(Paths.get(comPath));
                        }

                        String path2 = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + imgname;
                        patharray.add(path2);
                    }
                }
            }

            patharray.clear();
            Path directory = Paths.get("uploads/" + comid + "/" + folderName + "/" + id + "/" + sub);
            String pathPrefix = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
            if (Files.exists(directory)) {
                try (var stream = Files.list(directory)) {
                    List<String> fileNameList = deleteFileName.isEmpty() ? new ArrayList<>() :
                            Arrays.stream(deleteFileName.split(",")).map(s -> s.replace(pathPrefix, "")).toList();
                    stream.forEach(file -> {
                        if (fileNameList.contains(file.getFileName().toString())) {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                logger.error("Error deleting file", e);
                            }
                        } else {
                            patharray.add(pathPrefix + file.getFileName().toString());
                        }
                    });
                }
            }

            if (filecount != 0 || !deleteFileName.isEmpty()) {
                return ResponseEntity.ok(Map.of("ok", true, "data", patharray, "message", "Uploaded Successfully"));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", "Uploaded Failed"));
            }
        } catch (Exception ex) {
            logger.error("Error in uploadFile2", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/uploadFile3")
    public ResponseEntity<Map<String, Object>> uploadFile3(
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "ExistingFilePath", required = false) String existingFilePath,
            @RequestParam(value = "MyImages0", required = false) MultipartFile file0,
            @RequestParam(value = "MyImages1", required = false) MultipartFile file1,
            @RequestParam(value = "MyImages2", required = false) MultipartFile file2,
            @RequestParam(value = "MyImages3", required = false) MultipartFile file3,
            @RequestParam(value = "MyImages4", required = false) MultipartFile file4,
            @RequestParam(value = "MyImages5", required = false) MultipartFile file5,
            @RequestParam(value = "MyImages6", required = false) MultipartFile file6,
            @RequestParam(value = "MyImages7", required = false) MultipartFile file7,
            @RequestParam(value = "MyImages8", required = false) MultipartFile file8,
            @RequestParam(value = "MyImages9", required = false) MultipartFile file9) {
        try {
            comid = comid != null ? comid : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            fileName = fileName != null ? fileName : "";
            deleteFileName = deleteFileName != null ? deleteFileName : "";
            subFolderName = subFolderName != null ? subFolderName : "";
            existingFilePath = existingFilePath != null ? existingFilePath : "";

            String sub = !subFolderName.isEmpty() ? subFolderName + "/" : "";
            List<MultipartFile> files = Arrays.asList(file0, file1, file2, file3, file4, file5, file6, file7, file8, file9);
            files = files.stream().filter(Objects::nonNull).toList();
            int filecount = files.size();
            List<String> patharray = new ArrayList<>();

            // CASE 1: New files are uploaded
            if (filecount != 0) {
                for (int i = 0; i < filecount; i++) {
                    MultipartFile pic = files.get(i);
                    if (pic != null && !pic.isEmpty()) {
                        String fileNameOriginal = pic.getOriginalFilename();
                        String ext = getFileExtension(fileNameOriginal);
                        String imgname = !fileName.isEmpty() ? fileName.replace(ext, "") : UUID.randomUUID().toString();

                        String path1 = "uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
                        Path dirPath = Paths.get(path1);
                        Files.createDirectories(dirPath);

                        String comPath = path1 + imgname + ext;
                        imgname = imgname + ext;
                        if (isImageFile(ext)) {
                            try (InputStream strm = pic.getInputStream()) {
                                compressImage(strm, comPath, fileNameOriginal);
                            }
                        } else {
                            pic.transferTo(Paths.get(comPath));
                        }

                        String path2 = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + imgname;
                        patharray.add(path2);
                    }
                }
            }

            // CASE 2: No new files, copy existing ones
            else if (!existingFilePath.isEmpty() && deleteFileName.isEmpty()) {
                List<String> filesToCopy = Arrays.asList(existingFilePath.split(","));

                for (String f : filesToCopy) {
                    Path srcPath = Paths.get("uploads" + f);
                    if (Files.exists(srcPath)) {
                        String destDir = "uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
                        Files.createDirectories(Paths.get(destDir));
                        String destPathStr = destDir + srcPath.getFileName().toString();
                        Files.copy(srcPath, Paths.get(destPathStr), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        String newVirtualPath = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + srcPath.getFileName().toString();
                        patharray.add(newVirtualPath);
                    }
                }
            }

            // CASE 3: Delete files if requested
            Path directory = Paths.get("uploads/" + comid + "/" + folderName + "/" + id + "/" + sub);
            String pathPrefix = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
            if (Files.exists(directory)) {
                List<String> fileNameList = !deleteFileName.isEmpty() ? Arrays.stream(deleteFileName.split(",")).map(s -> s.replace(pathPrefix, "")).toList() : new ArrayList<>();
                try (var stream = Files.list(directory)) {
                    stream.forEach(file -> {
                        if (fileNameList.contains(file.getFileName().toString())) {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                logger.error("Error deleting file", e);
                            }
                        } else if (!patharray.contains(pathPrefix + file.getFileName().toString())) {
                            patharray.add(pathPrefix + file.getFileName().toString());
                        }
                    });
                }
            }

            if (filecount != 0 || !existingFilePath.isEmpty() || !deleteFileName.isEmpty()) {
                return ResponseEntity.ok(Map.of("ok", true, "data", patharray, "message", "Uploaded or Updated Successfully"));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", "No files uploaded or copied"));
            }
        } catch (Exception ex) {
            logger.error("Error in uploadFile3", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/uploadFile5")
    public ResponseEntity<Map<String, Object>> uploadFile5(
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileNameHeader,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "ExistingFilePath", required = false) String existingFilePath,
            @RequestParam(value = "MyImages0", required = false) MultipartFile file0,
            @RequestParam(value = "MyImages1", required = false) MultipartFile file1,
            @RequestParam(value = "MyImages2", required = false) MultipartFile file2,
            @RequestParam(value = "MyImages3", required = false) MultipartFile file3,
            @RequestParam(value = "MyImages4", required = false) MultipartFile file4,
            @RequestParam(value = "MyImages5", required = false) MultipartFile file5,
            @RequestParam(value = "MyImages6", required = false) MultipartFile file6,
            @RequestParam(value = "MyImages7", required = false) MultipartFile file7,
            @RequestParam(value = "MyImages8", required = false) MultipartFile file8,
            @RequestParam(value = "MyImages9", required = false) MultipartFile file9) {
        try {
            comid = comid != null ? comid : 0;
            id = id != null ? id : 0;
            folderName = folderName != null ? folderName : "";
            fileNameHeader = fileNameHeader != null ? fileNameHeader : "";
            deleteFileName = deleteFileName != null ? deleteFileName : "";
            subFolderName = subFolderName != null ? subFolderName : "";
            existingFilePath = existingFilePath != null ? existingFilePath : "";

            String sub = subFolderName.isEmpty() ? "" : subFolderName + "/";
            List<MultipartFile> files = Arrays.asList(file0, file1, file2, file3, file4, file5, file6, file7, file8, file9);
            files = files.stream().filter(Objects::nonNull).toList();
            int uploadedFileCount = files.size();
            List<String> uploadedPaths = new ArrayList<>();

            String baseFolderPath = "uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
            Files.createDirectories(Paths.get(baseFolderPath));

            // CASE 1: New files uploaded
            if (uploadedFileCount != 0) {
                for (int i = 0; i < uploadedFileCount; i++) {
                    MultipartFile uploadedFile = files.get(i);
                    if (uploadedFile != null && !uploadedFile.isEmpty()) {
                        String originalFileName = uploadedFile.getOriginalFilename();
                        String extension = getFileExtension(originalFileName).toLowerCase();

                        String imgName = !fileNameHeader.isEmpty() ? originalFileName.replace(getFileExtension(originalFileName), "") : UUID.randomUUID().toString();
                        String completePath = baseFolderPath + imgName + extension;

                        // IMAGE FILE
                        if (isImageFile(extension)) {
                            try (InputStream strm = uploadedFile.getInputStream()) {
                                compressImage(strm, completePath, uploadedFile.getOriginalFilename());
                            }
                            uploadedPaths.add("/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + imgName + extension);
                        }
                        // PDF → IMAGE
                        else if (extension.equals(".pdf")) {
                            uploadedFile.transferTo(Paths.get(completePath));
                            List<String> pdfImages = convertPdfToImages(completePath, baseFolderPath, imgName, comid, folderName, id, sub);
                            Files.delete(Paths.get(completePath));
                            uploadedPaths.addAll(pdfImages);
                        }
                        // DOC / EXCEL FILES
                        else {
                            uploadedFile.transferTo(Paths.get(completePath));
                            uploadedPaths.add("/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + imgName + extension);
                        }
                    }
                }
            }
            // CASE 2: Copy existing files if no new upload
            else if (!existingFilePath.isEmpty()) {
                List<String> filesToCopy = Arrays.asList(existingFilePath.split(","));
                for (String f : filesToCopy) {
                    Path srcPath = Paths.get("uploads" + f);
                    if (Files.exists(srcPath)) {
                        String destPath = baseFolderPath + srcPath.getFileName().toString();
                        Files.copy(srcPath, Paths.get(destPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        uploadedPaths.add("/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + srcPath.getFileName().toString());
                    }
                }
            }

            // CASE 3: Delete files if requested
            Path directory = Paths.get(baseFolderPath);
            String pathPrefix = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub;
            if (Files.exists(directory)) {
                List<String> filesToDelete = !deleteFileName.isEmpty() ? Arrays.stream(deleteFileName.split(",")).map(s -> s.replace(pathPrefix, "")).toList() : new ArrayList<>();
                try (var stream = Files.list(directory)) {
                    stream.forEach(file -> {
                        if (filesToDelete.contains(file.getFileName().toString())) {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                logger.error("Error deleting file", e);
                            }
                        } else if (!uploadedPaths.contains(pathPrefix + file.getFileName().toString())) {
                            uploadedPaths.add(pathPrefix + file.getFileName().toString());
                        }
                    });
                }
            }

            if (uploadedFileCount != 0 || !existingFilePath.isEmpty() || !deleteFileName.isEmpty()) {
                return ResponseEntity.ok(Map.of("ok", true, "data", uploadedPaths, "message", "Uploaded / Updated Successfully"));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "message", "No files uploaded or copied"));
            }
        } catch (Exception ex) {
            logger.error("Error in uploadFile5", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    private List<String> convertPdfToImages(String pdfPath, String outputFolder, String imgBaseName, int comid, String folderName, int id, String sub) {
        List<String> result = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                String imgName = imgBaseName + "_Page_" + (page + 1) + ".jpg";
                String imgPath = outputFolder + imgName;
                ImageIO.write(bim, "jpg", new File(imgPath));
                String virtualPath = "/uploads/" + comid + "/" + folderName + "/" + id + "/" + sub + imgName;
                result.add(virtualPath);
            }
        } catch (IOException e) {
            logger.error("Error converting PDF to images", e);
        }
        return result;
    }

    // Firebase notification implementation
    @PostMapping("/sendNotification")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody List<FireBaseRequestModel> obj) {
        try {
            // Path to Firebase service account key
            String firebaseKeyPath = "src/main/resources/maleva-4eefb-firebase-adminsdk-zwr0y-ea250439ed.json"; // Adjust path
            String scopes = "https://www.googleapis.com/auth/firebase.messaging";
            String firebaseUrl = "https://fcm.googleapis.com/v1/projects/maleva-4eefb/messages:send";

            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(firebaseKeyPath))
                    .createScoped(scopes);
            credentials.refreshIfExpired();
            String bearerToken = credentials.getAccessToken().getTokenValue();

            FirebaseRoot rootObj = null;

            for (int i = 0; i < obj.size(); i++) {
                if (i == 0 || rootObj == null) {
                    rootObj = FirebaseRoot.builder()
                            .message(FirebaseMessage.builder()
                                    .token(obj.get(i).getTokenid())
                                    .data(FirebaseData.builder()
                                            .title(obj.get(i).getTitle())
                                            .body(obj.get(i).getBody())
                                            .key_1(obj.get(i).getKey_1())
                                            .key_2(obj.get(i).getKey_2())
                                            .build())
                                    .notification(FirebaseNotification.builder()
                                            .title(obj.get(i).getTitle())
                                            .body(obj.get(i).getMessage())
                                            .image(obj.get(i).getImageUrl())
                                            .build())
                                    .build())
                            .build();
                }

                // Send HTTP request to FCM
                com.google.api.client.http.HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(
                        new HttpRequestInitializer() {
                            @Override
                            public void initialize(HttpRequest request) throws IOException {
                                request.getHeaders().setAuthorization("Bearer " + bearerToken);
                                request.getHeaders().setContentType("application/json");
                            }
                        });

                GenericUrl url = new GenericUrl(firebaseUrl);
                HttpRequest request = requestFactory.buildPostRequest(url,
                        new ByteArrayContent("application/json",
                                new ObjectMapper().writeValueAsBytes(rootObj)));
                HttpResponse response = request.execute();
                // Optionally handle response
                response.getContent().close();
            }

            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            logger.error("Error in sendNotification", ex);
            return ResponseEntity.ok(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    private void compressImage(InputStream sourcePath, String targetPath, String filename) throws IOException {
        BufferedImage originalImage = ImageIO.read(sourcePath);
        if (originalImage == null) return;

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        float maxHeight = 900.0f;
        float maxWidth = 900.0f;
        int newWidth, newHeight;

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            float ratioX = maxWidth / originalWidth;
            float ratioY = maxHeight / originalHeight;
            float ratio = Math.min(ratioX, ratioY);
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        } else {
            newWidth = originalWidth;
            newHeight = originalHeight;
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        String extension = getFileExtension(targetPath).toLowerCase();
        if (extension.equals(".jpg") || extension.equals(".jpeg")) {
            // Compress JPEG
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f); // 50% quality
                try (FileOutputStream fos = new FileOutputStream(targetPath)) {
                    ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(resizedImage, null, null), param);
                    ios.close();
                }
                writer.dispose();
            }
        } else {
            ImageIO.write(resizedImage, extension.substring(1), new File(targetPath));
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    private boolean isImageFile(String extension) {
        String ext = extension == null ? "" : extension.toLowerCase();
        return ext.equals(".jpg")
                || ext.equals(".jpeg")
                || ext.equals(".png")
                || ext.equals(".gif")
                || ext.equals(".bmp")
                || ext.equals(".webp");
    }
}
