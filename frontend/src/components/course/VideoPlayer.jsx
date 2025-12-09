const VideoPlayer = ({ video, onVideoComplete }) => {
  if (!video || !video.youtubeId) {
    return (
      <div className="relative bg-neutral-900 aspect-video flex items-center justify-center">
        <p className="text-white">No video available</p>
      </div>
    );
  }

  // YouTube embed URL with autoplay and controls
  const youtubeEmbedUrl = `https://www.youtube.com/embed/${video.youtubeId}?autoplay=0&rel=0&modestbranding=1`;

  return (
    <div className="relative bg-black aspect-video">
      {/* YouTube Iframe with native controls */}
      <iframe
        src={youtubeEmbedUrl}
        title={video.title || 'Video'}
        className="w-full h-full"
        frameBorder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
        allowFullScreen
      />

      {/* Video Info Overlay */}
      <div className="mt-4">
        <h3 className="text-neutral-900 font-semibold text-xl mb-2">{video.title}</h3>
        <p className="text-neutral-600">{video.channel}</p>
      </div>
    </div>
  );
};

export default VideoPlayer;