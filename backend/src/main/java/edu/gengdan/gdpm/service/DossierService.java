package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.vo.BoardVO;
import edu.gengdan.gdpm.vo.DossierVO;

/** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
public interface DossierService {
    /** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 */
    DossierVO dossier(long studentId);

    /** 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
    BoardVO board();
}
