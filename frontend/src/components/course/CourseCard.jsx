import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { Play, Clock, BookOpen, Trophy, ChevronRight, Trash2 } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { useApiMutation } from '@/hooks/useApi';
import { topicsAPI } from '@/lib/api';
import toast from 'react-hot-toast';

const CourseCard = ({ course, progress, onDelete }) => {
  const getProgressPercentage = () => {
    if (!progress) return 0;
    if (progress.status === 'COMPLETED') return 100;
    if (progress.status === 'IN_PROGRESS') return progress.progressPercentage || 50;
    return 0;
  };

  const getStatusColor = () => {
    const percentage = getProgressPercentage();
    if (percentage === 100) return 'accent';
    if (percentage > 0) return 'primary';
    return 'neutral';
  };

  const getStatusText = () => {
    const percentage = getProgressPercentage();
    if (percentage === 100) return 'Completed';
    if (percentage > 0) return 'In Progress';
    return 'Not Started';
  };

  const progressPercentage = getProgressPercentage();
  const statusColor = getStatusColor();

  const deleteMutation = useApiMutation(
    () => topicsAPI.delete(course.id),
    {
      onSuccess: () => {
        toast.success('Course deleted successfully');
        onDelete?.();
      },
      onError: () => {
        toast.error('Failed to delete course');
      }
    }
  );

  const handleDelete = (e) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (window.confirm(`Are you sure you want to delete "${course.name}"? This action cannot be undone.`)) {
      deleteMutation.mutate();
    }
  };

  return (
    <motion.div
      whileHover={{ y: -4 }}
      transition={{ duration: 0.2 }}
    >
      <Card hover className="h-full overflow-hidden">
        {/* Course Header */}
        <div className="p-6 pb-4">
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-xl font-semibold text-neutral-900 mb-2 line-clamp-2">
                {course.name}
              </h3>
              <p className="text-sm text-neutral-600 line-clamp-2 mb-3">
                {course.description}
              </p>
            </div>
            <div className="flex flex-col items-end space-y-2">
              <div className={`px-2 py-1 rounded-full text-xs font-medium bg-${statusColor}-100 text-${statusColor}-700`}>
                {getStatusText()}
              </div>
              <button
                onClick={handleDelete}
                disabled={deleteMutation.isLoading}
                className="p-1.5 rounded-full hover:bg-red-50 text-neutral-400 hover:text-red-600 transition-colors disabled:opacity-50"
                title="Delete course"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          </div>

          {/* Course Meta */}
          <div className="flex items-center space-x-4 text-sm text-neutral-500 mb-4">
            <div className="flex items-center space-x-1">
              <Clock className="h-4 w-4" />
              <span>{course.estimatedDuration || '2-3 hours'}</span>
            </div>
            <div className="flex items-center space-x-1">
              <BookOpen className="h-4 w-4" />
              <span>{course.level}</span>
            </div>
            <div className="flex items-center space-x-1">
              <Trophy className="h-4 w-4" />
              <span>{course.videoCount || 0} videos</span>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mb-4">
            <div className="flex items-center justify-between text-sm mb-2">
              <span className="text-neutral-600">Progress</span>
              <span className="font-medium text-neutral-900">{progressPercentage}%</span>
            </div>
            <div className="w-full bg-neutral-200 rounded-full h-2">
              <motion.div
                className={`bg-${statusColor}-500 h-2 rounded-full`}
                initial={{ width: 0 }}
                animate={{ width: `${progressPercentage}%` }}
                transition={{ duration: 0.8, delay: 0.2 }}
              />
            </div>
          </div>
        </div>

        {/* Course Footer */}
        <div className="px-6 pb-6">
          <Link to={`/course/${course.id}`}>
            <Button 
              className="w-full group"
              variant={progressPercentage > 0 ? 'primary' : 'outline'}
            >
              <Play className="h-4 w-4 mr-2" />
              {progressPercentage > 0 ? 'Continue Learning' : 'Start Course'}
              <ChevronRight className="h-4 w-4 ml-2 group-hover:translate-x-1 transition-transform" />
            </Button>
          </Link>
        </div>

        {/* Decorative Elements */}
        <div className="absolute top-0 right-0 w-20 h-20 bg-gradient-to-br from-primary-500/10 to-secondary-500/10 rounded-bl-full" />
        <div className="absolute bottom-0 left-0 w-16 h-16 bg-gradient-to-tr from-accent-500/10 to-warning-500/10 rounded-tr-full" />
      </Card>
    </motion.div>
  );
};

export default CourseCard;