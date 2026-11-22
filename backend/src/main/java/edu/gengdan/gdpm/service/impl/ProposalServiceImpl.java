package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.entity.ProposalReport;
import edu.gengdan.gdpm.mapper.ProposalReportMapper;
import edu.gengdan.gdpm.service.ProposalService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
@Service
public class ProposalServiceImpl
    extends AbstractMaterialStageService<ProposalReportMapper, ProposalReport>
    implements ProposalService
{

    /** 构造当前材料表的学生与阶段查询条件。 */
    protected LambdaQueryWrapper<ProposalReport> stageQuery(long studentId, String stage) {
        return query(ProposalReport.class)
            .eq(ProposalReport::getStudentId, studentId)
            .eq(ProposalReport::getStage, stage)
            .orderByDesc(ProposalReport::getVersion);
    }

    /** 创建当前阶段对应的材料实体。 */
    protected ProposalReport newMaterial() {
        return new ProposalReport();
    }

    /** 拒绝与当前模块不匹配的材料阶段。 */
    protected void validateStage(String stage) {
        ApiException.require(List.of("proposal").contains(stage), "材料阶段不存在");
    }
}
