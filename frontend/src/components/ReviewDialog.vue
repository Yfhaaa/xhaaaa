<script setup>
/**
 * 审核/审批对话框：展示待审对象摘要，提供常用意见快捷填入，通过或退回均须填写意见（留痕）。
 * 事件 submit 参数：{ approved: boolean, comment: string }
 */
import { ref, watch } from "vue";

const props = defineProps({
  modelValue: Boolean,
  title: { type: String, default: "审批意见" },
  /** 摘要信息，形如 [{ label: '学生', value: '刘冉' }] */
  summary: { type: Array, default: () => [] },
  loading: Boolean,
});
const emit = defineEmits(["update:modelValue", "submit"]);

const PASS_PHRASES = ["材料完整，符合要求，同意通过。", "整体质量较好，同意进入下一阶段。"];
const REJECT_PHRASES = ["格式不规范，请按模板修改后重新提交。", "内容不够完整，请补充后重新提交。"];

const comment = ref("");
watch(
  () => props.modelValue,
  (open) => {
    if (open) comment.value = "";
  },
);

function usePhrase(text) {
  comment.value = comment.value ? `${comment.value}\n${text}` : text;
}
function submit(approved) {
  if (!comment.value.trim()) return;
  emit("submit", { approved, comment: comment.value.trim() });
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="560px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-descriptions v-if="summary.length" :column="2" border size="small" class="gd-review__summary">
      <el-descriptions-item v-for="item in summary" :key="item.label" :label="item.label">
        {{ item.value ?? "—" }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="gd-review__label">审核意见 <span>（必填，将记录到审核留痕）</span></div>
    <el-input
      v-model="comment"
      type="textarea"
      :rows="5"
      maxlength="2000"
      show-word-limit
      placeholder="请填写具体意见；退回时请写明需要修改的内容"
    />
    <div class="gd-review__phrases">
      <span>常用：</span>
      <el-tag v-for="p in PASS_PHRASES" :key="p" type="success" effect="plain" @click="usePhrase(p)">
        {{ p }}
      </el-tag>
      <el-tag v-for="p in REJECT_PHRASES" :key="p" type="danger" effect="plain" @click="usePhrase(p)">
        {{ p }}
      </el-tag>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button :disabled="!comment.trim()" :loading="loading" type="danger" plain @click="submit(false)">
        退回修改
      </el-button>
      <el-button :disabled="!comment.trim()" :loading="loading" type="primary" @click="submit(true)">
        审核通过
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.gd-review__summary {
  margin-bottom: 16px;
}
.gd-review__label {
  margin-bottom: 8px;
  color: #273d37;
  font-size: 13px;
  font-weight: 600;
}
.gd-review__label span {
  color: #8a9a93;
  font-weight: 400;
}
.gd-review__phrases {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-top: 10px;
  font-size: 12px;
}
.gd-review__phrases span {
  color: #8a9a93;
}
.gd-review__phrases .el-tag {
  cursor: pointer;
}
</style>
