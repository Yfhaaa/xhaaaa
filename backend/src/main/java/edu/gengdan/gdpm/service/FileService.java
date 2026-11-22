package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.vo.UploadVO;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
public interface FileService extends IService<FileRecord> {
    /** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 */
    UploadVO upload(MultipartFile file) throws IOException;

    /** 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
    ResponseEntity<Resource> download(long fileId);
}
