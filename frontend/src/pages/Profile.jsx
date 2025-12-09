import { useState } from 'react';
import { useUser } from '@clerk/clerk-react';
import { User, Award, Settings, BookOpen } from 'lucide-react';
import Credentials from '../components/profile/Credentials';

const Profile = () => {
  const { user } = useUser();
  const [activeTab, setActiveTab] = useState('credentials');

  const tabs = [
    { id: 'overview', label: 'Overview', icon: User },
    { id: 'credentials', label: 'Credentials', icon: Award },
    { id: 'learning', label: 'Learning Path', icon: BookOpen },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Profile Header */}
        <div className="bg-white rounded-xl shadow-sm p-8 mb-6">
          <div className="flex items-center gap-6">
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-3xl font-bold">
              {user?.firstName?.[0] || user?.username?.[0] || 'U'}
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                {user?.firstName && user?.lastName 
                  ? `${user.firstName} ${user.lastName}`
                  : user?.username || 'User'}
              </h1>
              <p className="text-gray-600 mt-1">
                {user?.primaryEmailAddress?.emailAddress || 'learner@learnnow.com'}
              </p>
              <div className="flex items-center gap-4 mt-3">
                <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-semibold">
                  Active Learner
                </span>
                <span className="text-sm text-gray-600">
                  Member since {new Date(user?.createdAt).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-xl shadow-sm mb-6">
          <div className="border-b border-gray-200">
            <nav className="flex gap-8 px-6" aria-label="Tabs">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`
                      flex items-center gap-2 py-4 px-1 border-b-2 font-medium text-sm transition-colors
                      ${activeTab === tab.id
                        ? 'border-indigo-500 text-indigo-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                      }
                    `}
                  >
                    <Icon className="w-5 h-5" />
                    {tab.label}
                  </button>
                );
              })}
            </nav>
          </div>
        </div>

        {/* Tab Content */}
        <div className="bg-white rounded-xl shadow-sm p-8">
          {activeTab === 'credentials' && <Credentials />}
          
          {activeTab === 'overview' && (
            <div className="text-center py-12">
              <User className="mx-auto h-16 w-16 text-gray-400 mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Profile Overview</h3>
              <p className="text-gray-600">Coming soon - View your learning statistics and achievements</p>
            </div>
          )}
          
          {activeTab === 'learning' && (
            <div className="text-center py-12">
              <BookOpen className="mx-auto h-16 w-16 text-gray-400 mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Learning Path</h3>
              <p className="text-gray-600">Coming soon - Track your personalized learning journey</p>
            </div>
          )}
          
          {activeTab === 'settings' && (
            <div className="text-center py-12">
              <Settings className="mx-auto h-16 w-16 text-gray-400 mb-4" />
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Settings</h3>
              <p className="text-gray-600">Coming soon - Manage your account preferences</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
