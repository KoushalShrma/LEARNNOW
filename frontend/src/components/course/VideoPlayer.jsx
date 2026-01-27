import { useState, useEffect, useRef } from 'react';
import { CheckCircle, Play, Clock, Eye } from 'lucide-react';

const VideoPlayer = ({ video, onVideoComplete, isCompleted = false, videoIndex }) => {
  const [hasStartedWatching, setHasStartedWatching] = useState(false);
  const [watchTime, setWatchTime] = useState(0);
  const [showCompleteButton, setShowCompleteButton] = useState(false);
  const [isMarkedComplete, setIsMarkedComplete] = useState(isCompleted);
  const watchTimerRef = useRef(null);
  const iframeRef = useRef(null);

  // Reset state when video changes
  useEffect(() => {
    setHasStartedWatching(false);
    setWatchTime(0);
    setShowCompleteButton(false);
    setIsMarkedComplete(isCompleted);
    
    // Clear any existing timer
    if (watchTimerRef.current) {
      clearInterval(watchTimerRef.current);
    }
  }, [video?.youtubeId, isCompleted]);

  // Track watch time when user starts watching
  useEffect(() => {
    if (hasStartedWatching && !isMarkedComplete) {
      watchTimerRef.current = setInterval(() => {
        setWatchTime(prev => {
          const newTime = prev + 1;
          // Show complete button after 10 seconds of watching
          if (newTime >= 10 && !showCompleteButton) {
            setShowCompleteButton(true);
          }
          return newTime;
        });
      }, 1000);
    }

    return () => {
      if (watchTimerRef.current) {
        clearInterval(watchTimerRef.current);
      }
    };
  }, [hasStartedWatching, isMarkedComplete, showCompleteButton]);

  const handleVideoClick = () => {
    if (!hasStartedWatching) {
      setHasStartedWatching(true);
    }
  };

  const handleMarkComplete = async () => {
    setIsMarkedComplete(true);
    if (watchTimerRef.current) {
      clearInterval(watchTimerRef.current);
    }
    
    // Call the parent callback to update progress
    if (onVideoComplete) {
      await onVideoComplete();
    }
  };

  const formatWatchTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (!video || !video.youtubeId) {
    return (
      <div className="relative bg-neutral-900 aspect-video flex items-center justify-center">
        <p className="text-white">No video available</p>
      </div>
    );
  }

  // YouTube embed URL with autoplay and controls
  const youtubeEmbedUrl = `https://www.youtube.com/embed/${video.youtubeId}?autoplay=0&rel=0&modestbranding=1&enablejsapi=1`;

  return (
    <div className="relative">
      {/* Video Container */}
      <div 
        className="relative bg-black aspect-video cursor-pointer"
        onClick={handleVideoClick}
      >
        {/* YouTube Iframe with native controls */}
        <iframe
          ref={iframeRef}
          src={youtubeEmbedUrl}
          title={video.title || 'Video'}
          className="w-full h-full"
          frameBorder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          allowFullScreen
        />

        {/* Completed Overlay Badge */}
        {isMarkedComplete && (
          <div className="absolute top-4 right-4 bg-green-500 text-white px-3 py-1 rounded-full flex items-center gap-1 text-sm font-medium shadow-lg">
            <CheckCircle className="w-4 h-4" />
            Completed
          </div>
        )}

        {/* Click to Start Watching Overlay - Only shows before first click */}
        {!hasStartedWatching && !isMarkedComplete && (
          <div 
            className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity pointer-events-none"
          >
            <div className="bg-white/90 backdrop-blur-sm px-4 py-2 rounded-lg flex items-center gap-2 text-neutral-800">
              <Play className="w-5 h-5" />
              <span className="font-medium">Click to start tracking progress</span>
            </div>
          </div>
        )}
      </div>

      {/* Video Info & Progress Section */}
      <div className="p-4 bg-white border-t">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1">
            <h3 className="text-neutral-900 font-semibold text-xl mb-1">{video.title}</h3>
            <p className="text-neutral-600 text-sm">{video.channel}</p>
          </div>

          {/* Status Badge */}
          <div className="flex-shrink-0">
            {isMarkedComplete ? (
              <div className="flex items-center gap-1 text-green-600 bg-green-50 px-3 py-1.5 rounded-full">
                <CheckCircle className="w-4 h-4" />
                <span className="text-sm font-medium">Watched</span>
              </div>
            ) : hasStartedWatching ? (
              <div className="flex items-center gap-1 text-blue-600 bg-blue-50 px-3 py-1.5 rounded-full">
                <Eye className="w-4 h-4" />
                <span className="text-sm font-medium">Watching</span>
              </div>
            ) : (
              <div className="flex items-center gap-1 text-neutral-500 bg-neutral-100 px-3 py-1.5 rounded-full">
                <Play className="w-4 h-4" />
                <span className="text-sm font-medium">Not Started</span>
              </div>
            )}
          </div>
        </div>

        {/* Watch Time & Complete Button */}
        {hasStartedWatching && !isMarkedComplete && (
          <div className="mt-4 flex items-center justify-between gap-4 p-3 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-100">
            <div className="flex items-center gap-2 text-blue-700">
              <Clock className="w-4 h-4" />
              <span className="text-sm">
                Watch time: <span className="font-semibold">{formatWatchTime(watchTime)}</span>
              </span>
            </div>

            {showCompleteButton && (
              <button
                onClick={handleMarkComplete}
                className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all font-medium text-sm shadow-md hover:shadow-lg transform hover:scale-105"
              >
                <CheckCircle className="w-4 h-4" />
                Mark as Complete
              </button>
            )}

            {!showCompleteButton && (
              <div className="text-xs text-blue-600">
                Watch for {10 - watchTime}s more to mark complete
              </div>
            )}
          </div>
        )}

        {/* Immediate Complete Button for Quick Access */}
        {!isMarkedComplete && (
          <div className="mt-3">
            <button
              onClick={handleMarkComplete}
              className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-neutral-100 hover:bg-neutral-200 text-neutral-700 rounded-lg transition-colors font-medium text-sm border border-neutral-200"
            >
              <CheckCircle className="w-4 h-4" />
              {hasStartedWatching ? 'Complete Video Now' : 'Mark as Watched'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default VideoPlayer;