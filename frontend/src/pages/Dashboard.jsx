import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Plus, BookOpen, Play, Trophy, Clock, TrendingUp, Target } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth.jsx';
import { useApiQuery } from '@/hooks/useApi';
import { topicsAPI, progressAPI, dashboardAPI } from '@/lib/api';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import CreateCourseModal from '@/components/course/CreateCourseModal';
import CourseCard from '@/components/course/CourseCard';

const Dashboard = () => {
  const { user } = useAuth();
  const [showCreateModal, setShowCreateModal] = useState(false);
  
  // Fetch user's topics/courses
  const { data: topics, isLoading: topicsLoading, refetch: refetchTopics } = useApiQuery(
    'topics',
    topicsAPI.getAll
  );

  // Ensure topics is an array
  const topicsArray = Array.isArray(topics) ? topics : [];

  // Fetch user stats - Disabled until backend supports Clerk user IDs
  const { data: stats, isLoading: statsLoading } = useApiQuery(
    ['user-stats', user?.id],
    () => dashboardAPI.getStats(user?.id),
    { enabled: false } // Disabled: backend expects numeric IDs, Clerk provides string IDs
  );

  // Fetch user progress - Disabled until backend supports Clerk user IDs
  const { data: progress, isLoading: progressLoading } = useApiQuery(
    ['user-progress', user?.id],
    () => progressAPI.getUserProgress(user?.id),
    { enabled: false } // Disabled: backend expects numeric IDs, Clerk provides string IDs
  );

  const handleCourseCreated = () => {
    refetchTopics();
    setShowCreateModal(false);
  };

  const statCards = [
    {
      title: 'Courses Enrolled',
      value: topicsArray.length,
      icon: BookOpen,
      color: 'primary',
    },
    {
      title: 'Completed',
      value: stats?.completedTopics || 0,
      icon: Trophy,
      color: 'accent',
    },
    {
      title: 'Study Time',
      value: `${Math.floor((stats?.totalStudyTimeMinutes || 0) / 60)}h`,
      icon: Clock,
      color: 'secondary',
    },
    {
      title: 'Current Streak',
      value: `${stats?.streakDays || 0} days`,
      icon: TrendingUp,
      color: 'warning',
    },
  ];

  if (topicsLoading || statsLoading) {
    return (
      <div className="min-h-screen bg-surface flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-neutral-900 mb-2">
                Welcome back, {user?.firstName || 'Learner'}! 👋
              </h1>
              <p className="text-neutral-600">
                Continue your learning journey or start something new.
              </p>
            </div>
            <Button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center space-x-2"
              size="lg"
            >
              <Plus className="h-5 w-5" />
              <span>Create Course</span>
            </Button>
          </div>
        </motion.div>

        {/* Stats Grid */}
        {topicsArray.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8"
          >
            {statCards.map((stat, index) => {
              const Icon = stat.icon;
              return (
                <Card key={stat.title} hover className="relative overflow-hidden">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-neutral-600 mb-1">
                        {stat.title}
                      </p>
                      <p className="text-2xl font-bold text-neutral-900">
                        {stat.value}
                      </p>
                    </div>
                    <div className={`p-3 rounded-lg bg-${stat.color}-100`}>
                      <Icon className={`h-6 w-6 text-${stat.color}-600`} />
                    </div>
                  </div>
                </Card>
              );
            })}
          </motion.div>
        )}

        {/* Courses Section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          {topicsArray.length > 0 ? (
            <div>
              <h2 className="text-2xl font-semibold text-neutral-900 mb-6">
                Your Courses
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {topicsArray.map((topic, index) => (
                  <motion.div
                    key={topic.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.1 }}
                  >
                    <CourseCard 
                      course={topic} 
                      progress={progress?.find(p => p.topic?.id === topic.id)}
                      onDelete={refetchTopics}
                    />
                  </motion.div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-16">
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.3 }}
              >
                <div className="w-24 h-24 bg-gradient-to-br from-primary-100 to-secondary-100 rounded-full flex items-center justify-center mx-auto mb-6">
                  <BookOpen className="h-12 w-12 text-primary-600" />
                </div>
                <h3 className="text-2xl font-semibold text-neutral-900 mb-4">
                  Start Your Learning Journey
                </h3>
                <p className="text-neutral-600 mb-8 max-w-md mx-auto">
                  Create your first course and get personalized learning paths with curated content from top educators.
                </p>
                <Button
                  onClick={() => setShowCreateModal(true)}
                  size="lg"
                  className="inline-flex items-center space-x-2"
                >
                  <Plus className="h-5 w-5" />
                  <span>Create Your First Course</span>
                </Button>
              </motion.div>
            </div>
          )}
        </motion.div>
      </div>

      {/* Create Course Modal */}
      <CreateCourseModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCourseCreated={handleCourseCreated}
      />
    </div>
  );
};

export default Dashboard;