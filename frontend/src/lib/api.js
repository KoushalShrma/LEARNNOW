import axios from 'axios';
import toast from 'react-hot-toast';

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Token management (simplified for Clerk integration)
export const tokenManager = {
  getToken: () => null, // Clerk handles tokens automatically
  setToken: () => {}, // Not needed with Clerk
  removeToken: () => {}, // Not needed with Clerk
  getUser: () => {
    const user = localStorage.getItem('learnnow_user');
    return user ? JSON.parse(user) : null;
  },
  setUser: (user) => localStorage.setItem('learnnow_user', JSON.stringify(user)),
  removeUser: () => localStorage.removeItem('learnnow_user'),
  clear: () => {
    localStorage.removeItem('learnnow_user');
  }
};

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      toast.error('Authentication required. Please sign in.');
    } else if (error.response?.status === 403) {
      toast.error('Access denied. Insufficient permissions.');
    } else if (error.response?.status >= 500) {
      toast.error('Server error. Please try again later.');
    } else if (error.code === 'NETWORK_ERROR') {
      toast.error('Network error. Please check your connection.');
    }
    return Promise.reject(error);
  }
);

// API endpoints
export const topicsAPI = {
  getAll: () => api.get('/api/topics'),
  getById: (id) => api.get(`/api/topics/${id}`),
  create: (topicData) => api.post('/api/topics', topicData),
  update: (id, topicData) => api.put(`/api/topics/${id}`, topicData),
  delete: (id) => api.delete(`/api/topics/${id}`),
  getVideos: (id) => api.get(`/api/topics/${id}/videos`),
  getQuizzes: (id) => api.get(`/api/topics/${id}/quizzes`),
  reorderVideos: (id, videoIds) => api.patch(`/api/topics/${id}/videos/reorder`, videoIds),
};

export const videosAPI = {
  getAll: () => api.get('/api/videos'),
  getById: (id) => api.get(`/api/videos/${id}`),
  create: (videoData) => api.post('/api/videos', videoData),
  update: (id, videoData) => api.put(`/api/videos/${id}`, videoData),
  delete: (id) => api.delete(`/api/videos/${id}`),
};

export const quizzesAPI = {
  getAll: () => api.get('/api/quizzes'),
  getById: (id) => api.get(`/api/quizzes/${id}`),
  create: (quizData) => api.post('/api/quizzes', quizData),
  update: (id, quizData) => api.put(`/api/quizzes/${id}`, quizData),
  delete: (id) => api.delete(`/api/quizzes/${id}`),
  toggleActive: (id, active) => api.patch(`/api/quizzes/${id}/active?active=${active}`),
  getByTopic: (topicId) => api.get(`/api/quizzes/topic/${topicId}`),
  getByDifficulty: (difficulty) => api.get(`/api/quizzes/difficulty/${difficulty}`),
  generate: (topicId, difficulty, language = 'en') => 
    api.post(`/api/quizzes/generate?topicId=${topicId}&difficulty=${difficulty}&language=${language}`),
  submit: (quizId, answers) => api.post(`/api/quizzes/${quizId}/submit`, answers),
};

export const progressAPI = {
  // New progress API for Clerk users (string IDs)
  getCourseProgress: (userId, topicId) => api.get(`/api/progress/${userId}/course/${topicId}`),
  getAllProgress: (userId) => api.get(`/api/progress/${userId}`),
  markVideoComplete: (userId, topicId, videoIndex) => 
    api.post(`/api/progress/${userId}/course/${topicId}/video/${videoIndex}`),
  setCurrentVideo: (userId, topicId, videoIndex) => 
    api.put(`/api/progress/${userId}/course/${topicId}/current-video`, { videoIndex }),
  addWatchTime: (userId, topicId, seconds) => 
    api.post(`/api/progress/${userId}/course/${topicId}/watch-time`, { seconds }),
  syncProgress: (userId, progressData) => api.post(`/api/progress/${userId}/sync`, progressData),
  getStats: (userId) => api.get(`/api/progress/${userId}/stats`),
  
  // Legacy endpoints (kept for compatibility)
  getUserProgress: (userId) => api.get(`/api/users/${userId}/progress`),
  createProgress: (userId, progressData) => api.post(`/api/users/${userId}/progress`, progressData),
  getProgress: (userId, progressId) => api.get(`/api/users/${userId}/progress/${progressId}`),
  updateProgress: (userId, progressId, progressData) => 
    api.patch(`/api/users/${userId}/progress/${progressId}`, progressData),
  deleteProgress: (userId, progressId) => api.delete(`/api/users/${userId}/progress/${progressId}`),
  getByTopic: (userId, topicId) => api.get(`/api/users/${userId}/progress/by-topic/${topicId}`),
  setStatus: (userId, topicId, status) => 
    api.put(`/api/users/${userId}/progress/by-topic/${topicId}/status`, { value: status }),
  setWatchSeconds: (userId, topicId, seconds) => 
    api.put(`/api/users/${userId}/progress/by-topic/${topicId}/watch-seconds`, { value: seconds }),
};

export const dashboardAPI = {
  getStats: (userId) => api.get(`/api/users/${userId}/stats`),
  getActivity: (userId, limit = 5) => api.get(`/api/users/${userId}/activity?limit=${limit}`),
  getRecommendations: (userId, limit = 3) => api.get(`/api/users/${userId}/recommendations?limit=${limit}`),
  trackCourseAction: (userId, actionData) => api.post(`/api/users/${userId}/progress`, actionData),
  startChallenge: (userId, challengeData) => api.post(`/api/users/${userId}/challenges/start`, challengeData),
};

export const scoreCardAPI = {
  getAll: () => api.get('/api/scorecards'),
  getById: (id) => api.get(`/api/scorecards/${id}`),
  create: (scoreCardData) => api.post('/api/scorecards', scoreCardData),
  update: (id, scoreCardData) => api.put(`/api/scorecards/${id}`, scoreCardData),
  delete: (id) => api.delete(`/api/scorecards/${id}`),
  getUserScoreCard: (userId) => api.get(`/api/users/${userId}/scorecard`),
  generateScoreCard: (userId) => api.post(`/api/users/${userId}/scorecard/generate`),
  getPerformanceSummary: (userId) => api.get(`/api/users/${userId}/performance-summary`),
};

export const learningPathAPI = {
  create: (userId, pathData) => api.post(`/api/learning-paths/user/${userId}`, pathData),
  getUserPaths: (userId) => api.get(`/api/learning-paths/user/${userId}`),
  getById: (pathId) => api.get(`/api/learning-paths/${pathId}`),
  getPublic: () => api.get('/api/learning-paths/public'),
  getPopular: () => api.get('/api/learning-paths/popular'),
  addTopic: (pathId, topicId) => api.post(`/api/learning-paths/${pathId}/topics/${topicId}`),
  removeTopic: (pathId, topicId) => api.delete(`/api/learning-paths/${pathId}/topics/${topicId}`),
  update: (pathId, pathData) => api.put(`/api/learning-paths/${pathId}`, pathData),
  delete: (pathId) => api.delete(`/api/learning-paths/${pathId}`),
  filterByPurpose: (purpose) => api.get(`/api/learning-paths/filter/purpose/${purpose}`),
  filterByLanguage: (language) => api.get(`/api/learning-paths/filter/language/${language}`),
};

export const youtubeAPI = {
  search: (query, max = 10) => api.get(`/api/youtube/search?query=${encodeURIComponent(query)}&max=${max}`),
  searchBest: (query) => api.get(`/api/youtube/best?query=${encodeURIComponent(query)}`),
  searchBestMultiple: (query, max = 3, language = 'English') => 
    api.get(`/api/youtube/best-multiple?query=${encodeURIComponent(query)}&max=${max}&language=${encodeURIComponent(language)}`),
  getPopular: (max = 8) => api.get(`/api/youtube/popular?max=${max}`),
  getByTopic: (topic, max = 6) => api.get(`/api/youtube/topic/${encodeURIComponent(topic)}?max=${max}`),
  healthCheck: () => api.get('/api/youtube/health'),
};

export const coursesAPI = {
  generateSubtopics: (courseData) => api.post('/api/courses/generate-subtopics', courseData),
};

export const usersAPI = {
  getAll: () => api.get('/api/users'),
  getById: (id) => api.get(`/api/users/${id}`),
  update: (id, userData) => api.put(`/api/users/${id}`, userData),
  updatePassword: (id, passwordData) => api.put(`/api/users/pass/${id}`, passwordData),
  delete: (id) => api.delete(`/api/users/${id}`),
};

export const certificatesAPI = {
  generate: (userId, courseId, userName) => 
    api.post('/api/certificates/generate', { userId, courseId, userName }),
  getUserCertificates: (userId) => api.get(`/api/certificates/user/${userId}`),
  verify: (certificateNumber) => api.get(`/api/certificates/verify/${certificateNumber}`),
  check: (userId, courseId) => api.get(`/api/certificates/check/${userId}/${courseId}`),
};

export default api;