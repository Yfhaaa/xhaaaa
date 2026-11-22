<script setup>
/**
 * 学生全过程档案：基本信息、九个流程节点进度、各节点版本与审核意见、指导记录与成绩构成。
 * 学生访问本人档案（/my-project），教师与管理员从列表进入（/students/:id），数据范围由后端校验。
 */
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { Download, Back } from "@element-plus/icons-vue";
import { useAuth } from "../../stores/auth";
import { download, get } from "../../api/advanced";
import { dateText } from "../../utils/format";
import StageTimeline from "../../components/StageTimeline.vue";

const route = useRoute();
const auth = useAuth();
const data = ref(null);
const loading = ref(true);
const studentId = route.params.id || auth.user.id;
const isSelf = !route.params.id;

onMounted(async () => {
  try {
    data.value = await get(`/students/${studentId}/dossier`);
  } finally {
    loading.value = false;
  }
});

const DONE = ["已通过", "已完成"];
const doneCount = computed(() => data.value?.timeline.filter((n) => DONE.includes(n.status)).length ?? 0);
const total = computed(() => data.value?.timeline.length ?? 9);
const percent = computed(() => Math.round((doneCount.value / total.value) * 100));
/** 当前所处节点：第一个未完成的节点 */
const activeStep = computed(() => {
  const index = data.value?.timeline.findIndex((n) => !DONE.includes(n.status)) ?? 0;
  return index < 0 ? total.value : index;
});
function stepStatus(node, index) {
  if (DONE.includes(node.status)) return "success";
  if (node.status === "已退回") return "error";
  if (index === activeStep.value) return "process";
  return "wait";
}

const score = computed(() => data.value?.scores.find((s) => s.published) || null);
const scoreParts = computed(() => {
  const s = score.value;
  if (!s) return [];
  return [
    { label: "指导", score: s.guidance_score, weight: s.guidance_weight },
    { label: "评阅", score: s.review_score, weight: s.review_weight },
    { label: "答辩", score: s.defense_score, weight: s.defense_weight },
  ];
});
function grade(total) {
  const t = Number(total);
  if (t >= 90) return "优秀";
  if (t >= 80) return "良好";
  if (t >= 70) return "中等";
  if (t >= 60) return "及格";
  return "不及格";
}
</script>

<template>
  <div>
    <PageHeader
      :title="isSelf ? '我的毕设' : '学生全过程档案'"
      description="九个流程节点、历次版本、审核意见、指导记录与成绩构成全程留痕"
    >
      <el-button
        v-if="score && auth.role !== 'STUDENT'"
        :icon="Download"
        @click="download('/archives/download', `${data.student.name}_毕业设计归档.zip`, { studentId })"
      >
        下载归档包
      </el-button>
      <el-button v-if="!isSelf" :icon="Back" @click="$router.back()">返回</el-button>
    </PageHeader>

    <el-skeleton :loading="loading" :rows="10" animated>
      <template #default>
        <template v-if="data">
          <!-- 档案卡 -->
          <section class="gd-dossier__hero">
            <div class="gd-dossier__avatar">{{ data.student.name.slice(0, 1) }}</div>
            <div class="gd-dossier__info">
              <div class="gd-dossier__name">
                <h2>{{ data.student.name }}</h2>
                <span>学号 {{ data.student.username }}</span>
              </div>
              <div class="gd-dossier__meta">
                <span>{{ data.student.major_name }}</span>
                <span>{{ data.student.class_name }}</span>
                <span>{{ data.student.cohort }} 届</span>
              </div>
              <div class="gd-dossier__topic">
                <label>课题</label>
                <strong>{{ data.student.topic_title || "尚未确定课题" }}</strong>
                <label>指导教师</label>
                <strong>{{ data.student.teacher_name || "—" }}</strong>
              </div>
            </div>
            <div class="gd-dossier__ring">
              <el-progress
                type="dashboard"
                :percentage="percent"
                :width="120"
                :stroke-width="9"
                color="#d8be7b"
              >
                <template #default>
                  <strong
                    >{{ doneCount }}<small>/{{ total }}</small></strong
                  >
                  <span>已完成节点</span>
                </template>
              </el-progress>
            </div>
          </section>

          <!-- 九节点步骤 -->
          <section class="panel gd-dossier__steps">
            <el-steps :active="activeStep" align-center>
              <el-step
                v-for="(node, index) in data.timeline"
                :key="node.code"
                :title="node.name"
                :status="stepStatus(node, index)"
                :description="node.status || '未开始'"
              />
            </el-steps>
          </section>

          <div class="gd-dossier__grid">
            <section class="panel">
              <h3 class="panel-title">全过程时间线</h3>
              <StageTimeline :nodes="data.timeline" />
            </section>

            <aside class="gd-dossier__side">
              <section class="panel">
                <h3 class="panel-title">成绩构成</h3>
                <template v-if="score">
                  <div class="gd-dossier__total">
                    <strong>{{ score.total_score }}</strong>
                    <el-tag effect="dark" round>{{ grade(score.total_score) }}</el-tag>
                  </div>
                  <div v-for="p in scoreParts" :key="p.label" class="gd-dossier__part">
                    <span
                      >{{ p.label }} <small>×{{ p.weight }}</small></span
                    >
                    <el-progress :percentage="Number(p.score)" :stroke-width="8" color="#175c4b">
                      <b>{{ p.score }}</b>
                    </el-progress>
                  </div>
                </template>
                <el-empty v-else description="成绩尚未发布" :image-size="70" />
              </section>

              <section class="panel">
                <h3 class="panel-title">
                  指导记录 <small>{{ data.guidance.length }} 次</small>
                </h3>
                <el-timeline v-if="data.guidance.length">
                  <el-timeline-item
                    v-for="g in data.guidance"
                    :key="g.id"
                    :timestamp="`${dateText(g.guidance_at)} · ${g.teacher_name}`"
                    placement="top"
                    color="#d8be7b"
                  >
                    <p class="gd-dossier__guide">{{ g.content }}</p>
                  </el-timeline-item>
                </el-timeline>
                <el-empty v-else description="暂无指导记录" :image-size="70" />
              </section>
            </aside>
          </div>
        </template>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.gd-dossier__hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 28px;
  align-items: center;
  margin-bottom: 16px;
  padding: 26px 32px;
  border-radius: 14px;
  background:
    radial-gradient(circle at 92% 20%, rgb(216 190 123 / 18%), transparent 40%),
    linear-gradient(120deg, #175c4b, #10352c);
  color: #cfe2da;
  box-shadow: 0 12px 30px rgb(18 62 52 / 15%);
}
.gd-dossier__avatar {
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border: 3px solid rgb(216 190 123 / 60%);
  border-radius: 50%;
  background: rgb(255 255 255 / 8%);
  color: #d8be7b;
  font:
    700 36px/1 Georgia,
    "SimSun",
    serif;
}
.gd-dossier__name {
  display: flex;
  gap: 14px;
  align-items: baseline;
}
.gd-dossier__name h2 {
  margin: 0;
  color: #fff;
  font-size: 26px;
  letter-spacing: 2px;
}
.gd-dossier__name span {
  color: #8fb3a5;
  font-size: 13px;
}
.gd-dossier__meta {
  display: flex;
  gap: 8px;
  margin: 10px 0 14px;
}
.gd-dossier__meta span {
  padding: 2px 10px;
  border-radius: 12px;
  background: rgb(255 255 255 / 10%);
  font-size: 12px;
}
.gd-dossier__topic {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 6px 12px;
  align-items: baseline;
}
.gd-dossier__topic label {
  color: #8fb3a5;
  font-size: 12px;
}
.gd-dossier__topic strong {
  color: #fff;
  font-size: 15px;
}
.gd-dossier__ring :deep(.el-progress__text) {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #fff !important;
}
.gd-dossier__ring strong {
  font:
    700 28px/1 Georgia,
    serif;
}
.gd-dossier__ring small {
  color: #8fb3a5;
  font-size: 14px;
}
.gd-dossier__ring span {
  color: #8fb3a5;
  font-size: 11px;
}
.gd-dossier__ring :deep(.el-progress-circle__track) {
  stroke: rgb(255 255 255 / 12%);
}
.gd-dossier__steps {
  padding: 24px 12px 16px;
}
.gd-dossier__steps :deep(.el-step__title) {
  font-size: 13px;
}
.gd-dossier__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: start;
}
.gd-dossier__side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.gd-dossier__side .panel {
  margin: 0;
}
.panel-title small {
  margin-left: 6px;
  color: #8a9a93;
  font-size: 12px;
  font-weight: 400;
}
.gd-dossier__total {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.gd-dossier__total strong {
  color: #175c4b;
  font:
    700 44px/1 Georgia,
    serif;
}
.gd-dossier__part {
  margin-bottom: 12px;
}
.gd-dossier__part span {
  color: #4f665d;
  font-size: 13px;
}
.gd-dossier__part small {
  color: #8a9a93;
}
.gd-dossier__guide {
  margin: 0;
  color: #4f665d;
  font-size: 13px;
  line-height: 1.6;
}
@media (max-width: 1200px) {
  .gd-dossier__grid {
    grid-template-columns: 1fr;
  }
  .gd-dossier__hero {
    grid-template-columns: auto 1fr;
  }
  .gd-dossier__ring {
    display: none;
  }
}
</style>
