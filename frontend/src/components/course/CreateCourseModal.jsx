import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { motion } from 'framer-motion';
import { X, BookOpen, Target, Globe, Sparkles, CheckSquare, Square, Lightbulb } from 'lucide-react';
import { useApiMutation } from '@/hooks/useApi';
import { topicsAPI, learningPathAPI, youtubeAPI, coursesAPI, videosAPI } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth.jsx';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import toast from 'react-hot-toast';

const CreateCourseModal = ({ isOpen, onClose, onCourseCreated }) => {
  const { user } = useAuth();
  const [step, setStep] = useState(1); // 1: Basic info, 2: Subtopic selection, 3: Generating
  const [createdTopic, setCreatedTopic] = useState(null);
  const [subtopics, setSubtopics] = useState([]);
  const [selectedSubtopics, setSelectedSubtopics] = useState([]);
  const [isLoadingSubtopics, setIsLoadingSubtopics] = useState(false);
  const [isGeneratingPath, setIsGeneratingPath] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch
  } = useForm();

  const courseName = watch('name');

  const createTopicMutation = useApiMutation(topicsAPI.create, {
    onSuccess: (data) => {
      setCreatedTopic(data.data);
      setStep(2);
    },
    onError: (error) => {
      toast.error('Failed to create course');
    }
  });

  const languageOptions = [
    { value: 'English', label: 'English' },
    { value: 'Hindi', label: 'Hindi' },
    { value: 'Spanish', label: 'Spanish' },
    { value: 'French', label: 'French' },
    { value: 'German', label: 'German' },
    { value: 'Multi', label: 'Multi-Language (Mixed)' },
  ];

  const levelOptions = [
    { value: 'Beginner', label: 'Beginner' },
    { value: 'Intermediate', label: 'Intermediate' },
    { value: 'Advanced', label: 'Advanced' },
  ];

  const onSubmit = async (data) => {
    // First, create the topic with user ID
    const topicData = {
      name: data.name,
      description: data.description,
      purpose: data.purpose,
      language: data.language,
      level: data.level,
      estimatedDurationMinutes: 120,
      enrolledUsers: 1,
      rating: 0,
      createdByUserId: user?.id  // Store Clerk user ID
    };

    try {
      const topicResponse = await topicsAPI.create(topicData);
      setCreatedTopic(topicResponse.data);
      
      // Generate subtopics using Groq AI
      setIsLoadingSubtopics(true);
      const subtopicsResponse = await coursesAPI.generateSubtopics({
        topicName: data.name,
        purpose: data.purpose,
        level: data.level
      });
      
      setSubtopics(subtopicsResponse.data.subtopics || []);
      setSelectedSubtopics(subtopicsResponse.data.subtopics || []); // Select all by default
      setIsLoadingSubtopics(false);
      setStep(2); // Move to subtopic selection
    } catch (error) {
      setIsLoadingSubtopics(false);
      console.error('Error creating course or generating subtopics:', error);
      toast.error('Failed to create course. Please try again.');
    }
  };

  const toggleSubtopic = (subtopic) => {
    setSelectedSubtopics(prev => {
      if (prev.includes(subtopic)) {
        return prev.filter(s => s !== subtopic);
      } else {
        return [...prev, subtopic];
      }
    });
  };

  const toggleSelectAll = () => {
    if (selectedSubtopics.length === subtopics.length) {
      setSelectedSubtopics([]);
    } else {
      setSelectedSubtopics([...subtopics]);
    }
  };

  const generateLearningPath = async () => {
    if (!createdTopic || selectedSubtopics.length === 0) {
      toast.error('Please select at least one subtopic');
      return;
    }

    setIsGeneratingPath(true);
    setStep(3); // Move to generating step
    
    try {
      // Search for TOP 3 BEST YouTube videos for each selected subtopic
      const allVideos = [];
      const courseLanguage = createdTopic.language || 'English'; // Get selected language
      
      for (const subtopic of selectedSubtopics) {
        try {
          // Create specific search query for this subtopic
          const searchQuery = `${createdTopic.name} ${subtopic} tutorial`;
          
          // Use the BEST MULTIPLE videos endpoint with language filter
          const bestVideosResponse = await youtubeAPI.searchBestMultiple(searchQuery, 3, courseLanguage);
          
          if (bestVideosResponse.data && bestVideosResponse.data.length > 0) {
            // Add all best videos for this subtopic
            bestVideosResponse.data.forEach(video => {
              allVideos.push({
                ...video,
                subtopic
              });
            });
            console.log(`[Best Videos] ${subtopic} (${courseLanguage}): Found ${bestVideosResponse.data.length} quality videos`);
          } else {
            console.warn(`[Best Videos] No videos found for subtopic: ${subtopic}`);
          }
        } catch (error) {
          console.error(`Error searching best videos for ${subtopic}:`, error);
          // Fallback: try single best video if multiple search fails
          try {
            const fallbackResponse = await youtubeAPI.searchBest(searchQuery);
            if (fallbackResponse.data) {
              allVideos.push({
                ...fallbackResponse.data,
                subtopic
              });
            }
          } catch (fallbackError) {
            console.error(`Fallback search also failed for ${subtopic}`);
          }
        }
      }

      if (allVideos.length > 0) {
        // Create video records in the database
        const videoPromises = allVideos.map((video, index) => {
          return videosAPI.create({
            youtubeId: video.videoId,
            title: video.title,
            channel: video.channelTitle,
            duration: video.duration || 600,
            language: createdTopic.language,
            position: index + 1,
            subtopic: video.subtopic,
            chaptersJson: JSON.stringify({
              chapters: [
                { title: 'Introduction', time: 0 },
                { title: video.subtopic || 'Main Content', time: Math.floor((video.duration || 600) * 0.3) },
                { title: 'Summary', time: Math.floor((video.duration || 600) * 0.8) }
              ]
            }),
            topic: { id: createdTopic.id }
          });
        });

        await Promise.all(videoPromises);
        
        // Skip learning path creation for now due to user ID type mismatch
        // TODO: Implement proper user ID mapping between Clerk (string) and backend (Long)
        // if (user?.id) {
        //   await learningPathAPI.create(user.id, {
        //     name: `${createdTopic.name} Learning Path`,
        //     description: `Complete learning path for ${createdTopic.name} covering: ${selectedSubtopics.join(', ')}`,
        //     purpose: createdTopic.purpose,
        //     language: createdTopic.language,
        //     level: createdTopic.level,
        //     estimatedHours: Math.ceil((allVideos.length * 10) / 60), // Estimate 10 min per video
        //     isPublic: false
        //   });
        // }
      }

      toast.success(`Course created with ${allVideos.length} videos! Visit the course page to start learning.`);
      onCourseCreated();
      handleClose();
    } catch (error) {
      console.error('Error generating learning path:', error);
      toast.error('Failed to generate learning path');
    } finally {
      setIsGeneratingPath(false);
    }
  };

  const handleClose = () => {
    setStep(1);
    setCreatedTopic(null);
    setSubtopics([]);
    setSelectedSubtopics([]);
    setIsGeneratingPath(false);
    setIsLoadingSubtopics(false);
    reset();
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} size="lg">
      <div className="p-6">
        {step === 1 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <div className="flex items-center space-x-3 mb-6">
              <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                <BookOpen className="h-5 w-5 text-primary-600" />
              </div>
              <h2 className="text-2xl font-semibold text-neutral-900">
                Create New Course
              </h2>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-2">
                  <Target className="inline h-4 w-4 mr-1" />
                  Course Name
                </label>
                <input
                  {...register('name', { required: 'Course name is required' })}
                  type="text"
                  placeholder="e.g., JavaScript Fundamentals, Python for Data Science"
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-2">
                  Description (Why do you want to learn this?)
                </label>
                <textarea
                  {...register('description', { required: 'Description is required' })}
                  rows={3}
                  placeholder="Describe your learning goals and what you hope to achieve..."
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
                />
                {errors.description && (
                  <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-2">
                  Learning Purpose
                </label>
                <input
                  {...register('purpose', { required: 'Purpose is required' })}
                  type="text"
                  placeholder="e.g., Career advancement, Personal interest, Job interview prep"
                  className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
                {errors.purpose && (
                  <p className="mt-1 text-sm text-red-600">{errors.purpose.message}</p>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-neutral-700 mb-2">
                    <Globe className="inline h-4 w-4 mr-1" />
                    Preferred Language
                  </label>
                  <select
                    {...register('language', { required: 'Language is required' })}
                    className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  >
                    <option value="">Select language</option>
                    {languageOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  {errors.language && (
                    <p className="mt-1 text-sm text-red-600">{errors.language.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-neutral-700 mb-2">
                    Skill Level
                  </label>
                  <select
                    {...register('level', { required: 'Level is required' })}
                    className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  >
                    <option value="">Select level</option>
                    {levelOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  {errors.level && (
                    <p className="mt-1 text-sm text-red-600">{errors.level.message}</p>
                  )}
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={handleClose}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  loading={isLoadingSubtopics}
                  className="min-w-[120px]"
                  disabled={isLoadingSubtopics}
                >
                  {isLoadingSubtopics ? 'Generating...' : 'Next: Select Topics'}
                </Button>
              </div>
            </form>
          </motion.div>
        )}

        {step === 2 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <div className="flex items-center space-x-3 mb-6">
              <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                <Lightbulb className="h-5 w-5 text-primary-600" />
              </div>
              <div>
                <h2 className="text-2xl font-semibold text-neutral-900">
                  Select Subtopics to Learn
                </h2>
                <p className="text-sm text-neutral-600 mt-1">
                  Choose the topics you want to include in your course
                </p>
              </div>
            </div>

            <div className="bg-gradient-to-r from-primary-50 to-secondary-50 rounded-lg p-4 mb-6">
              <p className="text-sm text-neutral-700">
                <strong className="text-primary-700">AI-Generated Curriculum</strong> for{' '}
                <strong>{createdTopic?.name}</strong>
              </p>
              <p className="text-xs text-neutral-600 mt-1">
                Select the subtopics you want to learn. We'll find the best videos for each.
              </p>
            </div>

            <div className="mb-4 flex justify-between items-center">
              <button
                type="button"
                onClick={toggleSelectAll}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center"
              >
                {selectedSubtopics.length === subtopics.length ? (
                  <>
                    <CheckSquare className="h-4 w-4 mr-1" />
                    Deselect All
                  </>
                ) : (
                  <>
                    <Square className="h-4 w-4 mr-1" />
                    Select All
                  </>
                )}
              </button>
              <span className="text-sm text-neutral-600">
                {selectedSubtopics.length} of {subtopics.length} selected
              </span>
            </div>

            <div className="space-y-2 max-h-96 overflow-y-auto mb-6">
              {subtopics.map((subtopic, index) => (
                <label
                  key={index}
                  className="flex items-start p-4 border border-neutral-200 rounded-lg cursor-pointer hover:border-primary-300 hover:bg-primary-50 transition-colors"
                >
                  <div className="flex items-center h-5 mt-0.5">
                    {selectedSubtopics.includes(subtopic) ? (
                      <CheckSquare
                        className="h-5 w-5 text-primary-600"
                        onClick={() => toggleSubtopic(subtopic)}
                      />
                    ) : (
                      <Square
                        className="h-5 w-5 text-neutral-400"
                        onClick={() => toggleSubtopic(subtopic)}
                      />
                    )}
                  </div>
                  <div className="ml-3 flex-1">
                    <span className="text-sm font-medium text-neutral-900">
                      {subtopic}
                    </span>
                  </div>
                </label>
              ))}
            </div>

            <div className="flex justify-between space-x-3 pt-4 border-t">
              <Button
                type="button"
                variant="ghost"
                onClick={() => setStep(1)}
              >
                Back
              </Button>
              <Button
                type="button"
                onClick={generateLearningPath}
                disabled={selectedSubtopics.length === 0}
                className="min-w-[180px]"
              >
                <Sparkles className="h-4 w-4 mr-2" />
                Generate Course ({selectedSubtopics.length} topics)
              </Button>
            </div>
          </motion.div>
        )}

        {step === 3 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center py-8"
          >
            <div className="w-16 h-16 bg-gradient-to-br from-primary-500 to-secondary-500 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
              <Sparkles className="h-8 w-8 text-white" />
            </div>
            
            <h2 className="text-2xl font-semibold text-neutral-900 mb-4">
              Creating Your Learning Path...
            </h2>
            
            <p className="text-neutral-600 mb-8">
              Finding the best videos for {selectedSubtopics.length} subtopics
            </p>

            <div className="bg-neutral-50 rounded-lg p-6">
              <LoadingSpinner className="mx-auto mb-4" />
              <ul className="text-sm text-neutral-600 space-y-2 text-left max-w-md mx-auto">
                <li>• Searching YouTube for top educational content</li>
                <li>• Curating videos for: {selectedSubtopics.slice(0, 3).join(', ')}
                  {selectedSubtopics.length > 3 && ` and ${selectedSubtopics.length - 3} more...`}
                </li>
                <li>• Creating your personalized learning path</li>
              </ul>
            </div>
          </motion.div>
        )}
      </div>
    </Modal>
  );
};

export default CreateCourseModal;
