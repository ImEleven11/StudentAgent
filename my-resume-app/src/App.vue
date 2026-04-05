<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo">简历 AI 助手</div>

      <section class="auth-card">
        <template v-if="!isAuthenticated">
          <div class="auth-toggle">
            <button :class="['auth-toggle-btn', authMode === 'login' ? 'active' : '']" @click="authMode = 'login'">登录</button>
            <button :class="['auth-toggle-btn', authMode === 'register' ? 'active' : '']" @click="authMode = 'register'">注册</button>
          </div>

          <form class="auth-form" @submit.prevent="handleAuthSubmit">
            <input v-if="authMode === 'register'" v-model.trim="authForm.username" type="text" placeholder="用户名" />
            <input v-if="authMode === 'register'" v-model.trim="authForm.email" type="email" placeholder="邮箱" />
            <input v-else v-model.trim="authForm.account" type="text" placeholder="用户名或邮箱" />
            <input v-model="authForm.password" type="password" placeholder="密码" />
            <button class="primary-btn" type="submit" :disabled="authLoading || loading.bootstrap">
              {{ authLoading ? '处理中...' : authMode === 'login' ? '登录' : '注册并登录' }}
            </button>
          </form>
        </template>

        <template v-else>
          <div class="auth-user">
            <strong>{{ currentUser?.nickname || currentUser?.username }}</strong>
            <p>{{ currentUser?.email }}</p>
            <small v-if="currentProfile">画像完整度 {{ formatPercent(currentProfile.completenessScore) }}</small>
          </div>
          <button class="secondary-btn" type="button" @click="handleLogout" :disabled="authLoading">
            {{ authLoading ? '退出中...' : '退出登录' }}
          </button>
        </template>
      </section>

      <nav>
        <div :class="['nav-item', currentTab === 'upload' ? 'active' : '']" @click="currentTab = 'upload'">简历上传</div>
        <div :class="['nav-item', currentTab === 'chat' ? 'active' : '']" @click="currentTab = 'chat'">AI 深度对话</div>
        <div :class="['nav-item', currentTab === 'report' ? 'active' : '']" @click="currentTab = 'report'">职业晋升路径</div>
        <div :class="['nav-item', currentTab === 'archive' ? 'active' : '']" @click="currentTab = 'archive'">个人档案库</div>
      </nav>

      <div class="sidebar-summary" v-if="currentProfile">
        <div>
          <span>解析进度</span>
          <strong>{{ currentProfile.parseProgress ?? 0 }}%</strong>
        </div>
        <div>
          <span>缺失字段</span>
          <strong>{{ currentProfile.missingFields?.length || 0 }}</strong>
        </div>
      </div>

      <div class="user-info">服务创新大赛项目</div>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <span>当前功能：{{ tabNames[currentTab] }}</span>
        <span>{{ topBarStatus }}</span>
      </header>

      <div v-if="notice.text" :class="['notice', notice.type]">{{ notice.text }}</div>

      <section v-if="currentTab === 'upload'" class="content-body">
        <div class="welcome-text">
          <h1>你好！我是你的职业规划 AI</h1>
          <p>上传简历后，我会读取后端画像、推荐岗位、阶段化计划和报告数据，让后续页面真正联动起来。</p>
        </div>

        <div class="upload-zone">
          <div class="upload-card">
            <div class="icon">📨</div>
            <h3>点击或拖拽简历到这里</h3>
            <p>支持 PDF、Word 格式</p>
            <p class="upload-hint">{{ isAuthenticated ? '登录后将直接上传到后端画像模块，并同步到后续页面。' : '请先登录后再上传。' }}</p>
            <input type="file" class="hidden-input" accept=".pdf,.doc,.docx" @change="handleUpload" />
          </div>
        </div>

        <div v-if="uploadedResumeName" class="status-card">
          <strong>最近上传：</strong>
          <span>{{ uploadedResumeName }}</span>
        </div>

        <div v-if="currentProfile" class="profile-overview-grid">
          <div class="overview-card">
            <span>简历解析状态</span>
            <strong>{{ currentProfile.parseStatus }}</strong>
            <p>当前进度 {{ currentProfile.parseProgress ?? 0 }}%</p>
          </div>
          <div class="overview-card">
            <span>画像完整度</span>
            <strong>{{ formatPercent(currentProfile.completenessScore) }}</strong>
            <p>{{ currentProfile.missingFields?.length ? `仍缺少 ${currentProfile.missingFields.length} 项信息` : '画像信息已较完整' }}</p>
          </div>
          <div class="overview-card">
            <span>推荐岗位</span>
            <strong>{{ recommendedJobs[0]?.jobTitle || '待生成' }}</strong>
            <p>{{ recommendedJobs[0]?.industry || '上传后自动联动匹配结果' }}</p>
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'chat'" class="chat-body">
        <div v-if="!isAuthenticated" class="empty-state">
          <h3>请先登录</h3>
          <p>登录后才会恢复账户下的画像、报告与对话状态。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">返回首页</button>
        </div>

        <div v-else-if="!isUploaded" class="empty-state">
          <h3>请先上传简历</h3>
          <p>上传成功后，这里会接入真实聊天接口，并结合你的画像内容继续对话。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">去上传</button>
        </div>

        <template v-else>
          <div class="chat-meta-row">
            <div class="meta-chip">画像ID #{{ activeProfileId || '--' }}</div>
            <div class="meta-chip">会话 {{ chatSessionId || '未建立' }}</div>
            <div class="meta-chip">推荐岗位 {{ recommendedJobs[0]?.jobTitle || '待推荐' }}</div>
          </div>

          <div class="chat-window" ref="chatWindow">
            <div v-for="(msg, index) in messages" :key="`${msg.role}-${index}-${msg.text}`" :class="['message-row', msg.role]">
              <div class="bubble">{{ msg.text }}</div>
            </div>
            <div v-if="loading.chat" class="message-row ai">
              <div class="bubble loading-bubble">AI 正在读取你的画像并整理回复...</div>
            </div>
          </div>

          <div class="chat-input-area">
            <input v-model="userInput" type="text" placeholder="补充你的项目、技能、目标岗位或求职顾虑..." @keyup.enter="sendMessage" />
            <button @click="sendMessage" class="send-btn" :disabled="loading.chat">发送</button>
            <button @click="handleClearChat" class="secondary-inline-btn" :disabled="loading.chat">清空会话</button>
            <button @click="startAnalysis" class="analyze-btn" :disabled="isAnalyzing || loading.report">生成真实报告</button>
          </div>
        </template>

        <div v-if="isAnalyzing" class="progress-mask">
          <div class="progress-box">
            <p>正在联动后端生成报告与成长计划...</p>
            <div class="progress-bar-container">
              <div class="progress-bar" :style="{ width: analysisProgress + '%' }"></div>
            </div>
            <p>{{ analysisProgress }}%</p>
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'report'" class="report-body">
        <div v-if="!isAuthenticated" class="empty-state">
          <h3>请先登录后再查看报告</h3>
          <p>登录后才能读取账户下的真实职业报告和计划。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">返回首页</button>
        </div>

        <div v-else-if="!isUploaded" class="empty-state">
          <h3>请先在首页上传您的简历</h3>
          <p>上传完成后，报告页会展示真实的画像、岗位推荐、报告章节与计划任务。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">去上传</button>
        </div>

        <div v-else class="report-shell">
          <div class="report-header-block">
            <h2>{{ activeReport?.title || '职业晋升路径规划' }}</h2>
            <div class="report-actions">
              <button class="nav-btn" @click="startAnalysis" :disabled="isAnalyzing || loading.report">{{ activeReport ? '重新生成报告' : '生成报告' }}</button>
              <button class="secondary-inline-btn" @click="openLatestReport" :disabled="!reportItems.length || loading.report">刷新最新报告</button>
              <button class="secondary-inline-btn" @click="handleExportReport('pdf')" :disabled="!activeReport || exportingFormat !== ''">
                {{ exportingFormat === 'pdf' ? '导出中...' : '导出 PDF' }}
              </button>
              <button class="secondary-inline-btn" @click="handleExportReport('word')" :disabled="!activeReport || exportingFormat !== ''">
                {{ exportingFormat === 'word' ? '导出中...' : '导出 Word' }}
              </button>
            </div>
          </div>

          <div class="path-container">
            <div class="path-node">已上传简历</div>
            <div class="arrow">→</div>
            <div class="path-node active">{{ recommendedJobs[0]?.jobTitle || '推荐岗位待生成' }}</div>
            <div class="arrow">→</div>
            <div class="path-node">阶段化成长与求职投递</div>
          </div>

          <div class="report-metric-grid" v-if="currentProfile">
            <div class="metric-card">
              <span>能力值</span>
              <strong>{{ formatPercent(currentProfile.abilityScore) }}</strong>
            </div>
            <div class="metric-card">
              <span>竞争力</span>
              <strong>{{ formatPercent(currentProfile.competitivenessScore) }}</strong>
            </div>
            <div class="metric-card">
              <span>完整度</span>
              <strong>{{ formatPercent(currentProfile.completenessScore) }}</strong>
            </div>
          </div>

          <div v-if="!activeReport" class="empty-state inline-empty-state">
            <h3>还没有生成真实报告</h3>
            <p>你已经完成上传，可以直接点击上方按钮生成报告，系统会同时刷新推荐岗位和成长计划。</p>
          </div>

          <div v-else class="insight-grid">
            <article class="insight-card">
              <h3>报告章节</h3>
              <div class="section-list">
                <div v-for="section in activeReport.sections" :key="section.title" class="section-item">
                  <strong>{{ section.title }}</strong>
                  <p>{{ section.content }}</p>
                </div>
              </div>
            </article>

            <article class="insight-card">
              <h3>推荐岗位</h3>
              <div class="recommend-list">
                <button v-for="job in recommendedJobs" :key="job.jobId" type="button" class="recommend-item" @click="startAnalysis(job.jobId)">
                  <strong>{{ job.jobTitle }}</strong>
                  <p>{{ job.industry }} · {{ job.location }}</p>
                  <span>匹配度 {{ formatPercent(job.matchScore) }}</span>
                </button>
                <div v-if="!recommendedJobs.length" class="archive-item empty">暂无推荐岗位</div>
              </div>
            </article>

            <article class="insight-card">
              <h3>阶段化计划</h3>
              <div v-if="activePlan" class="task-list">
                <div class="plan-summary">{{ activePlan.summary }}</div>
                <div v-for="task in activePlan.tasks" :key="task.taskId" class="task-item" :class="task.completed ? 'done' : ''">
                  <strong>{{ task.phaseName }} · {{ task.taskTitle }}</strong>
                  <p>{{ task.description }}</p>
                  <div class="task-footer">
                    <span>{{ task.recommendedDays }} 天</span>
                    <button
                      class="secondary-inline-btn task-action-btn"
                      type="button"
                      @click="handleCompleteTask(task)"
                      :disabled="task.completed || completingTaskId === task.taskId"
                    >
                      {{ task.completed ? '已完成' : completingTaskId === task.taskId ? '提交中...' : '标记完成' }}
                    </button>
                  </div>
                </div>
              </div>
              <div v-else class="archive-item empty">系统尚未生成计划</div>
            </article>
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'archive'" class="content-body archive-body">
        <div v-if="!isAuthenticated" class="empty-state">
          <h3>请先登录</h3>
          <p>档案库会展示真实报告、成长计划和当前画像状态。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">返回首页</button>
        </div>

        <div v-else-if="!isUploaded" class="empty-state">
          <h3>暂无可归档内容</h3>
          <p>先上传简历并生成报告，档案库才会逐步丰富起来。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">去上传</button>
        </div>

        <template v-else>
          <h2>已保存的分析记录</h2>
          <div class="archive-grid">
            <article class="archive-panel">
              <h3>职业报告</h3>
              <div class="archive-list">
                <button v-for="report in reportItems" :key="report.reportId" type="button" class="archive-item action-item" @click="openReport(report.reportId)">
                  <strong>{{ report.title }}</strong>
                  <p>{{ formatDate(report.createdAt) }}</p>
                </button>
                <div v-if="!reportItems.length" class="archive-item empty">暂无报告</div>
              </div>
            </article>

            <article class="archive-panel">
              <h3>阶段计划</h3>
              <div class="archive-list">
                <button v-for="plan in planItems" :key="plan.planId" type="button" class="archive-item action-item" @click="openPlan(plan)">
                  <strong>{{ plan.title }}</strong>
                  <p>进度 {{ formatPercent(plan.progress) }}</p>
                </button>
                <div v-if="!planItems.length" class="archive-item empty">暂无计划</div>
              </div>
            </article>

            <article class="archive-panel">
              <h3>当前画像</h3>
              <div v-if="currentProfile" class="profile-panel">
                <p><strong>简历：</strong>{{ currentProfile.resumeFileName || '未上传' }}</p>
                <p><strong>技能：</strong>{{ currentProfile.skills?.length || 0 }} 项</p>
                <p><strong>经历：</strong>{{ currentProfile.experiences?.length || 0 }} 条</p>
                <p><strong>缺失：</strong>{{ currentProfile.missingFields?.join('、') || '无' }}</p>
              </div>
            </article>
          </div>
        </template>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { getCurrentUserProfile, loginUser, logoutUser, registerUser } from './api/modules/auth'
import { clearChatHistory, getChatHistory, sendChatMessage } from './api/modules/chat'
import { recommendJobs } from './api/modules/match'
import { completePlanTask, generatePlan, getPlan, listPlans } from './api/modules/plan'
import { getStudentProfile, uploadResumeFile } from './api/modules/profile'
import { exportGeneratedReport, generateReport, getReport, listReports } from './api/modules/report'
import { clearClientSession, clearStoredChatSessionId, getAccessToken, getActiveProfileId, getChatSessionId, setActiveProfileId, setChatSessionId } from './api/session'

const currentTab = ref('upload')
const isUploaded = ref(false)
const uploadedResumeName = ref('')
const userInput = ref('')
const chatWindow = ref(null)
const isAnalyzing = ref(false)
const analysisProgress = ref(0)
const authMode = ref('login')
const authLoading = ref(false)
const currentUser = ref(null)
const currentProfile = ref(null)
const activeReport = ref(null)
const activePlan = ref(null)
const reportItems = ref([])
const planItems = ref([])
const recommendedJobs = ref([])
const chatSessionId = ref(getChatSessionId())
const notice = ref({ type: 'info', text: '' })
const messages = ref([])
const completingTaskId = ref(null)
const exportingFormat = ref('')

const loading = reactive({ bootstrap: false, profile: false, chat: false, report: false, archive: false })

const authForm = reactive({ account: '', username: '', email: '', password: '' })

const tabNames = { upload: '简历上传', chat: 'AI 深度对话', report: '职业晋升路径', archive: '个人档案库' }

const isAuthenticated = computed(() => Boolean(currentUser.value && getAccessToken()))
const activeProfileId = computed(() => currentProfile.value?.profileId || getActiveProfileId())
const topBarStatus = computed(() => {
  if (!isAuthenticated.value) return '未登录'
  if (!currentProfile.value) return `已登录：${currentUser.value?.nickname || currentUser.value?.username || currentUser.value?.email}`
  return `已登录：${currentUser.value?.nickname || currentUser.value?.username || currentUser.value?.email} · 画像 #${currentProfile.value.profileId}`
})

function setNotice(text, type = 'info') {
  notice.value = { text, type }
}

function formatPercent(value) {
  if (value == null || Number.isNaN(Number(value))) return '--'
  return `${Number(value).toFixed(0)}%`
}

function formatDate(value) {
  if (!value) return '刚刚生成'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

function normalizeChatMessages(list) {
  return (list || []).filter((item) => item?.content).map((item) => ({ role: String(item.role || '').toLowerCase() === 'user' ? 'user' : 'ai', text: item.content }))
}

function seedPrototypeMessages(profile = currentProfile.value) {
  const intro = profile?.resumeFileName ? `你好！我已经读取了你的简历《${profile.resumeFileName}》。` : '你好！我已经读取了你的简历。'
  const followUp = profile?.missingFields?.length
    ? `当前还建议你补充：${profile.missingFields.join('、')}。你也可以直接告诉我目标岗位、项目亮点或求职担忧。`
    : '你可以继续补充目标岗位、项目亮点、技能细节或求职担忧，我会基于真实画像继续回答。'
  messages.value = [{ role: 'ai', text: `${intro}${followUp}` }]
}

function resetAuthForm() {
  authForm.account = ''
  authForm.username = ''
  authForm.email = ''
  authForm.password = ''
}

function resetPrototypeFlow() {
  isUploaded.value = false
  uploadedResumeName.value = ''
  currentProfile.value = null
  activeReport.value = null
  activePlan.value = null
  reportItems.value = []
  planItems.value = []
  recommendedJobs.value = []
  chatSessionId.value = ''
  currentTab.value = 'upload'
  userInput.value = ''
  clearStoredChatSessionId()
  seedPrototypeMessages(null)
}

function validateAuthForm() {
  if (authMode.value === 'register') {
    if (!authForm.username || !authForm.email || !authForm.password) {
      setNotice('注册时请完整填写用户名、邮箱和密码。', 'warning')
      return false
    }
    return true
  }
  if (!authForm.account || !authForm.password) {
    setNotice('登录时请输入用户名或邮箱，以及密码。', 'warning')
    return false
  }
  return true
}

async function loadProfile(profileId) {
  if (!profileId || !isAuthenticated.value) return null
  loading.profile = true
  try {
    const profile = await getStudentProfile(profileId)
    currentProfile.value = profile
    setActiveProfileId(profile.profileId)
    uploadedResumeName.value = profile.resumeFileName || uploadedResumeName.value
    isUploaded.value = Boolean(profile.resumeFileName)
    return profile
  } catch (error) {
    setNotice(error.message || '学生画像读取失败。', 'error')
    return null
  } finally {
    loading.profile = false
  }
}

async function loadArchiveData(profileId = activeProfileId.value, options = {}) {
  if (!profileId || !isAuthenticated.value) return
  if (!options.silent) loading.archive = true
  try {
    const [reports, plans, jobs] = await Promise.all([
      listReports(profileId).catch(() => []),
      listPlans(profileId).catch(() => []),
      recommendJobs({ profileId, topN: 3 }).catch(() => []),
    ])
    reportItems.value = Array.isArray(reports) ? reports : []
    planItems.value = Array.isArray(plans) ? plans : []
    recommendedJobs.value = Array.isArray(jobs) ? jobs : []
    if (!activePlan.value && planItems.value.length) activePlan.value = planItems.value[0]
    if (!activeReport.value && reportItems.value.length) activeReport.value = await getReport(reportItems.value[0].reportId).catch(() => null)
  } finally {
    if (!options.silent) loading.archive = false
  }
}

async function loadChatHistoryIfNeeded() {
  if (!isAuthenticated.value || !isUploaded.value) {
    seedPrototypeMessages(currentProfile.value)
    return
  }
  if (!chatSessionId.value) {
    seedPrototypeMessages(currentProfile.value)
    return
  }
  loading.chat = true
  try {
    const history = await getChatHistory(chatSessionId.value, 1, 50)
    const normalized = normalizeChatMessages(history?.messages)
    messages.value = normalized.length ? normalized : []
    if (!messages.value.length) seedPrototypeMessages(currentProfile.value)
  } catch (error) {
    clearStoredChatSessionId()
    chatSessionId.value = ''
    seedPrototypeMessages(currentProfile.value)
    setNotice(error.message || '聊天记录恢复失败，已为你开启新会话。', 'warning')
  } finally {
    loading.chat = false
  }
}

async function bootstrapProfileContext(profileId = getActiveProfileId()) {
  if (!profileId) {
    seedPrototypeMessages(currentProfile.value)
    return
  }
  const profile = await loadProfile(profileId)
  if (!profile) {
    seedPrototypeMessages(null)
    return
  }
  await loadArchiveData(profile.profileId, { silent: true })
  await loadChatHistoryIfNeeded()
}

async function initializeAuth() {
  if (!getAccessToken()) {
    currentUser.value = null
    seedPrototypeMessages(null)
    return
  }
  loading.bootstrap = true
  try {
    currentUser.value = await getCurrentUserProfile()
    await bootstrapProfileContext()
  } catch (error) {
    clearClientSession()
    currentUser.value = null
    resetPrototypeFlow()
    setNotice('本地登录状态已失效，请重新登录。', 'warning')
  } finally {
    loading.bootstrap = false
  }
}

async function handleAuthSubmit() {
  if (!validateAuthForm()) return
  authLoading.value = true
  try {
    if (authMode.value === 'register') {
      await registerUser({ username: authForm.username, email: authForm.email, password: authForm.password })
      const loginResponse = await loginUser({ account: authForm.username, password: authForm.password })
      currentUser.value = loginResponse?.user || (await getCurrentUserProfile())
      setNotice('注册成功，已自动登录。', 'success')
    } else {
      const loginResponse = await loginUser({ account: authForm.account, password: authForm.password })
      currentUser.value = loginResponse?.user || (await getCurrentUserProfile())
      setNotice('登录成功。', 'success')
    }
    resetAuthForm()
    activeReport.value = null
    activePlan.value = null
    await bootstrapProfileContext()
  } catch (error) {
    setNotice(error.message || '登录或注册失败。', 'error')
  } finally {
    authLoading.value = false
  }
}

async function handleLogout() {
  authLoading.value = true
  try {
    await logoutUser()
    currentUser.value = null
    resetAuthForm()
    resetPrototypeFlow()
    setNotice('已退出登录。', 'success')
  } catch (error) {
    setNotice(error.message || '退出登录失败。', 'error')
  } finally {
    authLoading.value = false
  }
}

async function handleUpload(event) {
  const [file] = event.target.files || []
  if (!file) return
  if (!file.size) {
    event.target.value = ''
    setNotice(`上传失败：文件“${file.name || '未命名文件'}”是空文件，请重新选择非 0 KB 的 PDF、DOC 或 DOCX。`, 'warning')
    return
  }
  if (!isAuthenticated.value) {
    event.target.value = ''
    setNotice('请先登录后再上传简历。', 'warning')
    return
  }

  loading.profile = true
  try {
    const response = await uploadResumeFile(file)
    if (response?.profileId) setActiveProfileId(response.profileId)
    clearStoredChatSessionId()
    chatSessionId.value = ''
    activeReport.value = null
    activePlan.value = null
    const profile = await loadProfile(response?.profileId)
    await loadArchiveData(response?.profileId, { silent: true })
    seedPrototypeMessages(profile)
    isUploaded.value = true
    uploadedResumeName.value = file.name
    currentTab.value = 'chat'
    setNotice(response?.parseProgress != null ? `简历上传成功，解析进度 ${response.parseProgress}%，后续页面已开始联动真实数据。` : '简历上传成功，后续页面已开始联动真实数据。', 'success')
  } catch (error) {
    setNotice(error.message || '简历上传失败。', 'error')
  } finally {
    loading.profile = false
    event.target.value = ''
  }
}

async function sendMessage() {
  if (!isAuthenticated.value) return setNotice('请先登录后再进行 AI 对话。', 'warning')
  if (!isUploaded.value || !activeProfileId.value) return setNotice('请先上传简历后再开始对话。', 'warning')
  if (!userInput.value.trim()) return

  const value = userInput.value.trim()
  messages.value.push({ role: 'user', text: value })
  userInput.value = ''
  loading.chat = true
  try {
    const response = await sendChatMessage({ message: value, sessionId: chatSessionId.value || undefined })
    if (response?.sessionId) {
      chatSessionId.value = response.sessionId
      setChatSessionId(response.sessionId)
    }
    if (response?.reply?.content) messages.value.push({ role: 'ai', text: response.reply.content })
  } catch (error) {
    setNotice(error.message || '消息发送失败。', 'error')
  } finally {
    loading.chat = false
  }
}

async function handleClearChat() {
  if (!chatSessionId.value) {
    seedPrototypeMessages(currentProfile.value)
    setNotice('当前还没有持久化会话，已重置为初始引导语。', 'success')
    return
  }
  loading.chat = true
  try {
    await clearChatHistory(chatSessionId.value)
    clearStoredChatSessionId()
    chatSessionId.value = ''
    seedPrototypeMessages(currentProfile.value)
    setNotice('已清空当前对话会话。', 'success')
  } catch (error) {
    setNotice(error.message || '清空对话失败。', 'error')
  } finally {
    loading.chat = false
  }
}

async function openReport(reportId) {
  if (!reportId) return
  loading.report = true
  try {
    activeReport.value = await getReport(reportId)
    currentTab.value = 'report'
  } catch (error) {
    setNotice(error.message || '读取报告详情失败。', 'error')
  } finally {
    loading.report = false
  }
}

async function openLatestReport() {
  if (!reportItems.value.length) return setNotice('当前还没有已生成的报告。', 'warning')
  await openReport(reportItems.value[0].reportId)
}

async function openPlan(plan) {
  if (!plan?.planId) return
  loading.archive = true
  try {
    activePlan.value = plan.tasks ? plan : await getPlan(plan.planId)
    currentTab.value = 'report'
  } catch (error) {
    setNotice(error.message || '读取计划详情失败。', 'error')
  } finally {
    loading.archive = false
  }
}

async function handleCompleteTask(task) {
  if (!activePlan.value?.planId || !task?.taskId) return
  if (task.completed) {
    setNotice('\u8be5\u4efb\u52a1\u5df2\u7ecf\u5b8c\u6210\uff0c\u65e0\u9700\u91cd\u590d\u63d0\u4ea4\u3002', 'info')
    return
  }
  completingTaskId.value = task.taskId
  try {
    activePlan.value = await completePlanTask(activePlan.value.planId, task.taskId)
    await loadArchiveData(activeProfileId.value, { silent: true })
    setNotice(`\u4efb\u52a1\u201c${task.taskTitle}\u201d\u5df2\u6807\u8bb0\u4e3a\u5b8c\u6210\u3002`, 'success')
  } catch (error) {
    setNotice(error.message || '\u66f4\u65b0\u4efb\u52a1\u72b6\u6001\u5931\u8d25\u3002', 'error')
  } finally {
    completingTaskId.value = null
  }
}

async function handleExportReport(format) {
  if (!activeReport.value?.reportId) {
    setNotice('\u8bf7\u5148\u751f\u6210\u6216\u6253\u5f00\u4e00\u4efd\u62a5\u544a\u540e\u518d\u5bfc\u51fa\u3002', 'warning')
    return
  }
  exportingFormat.value = format
  try {
    const { blob, filename } = await exportGeneratedReport(activeReport.value.reportId, format)
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(objectUrl)
    setNotice(`\u62a5\u544a\u5df2\u5bfc\u51fa\u4e3a${format === 'word' ? ' Word' : ' PDF'}\u3002`, 'success')
  } catch (error) {
    setNotice(error.message || '\u62a5\u544a\u5bfc\u51fa\u5931\u8d25\u3002', 'error')
  } finally {
    exportingFormat.value = ''
  }
}

async function startAnalysis(targetJobId = recommendedJobs.value[0]?.jobId || null) {
  if (!isAuthenticated.value) return setNotice('请先登录后再生成报告。', 'warning')
  if (!isUploaded.value || !activeProfileId.value) return setNotice('请先上传简历。', 'warning')

  isAnalyzing.value = true
  analysisProgress.value = 8
  const timer = setInterval(() => {
    analysisProgress.value = Math.min(analysisProgress.value + 8, 88)
  }, 250)

  try {
    const [reportGenerateResponse, generatedPlan] = await Promise.all([
      generateReport({ profileId: activeProfileId.value, targetJobId }),
      planItems.value.length ? Promise.resolve(activePlan.value || planItems.value[0]) : generatePlan({ profileId: activeProfileId.value, targetJobId }),
    ])
    if (generatedPlan?.planId) activePlan.value = generatedPlan
    activeReport.value = await getReport(reportGenerateResponse.reportId)
    await loadArchiveData(activeProfileId.value, { silent: true })
    analysisProgress.value = 100
    setTimeout(() => {
      currentTab.value = 'report'
      setNotice('真实报告与成长计划已联动完成。', 'success')
    }, 250)
  } catch (error) {
    setNotice(error.message || '报告生成失败。', 'error')
  } finally {
    clearInterval(timer)
    setTimeout(() => {
      isAnalyzing.value = false
      analysisProgress.value = 0
    }, 400)
  }
}

watch(messages, () => {
  nextTick(() => {
    if (chatWindow.value) chatWindow.value.scrollTop = chatWindow.value.scrollHeight
  })
}, { deep: true })

onMounted(() => {
  seedPrototypeMessages(null)
  initializeAuth()
})
</script>

<style scoped>
.app-container { display: flex; height: 100vh; background: #fdfcf0; font-family: sans-serif; }
.sidebar { width: 280px; background: #b1c9e8; padding: 20px; display: flex; flex-direction: column; gap: 18px; backdrop-filter: blur(10px); border-right: 1px solid rgba(255,255,255,0.3); }
.logo { font-size: 1.5rem; color: white; font-weight: bold; }
.auth-card, .overview-card, .metric-card, .insight-card, .archive-panel, .profile-panel, .status-card, .empty-state { background: rgba(255,255,255,0.88); border-radius: 16px; border: 1px solid rgba(255,255,255,0.45); }
.auth-card { padding: 14px; display: grid; gap: 12px; }
.auth-toggle { display: flex; gap: 8px; }
.auth-toggle-btn, .primary-btn, .secondary-btn, .secondary-inline-btn { border: none; border-radius: 10px; padding: 10px 12px; cursor: pointer; }
.auth-toggle-btn { flex: 1; background: rgba(177,201,232,0.45); color: #4a5568; }
.auth-toggle-btn.active, .primary-btn { background: #7d9bbd; color: white; }
.secondary-btn, .secondary-inline-btn { background: #d4e4bc; color: #4a5568; }
.auth-form { display: grid; gap: 10px; }
.auth-form input, .chat-input-area input { width: 100%; box-sizing: border-box; padding: 10px; border-radius: 8px; border: 1px solid #ddd; outline: none; }
.auth-user strong { display: block; color: #4a5568; }
.auth-user p, .auth-user small { display: block; margin: 6px 0 0; color: #717a87; }
.nav-item { padding: 15px; margin-bottom: 8px; border-radius: 10px; cursor: pointer; color: #4a5568; }
.nav-item.active { background: white; color: #7d9bbd; font-weight: bold; }
.sidebar-summary { display: grid; gap: 10px; padding: 14px; border-radius: 14px; background: rgba(255,255,255,0.2); color: white; }
.sidebar-summary span { display: block; font-size: 0.85rem; }
.sidebar-summary strong { font-size: 1.15rem; }
.user-info { margin-top: auto; color: white; font-size: 0.95rem; }
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-bar { padding: 15px 30px; background: white; border-bottom: 1px solid #eee; color: #717a87; display: flex; justify-content: space-between; gap: 12px; }
.notice { margin: 16px 32px 0; padding: 12px 14px; border-radius: 12px; }
.notice.info { background: #eef2ff; color: #4a5568; }
.notice.success { background: #e5f4df; color: #4f6a44; }
.notice.warning { background: #fff3d6; color: #8c6a2c; }
.notice.error { background: #fde4e1; color: #9a4d43; }
.content-body, .chat-body, .report-body { padding: 40px; flex: 1; display: flex; flex-direction: column; align-items: center; overflow-y: auto; }
.welcome-text { max-width: 760px; text-align: center; }
.welcome-text h1 { color: #717a87; font-weight: 800; font-size: 2.2rem; }
.welcome-text p, .empty-state p, .upload-hint, .section-item p, .plan-summary, .archive-item p, .overview-card p, .metric-card span, .recommend-item p, .recommend-item span, .task-item p, .task-item span, .profile-panel p { color: #717a87; }
.report-body h2, .content-body h2 { color: #717a87 !important; font-weight: 800; margin-bottom: 24px; }
.upload-zone { width: 100%; max-width: 620px; margin-top: 20px; }
.upload-card { padding: 60px; border: 3px dashed #b1c9e8; background: white; border-radius: 24px; text-align: center; position: relative; }
.hidden-input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
.icon { font-size: 3rem; margin-bottom: 10px; }
.status-card { margin-top: 20px; padding: 14px 18px; color: #5a6e4d; }
.profile-overview-grid, .report-metric-grid, .insight-grid, .archive-grid { width: 100%; max-width: 1100px; display: grid; gap: 16px; }
.profile-overview-grid, .report-metric-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); margin-top: 24px; }
.overview-card, .metric-card, .insight-card, .archive-panel { padding: 18px; }
.overview-card span, .metric-card span { display: block; font-size: 0.92rem; }
.overview-card strong, .metric-card strong { display: block; margin: 8px 0; font-size: 1.4rem; color: #4a5568; }
.chat-meta-row { width: 100%; max-width: 760px; display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 14px; }
.meta-chip { padding: 8px 12px; border-radius: 999px; background: rgba(177,201,232,0.22); color: #5a6a7d; }
.chat-window { width: 100%; max-width: 760px; height: 430px; background: white; border-radius: 15px; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; border: 1px solid #eee; }
.message-row { margin-bottom: 10px; display: flex; }
.message-row.ai { justify-content: flex-start; }
.message-row.user { justify-content: flex-end; }
.bubble { padding: 10px 15px; border-radius: 15px; max-width: 74%; font-size: 0.92rem; line-height: 1.5; }
.loading-bubble { opacity: 0.86; }
.ai .bubble { background: #eef2ff; color: #4a5568; }
.user .bubble { background: #d4e4bc; color: #4a5568; }
.chat-input-area { margin-top: 15px; width: 100%; max-width: 760px; display: flex; gap: 10px; flex-wrap: wrap; }
.send-btn, .analyze-btn, .nav-btn { padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; }
.send-btn, .nav-btn { background: #b1c9e8; }
.analyze-btn { background: #d4e4bc; color: #5a6e4d; font-weight: bold; }
.progress-mask { position: fixed; inset: 0; background: rgba(255,255,255,0.8); display: flex; justify-content: center; align-items: center; z-index: 100; }
.progress-box { text-align: center; background: white; padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
.progress-bar-container { width: 300px; background: #edf2f7; border-radius: 10px; margin: 20px 0; }
.progress-bar { height: 10px; background: #b1c9e8; border-radius: 10px; transition: width 0.3s; }
.report-shell { width: 100%; max-width: 1120px; display: grid; gap: 20px; }
.report-header-block { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.report-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.path-container { display: flex; align-items: center; justify-content: center; gap: 12px; flex-wrap: wrap; }
.path-node { padding: 15px 24px; background: white; border: 2px solid #b1c9e8; border-radius: 10px; color: #717a87; }
.path-node.active { background: #d4e4bc; color: #5a6e4d !important; border-color: #5a6e4d; font-weight: bold; }
.arrow { font-size: 20px; color: #b1c9e8; }
.insight-grid, .archive-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.insight-card h3, .archive-panel h3 { margin-top: 0; color: #4a5568; }
.section-list, .recommend-list, .task-list, .archive-list { display: grid; gap: 12px; }
.section-item, .task-item, .archive-item, .plan-summary, .recommend-item { padding: 14px 16px; border-radius: 14px; background: rgba(177,201,232,0.12); border: 1px solid rgba(177,201,232,0.18); }
.section-item strong, .task-item strong, .archive-item strong, .recommend-item strong { color: #4a5568; }
.task-item.done { background: rgba(212,228,188,0.35); }
.task-footer { margin-top: 10px; display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.task-action-btn { white-space: nowrap; }
.recommend-item, .action-item { width: 100%; text-align: left; border: none; cursor: pointer; }
.profile-panel { padding: 16px; }
.archive-body { align-items: stretch; }
.empty-state { text-align: center; padding: 40px; }
.send-btn, .analyze-btn, .nav-btn, .upload-card, .primary-btn, .secondary-btn, .auth-toggle-btn, .secondary-inline-btn, .recommend-item, .action-item { transition: transform 0.1s ease-in-out, box-shadow 0.2s ease; }
.send-btn:hover, .analyze-btn:hover, .nav-btn:hover, .upload-card:hover, .primary-btn:hover, .secondary-btn:hover, .auth-toggle-btn:hover, .secondary-inline-btn:hover, .recommend-item:hover, .action-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.send-btn:active, .analyze-btn:active, .nav-btn:active, .primary-btn:active, .secondary-btn:active, .auth-toggle-btn:active, .secondary-inline-btn:active, .recommend-item:active, .action-item:active { transform: scale(1.02); box-shadow: 0 2px 6px rgba(0,0,0,0.05); }
.upload-card:active { transform: scale(1.02); border-color: #d4e4bc !important; }
@media (max-width: 1100px) { .insight-grid, .archive-grid, .profile-overview-grid, .report-metric-grid { grid-template-columns: 1fr; } }
@media (max-width: 900px) {
  .app-container { flex-direction: column; height: auto; min-height: 100vh; }
  .sidebar { width: auto; }
  .top-bar, .report-header-block, .chat-input-area { flex-direction: column; }
  .chat-window, .chat-input-area, .chat-meta-row { max-width: 100%; }
}
</style>

