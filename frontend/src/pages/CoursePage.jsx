import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { 
  ArrowLeft, 
  Play, 
  CheckCircle, 
  Clock, 
  BookOpen, 
  Trophy,
  ChevronRight,
  Youtube,
  Award,
  Download,
  ExternalLink
} from 'lucide-react';
import { useApiQuery, useApiMutation } from '@/hooks/useApi';
import { topicsAPI, progressAPI, quizzesAPI, certificatesAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth.jsx';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import VideoPlayer from '@/components/course/VideoPlayer';
import QuizModal from '@/components/course/QuizModal';
import HierarchicalTree from '@/components/course/HierarchicalTree';
import toast from 'react-hot-toast';

const CoursePage = () => {
  const { courseId } = useParams();
  const { user } = useAuth();
  const [currentVideoIndex, setCurrentVideoIndex] = useState(0);
  const [showQuiz, setShowQuiz] = useState(false);
  const [currentQuiz, setCurrentQuiz] = useState(null);
  const [certificate, setCertificate] = useState(null);
  const [hasCertificate, setHasCertificate] = useState(false);
  const [generatingCertificate, setGeneratingCertificate] = useState(false);
  const [showCertificateModal, setShowCertificateModal] = useState(false);
  
  // Progress state
  const [courseProgress, setCourseProgress] = useState({
    status: 'NOT_STARTED',
    progressPercentage: 0,
    completedVideos: [],
    currentVideoIndex: 0,
    totalVideos: 0
  });

  // Fetch course details
  const { data: course, isLoading: courseLoading } = useApiQuery(
    ['topic', courseId],
    () => topicsAPI.getById(courseId),
    { enabled: !!courseId }
  );

  // Fetch course videos
  const { data: videos, isLoading: videosLoading } = useApiQuery(
    ['topic-videos', courseId],
    () => topicsAPI.getVideos(courseId),
    { enabled: !!courseId }
  );

  // Fetch course quizzes
  const { data: quizzes, isLoading: quizzesLoading } = useApiQuery(
    ['topic-quizzes', courseId],
    () => topicsAPI.getQuizzes(courseId),
    { enabled: !!courseId }
  );

  // Fetch and track progress
  const fetchProgress = useCallback(async () => {
    if (!user?.id || !courseId) return;
    
    try {
      const response = await progressAPI.getCourseProgress(user.id, courseId);
      if (response.data) {
        setCourseProgress({
          ...response.data,
          completedVideos: response.data.completedVideos || []
        });
        setCurrentVideoIndex(response.data.currentVideoIndex || 0);
      }
    } catch (error) {
      console.log('Progress not found, starting fresh');
    }
  }, [user?.id, courseId]);

  useEffect(() => {
    fetchProgress();
  }, [fetchProgress]);

  const currentVideo = videos?.[currentVideoIndex];

  // Mark video as complete and update progress
  const markVideoComplete = async (videoIndex) => {
    if (!user?.id || !courseId) return;
    
    try {
      const response = await progressAPI.markVideoComplete(user.id, courseId, videoIndex);
      if (response.data) {
        setCourseProgress({
          ...response.data,
          completedVideos: response.data.completedVideos || []
        });
        
        // Show completion toast
        if (response.data.progressPercentage >= 100) {
          toast.success('🎉 Course Completed! You can now get your certificate!');
        }
      }
    } catch (error) {
      console.error('Error marking video complete:', error);
    }
  };

  // Update current video position
  const updateCurrentVideo = async (videoIndex) => {
    if (!user?.id || !courseId) return;
    
    try {
      await progressAPI.setCurrentVideo(user.id, courseId, videoIndex);
    } catch (error) {
      console.error('Error updating current video:', error);
    }
  };

  const handleVideoComplete = async () => {
    if (!user?.id || !courseId) return;

    // Mark current video as complete
    await markVideoComplete(currentVideoIndex);

    // Check if there's a quiz for this video
    const videoQuiz = quizzes?.find(quiz => 
      quiz.subTopic?.toLowerCase().includes(currentVideo?.title?.toLowerCase().split(' ')[0] || '')
    );

    if (videoQuiz) {
      setCurrentQuiz(videoQuiz);
      setShowQuiz(true);
    } else {
      // Move to next video if no quiz
      handleNextVideo();
    }
  };

  const handleQuizComplete = (score) => {
    setShowQuiz(false);
    setCurrentQuiz(null);
    
    if (score >= 60) {
      toast.success(`Great job! You scored ${score}%`);
      handleNextVideo();
    } else {
      toast.error(`You scored ${score}%. Consider reviewing the material.`);
    }
  };

  const handleNextVideo = () => {
    if (videos && currentVideoIndex < videos.length - 1) {
      const nextIndex = currentVideoIndex + 1;
      setCurrentVideoIndex(nextIndex);
      updateCurrentVideo(nextIndex);
    } else {
      // Course completed
      toast.success('Congratulations! You completed the course!');
    }
  };

  const handleVideoSelect = (index) => {
    setCurrentVideoIndex(index);
    updateCurrentVideo(index);
  };

  // Check if video is completed
  const isVideoCompleted = (index) => {
    return courseProgress.completedVideos?.includes?.(index) || 
           Array.from(courseProgress.completedVideos || []).includes(index);
  };

  // Check if all videos are completed
  const allVideosCompleted = courseProgress.progressPercentage >= 100;

  // Check if user already has a certificate for this course
  useEffect(() => {
    const checkCertificate = async () => {
      if (!user?.id || !courseId) return;
      
      try {
        const response = await certificatesAPI.check(user.id, courseId);
        setHasCertificate(response.data?.hasCertificate || false);
        
        if (response.data?.hasCertificate) {
          // Fetch the actual certificate
          const certResponse = await certificatesAPI.getUserCertificates(user.id);
          const courseCert = certResponse.data?.find(c => c.courseId === parseInt(courseId));
          if (courseCert) {
            setCertificate(courseCert);
          }
        }
      } catch (error) {
        console.error('Error checking certificate:', error);
      }
    };

    checkCertificate();
  }, [user, courseId]);

  // Generate certificate handler
  const handleGenerateCertificate = async () => {
    if (!user?.id || !courseId || !course) return;
    
    setGeneratingCertificate(true);
    try {
      const userName = user.firstName && user.lastName 
        ? `${user.firstName} ${user.lastName}`
        : user.username || 'Learner';
      
      const response = await certificatesAPI.generate(user.id, courseId, userName);
      setCertificate(response.data);
      setHasCertificate(true);
      setShowCertificateModal(true);
      toast.success('Certificate generated successfully!');
    } catch (error) {
      console.error('Error generating certificate:', error);
      toast.error('Failed to generate certificate. Please try again.');
    } finally {
      setGeneratingCertificate(false);
    }
  };

  if (courseLoading || videosLoading) {
    return (
      <div className="min-h-screen bg-surface flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!course) {
    return (
      <div className="min-h-screen bg-surface flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-neutral-900 mb-4">Course Not Found</h2>
          <Link to="/dashboard">
            <Button>Back to Dashboard</Button>
          </Link>
        </div>
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
          <Link 
            to="/dashboard"
            className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Dashboard
          </Link>
          
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-3xl font-bold text-neutral-900 mb-2">
                {course.name}
              </h1>
              <p className="text-neutral-600 mb-4">
                {course.description}
              </p>
              <div className="flex items-center space-x-4 text-sm text-neutral-500">
                <div className="flex items-center space-x-1">
                  <Clock className="h-4 w-4" />
                  <span>{course.estimatedDuration || '2-3 hours'}</span>
                </div>
                <div className="flex items-center space-x-1">
                  <BookOpen className="h-4 w-4" />
                  <span>{course.level}</span>
                </div>
                <div className="flex items-center space-x-1">
                  <Play className="h-4 w-4" />
                  <span>{videos?.length || 0} videos</span>
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Video Player */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="lg:col-span-2"
          >
            <Card className="overflow-hidden">
              {currentVideo ? (
                <VideoPlayer
                  video={currentVideo}
                  onVideoComplete={handleVideoComplete}
                  isCompleted={isVideoCompleted(currentVideoIndex)}
                  videoIndex={currentVideoIndex}
                />
              ) : (
                <div className="aspect-video bg-neutral-100 flex items-center justify-center">
                  <div className="text-center">
                    <Youtube className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
                    <p className="text-neutral-600">No videos available</p>
                  </div>
                </div>
              )}
            </Card>
          </motion.div>

          {/* Course Outline */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <Card>
              <div className="p-6">
                {/* Real-time Progress Bar */}
                <div className="mb-6">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="text-lg font-semibold text-neutral-900">
                      Course Progress
                    </h3>
                    <span className="text-2xl font-bold text-primary-600">
                      {Math.round(courseProgress.progressPercentage || 0)}%
                    </span>
                  </div>
                  <div className="relative h-4 bg-neutral-200 rounded-full overflow-hidden">
                    <motion.div 
                      className="absolute inset-y-0 left-0 bg-gradient-to-r from-primary-500 to-accent-500 rounded-full"
                      initial={{ width: 0 }}
                      animate={{ width: `${courseProgress.progressPercentage || 0}%` }}
                      transition={{ duration: 0.5, ease: "easeOut" }}
                    />
                    {/* Animated shine effect */}
                    <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-pulse" />
                  </div>
                  <div className="flex items-center justify-between mt-2 text-sm text-neutral-600">
                    <span className="flex items-center gap-1">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      {courseProgress.completedVideos?.length || 0} completed
                    </span>
                    <span>{videos?.length || 0} total videos</span>
                  </div>
                </div>

                <div className="border-t border-neutral-200 pt-4">
                  <h4 className="text-sm font-medium text-neutral-500 mb-3">
                    Course Content
                  </h4>
                  
                  {/* Hierarchical Tree View */}
                  <HierarchicalTree
                    videos={videos}
                    currentVideoIndex={currentVideoIndex}
                    onVideoSelect={handleVideoSelect}
                    isVideoCompleted={isVideoCompleted}
                  />
                </div>

                {/* Certificate Section */}
                {videos && videos.length > 0 && (
                  <motion.div 
                    className="mt-6 pt-6 border-t border-gray-200"
                    key={allVideosCompleted ? 'completed' : 'in-progress'}
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.3 }}
                  >
                    {hasCertificate ? (
                      /* Already has certificate */
                      <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg p-4 border border-green-200">
                        <div className="flex items-start gap-3">
                          <Award className="w-6 h-6 text-green-600 mt-0.5 flex-shrink-0" />
                          <div className="flex-1">
                            <h4 className="font-semibold text-green-900 mb-1">
                              Certificate Earned!
                            </h4>
                            <p className="text-sm text-green-700 mb-3">
                              {certificate?.certificateNumber}
                            </p>
                            <a
                              href={certificate?.certificateUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="inline-flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-semibold text-sm"
                            >
                              <Download className="w-4 h-4" />
                              Download Certificate
                            </a>
                          </div>
                        </div>
                      </div>
                    ) : allVideosCompleted ? (
                      /* Can generate certificate - with celebration animation */
                      <motion.div 
                        className="bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 rounded-lg p-4 border-2 border-indigo-300 shadow-lg"
                        initial={{ scale: 0.9, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ 
                          type: "spring",
                          stiffness: 300,
                          damping: 20
                        }}
                      >
                        <div className="flex items-start gap-3">
                          <motion.div
                            animate={{ 
                              rotate: [0, -10, 10, -10, 0],
                              scale: [1, 1.1, 1]
                            }}
                            transition={{ 
                              duration: 0.5,
                              repeat: 2,
                              repeatDelay: 1
                            }}
                          >
                            <Trophy className="w-8 h-8 text-yellow-500 mt-0.5 flex-shrink-0" />
                          </motion.div>
                          <div className="flex-1">
                            <h4 className="font-bold text-indigo-900 mb-1 text-lg">
                              🎉 Course Completed!
                            </h4>
                            <p className="text-sm text-indigo-700 mb-3">
                              Amazing work! You've completed all {videos?.length} videos. Generate your certificate to showcase your achievement.
                            </p>
                            <motion.button
                              onClick={handleGenerateCertificate}
                              disabled={generatingCertificate}
                              className="inline-flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg hover:from-indigo-700 hover:to-purple-700 transition-all font-semibold text-sm disabled:opacity-50 disabled:cursor-not-allowed shadow-md hover:shadow-lg"
                              whileHover={{ scale: 1.02 }}
                              whileTap={{ scale: 0.98 }}
                            >
                              <Award className="w-5 h-5" />
                              {generatingCertificate ? 'Generating...' : '🏆 Generate Certificate'}
                            </motion.button>
                          </div>
                        </div>
                      </motion.div>
                    ) : (
                      /* In progress */
                      <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                        <div className="flex items-start gap-3">
                          <BookOpen className="w-6 h-6 text-gray-600 mt-0.5 flex-shrink-0" />
                          <div className="flex-1">
                            <h4 className="font-semibold text-gray-900 mb-1">
                              Keep Learning
                            </h4>
                            <p className="text-sm text-gray-600">
                              Complete all videos to earn your certificate
                            </p>
                            <div className="mt-2 flex items-center gap-2">
                              <div className="flex-1 bg-gray-200 rounded-full h-2">
                                <motion.div 
                                  className="bg-indigo-600 h-2 rounded-full"
                                  initial={{ width: 0 }}
                                  animate={{ width: `${courseProgress.progressPercentage || 0}%` }}
                                  transition={{ duration: 0.5 }}
                                />
                              </div>
                              <span className="text-sm font-semibold text-gray-700">
                                {courseProgress.completedVideos?.length || 0}/{videos?.length || 0}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    )}
                  </motion.div>
                )}
              </div>
            </Card>
          </motion.div>
        </div>
      </div>

      {/* Quiz Modal */}
      {showQuiz && currentQuiz && (
        <QuizModal
          quiz={currentQuiz}
          isOpen={showQuiz}
          onClose={() => setShowQuiz(false)}
          onComplete={handleQuizComplete}
        />
      )}

      {/* Certificate Success Modal */}
      {showCertificateModal && certificate && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-2xl max-w-lg w-full p-8 shadow-2xl"
          >
            {/* Success Icon */}
            <div className="flex justify-center mb-6">
              <div className="w-20 h-20 bg-gradient-to-br from-green-400 to-emerald-600 rounded-full flex items-center justify-center">
                <Award className="w-10 h-10 text-white" />
              </div>
            </div>

            {/* Content */}
            <h2 className="text-2xl font-bold text-center text-gray-900 mb-2">
              Certificate Generated!
            </h2>
            <p className="text-center text-gray-600 mb-6">
              Congratulations on completing the course! Your certificate is ready.
            </p>

            {/* Certificate Details */}
            <div className="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-lg p-6 mb-6 border border-indigo-200">
              <div className="text-center mb-4">
                <div className="text-sm font-semibold text-gray-600 uppercase tracking-wider mb-2">
                  Certificate ID
                </div>
                <div className="font-mono text-lg font-bold text-indigo-600 select-all">
                  {certificate.certificateNumber}
                </div>
              </div>
              <div className="text-center">
                <div className="text-sm text-gray-600">Course</div>
                <div className="font-semibold text-gray-900">{certificate.courseName}</div>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <a
                href={certificate.certificateUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex-1 flex items-center justify-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-semibold"
              >
                <Download className="w-4 h-4" />
                Download
              </a>
              <a
                href={certificate.certificateUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center justify-center gap-2 px-6 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:border-indigo-400 hover:text-indigo-600 transition-colors font-semibold"
              >
                <ExternalLink className="w-4 h-4" />
              </a>
            </div>

            {/* Close Button */}
            <button
              onClick={() => setShowCertificateModal(false)}
              className="w-full mt-4 px-6 py-2.5 text-gray-600 hover:text-gray-800 transition-colors"
            >
              Close
            </button>
          </motion.div>
        </div>
      )}
    </div>
  );
};

export default CoursePage;