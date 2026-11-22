import { ElMessageBox } from "element-plus";

/**
 * 二次确认：用户确认返回 true，取消或关闭返回 false；其他异常照常抛出（不吞掉接口错误）。
 * 用法：if (!(await confirmAction("确认删除？", "删除"))) return;
 */
export async function confirmAction(message, title = "操作确认", options = {}) {
  try {
    await ElMessageBox.confirm(message, title, {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      ...options,
    });
    return true;
  } catch (action) {
    if (action === "cancel" || action === "close") return false;
    throw action;
  }
}
