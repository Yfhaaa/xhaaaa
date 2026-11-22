package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.MaterialSubmitRequest;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.entity.ProcessMaterial;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.SubmissionVO;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
public interface MaterialStageService<T extends ProcessMaterial> extends IService<T> {
    /** 先校验学生数据范围，再读取指定阶段最新材料，尚未提交时返回空。 */
    T latest(long studentId, String stage);

    /** 学生按阶段顺序提交本人文件；任务书由指导教师下达，退回重提创建新版本。 */
    SubmissionVO submit(String stage, MaterialSubmitRequest body);

    /** 指导教师仅审核待审核的最新版本，定稿通过还需对应版本查重合格。 */
    MessageVO review(String stage, long materialId, ReviewRequest body);
}
