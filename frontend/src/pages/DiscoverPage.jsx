import { useState } from 'react';
import { Search, Filter, BookOpen, TrendingUp } from 'lucide-react';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

const DiscoverPage = () => {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-neutral-900 mb-2">Discover</h1>
        <p className="text-neutral-600">Find new topics and expand your knowledge</p>
      </div>

      {/* Search Bar */}
      <div className="mb-8">
        <div className="flex gap-4">
          <div className="flex-1">
            <Input
              placeholder="Search for topics, technologies, or skills..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              icon={Search}
            />
          </div>
          <Button variant="outline" className="flex items-center space-x-2">
            <Filter className="h-4 w-4" />
            <span>Filters</span>
          </Button>
        </div>
      </div>

      {/* Trending Topics */}
      <div className="mb-8">
        <h2 className="text-xl font-semibold text-neutral-900 mb-4 flex items-center space-x-2">
          <TrendingUp className="h-5 w-5 text-primary-600" />
          <span>Trending Topics</span>
        </h2>
        <div className="flex flex-wrap gap-2">
          {['React', 'Python', 'Machine Learning', 'Docker', 'AWS', 'TypeScript', 'GraphQL', 'Kubernetes'].map((topic) => (
            <button
              key={topic}
              className="px-4 py-2 bg-neutral-100 hover:bg-neutral-200 text-neutral-900 rounded-full text-sm font-medium transition-colors"
            >
              {topic}
            </button>
          ))}
        </div>
      </div>

      {/* Coming Soon Message */}
      <Card>
        <div className="p-12 text-center">
          <BookOpen className="h-16 w-16 text-neutral-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-neutral-900 mb-2">Discovery Features Coming Soon</h2>
          <p className="text-neutral-600">
            We're building personalized course recommendations and discovery features to help you find the perfect learning path.
          </p>
        </div>
      </Card>
    </div>
  );
};

export default DiscoverPage;
