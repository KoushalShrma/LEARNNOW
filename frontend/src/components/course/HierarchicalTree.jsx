import { useState } from 'react';
import { ChevronDown, ChevronRight, Play, CheckCircle, Folder, FolderOpen, Youtube } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

/**
 * Parse hierarchical subtopic paths into a tree structure
 * @param {Array} videos - Array of video objects with subtopic paths (e.g., "React > Hooks > useState")
 * @returns {Object} Tree structure with nested children
 */
const buildHierarchicalTree = (videos) => {
  const tree = {};
  
  videos.forEach((video, index) => {
    const path = video.subtopic || 'General';
    const parts = path.split(' > ').map(p => p.trim());
    
    let currentLevel = tree;
    
    parts.forEach((part, level) => {
      if (!currentLevel[part]) {
        currentLevel[part] = {
          name: part,
          path: parts.slice(0, level + 1).join(' > '),
          level: level,
          children: {},
          videos: [],
          isLeaf: false
        };
      }
      
      // If this is the last part, attach the video
      if (level === parts.length - 1) {
        currentLevel[part].videos.push({ ...video, globalIndex: index });
        currentLevel[part].isLeaf = true;
      }
      
      currentLevel = currentLevel[part].children;
    });
  });
  
  return tree;
};

/**
 * Recursive tree node component
 */
const TreeNode = ({ node, currentVideoIndex, onVideoSelect, depth = 0 }) => {
  const [isExpanded, setIsExpanded] = useState(depth === 0); // Auto-expand Level 1
  
  const hasChildren = Object.keys(node.children).length > 0;
  const hasVideos = node.videos.length > 0;
  const isLeaf = node.isLeaf && !hasChildren;
  
  // Check if any child video is current
  const isActive = node.videos.some(v => v.globalIndex === currentVideoIndex);
  const isCompleted = node.videos.every(v => v.globalIndex < currentVideoIndex);
  
  const toggleExpand = () => {
    if (hasChildren || hasVideos) {
      setIsExpanded(!isExpanded);
    }
  };
  
  return (
    <div className="select-none">
      {/* Node Header */}
      <div
        className={`
          flex items-center space-x-2 py-2 px-3 rounded-lg cursor-pointer transition-all duration-200
          ${depth === 0 ? 'font-semibold text-neutral-900' : ''}
          ${depth === 1 ? 'font-medium text-neutral-800' : ''}
          ${depth >= 2 ? 'text-neutral-700' : ''}
          ${isActive && isLeaf ? 'bg-primary-50 border border-primary-200' : 'hover:bg-neutral-50'}
        `}
        style={{ paddingLeft: `${depth * 16 + 12}px` }}
        onClick={toggleExpand}
      >
        {/* Expand/Collapse Icon */}
        {(hasChildren || (hasVideos && !isLeaf)) && (
          <div className="w-4 h-4 flex-shrink-0">
            {isExpanded ? (
              <ChevronDown className="h-4 w-4 text-neutral-500" />
            ) : (
              <ChevronRight className="h-4 w-4 text-neutral-500" />
            )}
          </div>
        )}
        
        {/* Folder Icon for non-leaf nodes */}
        {!isLeaf && (
          <div className="w-4 h-4 flex-shrink-0">
            {isExpanded ? (
              <FolderOpen className={`h-4 w-4 ${depth === 0 ? 'text-primary-600' : 'text-neutral-500'}`} />
            ) : (
              <Folder className={`h-4 w-4 ${depth === 0 ? 'text-primary-600' : 'text-neutral-500'}`} />
            )}
          </div>
        )}
        
        {/* Status Icon for leaf nodes with videos */}
        {isLeaf && hasVideos && (
          <div className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 ${
            isCompleted
              ? 'bg-accent-500 text-white'
              : isActive
              ? 'bg-primary-500 text-white'
              : 'bg-neutral-200 text-neutral-600'
          }`}>
            {isCompleted ? (
              <CheckCircle className="h-3 w-3" />
            ) : isActive ? (
              <Play className="h-3 w-3" />
            ) : (
              <Youtube className="h-3 w-3" />
            )}
          </div>
        )}
        
        {/* Node Name */}
        <span className={`flex-1 truncate ${depth === 0 ? 'text-sm' : 'text-sm'}`}>
          {node.name}
        </span>
        
        {/* Video Count Badge for non-leaf nodes */}
        {!isLeaf && hasChildren && (
          <span className="text-xs text-neutral-500 bg-neutral-100 px-2 py-0.5 rounded-full">
            {countVideos(node)}
          </span>
        )}
      </div>
      
      {/* Children & Videos */}
      <AnimatePresence>
        {isExpanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="overflow-hidden"
          >
            {/* Render child nodes */}
            {hasChildren && (
              <div className="space-y-1">
                {Object.values(node.children).map((child) => (
                  <TreeNode
                    key={child.path}
                    node={child}
                    currentVideoIndex={currentVideoIndex}
                    onVideoSelect={onVideoSelect}
                    depth={depth + 1}
                  />
                ))}
              </div>
            )}
            
            {/* Render videos if this is a leaf node */}
            {isLeaf && hasVideos && (
              <div className="space-y-1 mt-1">
                {node.videos.map((video) => (
                  <motion.div
                    key={video.id}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    className={`
                      flex items-center space-x-2 py-2 px-3 rounded-lg cursor-pointer transition-all duration-200
                      ${video.globalIndex === currentVideoIndex
                        ? 'bg-primary-100 border border-primary-300'
                        : 'hover:bg-neutral-50'
                      }
                    `}
                    style={{ paddingLeft: `${(depth + 1) * 16 + 12}px` }}
                    onClick={(e) => {
                      e.stopPropagation();
                      onVideoSelect(video.globalIndex);
                    }}
                  >
                    {/* Video Status Icon */}
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 ${
                      video.globalIndex < currentVideoIndex
                        ? 'bg-accent-500 text-white'
                        : video.globalIndex === currentVideoIndex
                        ? 'bg-primary-600 text-white'
                        : 'bg-neutral-200 text-neutral-600'
                    }`}>
                      {video.globalIndex < currentVideoIndex ? (
                        <CheckCircle className="h-3 w-3" />
                      ) : (
                        <Play className="h-3 w-3" />
                      )}
                    </div>
                    
                    {/* Video Title */}
                    <div className="flex-1 min-w-0">
                      <h5 className={`text-sm truncate ${
                        video.globalIndex === currentVideoIndex ? 'font-medium text-primary-900' : 'text-neutral-800'
                      }`}>
                        {video.title}
                      </h5>
                      <p className="text-xs text-neutral-500">
                        {Math.floor((video.duration || 600) / 60)} min
                      </p>
                    </div>
                    
                    {video.globalIndex === currentVideoIndex && (
                      <ChevronRight className="h-4 w-4 text-primary-600 flex-shrink-0" />
                    )}
                  </motion.div>
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

/**
 * Count total videos in a node and its children
 */
const countVideos = (node) => {
  let count = node.videos.length;
  Object.values(node.children).forEach(child => {
    count += countVideos(child);
  });
  return count;
};

/**
 * Main hierarchical tree component
 */
const HierarchicalTree = ({ videos, currentVideoIndex, onVideoSelect }) => {
  if (!videos || videos.length === 0) {
    return (
      <div className="text-center py-8">
        <Youtube className="h-8 w-8 text-neutral-400 mx-auto mb-2" />
        <p className="text-neutral-600 text-sm">No content available</p>
      </div>
    );
  }
  
  const tree = buildHierarchicalTree(videos);
  
  return (
    <div className="space-y-1">
      {Object.values(tree).map((node) => (
        <TreeNode
          key={node.path}
          node={node}
          currentVideoIndex={currentVideoIndex}
          onVideoSelect={onVideoSelect}
          depth={0}
        />
      ))}
    </div>
  );
};

export default HierarchicalTree;
