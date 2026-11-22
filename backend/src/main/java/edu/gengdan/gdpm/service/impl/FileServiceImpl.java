package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.id;
import static edu.gengdan.gdpm.common.Values.str;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.PlagiarismCheck;
import edu.gengdan.gdpm.entity.ThesisReview;
import edu.gengdan.gdpm.mapper.FileRecordMapper;
import edu.gengdan.gdpm.service.FileService;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.vo.UploadVO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
@Service
public class FileServiceImpl extends DomainSupport<FileRecordMapper, FileRecord> implements FileService {

    @Value("${app.upload-dir}")
    private String dir;

    @Autowired
    private MaterialService materials;

    /** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 */
    @OperLog("上传文件")
    public UploadVO upload(MultipartFile file) throws IOException {
        auth.permission("material:write");
        require(file != null && !file.isEmpty(), "请选择非空文件");
        require(file.getSize() <= 20L * 1024 * 1024, "文件不能超过20MB");
        String original = Objects.toString(file.getOriginalFilename(), "材料").replace('\\', '/');
        original = original.substring(original.lastIndexOf('/') + 1);
        require(
            original.length() <= 180 && !original.contains("\r") && !original.contains("\n"),
            "文件名过长或不合法"
        );
        int dot = original.lastIndexOf('.');
        String extension = dot < 0 ? "" : original.substring(dot).toLowerCase(Locale.ROOT);
        require(
            List.of(".pdf", ".doc", ".docx", ".txt", ".zip").contains(extension),
            "仅允许PDF、DOC、DOCX、TXT、ZIP"
        );
        byte[] header = new byte[8];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(header);
        }
        if (extension.equals(".pdf")) {
            require(
                read >= 4 && new String(header, 0, 4, StandardCharsets.US_ASCII).equals("%PDF"),
                "PDF文件内容不正确"
            );
        }
        if (extension.equals(".docx") || extension.equals(".zip")) {
            require(read >= 2 && header[0] == 'P' && header[1] == 'K', "ZIP/Word文件内容不正确");
        }
        if (extension.equals(".doc")) {
            require(
                read >= 4 &&
                    (header[0] & 255) == 208 &&
                    (header[1] & 255) == 207 &&
                    (header[2] & 255) == 17 &&
                    (header[3] & 255) == 224,
                "DOC文件内容不正确"
            );
        }
        if (extension.equals(".txt")) {
            for (int i = 0; i < read; i++) {
                require(header[i] != 0, "TXT文件不能包含二进制空字符");
            }
        }
        Path root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        String name = UUID.randomUUID() + extension;
        Path target = root.resolve(name);
        file.transferTo(target);
        FileRecord record = new FileRecord();
        record.setOwnerId(auth.uid());
        record.setOriginalName(original);
        record.setStoredName(name);
        record.setSizeBytes(file.getSize());
        try {
            save(record);
        } catch (RuntimeException e) {
            Files.deleteIfExists(target);
            throw e;
        }
        return new UploadVO(record.getId(), original, file.getSize());
    }

    /** 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
    public ResponseEntity<Resource> download(long fid) {
        auth.permission("material:read");
        Map<String, Object> f = EntityViews.view(required(getById(fid)));
        boolean allowed = id(f.get("owner_id")) == auth.uid() || auth.role("ADMIN");
        if (!allowed) {
            for (Map<String, Object> material : materials.listMaterials(null)) {
                if (id(material.get("file_id")) == fid) {
                    allowed = true;
                    break;
                }
            }
        }
        if (!allowed && auth.role("TEACHER")) {
            for (ThesisReview review : thesisReviews.selectList(
                query(ThesisReview.class).eq(ThesisReview::getReviewerId, auth.uid())
            )) {
                if (required(theses.selectById(review.getThesisId())).getFileId() == fid) {
                    allowed = true;
                }
            }
        }
        if (!allowed) {
            for (PlagiarismCheck check : checks.selectList(
                query(PlagiarismCheck.class).eq(PlagiarismCheck::getFileId, fid)
            )) {
                try {
                    auth.student(check.getStudentId());
                    allowed = true;
                } catch (ApiException ignored) {}
            }
        }
        if (!allowed) {
            throw new ApiException(403, "无权访问此文件");
        }
        Path root = Paths.get(dir).toAbsolutePath().normalize(),
            target = root.resolve(str(f, "stored_name")).normalize();
        require(target.startsWith(root), "文件路径不合法");
        if (!Files.isRegularFile(target)) {
            throw new ApiException(404, "文件已丢失");
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("X-Content-Type-Options", "nosniff")
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(str(f, "original_name"), StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .body(new FileSystemResource(target));
    }
}
