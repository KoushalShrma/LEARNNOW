import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import toast from 'react-hot-toast';

// Generic API hook for GET requests
export const useApiQuery = (key, apiFunction, options = {}) => {
  return useQuery(
    key, 
    async () => {
      const response = await apiFunction();
      return response.data;
    },
    {
      retry: 2,
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      onError: (error) => {
        if (!options.silent) {
          const message = error.response?.data?.message || 'Failed to fetch data';
          toast.error(message);
        }
      },
      ...options,
    }
  );
};

// Generic API hook for mutations (POST, PUT, DELETE)
export const useApiMutation = (apiFunction, options = {}) => {
  const queryClient = useQueryClient();

  return useMutation(apiFunction, {
    onSuccess: (data, variables) => {
      if (options.successMessage) {
        toast.success(options.successMessage);
      }
      if (options.invalidateQueries) {
        options.invalidateQueries.forEach(key => {
          queryClient.invalidateQueries(key);
        });
      }
      if (options.onSuccess) {
        options.onSuccess(data, variables);
      }
    },
    onError: (error) => {
      const message = error.response?.data?.message || options.errorMessage || 'Operation failed';
      toast.error(message);
      if (options.onError) {
        options.onError(error);
      }
    },
    ...options,
  });
};

// Specific hooks for common operations
export const useTopics = () => {
  return useApiQuery('topics', () => import('@/lib/api').then(api => api.topicsAPI.getAll()));
};

export const useTopic = (id) => {
  return useApiQuery(
    ['topic', id], 
    () => import('@/lib/api').then(api => api.topicsAPI.getById(id)),
    { enabled: !!id }
  );
};

export const useTopicVideos = (topicId) => {
  return useApiQuery(
    ['topic-videos', topicId],
    () => import('@/lib/api').then(api => api.topicsAPI.getVideos(topicId)),
    { enabled: !!topicId }
  );
};

export const useTopicQuizzes = (topicId) => {
  return useApiQuery(
    ['topic-quizzes', topicId],
    () => import('@/lib/api').then(api => api.topicsAPI.getQuizzes(topicId)),
    { enabled: !!topicId }
  );
};

export const useUserProgress = (userId) => {
  return useApiQuery(
    ['user-progress', userId],
    () => import('@/lib/api').then(api => api.progressAPI.getUserProgress(userId)),
    { enabled: !!userId }
  );
};

export const useUserStats = (userId) => {
  return useApiQuery(
    ['user-stats', userId],
    () => import('@/lib/api').then(api => api.dashboardAPI.getStats(userId)),
    { enabled: !!userId }
  );
};

export const useUserActivity = (userId, limit = 5) => {
  return useApiQuery(
    ['user-activity', userId, limit],
    () => import('@/lib/api').then(api => api.dashboardAPI.getActivity(userId, limit)),
    { enabled: !!userId }
  );
};

export const useRecommendations = (userId, limit = 3) => {
  return useApiQuery(
    ['recommendations', userId, limit],
    () => import('@/lib/api').then(api => api.dashboardAPI.getRecommendations(userId, limit)),
    { enabled: !!userId }
  );
};

export const useScoreCard = (userId) => {
  return useApiQuery(
    ['scorecard', userId],
    () => import('@/lib/api').then(api => api.scoreCardAPI.getUserScoreCard(userId)),
    { enabled: !!userId }
  );
};

export const useLearningPaths = (userId) => {
  return useApiQuery(
    ['learning-paths', userId],
    () => import('@/lib/api').then(api => api.learningPathAPI.getUserPaths(userId)),
    { enabled: !!userId }
  );
};

export const usePublicLearningPaths = () => {
  return useApiQuery('public-learning-paths', () => 
    import('@/lib/api').then(api => api.learningPathAPI.getPublic())
  );
};

// Custom hook for handling async operations with loading states
export const useAsyncOperation = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = async (asyncFunction, options = {}) => {
    try {
      setLoading(true);
      setError(null);
      const result = await asyncFunction();
      
      if (options.successMessage) {
        toast.success(options.successMessage);
      }
      
      return { success: true, data: result };
    } catch (err) {
      const errorMessage = err.response?.data?.message || options.errorMessage || 'Operation failed';
      setError(errorMessage);
      
      if (!options.silent) {
        toast.error(errorMessage);
      }
      
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  };

  return { loading, error, execute };
};

// Hook for debounced search
export const useDebounce = (value, delay) => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

// Hook for local storage state
export const useLocalStorage = (key, initialValue) => {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error);
      return initialValue;
    }
  });

  const setValue = (value) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error);
    }
  };

  return [storedValue, setValue];
};