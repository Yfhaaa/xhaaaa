package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** ArchiveService 模块业务契约。 */
public interface ArchiveService extends IService<ArchiveRecord> {
    /** 查询范围内的归档快照。 */
    Object archives();
    /** 校验材料和成绩后创建归档快照。 */
    Object archive(Map<String, Object> body) throws Exception;
}
