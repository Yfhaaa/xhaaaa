package edu.gengdan.gdpm.common;



/** 课题、双选、过程材料与答辩的统一状态。 */
public enum BusinessStatus {
    NOT_SUBMITTED("待提交"),
    PENDING("待审核"),
    APPROVED("已通过"),
    RETURNED("已退回"),
    COMPLETED("已完成"),
    AWAITING_REVIEW("待评阅"),
    REVIEWED("已评阅"),
    AWAITING_PUBLICATION("待发布"),
    AWAITING_OLD_TEACHER("待原教师审批"),
    AWAITING_NEW_TEACHER("待新教师审批"),
    AWAITING_ADMIN("待管理员审批");

    private final String label;

    BusinessStatus(String label) {
        this.label = label;
    }

    /** 返回持久化使用的中文状态。 */
    public String label() {
        return label;
    }

    /** 将审核结果转换为统一状态。 */
    public static String reviewed(boolean approved) {
        return approved ? APPROVED.label : RETURNED.label;
    }
}
