package my.maleva.api.module.filehandling.service.impl;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.dto.AttachmentUploadCommand;
import my.maleva.api.module.filehandling.dto.AttachmentUploadResultDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.model.AttachmentUploadMode;
import my.maleva.api.module.filehandling.repository.AttachmentFilePathRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Exercises the behaviour the five legacy upload actions had between them,
 * against a real temporary directory.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttachmentStorageServiceImplTest {

    @TempDir
    Path storageRoot;

    @Mock
    private AttachmentFilePathRepository filePathRepository;

    private AttachmentStorageServiceImpl service;
    private FileUploadConfig config;

    private static final AttachmentScope SCOPE = AttachmentScope.of(6, "SalesOrder", 12056, null);

    @BeforeEach
    void setUp() {
        config = new FileUploadConfig();
        config.setUploadDir(storageRoot.toString());
        config.setPublicUrlPrefix("/uploads");
        config.setMaxFiles(10);

        service = new AttachmentStorageServiceImpl(
                config, new ImageCompressorImpl(config), new PdfPageRasterizerImpl(config), filePathRepository);
    }

    private byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private AttachmentUploadCommand.AttachmentUploadCommandBuilder command() {
        return AttachmentUploadCommand.builder().scope(SCOPE).mode(AttachmentUploadMode.MIXED);
    }

    @Test
    void storesAnImageUnderTheRecordFolderAndReturnsItsPublicPath() throws Exception {
        MockMultipartFile upload = new MockMultipartFile(
                "files", "photo.png", "image/png", pngBytes(40, 40));

        AttachmentUploadResultDto result = service.upload(command().files(List.of(upload)).build());

        assertEquals(1, result.getStoredCount());
        assertEquals(1, result.getPaths().size());
        assertTrue(result.getPaths().get(0).startsWith("/uploads/6/SalesOrder/12056/"),
                "expected the legacy company/folder/record layout, got " + result.getPaths().get(0));
        assertTrue(Files.isDirectory(storageRoot.resolve("6/SalesOrder/12056")));
    }

    @Test
    void keepsTheUploadedFileNameOnlyWhenAsked() throws Exception {
        MockMultipartFile upload = new MockMultipartFile(
                "files", "invoice.png", "image/png", pngBytes(10, 10));

        AttachmentUploadResultDto named = service.upload(
                command().files(List.of(upload)).keepOriginalName(true).build());
        assertEquals("invoice.png", named.getAttachments().get(0).getFileName());

        AttachmentUploadResultDto generated = service.upload(command().files(List.of(upload)).build());
        assertTrue(generated.getAttachments().stream()
                        .anyMatch(attachment -> !attachment.getFileName().equals("invoice.png")),
                "a second upload without the flag should get a generated name");
    }

    @Test
    void downscalesImagesPastTheConfiguredBound() throws Exception {
        config.setImageMaxDimension(100);
        MockMultipartFile upload = new MockMultipartFile(
                "files", "wide.png", "image/png", pngBytes(400, 200));

        service.upload(command().files(List.of(upload)).keepOriginalName(true).build());

        BufferedImage stored = ImageIO.read(storageRoot.resolve("6/SalesOrder/12056/wide.png").toFile());
        assertEquals(100, stored.getWidth());
        assertEquals(50, stored.getHeight(), "aspect ratio should be preserved");
    }

    @Test
    void storesDocumentsByteForByteInsteadOfCompressingThem() {
        byte[] content = "purchase order".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile upload = new MockMultipartFile("files", "po.txt", "text/plain", content);

        AttachmentUploadResultDto result = service.upload(
                command().files(List.of(upload)).keepOriginalName(true).build());

        assertEquals(1, result.getStoredCount());
        Path stored = storageRoot.resolve("6/SalesOrder/12056/po.txt");
        assertTrue(Files.exists(stored));
    }

    @Test
    void deletesTheNamedFilesAndReportsWhatRemains() throws Exception {
        service.upload(command()
                .files(List.of(
                        new MockMultipartFile("files", "keep.png", "image/png", pngBytes(10, 10)),
                        new MockMultipartFile("files", "drop.png", "image/png", pngBytes(10, 10))))
                .keepOriginalName(true)
                .build());

        AttachmentUploadResultDto result = service.upload(command()
                .deletePaths(List.of("/uploads/6/SalesOrder/12056/drop.png"))
                .build());

        assertEquals(1, result.getDeletedCount());
        assertEquals(List.of("/uploads/6/SalesOrder/12056/keep.png"), result.getPaths());
        assertFalse(Files.exists(storageRoot.resolve("6/SalesOrder/12056/drop.png")));
    }

    @Test
    void copiesAnotherRecordsFilesForwardWhenNothingIsPosted() throws Exception {
        AttachmentScope source = AttachmentScope.of(6, "SalesOrder", 900, null);
        service.upload(AttachmentUploadCommand.builder()
                .scope(source)
                .mode(AttachmentUploadMode.MIXED)
                .files(List.of(new MockMultipartFile("files", "carried.png", "image/png", pngBytes(10, 10))))
                .keepOriginalName(true)
                .build());

        AttachmentUploadResultDto result = service.upload(command()
                .copyFromPaths(List.of("/uploads/6/SalesOrder/900/carried.png"))
                .build());

        assertEquals(1, result.getCopiedCount());
        assertEquals(List.of("/uploads/6/SalesOrder/12056/carried.png"), result.getPaths());
        assertTrue(Files.exists(storageRoot.resolve("6/SalesOrder/900/carried.png")),
                "the source record should keep its own copy");
    }

    @Test
    void listsNothingForARecordThatHasNoFolderYet() {
        assertEquals(List.of(), service.list(AttachmentScope.of(6, "SalesOrder", 424242, null)));
    }

    @Test
    void writesTheFilePathColumnOnlyWhenATableIsNamed() throws Exception {
        MockMultipartFile upload = new MockMultipartFile("files", "a.png", "image/png", pngBytes(8, 8));

        service.upload(command().files(List.of(upload)).build());
        verify(filePathRepository, never()).updateFilePath(anyString(), anyInt(), anyInt(), anyString());

        AttachmentUploadResultDto result = service.upload(
                command().files(List.of(upload)).filePathTable("SaleOrderMaster").build());
        verify(filePathRepository).updateFilePath(
                "SaleOrderMaster", 12056, 6, String.join(",", result.getPaths()));
    }

    @Test
    void rejectsAFolderNameThatWouldEscapeTheStorageRoot() {
        assertThrows(InvalidRequestException.class,
                () -> AttachmentScope.of(6, "../../etc", 1, null));
        assertThrows(InvalidRequestException.class,
                () -> AttachmentScope.of(6, "SalesOrder", 1, ".."));
    }

    @Test
    void rejectsACopySourceOutsideTheStorageRoot() {
        assertThrows(InvalidRequestException.class, () -> service.upload(command()
                .copyFromPaths(List.of("/uploads/../../../../windows/win.ini"))
                .build()));
    }

    @Test
    void ignoresAnyDirectoryPartOnAPathQueuedForDeletion() throws Exception {
        service.upload(command()
                .files(List.of(new MockMultipartFile("files", "safe.png", "image/png", pngBytes(8, 8))))
                .keepOriginalName(true)
                .build());

        // Only the file name is significant; the directory comes from the scope.
        AttachmentUploadResultDto result = service.upload(command()
                .deletePaths(List.of("/uploads/1/Other/2/safe.png"))
                .build());

        assertEquals(1, result.getDeletedCount());
        assertTrue(result.getPaths().isEmpty());
    }

    @Test
    void refusesMoreFilesThanTheConfiguredLimit() throws Exception {
        config.setMaxFiles(2);
        MockMultipartFile upload = new MockMultipartFile("files", "a.png", "image/png", pngBytes(8, 8));

        assertThrows(InvalidRequestException.class,
                () -> service.upload(command().files(List.of(upload, upload, upload)).build()));
    }

    @Test
    void keepsTheFilesWhenTheFilePathColumnCannotBeUpdated() throws Exception {
        doThrow(new InvalidRequestException("no such table"))
                .when(filePathRepository).updateFilePath(anyString(), anyInt(), anyInt(), anyString());

        AttachmentUploadResultDto result = service.upload(command()
                .files(List.of(new MockMultipartFile("files", "kept.png", "image/png", pngBytes(8, 8))))
                .keepOriginalName(true)
                .filePathTable("NotATable")
                .build());

        assertEquals(1, result.getStoredCount());
        assertTrue(Files.exists(storageRoot.resolve("6/SalesOrder/12056/kept.png")),
                "a failed column sync must not discard a file already written to disk");
    }

    @Test
    void reportsSizeAndNameForEachStoredFile() throws Exception {
        service.upload(command()
                .files(List.of(new MockMultipartFile("files", "meta.png", "image/png", pngBytes(20, 20))))
                .keepOriginalName(true)
                .build());

        List<AttachmentDto> attachments = service.list(SCOPE);
        assertEquals(1, attachments.size());
        AttachmentDto attachment = attachments.get(0);
        assertEquals("meta.png", attachment.getFileName());
        assertEquals("/uploads/6/SalesOrder/12056/meta.png", attachment.getPath());
        assertTrue(attachment.getSizeBytes() > 0);
    }
}
