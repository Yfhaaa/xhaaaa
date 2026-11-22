package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.GuidanceSaveRequest;
import edu.gengdan.gdpm.entity.GuidanceRecord;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
public interface GuidanceService extends IService<GuidanceRecord> {
    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    List<Map<String, Object>> guidance();

    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    IdVO guidance(GuidanceSaveRequest body);
}
