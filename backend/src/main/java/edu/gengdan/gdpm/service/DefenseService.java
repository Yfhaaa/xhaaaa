package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.DefenseArrangeRequest;
import edu.gengdan.gdpm.dto.DefenseGroupRequest;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/**
 * 持 defense:read 权限查询角色范围内答辩分组。 管理员创建由有效教师组成的答辩组，记录时间与地点。 持 defense:read 权限读取本人或指导范围内答辩安排。
 * 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。
 */
public interface DefenseService extends IService<DefenseInfo> {
    /** 持 defense:read 权限查询角色范围内答辩分组。 */
    List<Map<String, Object>> groups();

    /** 管理员创建由有效教师组成的答辩组，记录时间与地点。 */
    IdVO group(DefenseGroupRequest body);

    /** 持 defense:read 权限读取本人或指导范围内答辩安排。 */
    List<Map<String, Object>> defenses();

    /** 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。 */
    IdVO arrange(DefenseArrangeRequest body);
}
