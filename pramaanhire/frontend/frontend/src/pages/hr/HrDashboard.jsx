import { useState, useEffect } from 'react';
import api from '../../api/axios';
import {
    BriefcaseIcon,
    DocumentTextIcon,
    StarIcon,
    CheckBadgeIcon,
    ClockIcon,
    XCircleIcon,
    ArrowTrendingUpIcon
} from '@heroicons/react/24/outline';
import {
    PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
    LineChart, Line, XAxis, YAxis, CartesianGrid
} from 'recharts';

const HrDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async () => {
        try {
            const response = await api.get('/hr/dashboard');
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

    // Data for Donut Chart
    const pieData = [
        { name: 'Submitted', value: stats.statusDistribution.Submitted, color: '#60A5FA' }, // Blue-400
        { name: 'Under Review', value: stats.statusDistribution['Under Review'], color: '#FBBF24' }, // Amber-400
        { name: 'Shortlisted', value: stats.statusDistribution.Shortlisted, color: '#818CF8' }, // Indigo-400
        { name: 'Hired', value: stats.statusDistribution.Hired, color: '#34D399' }, // Emerald-400
        { name: 'Rejected', value: stats.statusDistribution.Rejected, color: '#F87171' }, // Red-400
    ].filter(item => item.value > 0);

    // Data for Line Chart
    const lineData = Object.entries(stats.applicationsTrend).map(([date, count]) => ({
        date,
        applications: count
    }));

    return (
        <div className="max-w-7xl mx-auto space-y-8">
            {/* Header */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900">HR Overview</h1>
                <p className="text-gray-500">Welcome back! Here's what's happening with your hiring.</p>
            </div>

            {/* 1. KPI Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <KpiCard title="Open Jobs" value={stats.openJobsCount} icon={BriefcaseIcon} color="text-blue-600" bg="bg-blue-50" />
                <KpiCard title="Total Applications" value={stats.totalApplicationsCount} icon={DocumentTextIcon} color="text-purple-600" bg="bg-purple-50" />
                <KpiCard title="Shortlisted" value={stats.shortlistedCount} icon={StarIcon} color="text-indigo-600" bg="bg-indigo-50" />
                <KpiCard title="Hired Candidates" value={stats.hiredCount} icon={CheckBadgeIcon} color="text-green-600" bg="bg-green-50" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* 2. Status Distribution (Donut Chart) */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 lg:col-span-1">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">Application Status</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={pieData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="value"
                                >
                                    {pieData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend verticalAlign="bottom" height={36} />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* 3. Application Trend (Line Chart) */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 lg:col-span-2">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">Application Trend (30 Days)</h3>
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
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* 4. AI Snapshot */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 lg:col-span-2">
                    <h3 className="text-lg font-semibold text-gray-800 mb-6 flex items-center">
                        <ArrowTrendingUpIcon className="h-5 w-5 mr-2 text-blue-500" />
                        AI Evaluation Snapshot
                    </h3>
                    <div className="grid grid-cols-3 gap-4">
                        <div className="text-center p-4 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-500 mb-1">Average Score</p>
                            <p className="text-3xl font-bold text-gray-900">{stats.averageAiScore}</p>
                        </div>
                        <div className="text-center p-4 bg-green-50 rounded-lg">
                            <p className="text-sm text-green-600 mb-1">Highest Score</p>
                            <p className="text-3xl font-bold text-green-700">{stats.highestAiScore}</p>
                        </div>
                        <div className="text-center p-4 bg-red-50 rounded-lg">
                            <p className="text-sm text-red-600 mb-1">Lowest Score</p>
                            <p className="text-3xl font-bold text-red-700">{stats.lowestAiScore}</p>
                        </div>
                    </div>
                    <div className="mt-6 grid grid-cols-2 gap-4">
                        <div className="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
                            <div className="flex items-center">
                                <div className="h-2 w-2 rounded-full bg-yellow-400 mr-2"></div>
                                <span className="text-sm text-gray-600">Under Review</span>
                            </div>
                            <span className="font-semibold">{stats.underReviewCount}</span>
                        </div>
                        <div className="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
                            <div className="flex items-center">
                                <div className="h-2 w-2 rounded-full bg-red-400 mr-2"></div>
                                <span className="text-sm text-gray-600">Rejected</span>
                            </div>
                            <span className="font-semibold">{stats.rejectedCount}</span>
                        </div>
                    </div>
                </div>

                {/* 5. Recent Activity */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">Recent Activity</h3>
                    <div className="space-y-4">
                        {stats.recentActivities.length === 0 ? (
                            <p className="text-sm text-gray-500 text-center py-4">No recent activity</p>
                        ) : (
                            stats.recentActivities.map((activity, idx) => (
                                <div key={idx} className="flex items-start space-x-3">
                                    <div className="flex-shrink-0 mt-1">
                                        <div className="h-2 w-2 rounded-full bg-blue-500"></div>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-800">{activity.description}</p>
                                        <p className="text-xs text-gray-400 mt-0.5">{activity.timeAgo}</p>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

const KpiCard = ({ title, value, icon: Icon, color, bg }) => (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 flex items-center space-x-4">
        <div className={`p-3 rounded-lg ${bg}`}>
            <Icon className={`h-6 w-6 ${color}`} />
        </div>
        <div>
            <p className="text-sm font-medium text-gray-500">{title}</p>
            <p className="text-2xl font-bold text-gray-900">{value}</p>
        </div>
    </div>
);

export default HrDashboard;
