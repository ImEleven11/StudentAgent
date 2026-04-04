<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-card brand-card">
        <p class="eyebrow">StudentAgent</p>
        <h1>前后端对齐工作台</h1>
        <p>认证、画像、聊天、规划与归档已经按后端模块拆分接入。</p>
      </div>

      <div class="sidebar-card">
        <strong>{{ isAuthenticated ? displayName : '未登录' }}</strong>
        <p>{{ isAuthenticated ? currentUser?.email : '请先登录后再调用受保护接口' }}</p>
      </div>

      <nav class="nav-list">
        <button
          v-for="item in navigationItems"
          :key="item.key"
          type="button"
          class="nav-button"
          :class="{ active: currentTab === item.key }"
          :disabled="item.requiresAuth && !isAuthenticated"
          @click="currentTab = item.key"
        >
          {{ item.label }}
        </button>
      </nav>

      <div v-if="studentProfile" class="sidebar-card metrics-grid">
        <div>
          <span>完整度</span>
          <strong>{{ formatPercent(studentProfile.completenessScore) }}</strong>
        </div>
        <div>
          <span>能力值</span>
          <strong>{{ formatPercent(studentProfile.abilityScore) }}</strong>
        </div>
        <div>
          <span>竞争力</span>
          <strong>{{ formatPercent(studentProfile.competitivenessScore) }}</strong>
        </div>
      </div>
    </aside>

    <main class="main-panel">
      <header class="page-header">
        <div>
          <p class="eyebrow">架构一致性</p>
          <h2>{{ tabMeta[currentTab].title }}</h2>
          <p>{{ tabMeta[currentTab].description }}</p>
        </div>
        <div class="header-actions">
          <button type="button" class="button secondary" @click="refreshJobSearch" :disabled="loading.jobs">
            {{ loading.jobs ? '刷新中...' : '刷新岗位' }}
          </button>
          <button v-if="isAuthenticated" type="button" class="button ghost" @click="handleLogout" :disabled="loading.auth">
            退出登录
          </button>
        </div>
      </header>

      <div v-if="statusBanner.text" class="notice" :class="statusBanner.type">{{ statusBanner.text }}</div>

      <section v-if="!isAuthenticated || currentTab === 'auth'" class="grid two">
        <article class="card">
          <div class="card-head">
            <h3>{{ isAuthenticated ? '账户资料' : authMode === 'login' ? '登录' : '注册' }}</h3>
            <div v-if="!isAuthenticated" class="switch-row">
              <button type="button" class="mini-button" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button>
              <button type="button" class="mini-button" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册</button>
            </div>
          </div>

          <form v-if="!isAuthenticated" class="form-stack" @submit.prevent="handleAuthSubmit">
            <label v-if="authMode === 'register'"><span>用户名</span><input v-model.trim="authForm.username" type="text" placeholder="4-32 位用户名" /></label>
            <label v-if="authMode === 'register'"><span>邮箱</span><input v-model.trim="authForm.email" type="email" placeholder="you@example.com" /></label>
            <label v-else><span>账号</span><input v-model.trim="authForm.account" type="text" placeholder="用户名或邮箱" /></label>
            <label><span>密码</span><input v-model="authForm.password" type="password" placeholder="至少 8 位" /></label>
            <button type="submit" class="button primary" :disabled="loading.auth">
              {{ loading.auth ? '提交中...' : authMode === 'login' ? '登录并保存 Token' : '注册并自动登录' }}
            </button>
          </form>

          <form v-else class="form-stack" @submit.prevent="handleUserProfileSave">
            <label><span>用户名</span><input :value="currentUser?.username || ''" type="text" disabled /></label>
            <label><span>邮箱</span><input :value="currentUser?.email || ''" type="email" disabled /></label>
            <label><span>昵称</span><input v-model.trim="userProfileForm.nickname" type="text" placeholder="展示名称" /></label>
            <label><span>头像地址</span><input v-model.trim="userProfileForm.avatar" type="text" placeholder="https://..." /></label>
            <label><span>手机号</span><input v-model.trim="userProfileForm.phone" type="text" placeholder="用于完善个人资料" /></label>
            <button type="submit" class="button primary" :disabled="loading.user">{{ loading.user ? '保存中...' : '保存资料' }}</button>
          </form>
        </article>

        <article class="card">
          <h3>已接入的接口约束</h3>
          <div class="list-block">
            <div class="list-item"><strong>统一解包</strong><p>所有接口通过 `code / message / data` 解包并统一处理异常。</p></div>
            <div class="list-item"><strong>本地会话恢复</strong><p>JWT、画像 ID、聊天会话 ID 分别保存，刷新页面后仍可恢复上下文。</p></div>
            <div class="list-item"><strong>独立 API 模块</strong><p>`auth / profile / chat / job / match / plan / report` 已分文件维护。</p></div>
          </div>
          <h3 class="top-gap">岗位接口预览</h3>
          <div class="list-block">
            <div v-for="job in jobResults.slice(0, 4)" :key="job.jobId" class="list-item">
              <strong>{{ job.title }}</strong>
              <p>{{ job.industry }} · {{ job.location }}</p>
            </div>
            <div v-if="!jobResults.length" class="empty-box">后端启动后会通过 `/api/job/search` 拉取岗位数据。</div>
          </div>
        </article>
      </section>

      <section v-if="isAuthenticated && currentTab === 'profile'" class="grid two">
        <article class="card">
          <h3>上传简历</h3>
          <label class="upload-box">
            <input type="file" accept=".pdf,.doc,.docx" @change="handleResumeUpload" />
            <span>点击上传 PDF / Word 简历，调用 `/api/profile/upload-resume`</span>
          </label>
          <p class="meta-text">文件：{{ studentProfile?.resumeFileName || '未上传' }}</p>
          <p class="meta-text">状态：{{ studentProfile?.parseStatus || 'WAITING' }}<span v-if="studentProfile"> · {{ studentProfile.parseProgress }}%</span></p>
        </article>

        <article class="card">
          <h3>学生画像表单</h3>
          <form class="form-stack" @submit.prevent="handleProfileSubmit">
            <label><span>教育背景</span><textarea v-model.trim="profileForm.educationSummary" rows="3" placeholder="专业、课程、方向"></textarea></label>
            <label><span>技能清单</span><textarea v-model.trim="profileForm.skillsText" rows="3" placeholder="每行一项或逗号分隔"></textarea></label>
            <label><span>项目 / 实习经历</span><textarea v-model.trim="profileForm.experiencesText" rows="3" placeholder="每行一项经历"></textarea></label>
            <label><span>证书 / 荣誉</span><textarea v-model.trim="profileForm.certificatesText" rows="3" placeholder="证书、奖项、竞赛"></textarea></label>
            <label><span>创新能力</span><textarea v-model.trim="profileForm.innovationSummary" rows="3" placeholder="科研、作品、竞赛成果"></textarea></label>
            <label><span>兴趣方向</span><textarea v-model.trim="profileForm.interestSummary" rows="2" placeholder="偏好的行业或岗位"></textarea></label>
            <label><span>性格特征</span><textarea v-model.trim="profileForm.personalitySummary" rows="2" placeholder="协作方式、执行风格"></textarea></label>
            <button type="submit" class="button primary" :disabled="loading.profile">
              {{ loading.profile ? '保存中...' : studentProfile ? '更新画像' : '创建画像' }}
            </button>
          </form>
        </article>

        <article class="card">
          <h3>画像概览</h3>
          <div v-if="studentProfile" class="list-block">
            <div class="list-item"><strong>画像 ID</strong><p>#{{ studentProfile.profileId }}</p></div>
            <div class="list-item"><strong>创建时间</strong><p>{{ formatDate(studentProfile.createdAt) }}</p></div>
            <div class="list-item"><strong>更新时间</strong><p>{{ formatDate(studentProfile.updatedAt) }}</p></div>
          </div>
          <div v-else class="empty-box">先上传简历或填写画像表单。</div>
        </article>

        <article class="card">
          <h3>待补全字段</h3>
          <div class="tag-list">
            <span v-for="field in missingFields" :key="field" class="tag warning">{{ field }}</span>
            <span v-if="!missingFields.length" class="tag success">画像已完整</span>
          </div>
        </article>
      </section>

      <section v-if="isAuthenticated && currentTab === 'chat'" class="grid chat-layout">
        <article class="card">
          <div class="card-head">
            <h3>智能对话</h3>
            <button type="button" class="button ghost" @click="handleClearChat" :disabled="!chatSessionId || loading.chat">清空会话</button>
          </div>
          <div class="chat-box">
            <div v-for="message in chatMessages" :key="message.id || `${message.role}-${message.createdAt || message.content}`" class="message-row" :class="message.role?.toLowerCase()">
              <div class="message-bubble">
                <strong>{{ message.role?.toLowerCase() === 'user' ? '你' : 'AI' }}</strong>
                <p>{{ message.content }}</p>
              </div>
            </div>
          </div>
          <form class="form-stack top-gap" @submit.prevent="handleSendMessage">
            <textarea v-model.trim="chatInput" rows="3" placeholder="聊聊目标岗位、技能差距或下一步计划..."></textarea>
            <button type="submit" class="button primary" :disabled="loading.chat || !chatInput.trim()">{{ loading.chat ? '发送中...' : '发送消息' }}</button>
          </form>
        </article>

        <article class="card">
          <h3>上下文状态</h3>
          <div class="list-block">
            <div class="list-item"><strong>当前画像</strong><p>{{ studentProfile ? `#${studentProfile.profileId}` : '未创建' }}</p></div>
            <div class="list-item"><strong>会话 ID</strong><p>{{ chatSessionId || '未创建' }}</p></div>
            <div class="list-item"><strong>待补字段</strong><p>{{ missingFields.length ? missingFields.join('、') : '无' }}</p></div>
          </div>
        </article>
      </section>

      <section v-if="isAuthenticated && currentTab === 'planning'" class="grid two">
        <article class="card">
          <h3>岗位搜索</h3>
          <form class="search-row" @submit.prevent="refreshJobSearch">
            <input v-model.trim="jobSearchForm.keyword" type="text" placeholder="关键词" />
            <input v-model.trim="jobSearchForm.industry" type="text" placeholder="行业" />
            <input v-model.trim="jobSearchForm.location" type="text" placeholder="城市" />
            <button type="submit" class="button secondary" :disabled="loading.jobs">搜索</button>
          </form>
          <div class="list-block">
            <button v-for="job in jobResults" :key="job.jobId" type="button" class="select-card" :class="{ active: selectedJobId === job.jobId }" @click="handleSelectJob(job.jobId)">
              <strong>{{ job.title }}</strong>
              <p>{{ job.industry }} · {{ job.location }} · {{ formatSalaryRange(job.salaryMin, job.salaryMax) }}</p>
            </button>
            <div v-if="!jobResults.length" class="empty-box">暂无岗位数据。</div>
          </div>
        </article>

        <article class="card">
          <div class="card-head">
            <h3>推荐与匹配</h3>
            <button type="button" class="button ghost" @click="refreshRecommendations" :disabled="loading.recommend || !studentProfile">{{ loading.recommend ? '计算中...' : '刷新推荐' }}</button>
          </div>
          <div class="tag-list">
            <button v-for="job in recommendedJobs" :key="job.jobId" type="button" class="tag button-tag" :class="{ active: selectedJobId === job.jobId }" @click="handleSelectJob(job.jobId)">
              {{ job.jobTitle }} · {{ formatPercent(job.matchScore) }}
            </button>
          </div>
          <div v-if="selectedJobDetail" class="list-item top-gap">
            <strong>{{ selectedJobDetail.title }}</strong>
            <p>{{ selectedJobDetail.description }}</p>
          </div>
          <div v-if="matchInsight" class="list-block top-gap">
            <div class="list-item"><strong>综合匹配</strong><p>{{ formatPercent(matchInsight.overallScore) }}</p></div>
            <div class="list-item"><strong>技能匹配</strong><p>{{ formatPercent(matchInsight.skillScore) }}</p></div>
            <div class="list-item"><strong>建议</strong><p>{{ matchInsight.summary }}</p></div>
          </div>
        </article>

        <article class="card">
          <h3>路径、计划与报告</h3>
          <div v-if="selectedJobPath" class="list-block">
            <div v-for="node in selectedJobPath.promotionPaths" :key="`promotion-${node.targetJobId}-${node.stepOrder}`" class="list-item">
              <strong>晋升 {{ node.stepOrder }} · {{ node.targetJobTitle }}</strong>
              <p>{{ node.description }}</p>
            </div>
            <div v-for="node in selectedJobPath.transitionPaths" :key="`transition-${node.targetJobId}-${node.stepOrder}`" class="list-item">
              <strong>转岗 {{ node.stepOrder }} · {{ node.targetJobTitle }}</strong>
              <p>{{ node.description }}</p>
            </div>
          </div>
          <div v-else class="empty-box">选择一个岗位后会加载职业路径。</div>
          <div class="action-row top-gap">
            <button type="button" class="button primary" @click="handleGeneratePlan" :disabled="loading.plan || !studentProfile">{{ loading.plan ? '生成中...' : '生成阶段计划' }}</button>
            <button type="button" class="button secondary" @click="handleGenerateReport" :disabled="loading.report || !studentProfile">{{ loading.report ? '生成中...' : '生成报告' }}</button>
          </div>
          <div v-if="activePlan" class="list-block top-gap">
            <div v-for="task in activePlan.tasks" :key="task.taskId" class="task-row">
              <div>
                <strong>{{ task.phaseName }} · {{ task.taskTitle }}</strong>
                <p>{{ task.description }}</p>
              </div>
              <button type="button" class="button ghost" :disabled="task.completed || loading.plan" @click="handleCompleteTask(task.taskId)">
                {{ task.completed ? '已完成' : '完成任务' }}
              </button>
            </div>
          </div>
        </article>

        <PlanningInsightPanel
          :student-profile="studentProfile"
          :active-plan="activePlan"
          :selected-job-id="selectedJobId"
          :selected-job-detail="selectedJobDetail"
          @plan-adjusted="handlePlanAdjusted"
          @notice="handleInsightNotice"
        />
      </section>

      <section v-if="isAuthenticated && currentTab === 'archive'" class="grid archive-layout">
        <article class="card">
          <h3>历史计划</h3>
          <div class="list-block">
            <button v-for="plan in planItems" :key="plan.planId" type="button" class="select-card" :class="{ active: activePlan?.planId === plan.planId }" @click="openPlan(plan.planId)">
              <strong>{{ plan.title }}</strong>
              <p>{{ formatPercent(plan.progress) }} · {{ plan.status }}</p>
            </button>
            <div v-if="!planItems.length" class="empty-box">暂无历史计划。</div>
          </div>
        </article>

        <article class="card">
          <h3>历史报告</h3>
          <div class="list-block">
            <button v-for="report in reportItems" :key="report.reportId" type="button" class="select-card" :class="{ active: activeReport?.reportId === report.reportId }" @click="openReport(report.reportId)">
              <strong>{{ report.title }}</strong>
              <p>{{ formatDate(report.createdAt) }}</p>
            </button>
            <div v-if="!reportItems.length" class="empty-box">暂无历史报告。</div>
          </div>
        </article>

        <article class="card">
          <div class="card-head">
            <h3>{{ activeReport ? activeReport.title : '报告详情' }}</h3>
            <div class="header-actions">
              <button type="button" class="button ghost" @click="handlePolishReport" :disabled="!activeReport || loading.polish">{{ loading.polish ? '润色中...' : '智能润色' }}</button>
              <button type="button" class="button secondary" @click="handleExportReport('pdf')" :disabled="!activeReport || loading.export">导出 PDF</button>
              <button type="button" class="button secondary" @click="handleExportReport('word')" :disabled="!activeReport || loading.export">导出 Word</button>
            </div>
          </div>
          <div v-if="activeReport" class="list-block">
            <div v-for="section in activeReport.sections" :key="section.title" class="list-item">
              <strong>{{ section.title }}</strong>
              <p>{{ section.content }}</p>
            </div>
            <div v-if="activeReport.polishedContent" class="list-item highlight-box">
              <strong>润色后的整合文本</strong>
              <p>{{ activeReport.polishedContent }}</p>
            </div>
          </div>
          <div v-else class="empty-box">选择一份报告后查看详情。</div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getCurrentUserProfile, loginUser, logoutUser, registerUser, updateCurrentUserProfile } from './api/modules/auth'
import { clearChatHistory, getChatHistory, sendChatMessage } from './api/modules/chat'
import { getJobDetail, getJobPath, searchJobs } from './api/modules/job'
import { analyzeMatch, recommendJobs } from './api/modules/match'
import { completePlanTask, generatePlan, getPlan, listPlans } from './api/modules/plan'
import { completeStudentProfile, createProfileByManualInput, getProfileMissingFields, getStudentProfile, updateStudentProfile, uploadResumeFile } from './api/modules/profile'
import { exportGeneratedReport, generateReport, getReport, listReports, polishGeneratedReport } from './api/modules/report'
import { clearClientSession, clearStoredChatSessionId, getAccessToken, getActiveProfileId, getChatSessionId, setActiveProfileId, setChatSessionId } from './api/session'
import PlanningInsightPanel from './components/PlanningInsightPanel.vue'

const navigationItems = [
  { key: 'auth', label: '账户接入', requiresAuth: false },
  { key: 'profile', label: '学生画像', requiresAuth: true },
  { key: 'chat', label: '智能对话', requiresAuth: true },
  { key: 'planning', label: '规划引擎', requiresAuth: true },
  { key: 'archive', label: '归档中心', requiresAuth: true },
]

const tabMeta = {
  auth: { title: '账户认证与接口入口', description: '负责用户登录、注册、JWT 持久化与账户资料维护。' },
  profile: { title: '学生画像采集与补全', description: '简历上传和手动输入都走后端画像接口。' },
  chat: { title: 'AI 对话与上下文恢复', description: '对话依赖画像与会话 ID，刷新后仍可恢复上下文。' },
  planning: { title: '岗位匹配与行动规划', description: '公开岗位搜索与受保护的匹配、计划、报告接口在此汇合。' },
  archive: { title: '历史计划与报告归档', description: '归档区支持计划复用、报告详情查看、润色和导出。' },
}

const currentTab = ref('auth')
const authMode = ref('login')
const currentUser = ref(null)
const studentProfile = ref(null)
const missingFields = ref([])
const chatMessages = ref([])
const chatInput = ref('')
const chatSessionId = ref(getChatSessionId())
const jobResults = ref([])
const recommendedJobs = ref([])
const selectedJobId = ref(null)
const selectedJobDetail = ref(null)
const selectedJobPath = ref(null)
const matchInsight = ref(null)
const planItems = ref([])
const activePlan = ref(null)
const reportItems = ref([])
const activeReport = ref(null)
const statusBanner = ref({ type: 'info', text: '' })

const authForm = reactive({ account: '', username: '', email: '', password: '' })
const userProfileForm = reactive({ nickname: '', avatar: '', phone: '' })
const profileForm = reactive({
  educationSummary: '',
  skillsText: '',
  experiencesText: '',
  certificatesText: '',
  innovationSummary: '',
  interestSummary: '',
  personalitySummary: '',
})
const jobSearchForm = reactive({ keyword: '', industry: '', location: '', page: 1, size: 8 })
const loading = reactive({ auth: false, user: false, profile: false, jobs: false, recommend: false, chat: false, plan: false, report: false, polish: false, export: false })

const isAuthenticated = computed(() => Boolean(currentUser.value && getAccessToken()))
const displayName = computed(() => currentUser.value?.nickname || currentUser.value?.username || '已登录用户')
const activeProfileId = computed(() => studentProfile.value?.profileId || getActiveProfileId())

function setNotice(text, type = 'info') {
  statusBanner.value = { text, type }
}

function clearTransientState() {
  studentProfile.value = null
  missingFields.value = []
  chatMessages.value = []
  chatInput.value = ''
  chatSessionId.value = ''
  recommendedJobs.value = []
  selectedJobId.value = null
  selectedJobDetail.value = null
  selectedJobPath.value = null
  matchInsight.value = null
  planItems.value = []
  activePlan.value = null
  reportItems.value = []
  activeReport.value = null
}

function syncUserProfileForm(user) {
  userProfileForm.nickname = user?.nickname || ''
  userProfileForm.avatar = user?.avatar || ''
  userProfileForm.phone = user?.phone || ''
}

function syncStudentProfileForm(profile) {
  profileForm.educationSummary = profile?.educationSummary || ''
  profileForm.skillsText = (profile?.skills || []).join('\n')
  profileForm.experiencesText = (profile?.experiences || []).join('\n')
  profileForm.certificatesText = (profile?.certificates || []).join('\n')
  profileForm.innovationSummary = profile?.innovationSummary || ''
  profileForm.interestSummary = profile?.interestSummary || ''
  profileForm.personalitySummary = profile?.personalitySummary || ''
}

function parseList(rawValue) {
  if (!rawValue) {
    return []
  }
  return [...new Set(rawValue.split(/\r?\n|,|，|;/).map((item) => item.trim()).filter(Boolean))]
}

function optionalText(value) {
  const normalized = value?.trim() || ''
  return normalized || null
}

function buildProfilePayload() {
  return {
    educationSummary: optionalText(profileForm.educationSummary),
    skills: parseList(profileForm.skillsText),
    experiences: parseList(profileForm.experiencesText),
    certificates: parseList(profileForm.certificatesText),
    innovationSummary: optionalText(profileForm.innovationSummary),
    interestSummary: optionalText(profileForm.interestSummary),
    personalitySummary: optionalText(profileForm.personalitySummary),
  }
}

function seedChatMessages() {
  if (!isAuthenticated.value) {
    chatMessages.value = [{ id: 'guest', role: 'AI', content: '登录后即可使用受保护的对话接口，我会基于你的画像给出职业建议。' }]
    return
  }
  if (!studentProfile.value) {
    chatMessages.value = [{ id: 'profile-needed', role: 'AI', content: '先上传简历或补全画像，之后我就能结合你的背景继续分析。' }]
    return
  }
  chatMessages.value = [{ id: 'profile-ready', role: 'AI', content: missingFields.value.length ? `画像仍建议补充：${missingFields.value.join('、')}。你也可以直接问我岗位方向和技能差距。` : '画像已准备完成，你可以直接咨询目标岗位、技能提升路径或计划执行策略。' }]
}

async function refreshJobSearch() {
  loading.jobs = true
  try {
    const pageData = await searchJobs(jobSearchForm)
    jobResults.value = pageData?.records || []
    if (!selectedJobId.value && jobResults.value.length) {
      await handleSelectJob(jobResults.value[0].jobId, false)
    }
  } catch (error) {
    setNotice(error.message || '岗位搜索失败，请检查后端服务。', 'error')
  } finally {
    loading.jobs = false
  }
}

async function handleSelectJob(jobId, showNotice = true) {
  selectedJobId.value = jobId
  try {
    const [detail, path] = await Promise.all([getJobDetail(jobId), getJobPath(jobId)])
    selectedJobDetail.value = detail
    selectedJobPath.value = path
    matchInsight.value = activeProfileId.value ? await analyzeMatch({ profileId: activeProfileId.value, jobId }) : null
    if (showNotice) {
      setNotice(`已切换到岗位「${detail.title}」。`, 'success')
    }
  } catch (error) {
    setNotice(error.message || '岗位详情加载失败。', 'error')
  }
}

async function refreshRecommendations() {
  if (!activeProfileId.value) {
    recommendedJobs.value = []
    return
  }
  loading.recommend = true
  try {
    recommendedJobs.value = await recommendJobs({ profileId: activeProfileId.value, topN: 5 })
    const preferredJobId = selectedJobId.value || recommendedJobs.value[0]?.jobId
    if (preferredJobId) {
      await handleSelectJob(preferredJobId, false)
    }
  } catch (error) {
    recommendedJobs.value = []
    setNotice(error.message || '岗位推荐暂时不可用。', 'warning')
  } finally {
    loading.recommend = false
  }
}

async function refreshPlans(selectCurrent = true) {
  if (!activeProfileId.value) {
    planItems.value = []
    activePlan.value = null
    return
  }
  const plans = await listPlans(activeProfileId.value)
  planItems.value = plans
  if (!selectCurrent) {
    return
  }
  if (activePlan.value) {
    activePlan.value = plans.find((item) => item.planId === activePlan.value.planId) || null
    return
  }
  activePlan.value = plans[0] || null
}

async function refreshReports(selectCurrent = true) {
  if (!activeProfileId.value) {
    reportItems.value = []
    activeReport.value = null
    return
  }
  const reports = await listReports(activeProfileId.value)
  reportItems.value = reports
  if (!selectCurrent) {
    return
  }
  const preferredReportId = activeReport.value?.reportId || reports[0]?.reportId
  activeReport.value = preferredReportId ? await getReport(preferredReportId) : null
}

async function hydrateProfile(profileId) {
  loading.profile = true
  try {
    const profile = await getStudentProfile(profileId)
    studentProfile.value = profile
    setActiveProfileId(profile.profileId)
    syncStudentProfileForm(profile)
    missingFields.value = await getProfileMissingFields(profile.profileId)
    seedChatMessages()
    await Promise.all([refreshRecommendations(), refreshPlans(), refreshReports()])
    if (chatSessionId.value) {
      await refreshChatMessages()
    }
  } catch (error) {
    setNotice(error.message || '画像加载失败。', 'error')
  } finally {
    loading.profile = false
  }
}

async function refreshChatMessages() {
  if (!chatSessionId.value) {
    seedChatMessages()
    return
  }
  try {
    const history = await getChatHistory(chatSessionId.value)
    chatMessages.value = history?.messages?.length ? history.messages : []
    if (!chatMessages.value.length) {
      seedChatMessages()
    }
  } catch (error) {
    chatSessionId.value = ''
    clearStoredChatSessionId()
    seedChatMessages()
    setNotice(error.message || '历史会话恢复失败，已重置聊天上下文。', 'warning')
  }
}

async function initializeApp() {
  await refreshJobSearch()
  if (!getAccessToken()) {
    seedChatMessages()
    return
  }
  try {
    currentUser.value = await getCurrentUserProfile()
    syncUserProfileForm(currentUser.value)
    currentTab.value = 'profile'
    const profileId = getActiveProfileId()
    if (profileId) {
      await hydrateProfile(profileId)
    } else {
      seedChatMessages()
    }
  } catch (error) {
    clearClientSession()
    currentUser.value = null
    clearTransientState()
    seedChatMessages()
    setNotice(error.message || '本地登录状态已失效，请重新登录。', 'warning')
  }
}

async function handleAuthSubmit() {
  loading.auth = true
  try {
    const loginResponse = authMode.value === 'register'
      ? (await registerUser({ username: authForm.username, email: authForm.email, password: authForm.password }), await loginUser({ account: authForm.username, password: authForm.password }))
      : await loginUser({ account: authForm.account, password: authForm.password })
    currentUser.value = loginResponse.user
    syncUserProfileForm(currentUser.value)
    currentTab.value = 'profile'
    setNotice(authMode.value === 'register' ? '注册成功，已自动完成登录。' : '登录成功，JWT 已保存到本地。', 'success')
    if (getActiveProfileId()) {
      await hydrateProfile(getActiveProfileId())
    } else {
      clearTransientState()
      seedChatMessages()
      await refreshJobSearch()
    }
  } catch (error) {
    setNotice(error.message || '认证失败，请检查输入信息。', 'error')
  } finally {
    loading.auth = false
  }
}

async function handleLogout() {
  loading.auth = true
  try {
    await logoutUser()
    currentUser.value = null
    clearTransientState()
    seedChatMessages()
    currentTab.value = 'auth'
    setNotice('已安全退出登录。', 'success')
  } catch (error) {
    setNotice(error.message || '退出登录失败。', 'error')
  } finally {
    loading.auth = false
  }
}

async function handleUserProfileSave() {
  loading.user = true
  try {
    currentUser.value = await updateCurrentUserProfile({ nickname: optionalText(userProfileForm.nickname), avatar: optionalText(userProfileForm.avatar), phone: optionalText(userProfileForm.phone) })
    syncUserProfileForm(currentUser.value)
    setNotice('账户资料已同步到后端。', 'success')
  } catch (error) {
    setNotice(error.message || '账户资料更新失败。', 'error')
  } finally {
    loading.user = false
  }
}

async function handleResumeUpload(event) {
  const [file] = event.target.files || []
  if (!file) {
    return
  }
  loading.profile = true
  try {
    const response = await uploadResumeFile(file)
    await hydrateProfile(response.profileId)
    currentTab.value = 'chat'
    setNotice(`简历「${file.name}」已上传，画像解析进度 ${response.parseProgress}%。`, 'success')
  } catch (error) {
    setNotice(error.message || '简历上传失败。', 'error')
  } finally {
    loading.profile = false
    event.target.value = ''
  }
}

async function handleProfileSubmit() {
  loading.profile = true
  try {
    const payload = buildProfilePayload()
    if (!studentProfile.value) {
      const response = await createProfileByManualInput(payload)
      await hydrateProfile(response.profileId)
    } else {
      const updated = missingFields.value.length ? await completeStudentProfile(studentProfile.value.profileId, payload) : await updateStudentProfile(studentProfile.value.profileId, payload)
      await hydrateProfile(updated.profileId)
    }
    currentTab.value = 'chat'
    setNotice(studentProfile.value ? '学生画像已更新。' : '学生画像已创建。', 'success')
  } catch (error) {
    setNotice(error.message || '画像保存失败。', 'error')
  } finally {
    loading.profile = false
  }
}

async function handleSendMessage() {
  const content = chatInput.value.trim()
  if (!content) {
    return
  }
  const optimisticMessage = { id: `local-${Date.now()}`, role: 'USER', content }
  chatMessages.value = [...chatMessages.value, optimisticMessage]
  chatInput.value = ''
  loading.chat = true
  try {
    const response = await sendChatMessage({ message: content, sessionId: chatSessionId.value || null })
    chatSessionId.value = response.sessionId
    setChatSessionId(response.sessionId)
    chatMessages.value = [...chatMessages.value, response.reply]
  } catch (error) {
    chatMessages.value = chatMessages.value.filter((message) => message.id !== optimisticMessage.id)
    setNotice(error.message || '消息发送失败。', 'error')
  } finally {
    loading.chat = false
  }
}

async function handleClearChat() {
  if (!chatSessionId.value) {
    seedChatMessages()
    return
  }
  loading.chat = true
  try {
    await clearChatHistory(chatSessionId.value)
    chatSessionId.value = ''
    clearStoredChatSessionId()
    seedChatMessages()
    setNotice('聊天记录已清空。', 'success')
  } catch (error) {
    setNotice(error.message || '清空聊天失败。', 'error')
  } finally {
    loading.chat = false
  }
}

async function handleGeneratePlan() {
  if (!activeProfileId.value) {
    setNotice('请先创建学生画像。', 'warning')
    return
  }
  loading.plan = true
  try {
    activePlan.value = await generatePlan({ profileId: activeProfileId.value, targetJobId: selectedJobId.value || null })
    await refreshPlans(false)
    setNotice('阶段计划已生成。', 'success')
  } catch (error) {
    setNotice(error.message || '计划生成失败。', 'error')
  } finally {
    loading.plan = false
  }
}

async function handleCompleteTask(taskId) {
  if (!activePlan.value) {
    return
  }
  loading.plan = true
  try {
    activePlan.value = await completePlanTask(activePlan.value.planId, taskId)
    await refreshPlans(false)
    setNotice('任务进度已更新。', 'success')
  } catch (error) {
    setNotice(error.message || '任务状态更新失败。', 'error')
  } finally {
    loading.plan = false
  }
}

async function openPlan(planId) {
  loading.plan = true
  try {
    activePlan.value = await getPlan(planId)
  } catch (error) {
    setNotice(error.message || '计划详情加载失败。', 'error')
  } finally {
    loading.plan = false
  }
}

async function handleGenerateReport() {
  if (!activeProfileId.value) {
    setNotice('请先创建学生画像。', 'warning')
    return
  }
  loading.report = true
  try {
    const response = await generateReport({ profileId: activeProfileId.value, targetJobId: selectedJobId.value || null })
    activeReport.value = await getReport(response.reportId)
    await refreshReports(false)
    currentTab.value = 'archive'
    setNotice('职业报告已生成。', 'success')
  } catch (error) {
    setNotice(error.message || '报告生成失败。', 'error')
  } finally {
    loading.report = false
  }
}

async function openReport(reportId) {
  loading.report = true
  try {
    activeReport.value = await getReport(reportId)
  } catch (error) {
    setNotice(error.message || '报告详情加载失败。', 'error')
  } finally {
    loading.report = false
  }
}

async function handlePolishReport() {
  if (!activeReport.value) {
    return
  }
  loading.polish = true
  try {
    const polishedContent = await polishGeneratedReport(activeReport.value.reportId)
    activeReport.value = { ...activeReport.value, polishedContent }
    setNotice('报告润色完成。', 'success')
  } catch (error) {
    setNotice(error.message || '报告润色失败。', 'error')
  } finally {
    loading.polish = false
  }
}

async function handleExportReport(format) {
  if (!activeReport.value) {
    return
  }
  loading.export = true
  try {
    const { blob, filename } = await exportGeneratedReport(activeReport.value.reportId, format)
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = filename
    link.click()
    URL.revokeObjectURL(objectUrl)
    setNotice(`报告已导出为 ${filename}。`, 'success')
  } catch (error) {
    setNotice(error.message || '报告导出失败。', 'error')
  } finally {
    loading.export = false
  }
}

function formatDate(value) {
  if (!value) {
    return '未生成'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

function formatPercent(value) {
  if (value == null || Number.isNaN(Number(value))) {
    return '--'
  }
  return `${Number(value).toFixed(2)}%`
}

function formatSalaryRange(min, max) {
  if (min == null && max == null) {
    return '薪资待补充'
  }
  return `${min ?? '--'} - ${max ?? '--'}`
}

async function handlePlanAdjusted(plan) {
  activePlan.value = plan
  await refreshPlans(false)
}

function handleInsightNotice(payload) {
  if (!payload) {
    return
  }
  setNotice(payload.text, payload.type)
}

onMounted(() => {
  initializeApp()
})
</script>




