// API Base URL
export const API_BASE_URL = 'https://learner-bursary-platform.onrender.com/api/v1';

// Local Storage Keys
export const STORAGE_KEYS = {
  LEARNER_TOKEN: 'bursary_learner_token',
  LEARNER_USER: 'bursary_learner_user',
  PROVIDER_TOKEN: 'bursary_provider_token',
  PROVIDER_USER: 'bursary_provider_user',
};

// User Types
export const USER_TYPES = {
  LEARNER: 'learner',
  PROVIDER: 'provider',
};

// Application Statuses
export const APPLICATION_STATUS = {
  SUBMITTED: 'submitted',
  UNDER_REVIEW: 'under_review',
  SHORTLISTED: 'shortlisted',
  INTERVIEW_SCHEDULED: 'interview_scheduled',
  ACCEPTED: 'accepted',
  REJECTED: 'rejected',
};

// Status Colors for Badges
export const STATUS_COLORS = {
  submitted: 'bg-blue-100 text-blue-800',
  under_review: 'bg-yellow-100 text-yellow-800',
  shortlisted: 'bg-purple-100 text-purple-800',
  interview_scheduled: 'bg-indigo-100 text-indigo-800',
  accepted: 'bg-green-100 text-green-800',
  rejected: 'bg-red-100 text-red-800',
};

// Notification Types
export const NOTIFICATION_TYPES = {
  NEW_FOLLOWER: 'new_follower',
  RESULT_UPDATE: 'result_update',
  NEW_OFFER: 'new_offer',
  NEW_APPLICATION: 'new_application',
  STATUS_UPDATE: 'status_update',
};

// Grade Levels
export const GRADE_LEVELS = [8, 9, 10, 11, 12];

// Term Numbers
export const TERM_NUMBERS = [1, 2, 3, 4];

// Common Subjects
export const COMMON_SUBJECTS = [
  'Mathematics',
  'Physical Sciences',
  'Life Sciences',
  'English',
  'Afrikaans',
  'IsiZulu',
  'Accounting',
  'Business Studies',
  'Economics',
  'Geography',
  'History',
  'Information Technology',
];

// Routes
export const ROUTES = {
  // Public
  HOME: '/',

  // Auth
  LEARNER_LOGIN: '/learner/login',
  LEARNER_SIGNUP: '/learner/signup',
  PROVIDER_LOGIN: '/provider/login',

  // Learner
  LEARNER_DASHBOARD: '/learner/dashboard',
  LEARNER_BURSARIES: '/learner/bursaries',
  LEARNER_APPLICATIONS: '/learner/applications',
  LEARNER_RESULTS: '/learner/results',
  LEARNER_PROFILE: '/learner/profile',
  LEARNER_NOTIFICATIONS: '/learner/notifications',

  // Provider
  PROVIDER_DASHBOARD: '/provider/dashboard',
  PROVIDER_SEARCH: '/provider/search',
  PROVIDER_APPLICATIONS: '/provider/applications',
  PROVIDER_FOLLOWING: '/provider/following',
  PROVIDER_NOTIFICATIONS: '/provider/notifications',
};