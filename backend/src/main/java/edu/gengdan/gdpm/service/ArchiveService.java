package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.ArchiveRequest;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/** 持 stats:read 权限查询本人或指导范围内归档快照。 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
public interface ArchiveService extends IService<ArchiveRecord> {
    /** 持 stats:read 权限查询本人或指导范围内归档快照。 */
    List<Map<String, Object>> archives();

    /** 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
    IdVO archive(ArchiveRequest body) throws Exception;
}
