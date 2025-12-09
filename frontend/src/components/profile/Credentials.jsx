import { useState, useEffect } from 'react';
import { useUser } from '@clerk/clerk-react';
import { certificatesAPI } from '../../lib/api';
import { Award, Download, ExternalLink, Calendar, BookOpen, CheckCircle2 } from 'lucide-react';
import toast from 'react-hot-toast';

const Credentials = () => {
  const { user } = useUser();
  const [certificates, setCertificates] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCertificates = async () => {
      if (!user?.id) return;
      
      try {
        const response = await certificatesAPI.getUserCertificates(user.id);
        setCertificates(response.data || []);
      } catch (error) {
        console.error('Failed to fetch certificates:', error);
        toast.error('Failed to load certificates');
      } finally {
        setLoading(false);
      }
    };

    fetchCertificates();
  }, [user]);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const getCourseColor = (courseName) => {
    const lowerName = courseName.toLowerCase();
    if (lowerName.includes('web') || lowerName.includes('html') || lowerName.includes('css')) {
      return 'bg-gradient-to-br from-indigo-500 to-purple-600';
    }
    if (lowerName.includes('java') || lowerName.includes('spring')) {
      return 'bg-gradient-to-br from-green-500 to-teal-600';
    }
    if (lowerName.includes('python') || lowerName.includes('data')) {
      return 'bg-gradient-to-br from-purple-500 to-orange-600';
    }
    if (lowerName.includes('react') || lowerName.includes('node')) {
      return 'bg-gradient-to-br from-cyan-500 to-blue-600';
    }
    if (lowerName.includes('cloud') || lowerName.includes('aws')) {
      return 'bg-gradient-to-br from-sky-500 to-indigo-600';
    }
    return 'bg-gradient-to-br from-indigo-500 to-purple-600';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (certificates.length === 0) {
    return (
      <div className="text-center py-12">
        <Award className="mx-auto h-16 w-16 text-gray-400 mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">No Certificates Yet</h3>
        <p className="text-gray-600 max-w-md mx-auto">
          Complete courses to earn certificates and showcase your achievements. 
          Start learning today and build your credential portfolio!
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">My Credentials</h2>
          <p className="text-gray-600 mt-1">
            {certificates.length} {certificates.length === 1 ? 'Certificate' : 'Certificates'} Earned
          </p>
        </div>
        <div className="flex items-center gap-2 text-green-600">
          <CheckCircle2 className="w-5 h-5" />
          <span className="font-semibold">Verified by LearnNow</span>
        </div>
      </div>

      {/* Certificates Grid */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {certificates.map((cert) => (
          <div
            key={cert.id}
            className="group relative bg-white rounded-xl border-2 border-gray-200 overflow-hidden hover:border-indigo-400 hover:shadow-xl transition-all duration-300"
          >
            {/* Header with gradient */}
            <div className={`${getCourseColor(cert.courseName)} p-6 text-white relative overflow-hidden`}>
              {/* Background pattern */}
              <div className="absolute inset-0 opacity-10">
                <div className="absolute inset-0" style={{
                  backgroundImage: 'radial-gradient(circle at 20px 20px, white 1px, transparent 1px)',
                  backgroundSize: '40px 40px'
                }}></div>
              </div>
              
              <div className="relative z-10">
                <Award className="w-10 h-10 mb-3" />
                <h3 className="text-lg font-bold line-clamp-2">{cert.courseName}</h3>
              </div>
            </div>

            {/* Certificate Details */}
            <div className="p-6 space-y-4">
              {/* Certificate ID */}
              <div className="bg-gray-50 rounded-lg p-3 border border-gray-200">
                <div className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">
                  Certificate ID
                </div>
                <div className="font-mono text-sm font-bold text-gray-900 select-all">
                  {cert.certificateNumber}
                </div>
              </div>

              {/* Issue Date */}
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <Calendar className="w-4 h-4" />
                <span>Issued {formatDate(cert.issuedAt)}</span>
              </div>

              {/* Completion Date */}
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <BookOpen className="w-4 h-4" />
                <span>Completed {formatDate(cert.completionDate)}</span>
              </div>

              {/* Actions */}
              <div className="flex gap-2 pt-2">
                <a
                  href={cert.certificateUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-semibold text-sm"
                >
                  <Download className="w-4 h-4" />
                  Download
                </a>
                <a
                  href={cert.certificateUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-4 py-2.5 border-2 border-gray-300 text-gray-700 rounded-lg hover:border-indigo-400 hover:text-indigo-600 transition-colors"
                >
                  <ExternalLink className="w-4 h-4" />
                </a>
              </div>
            </div>

            {/* Verified Badge */}
            <div className="absolute top-4 right-4 bg-white/90 backdrop-blur-sm rounded-full p-2 shadow-lg">
              <CheckCircle2 className="w-5 h-5 text-green-600" />
            </div>
          </div>
        ))}
      </div>

      {/* Share Section */}
      <div className="mt-8 p-6 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-xl border border-indigo-100">
        <h3 className="text-lg font-semibold text-gray-900 mb-2">Share Your Achievements</h3>
        <p className="text-gray-600 text-sm mb-4">
          Add your certificates to LinkedIn to showcase your skills to employers and your professional network.
        </p>
        <button
          onClick={() => {
            window.open(
              'https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME',
              '_blank',
              'noopener,noreferrer'
            );
            toast.success('Opening LinkedIn. Add your LearnNow certificate!');
          }}
          className="inline-flex items-center gap-2 px-4 py-2 bg-[#0077B5] text-white rounded-lg hover:bg-[#006399] transition-colors font-semibold text-sm"
        >
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
            <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
          </svg>
          Add to LinkedIn
        </button>
      </div>
    </div>
  );
};

export default Credentials;
