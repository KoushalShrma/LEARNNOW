import { Trophy, TrendingUp, Award, Target } from 'lucide-react';
import Card from '@/components/ui/Card';

const ProgressPage = () => {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-neutral-900 mb-2">Your Progress</h1>
        <p className="text-neutral-600">Track your learning journey and achievements</p>
      </div>

      {/* Stats Grid */}
      <div className="grid md:grid-cols-4 gap-6 mb-8">
        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-neutral-600 mb-1">Courses Completed</p>
                <p className="text-3xl font-bold text-neutral-900">0</p>
              </div>
              <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <Trophy className="h-6 w-6 text-primary-600" />
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-neutral-600 mb-1">Videos Watched</p>
                <p className="text-3xl font-bold text-neutral-900">0</p>
              </div>
              <div className="w-12 h-12 bg-secondary-100 rounded-full flex items-center justify-center">
                <TrendingUp className="h-6 w-6 text-secondary-600" />
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-neutral-600 mb-1">Quizzes Passed</p>
                <p className="text-3xl font-bold text-neutral-900">0</p>
              </div>
              <div className="w-12 h-12 bg-accent-100 rounded-full flex items-center justify-center">
                <Award className="h-6 w-6 text-accent-600" />
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-neutral-600 mb-1">Learning Streak</p>
                <p className="text-3xl font-bold text-neutral-900">0</p>
              </div>
              <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <Target className="h-6 w-6 text-primary-600" />
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Coming Soon Message */}
      <Card>
        <div className="p-12 text-center">
          <TrendingUp className="h-16 w-16 text-neutral-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-neutral-900 mb-2">Progress Tracking Coming Soon</h2>
          <p className="text-neutral-600">
            We're building detailed progress tracking features to help you monitor your learning journey.
          </p>
        </div>
      </Card>
    </div>
  );
};

export default ProgressPage;
