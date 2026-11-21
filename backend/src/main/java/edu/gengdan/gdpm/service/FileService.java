package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.FileRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** FileService 模块业务契约。 */
public interface FileService extends IService<FileRecord> {
    /** 校验文件扩展名和特征后保存文件记录。 */
    Object upload(MultipartFile file) throws java.io.IOException;
    /** 校验文件归属和指导范围后下载。 */
    ResponseEntity<Resource> download(long fileId);
}
