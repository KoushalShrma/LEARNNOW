import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Plus, BookOpen, Play, Trophy, Clock, TrendingUp, Target, Award } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth.jsx';
import { useApiQuery } from '@/hooks/useApi';
import { topicsAPI, progressAPI, certificatesAPI } from '@/lib/api';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import CreateCourseModal from '@/components/course/CreateCourseModal';
import CourseCard from '@/components/course/CourseCard';

const Dashboard = () => {
  const { user } = useAuth();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [userProgress, setUserProgress] = useState({});
  const [topics, setTopics] = useState([]);
  const [topicsLoading, setTopicsLoading] = useState(true);
  const [userStats, setUserStats] = useState({
    totalCourses: 0,
    completedCourses: 0,
    inProgressCourses: 0,
    totalWatchTimeMinutes: 0,
    averageProgress: 0
  });
  const [certificates, setCertificates] = useState([]);

  // Fetch user's topics/courses - only for this user
  const fetchUserTopics = async () => {
    if (!user?.id) return;
    setTopicsLoading(true);
    try {
      const response = await topicsAPI.getByUser(user.id);
      setTopics(response.data || []);
    } catch (error) {
      console.error('Error fetching user topics:', error);
      setTopics([]);
    } finally {
      setTopicsLoading(false);
    }
  };

  useEffect(() => {
    fetchUserTopics();
  }, [user?.id]);

  // Ensure topics is an array
  const topicsArray = Array.isArray(topics) ? topics : [];

  // Fetch user progress and stats when user is available
  useEffect(() => {
    const fetchProgressAndStats = async () => {
      if (!user?.id) return;
      
      try {
        // Fetch all progress
        const progressResponse = await progressAPI.getAllProgress(user.id);
        setUserProgress(progressResponse.data || {});
        
        // Fetch stats
        const statsResponse = await progressAPI.getStats(user.id);
        setUserStats(statsResponse.data || {
          totalCourses: 0,
          completedCourses: 0,
          inProgressCourses: 0,
          totalWatchTimeMinutes: 0,
          averageProgress: 0
        });
        
        // Fetch certificates
        const certResponse = await certificatesAPI.getUserCertificates(user.id);
        setCertificates(certResponse.data || []);
      } catch (error) {
        console.error('Error fetching progress:', error);
      }
    };
    
    fetchProgressAndStats();
  }, [user?.id, topicsArray.length]);

  const handleCourseCreated = () => {
    fetchUserTopics();
    setShowCreateModal(false);
  };

  // Get progress for a specific topic
  const getTopicProgress = (topicId) => {
    return userProgress[topicId] || {
      status: 'NOT_STARTED',
      progressPercentage: 0,
      currentVideoIndex: 0
    };
  };

  const statCards = [
    {
      title: 'Courses Enrolled',
      value: topicsArray.length,
      icon: BookOpen,
      color: 'primary',
      bgColor: 'bg-blue-100',
      textColor: 'text-blue-600',
    },
    {
      title: 'Completed',
      value: userStats.completedCourses || 0,
      icon: Trophy,
      color: 'accent',
      bgColor: 'bg-green-100',
      textColor: 'text-green-600',
    },
    {
      title: 'Study Time',
      value: `${Math.floor((userStats.totalWatchTimeMinutes || 0) / 60)}h ${(userStats.totalWatchTimeMinutes || 0) % 60}m`,
      icon: Clock,
      color: 'secondary',
      bgColor: 'bg-purple-100',
      textColor: 'text-purple-600',
    },
    {
      title: 'Certificates',
      value: certificates.length,
      icon: Award,
      color: 'warning',
      bgColor: 'bg-yellow-100',
      textColor: 'text-yellow-600',
      link: '/certificates',
    },
  ];

  if (topicsLoading) {
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
              const CardWrapper = stat.link ? Link : 'div';
              const wrapperProps = stat.link ? { to: stat.link } : {};
              
              return (
                <CardWrapper key={stat.title} {...wrapperProps}>
                  <Card hover className="relative overflow-hidden cursor-pointer">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-neutral-600 mb-1">
                          {stat.title}
                        </p>
                        <p className="text-2xl font-bold text-neutral-900">
                          {stat.value}
                        </p>
                      </div>
                      <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                        <Icon className={`h-6 w-6 ${stat.textColor}`} />
                      </div>
                    </div>
                  </Card>
                </CardWrapper>
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
                      progress={getTopicProgress(topic.id)}
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