package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.ThesisInfo;
import edu.gengdan.gdpm.mapper.ThesisInfoMapper;
import edu.gengdan.gdpm.service.ThesisService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
@Service
public class ThesisServiceImpl
    extends AbstractMaterialStageService<ThesisInfoMapper, ThesisInfo>
    implements ThesisService
{

    /** 构造当前材料表的学生与阶段查询条件。 */
    protected LambdaQueryWrapper<ThesisInfo> stageQuery(long studentId, String stage) {
        return query(ThesisInfo.class)
            .eq(ThesisInfo::getStudentId, studentId)
            .eq(ThesisInfo::getStage, stage)
            .orderByDesc(ThesisInfo::getVersion);
    }

    /** 创建当前阶段对应的材料实体。 */
    protected ThesisInfo newMaterial() {
        return new ThesisInfo();
    }

    /** 拒绝与当前模块不匹配的材料阶段。 */
    protected void validateStage(String stage) {
        ApiException.require(List.of("draft", "final").contains(stage), "材料阶段不存在");
    }
}
