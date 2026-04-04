<template>
  <article class="insight-card">
    <div class="section-grid">
      <section class="panel-block">
        <div class="block-head">
          <div>
            <p class="eyebrow">市场洞察</p>
            <h3>岗位热度与供需</h3>
          </div>
          <button type="button" class="tool-button" @click="loadMarketInsights" :disabled="loading.market || !selectedJobId">
            {{ loading.market ? '刷新中...' : '刷新市场数据' }}
          </button>
        </div>

        <div class="metric-grid">
          <div class="metric-item">
            <span>供需指数</span>
            <strong>{{ supplyDemand ? formatPercent(supplyDemand.supplyDemandIndex) : '--' }}</strong>
          </div>
          <div class="metric-item">
            <span>市场等级</span>
            <strong>{{ supplyDemand?.level || '--' }}</strong>
          </div>
          <div class="metric-item">
            <span>预测结论</span>
            <strong>{{ forecast?.result || '--' }}</strong>
          </div>
          <div class="metric-item">
            <span>置信度</span>
            <strong>{{ forecast ? formatPercent(forecast.confidence) : '--' }}</strong>
          </div>
        </div>

        <div v-if="marketTrend" class="stack-list top-gap">
          <div class="stack-item"><strong>行业岗位变化</strong><p>{{ marketTrend.jobCountTrend }}</p></div>
          <div class="stack-item"><strong>薪资趋势</strong><p>{{ marketTrend.salaryTrend }}</p></div>
          <div class="stack-item"><strong>人才缺口</strong><p>{{ marketTrend.talentGap }}</p></div>
        </div>

        <div class="stack-list top-gap">
          <div v-for="job in hotJobs" :key="job.jobId" class="stack-item">
            <strong>{{ job.title }}</strong>
            <p>{{ job.industry }} · {{ job.city }} · 热度 {{ formatPercent(job.hotScore) }}</p>
          </div>
          <div v-if="!hotJobs.length" class="empty-box">选择目标岗位后，这里会自动拉取热门岗位和市场趋势。</div>
        </div>
      </section>

      <section class="panel-block">
        <div class="block-head">
          <div>
            <p class="eyebrow">学习资源</p>
            <h3>课程、项目与实践</h3>
          </div>
          <button type="button" class="tool-button" @click="loadResources" :disabled="loading.resources || !studentProfile">
            {{ loading.resources ? '加载中...' : '刷新资源' }}
          </button>
        </div>

        <div class="resource-grid">
          <div class="resource-group">
            <h4>课程</h4>
            <div class="stack-list compact">
              <div v-for="item in resources.courses" :key="`course-${item.resourceId}`" class="stack-item">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
          <div class="resource-group">
            <h4>项目</h4>
            <div class="stack-list compact">
              <div v-for="item in resources.projects" :key="`project-${item.resourceId}`" class="stack-item">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
          <div class="resource-group">
            <h4>书籍</h4>
            <div class="stack-list compact">
              <div v-for="item in resources.books" :key="`book-${item.resourceId}`" class="stack-item">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
          <div class="resource-group">
            <h4>竞赛 / 实习</h4>
            <div class="stack-list compact">
              <div v-for="item in combinedPracticeResources" :key="`practice-${item.resourceId}`" class="stack-item">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <section class="panel-block top-gap">
      <div class="block-head">
        <div>
          <p class="eyebrow">阶段评估</p>
          <h3>计划复盘与调整</h3>
        </div>
        <button type="button" class="tool-button" @click="loadAssessmentHistory" :disabled="loading.assessment || !activePlan">
          {{ loading.assessment ? '刷新中...' : '刷新评估记录' }}
        </button>
      </div>

      <div class="assessment-toolbar">
        <select v-model="assessmentType" :disabled="!activePlan || loading.assessment">
          <option value="monthly">月度评估</option>
          <option value="quarterly">季度评估</option>
        </select>
        <button type="button" class="tool-button primary" @click="handleCreateAssessment" :disabled="!activePlan || loading.assessment">
          {{ loading.assessment ? '处理中...' : '创建评估' }}
        </button>
      </div>

      <div v-if="currentAssessment" class="assessment-box top-gap">
        <div class="stack-list">
          <div v-for="question in currentAssessment.questions || []" :key="question.order" class="stack-item">
            <strong>问题 {{ question.order }}</strong>
            <p>{{ question.question }}</p>
            <textarea
              v-model.trim="assessmentAnswers[question.order - 1]"
              rows="2"
              placeholder="输入你的复盘回答"
            ></textarea>
          </div>
        </div>
        <div class="assessment-actions top-gap">
          <button type="button" class="tool-button primary" @click="handleSubmitAssessment" :disabled="loading.assessment">
            提交评估答案
          </button>
          <button
            type="button"
            class="tool-button"
            @click="handleAdjustPlan"
            :disabled="loading.assessment || !currentAssessment.result"
          >
            根据结果调整计划
          </button>
        </div>
        <div v-if="currentAssessment.result" class="metric-grid top-gap">
          <div class="metric-item">
            <span>评估得分</span>
            <strong>{{ formatPercent(currentAssessment.result.score) }}</strong>
          </div>
          <div class="metric-item suggestions">
            <span>建议</span>
            <p>{{ (currentAssessment.result.suggestions || []).join('；') || '暂无建议' }}</p>
          </div>
        </div>
      </div>

      <div class="stack-list top-gap">
        <div v-for="item in assessmentHistory" :key="item.assessmentId" class="stack-item history-item">
          <div>
            <strong>{{ item.type }} · {{ item.status }}</strong>
            <p>{{ formatDate(item.createdAt) }}</p>
          </div>
          <button type="button" class="tool-button" @click="openAssessment(item.assessmentId)">查看详情</button>
        </div>
        <div v-if="!assessmentHistory.length" class="empty-box">当前计划还没有阶段评估记录。</div>
      </div>
    </section>
  </article>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { adjustPlanByAssessment, createAssessment, getAssessment, listAssessmentHistory, submitAssessment } from '../api/modules/assessment'
import { getHotJobs, getMarketForecast, getMarketTrend, getSupplyDemand } from '../api/modules/market'
import { getBookResources, getCompetitionResources, getCourseResources, getInternshipResources, getProjectResources } from '../api/modules/resources'

const props = defineProps({
  studentProfile: {
    type: Object,
    default: null,
  },
  activePlan: {
    type: Object,
    default: null,
  },
  selectedJobId: {
    type: Number,
    default: null,
  },
  selectedJobDetail: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['plan-adjusted', 'notice'])

const loading = reactive({
  market: false,
  resources: false,
  assessment: false,
})

const supplyDemand = ref(null)
const forecast = ref(null)
const marketTrend = ref(null)
const hotJobs = ref([])
const resources = reactive({
  courses: [],
  books: [],
  projects: [],
  internships: [],
  competitions: [],
})
const assessmentType = ref('monthly')
const assessmentHistory = ref([])
const currentAssessment = ref(null)
const assessmentAnswers = ref([])

const combinedPracticeResources = computed(() => [...resources.internships, ...resources.competitions].slice(0, 6))
const selectedSkills = computed(() => props.studentProfile?.skills || [])

function notice(text, type = 'info') {
  emit('notice', { text, type })
}

function formatPercent(value) {
  if (value == null || Number.isNaN(Number(value))) {
    return '--'
  }
  return `${Number(value).toFixed(2)}%`
}

function formatDate(value) {
  if (!value) {
    return '未生成'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

async function loadMarketInsights() {
  if (!props.selectedJobId) {
    supplyDemand.value = null
    forecast.value = null
    marketTrend.value = null
    hotJobs.value = []
    return
  }

  loading.market = true
  try {
    const [supply, forecastResult, trend, hotJobList] = await Promise.all([
      getSupplyDemand(props.selectedJobId),
      getMarketForecast({ jobId: props.selectedJobId, period: '6个月' }),
      getMarketTrend({ industry: props.selectedJobDetail?.industry || undefined }),
      getHotJobs({ industry: props.selectedJobDetail?.industry || undefined, city: props.selectedJobDetail?.location || undefined, limit: 5 }),
    ])

    supplyDemand.value = supply
    forecast.value = forecastResult
    marketTrend.value = trend
    hotJobs.value = hotJobList || []
  } catch (error) {
    notice(error.message || '市场洞察加载失败。', 'warning')
  } finally {
    loading.market = false
  }
}

async function loadResources() {
  if (!props.studentProfile) {
    Object.assign(resources, { courses: [], books: [], projects: [], internships: [], competitions: [] })
    return
  }

  loading.resources = true
  try {
    const [courses, books, projects, internships, competitions] = await Promise.all([
      getCourseResources({ skills: selectedSkills.value, level: 'intermediate' }),
      getBookResources({ skills: selectedSkills.value }),
      getProjectResources({ skills: selectedSkills.value, difficulty: 'medium' }),
      getInternshipResources({ location: props.selectedJobDetail?.location || undefined, skills: selectedSkills.value }),
      getCompetitionResources({ category: props.selectedJobDetail?.industry || undefined, timeRange: '近一年' }),
    ])

    resources.courses = courses || []
    resources.books = books || []
    resources.projects = projects || []
    resources.internships = internships || []
    resources.competitions = competitions || []
  } catch (error) {
    notice(error.message || '资源推荐加载失败。', 'warning')
  } finally {
    loading.resources = false
  }
}

async function loadAssessmentHistory() {
  if (!props.activePlan?.planId) {
    assessmentHistory.value = []
    currentAssessment.value = null
    return
  }

  loading.assessment = true
  try {
    assessmentHistory.value = await listAssessmentHistory(props.activePlan.planId)
  } catch (error) {
    notice(error.message || '评估历史加载失败。', 'warning')
  } finally {
    loading.assessment = false
  }
}

async function handleCreateAssessment() {
  if (!props.activePlan?.planId) {
    return
  }

  loading.assessment = true
  try {
    currentAssessment.value = await createAssessment({
      planId: props.activePlan.planId,
      type: assessmentType.value,
    })
    assessmentAnswers.value = (currentAssessment.value.questions || []).map(() => '')
    await loadAssessmentHistory()
    notice('阶段评估已创建。', 'success')
  } catch (error) {
    notice(error.message || '创建评估失败。', 'error')
  } finally {
    loading.assessment = false
  }
}

async function handleSubmitAssessment() {
  if (!currentAssessment.value?.assessmentId) {
    return
  }

  loading.assessment = true
  try {
    currentAssessment.value = await submitAssessment(currentAssessment.value.assessmentId, {
      answers: assessmentAnswers.value.map((item) => item || '待补充'),
    })
    await loadAssessmentHistory()
    notice('评估答案已提交。', 'success')
  } catch (error) {
    notice(error.message || '提交评估失败。', 'error')
  } finally {
    loading.assessment = false
  }
}

async function handleAdjustPlan() {
  if (!currentAssessment.value?.assessmentId) {
    return
  }

  loading.assessment = true
  try {
    const updatedPlan = await adjustPlanByAssessment(currentAssessment.value.assessmentId, {
      acceptSuggestions: true,
    })
    emit('plan-adjusted', updatedPlan)
    notice('已根据评估结果调整计划。', 'success')
  } catch (error) {
    notice(error.message || '计划调整失败。', 'error')
  } finally {
    loading.assessment = false
  }
}

async function openAssessment(assessmentId) {
  loading.assessment = true
  try {
    currentAssessment.value = await getAssessment(assessmentId)
    assessmentAnswers.value = [...(currentAssessment.value.answers || [])]
  } catch (error) {
    notice(error.message || '评估详情加载失败。', 'warning')
  } finally {
    loading.assessment = false
  }
}

watch(
  () => props.selectedJobId,
  () => {
    loadMarketInsights()
  },
  { immediate: true },
)

watch(
  () => props.studentProfile?.profileId,
  () => {
    loadResources()
  },
  { immediate: true },
)

watch(
  () => props.activePlan?.planId,
  () => {
    loadAssessmentHistory()
  },
  { immediate: true },
)
</script>

<style scoped>
.insight-card {
  display: grid;
  gap: 18px;
}

.section-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel-block {
  border-radius: 22px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(23, 48, 66, 0.08);
  box-shadow: 0 18px 40px rgba(19, 47, 63, 0.08);
}

.block-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.block-head h3,
.resource-group h4 {
  margin: 0;
}

.eyebrow {
  margin: 0 0 6px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 0.75rem;
  color: #8e7a32;
}

.tool-button {
  padding: 10px 14px;
  border-radius: 14px;
  border: 1px solid rgba(23, 48, 66, 0.16);
  background: transparent;
  color: #173042;
  cursor: pointer;
}

.tool-button.primary {
  background: linear-gradient(135deg, #173042, #1f6063);
  color: #fff;
  border-color: transparent;
}

.tool-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.metric-grid,
.resource-grid {
  display: grid;
  gap: 12px;
}

.metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-item,
.stack-item,
.empty-box,
.assessment-box {
  border-radius: 16px;
  padding: 14px 16px;
  background: rgba(23, 48, 66, 0.04);
}

.metric-item span {
  display: block;
  color: #6a7980;
}

.metric-item strong {
  display: block;
  margin-top: 6px;
}

.metric-item.suggestions {
  grid-column: span 3;
}

.stack-list {
  display: grid;
  gap: 10px;
}

.stack-list.compact {
  max-height: 260px;
  overflow: auto;
}

.stack-item strong {
  display: block;
}

.stack-item p,
.metric-item p {
  margin: 8px 0 0;
  color: #5b6b74;
}

.resource-group {
  display: grid;
  gap: 10px;
}

.resource-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.assessment-toolbar,
.assessment-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.assessment-toolbar select,
.assessment-box textarea {
  width: 100%;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(23, 48, 66, 0.12);
  background: rgba(255, 255, 255, 0.82);
  color: #173042;
}

.assessment-toolbar select {
  width: auto;
  min-width: 160px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.top-gap {
  margin-top: 16px;
}

@media (max-width: 1080px) {
  .section-grid,
  .resource-grid,
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .metric-item.suggestions {
    grid-column: auto;
  }
}

@media (max-width: 720px) {
  .block-head,
  .history-item,
  .assessment-toolbar,
  .assessment-actions {
    flex-direction: column;
  }

  .assessment-toolbar select {
    width: 100%;
  }
}
</style>
