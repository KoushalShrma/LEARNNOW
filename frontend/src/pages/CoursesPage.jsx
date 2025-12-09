import { useState } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Clock, Users, BarChart3 } from 'lucide-react';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import CourseCard from '@/components/course/CourseCard';
import { useApiQuery } from '@/hooks/useApi';
import { topicsAPI } from '@/lib/api';

const CoursesPage = () => {
  const [filter, setFilter] = useState('all'); // all, beginner, intermediate, advanced
  
  const { data: courses, isLoading, error, refetch } = useApiQuery('topics', topicsAPI.getAll);

  // Ensure courses is an array
  const coursesArray = Array.isArray(courses) ? courses : [];
  
  const filteredCourses = coursesArray.filter(course => {
    if (filter === 'all') return true;
    return course.level?.toLowerCase() === filter;
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">Failed to load courses</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-neutral-900 mb-2">All Courses</h1>
        <p className="text-neutral-600">Browse and explore all available courses</p>
      </div>

      {/* Filter tabs */}
      <div className="flex space-x-2 mb-6 border-b border-neutral-200">
        {['all', 'beginner', 'intermediate', 'advanced'].map((level) => (
          <button
            key={level}
            onClick={() => setFilter(level)}
            className={`px-4 py-2 text-sm font-medium capitalize transition-colors border-b-2 -mb-px ${
              filter === level
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-neutral-600 hover:text-neutral-900'
            }`}
          >
            {level}
          </button>
        ))}
      </div>

      {/* Courses grid */}
      {filteredCourses.length === 0 ? (
        <div className="text-center py-12">
          <BookOpen className="h-16 w-16 text-neutral-400 mx-auto mb-4" />
          <p className="text-neutral-600">No courses found</p>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCourses.map((course) => (
            <CourseCard 
              key={course.id}
              course={course}
              onDelete={refetch}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default CoursesPage;
