import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { 
  Award, 
  Download, 
  ExternalLink, 
  Calendar, 
  BookOpen, 
  ArrowLeft,
  Search,
  Filter,
  CheckCircle
} from 'lucide-react';
import { useAuth } from '@/hooks/useAuth.jsx';
import { certificatesAPI } from '@/lib/api';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

const CertificatesPage = () => {
  const { user } = useAuth();
  const [certificates, setCertificates] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('newest'); // newest, oldest, name

  useEffect(() => {
    const fetchCertificates = async () => {
      if (!user?.id) return;
      
      setIsLoading(true);
      try {
        const response = await certificatesAPI.getUserCertificates(user.id);
        setCertificates(response.data || []);
      } catch (error) {
        console.error('Error fetching certificates:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCertificates();
  }, [user?.id]);

  // Filter and sort certificates
  const filteredCertificates = certificates
    .filter(cert => 
      cert.courseName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      cert.certificateNumber?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => {
      switch (sortBy) {
        case 'oldest':
          return new Date(a.issuedAt) - new Date(b.issuedAt);
        case 'name':
          return a.courseName.localeCompare(b.courseName);
        case 'newest':
        default:
          return new Date(b.issuedAt) - new Date(a.issuedAt);
      }
    });

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-surface flex items-center justify-center">
        <LoadingSpinner size="lg" />
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
          
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center">
                <Award className="h-8 w-8 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-bold text-neutral-900">
                  My Certificates
                </h1>
                <p className="text-neutral-600 mt-1">
                  {certificates.length} certificate{certificates.length !== 1 ? 's' : ''} earned
                </p>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Search and Filter */}
        {certificates.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="mb-8 flex flex-col sm:flex-row gap-4"
          >
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-neutral-400" />
              <input
                type="text"
                placeholder="Search by course name or certificate number..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
            <div className="flex items-center gap-2">
              <Filter className="h-5 w-5 text-neutral-500" />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              >
                <option value="newest">Newest First</option>
                <option value="oldest">Oldest First</option>
                <option value="name">By Course Name</option>
              </select>
            </div>
          </motion.div>
        )}

        {/* Certificates Grid */}
        {filteredCertificates.length > 0 ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            {filteredCertificates.map((cert, index) => (
              <motion.div
                key={cert.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
              >
                <Card hover className="h-full overflow-hidden">
                  {/* Certificate Preview */}
                  <div className="relative bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 p-6 border-b border-neutral-200">
                    <div className="absolute top-3 right-3">
                      <div className="flex items-center gap-1 px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium">
                        <CheckCircle className="h-3 w-3" />
                        Verified
                      </div>
                    </div>
                    
                    <div className="flex justify-center mb-4">
                      <div className="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center shadow-lg">
                        <Award className="h-8 w-8 text-white" />
                      </div>
                    </div>
                    
                    <h3 className="text-lg font-semibold text-center text-neutral-900 mb-1">
                      {cert.courseName}
                    </h3>
                    <p className="text-sm text-center text-neutral-600">
                      Certificate of Completion
                    </p>
                  </div>

                  {/* Certificate Details */}
                  <div className="p-6">
                    <div className="space-y-3 mb-6">
                      <div className="flex items-center gap-3 text-sm">
                        <BookOpen className="h-4 w-4 text-neutral-400" />
                        <span className="text-neutral-600">Awarded to:</span>
                        <span className="font-medium text-neutral-900">{cert.userName}</span>
                      </div>
                      
                      <div className="flex items-center gap-3 text-sm">
                        <Calendar className="h-4 w-4 text-neutral-400" />
                        <span className="text-neutral-600">Issued:</span>
                        <span className="font-medium text-neutral-900">
                          {formatDate(cert.issuedAt)}
                        </span>
                      </div>
                      
                      <div className="bg-neutral-50 rounded-lg p-3">
                        <span className="text-xs text-neutral-500 uppercase tracking-wider">Certificate ID</span>
                        <p className="font-mono text-sm font-medium text-indigo-600 mt-1 select-all">
                          {cert.certificateNumber}
                        </p>
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="flex gap-2">
                      <a
                        href={cert.certificateUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-semibold text-sm"
                      >
                        <Download className="h-4 w-4" />
                        Download
                      </a>
                      <a
                        href={cert.certificateUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center justify-center gap-2 px-4 py-2.5 border-2 border-neutral-300 text-neutral-700 rounded-lg hover:border-indigo-400 hover:text-indigo-600 transition-colors"
                        title="Open in new tab"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </a>
                    </div>
                  </div>
                </Card>
              </motion.div>
            ))}
          </motion.div>
        ) : certificates.length > 0 ? (
          /* No search results */
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="text-center py-16"
          >
            <Search className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-neutral-900 mb-2">
              No certificates found
            </h3>
            <p className="text-neutral-600">
              Try adjusting your search term
            </p>
          </motion.div>
        ) : (
          /* Empty state */
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="text-center py-16"
          >
            <div className="w-24 h-24 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <Award className="h-12 w-12 text-indigo-600" />
            </div>
            <h3 className="text-2xl font-semibold text-neutral-900 mb-4">
              No Certificates Yet
            </h3>
            <p className="text-neutral-600 mb-8 max-w-md mx-auto">
              Complete your courses to earn certificates that showcase your achievements.
            </p>
            <Link to="/dashboard">
              <Button size="lg" className="inline-flex items-center space-x-2">
                <BookOpen className="h-5 w-5" />
                <span>Browse Courses</span>
              </Button>
            </Link>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default CertificatesPage;
