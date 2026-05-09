package my.maleva.api.module.common.service;

import my.maleva.api.common.dto.ResponseViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Common Service Implementation
 * Equivalent to .NET CommonServices
 */
@Service
public class CommonServiceImpl implements ICommonService {

    private static final Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public CommonServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseViewModel uploadFile(int id, int comid, String tableName, String paths) {
        try {
            String sql = "UPDATE " + tableName + " SET FilePath = ? WHERE Id = ? AND CompanyRefId = ?";
            jdbcTemplate.update(sql, paths, id, comid);
            return ResponseViewModel.success(null, "UploadFile Success", 200);
        } catch (Exception ex) {
            logger.error("Error in uploadFile", ex);
            return ResponseViewModel.error(ex.getMessage(), 500);
        }
    }

    @Override
    public ResponseViewModel fetchFiles(String imageDirectory) {
        try {
            // Assuming imageDirectory is like "/Upload/1/folder/1/sub/"
            String basePath = System.getProperty("user.dir") + "/uploads" + imageDirectory; // Adjust path as needed
            Path dir = Paths.get(basePath);
            List<String> imageNames = new ArrayList<>();

            if (Files.exists(dir) && Files.isDirectory(dir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{png,jpg,jpeg,pdf}")) {
                    for (Path entry : stream) {
                        imageNames.add(entry.getFileName().toString());
                    }
                }
            }

            return ResponseViewModel.success(imageNames, "Success", 200);
        } catch (Exception ex) {
            logger.error("Error in fetchFiles", ex);
            return ResponseViewModel.error(ex.getMessage(), 500);
        }
    }

    @Override
    public ResponseViewModel checkFiles(String imageDirectory) {
        try {
            String basePath = System.getProperty("user.dir") + "/uploads" + imageDirectory;
            Path dir = Paths.get(basePath);
            List<Integer> folderNames = new ArrayList<>();

            if (Files.exists(dir) && Files.isDirectory(dir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            Path subDir = entry;
                            try (DirectoryStream<Path> subStream = Files.newDirectoryStream(subDir)) {
                                boolean hasFiles = subStream.iterator().hasNext();
                                if (hasFiles) {
                                    try {
                                        int folderId = Integer.parseInt(subDir.getFileName().toString());
                                        folderNames.add(folderId);
                                    } catch (NumberFormatException e) {
                                        // Ignore non-numeric folder names
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return ResponseViewModel.success(folderNames, "Success", 200);
        } catch (Exception ex) {
            logger.error("Error in checkFiles", ex);
            return ResponseViewModel.error(ex.getMessage(), 500);
        }
    }
}
