import { useState, useEffect } from 'react';
import api from '../../api/axios';
import {
    DocumentTextIcon,
    ClockIcon,
    StarIcon,
    XCircleIcon,
    CheckBadgeIcon,
    ArrowTrendingUpIcon,
    SparklesIcon
} from '@heroicons/react/24/outline';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';

const CandidateDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async () => {
        try {
            const response = await api.get('/candidate/jobs/dashboard');
            setStats(response.data);
        } catch (error) {
            console.error("Failed to fetch dashboard stats", error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    if (!stats) return null;

    const lineData = Object.entries(stats.applicationsTrend || {}).map(([date, count]) => ({
        date,
        applications: count
    }));

    return (
        <div className="max-w-7xl mx-auto space-y-8">
            {/* 1. Welcome Banner */}
            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white shadow-lg">
                <h1 className="text-3xl font-bold">Welcome back! 👋</h1>
                <p className="mt-2 text-blue-100 text-lg">
                    Here’s a quick overview of your job journey. Keep applying and improving your profile!
                </p>
            </div>

            {/* 2. KPI Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
                <KpiCard title="Total Applied" value={stats.totalApplications} icon={DocumentTextIcon} color="text-blue-600" bg="bg-blue-50" />
                <KpiCard title="Under Review" value={stats.statusBreakdown?.UNDER_REVIEW || 0} icon={ClockIcon} color="text-yellow-600" bg="bg-yellow-50" />
                <KpiCard title="Shortlisted" value={stats.statusBreakdown?.SHORTLISTED || 0} icon={StarIcon} color="text-indigo-600" bg="bg-indigo-50" />
                <KpiCard title="Rejected" value={stats.statusBreakdown?.REJECTED || 0} icon={XCircleIcon} color="text-red-600" bg="bg-red-50" />
                <KpiCard title="Hired" value={stats.statusBreakdown?.HIRED || 0} icon={CheckBadgeIcon} color="text-green-600" bg="bg-green-50" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* 3. Application Trend */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 lg:col-span-2">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">Application Activity (30 Days)</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={lineData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="date" tick={{fontSize: 12}} />
                                <YAxis allowDecimals={false} />
                                <Tooltip />
                                <Line type="monotone" dataKey="applications" stroke="#3B82F6" strokeWidth={3} dot={{r: 4}} activeDot={{r: 6}} />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* 4. AI Snapshot */}
                <div className="space-y-6">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                            <ArrowTrendingUpIcon className="h-5 w-5 mr-2 text-blue-500" />
                            AI Performance
                        </h3>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                                <span className="text-gray-600">Average Score</span>
                                <span className="text-xl font-bold text-gray-900">{stats.averageAiScore}</span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
                                <span className="text-green-700">Highest Score</span>
                                <span className="text-xl font-bold text-green-700">{stats.highestAiScore}</span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                                <span className="text-blue-700">Latest Score</span>
                                <span className="text-xl font-bold text-blue-700">{stats.latestAiScore}</span>
                            </div>
                        </div>
                    </div>

                    {/* 5. Opportunity Suggestion */}
                    <div className="bg-gradient-to-br from-purple-500 to-indigo-600 p-6 rounded-xl shadow-md text-white">
                        <div className="flex items-start">
                            <SparklesIcon className="h-6 w-6 mr-3 mt-1 text-yellow-300" />
                            <div>
                                <h3 className="font-bold text-lg">Pro Tip</h3>
                                <p className="mt-2 text-purple-100 text-sm">
                                    Based on your recent scores, consider adding more quantitative results to your resume descriptions to boost your AI score!
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 6. Recent Updates */}
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Recent Updates</h3>
                <div className="space-y-4">
                    {stats.recentApplications.length === 0 ? (
                        <p className="text-sm text-gray-500 text-center py-4">No recent activity</p>
                    ) : (
                        stats.recentApplications.map((app) => (
                            <div key={app.applicationId} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors border border-gray-100">
                                <div>
                                    <p className="text-sm font-medium text-gray-900">
                                        Applied to <span className="text-blue-600">{app.jobTitle}</span>
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                        {app.location} • {new Date(app.submittedAt).toLocaleDateString()}
                                    </p>
                                </div>
                                <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                    app.status === 'HIRED' ? 'bg-green-100 text-green-800' :
                                    app.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                                    app.status === 'SHORTLISTED' ? 'bg-indigo-100 text-indigo-800' :
                                    'bg-blue-100 text-blue-800'
                                }`}>
                                    {app.status}
                                </span>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

const KpiCard = ({ title, value, icon: Icon, color, bg }) => (
    <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200 flex items-center space-x-3">
        <div className={`p-2 rounded-lg ${bg}`}>
            <Icon className={`h-5 w-5 ${color}`} />
        </div>
        <div>
            <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">{title}</p>
            <p className="text-xl font-bold text-gray-900">{value}</p>
        </div>
    </div>
);

export default CandidateDashboard;
