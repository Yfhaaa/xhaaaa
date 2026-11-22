package edu.gengdan.gdpm.vo;

import java.math.BigDecimal;

/** 成绩保存后的编号和服务端计算总分。 */
public class ScoreSavedVO {

    public final Long id;

    public final BigDecimal totalScore;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public ScoreSavedVO(
        Long id,
        BigDecimal totalScore
    ) {
        this.id = id;
        this.totalScore = totalScore;
    }
}
