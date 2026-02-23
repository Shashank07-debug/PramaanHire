import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { toast } from 'react-toastify';
import {
    ArrowLeftIcon,
    DocumentTextIcon,
    ChatBubbleLeftRightIcon,
    XCircleIcon,
    LightBulbIcon,
    CheckCircleIcon
} from '@heroicons/react/24/outline';

const ApplicationView = () => {
    const { applicationId } = useParams();
    const navigate = useNavigate();
    const [app, setApp] = useState(null);
    const [loading, setLoading] = useState(true);
    const [withdrawing, setWithdrawing] = useState(false);

    useEffect(() => {
        fetchApplicationDetails();
    }, [applicationId]);

    const fetchApplicationDetails = async () => {
        try {
            const response = await api.get(`/candidate/jobs/my-applications/${applicationId}`);
            setApp(response.data);
        } catch (error) {
            toast.error("Failed to load application details");
            navigate('/candidate/my-applications');
        } finally {
            setLoading(false);
        }
    };

    const handleWithdraw = async () => {
        if (!window.confirm("Are you sure you want to withdraw this application? This action cannot be undone.")) return;

        setWithdrawing(true);
        try {
            await api.post(`/candidate/jobs/my-applications/${applicationId}/withdraw`);
            toast.success("Application withdrawn successfully");
            fetchApplicationDetails(); // Refresh to show updated status
        } catch (error) {
            toast.error(error.response?.data?.error || "Failed to withdraw application");
        } finally {
            setWithdrawing(false);
        }
    };

    if (loading) return <div className="p-10 text-center">Loading...</div>;
    if (!app) return null;

    return (
        <div className="max-w-4xl mx-auto pb-10">
            <button
                onClick={() => navigate('/candidate/my-applications')}
                className="flex items-center text-gray-600 hover:text-blue-600 mb-6 transition-colors"
            >
                <ArrowLeftIcon className="h-5 w-5 mr-2" />
                Back to Applications
            </button>

            <div className="bg-white shadow rounded-xl overflow-hidden border border-gray-200">
                {/* Header */}
                <div className="px-6 py-5 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
                    <div>
                        <h1 className="text-xl font-bold text-gray-900">{app.jobTitle}</h1>
                        <p className="text-sm text-gray-500 mt-1">Submitted on {new Date(app.submittedAt).toLocaleDateString()}</p>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                        app.status === 'HIRED' ? 'bg-green-100 text-green-800' :
                        app.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                        app.status === 'SHORTLISTED' ? 'bg-indigo-100 text-indigo-800' :
                        app.status === 'WITHDRAWN' ? 'bg-gray-100 text-gray-800' :
                        'bg-yellow-100 text-yellow-800'
                    }`}>
                        {app.status.replace('_', ' ')}
                    </span>
                </div>

                <div className="p-6 space-y-8">
                    {/* AI Feedback (Visible if Rejected or if feedback exists) */}
                    {app.aiEvaluation && (app.status === 'REJECTED' || app.status === 'HIRED' || app.status === 'SHORTLISTED') && (
                        <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-6 border border-blue-100">
                            <div className="flex items-center mb-4">
                                <LightBulbIcon className="h-6 w-6 text-blue-600 mr-2" />
                                <h3 className="text-lg font-bold text-gray-800">AI Feedback & Insights</h3>
                            </div>

                            <div className="space-y-4">
                                {app.aiEvaluation.strengths && (
                                    <div className="bg-white p-4 rounded-lg border border-green-100 shadow-sm">
                                        <h4 className="text-sm font-bold text-green-700 mb-1 flex items-center">
                                            <CheckCircleIcon className="h-4 w-4 mr-1" /> Strengths
                                        </h4>
                                        <p className="text-sm text-gray-700">{app.aiEvaluation.strengths}</p>
                                    </div>
                                )}

                                {app.aiEvaluation.weaknesses && (
                                    <div className="bg-white p-4 rounded-lg border border-red-100 shadow-sm">
                                        <h4 className="text-sm font-bold text-red-700 mb-1 flex items-center">
                                            <XCircleIcon className="h-4 w-4 mr-1" /> Areas for Improvement
                                        </h4>
                                        <p className="text-sm text-gray-700">{app.aiEvaluation.weaknesses}</p>
                                    </div>
                                )}

                                {app.aiEvaluation.improvementTips && (
                                    <div className="bg-white p-4 rounded-lg border border-yellow-100 shadow-sm">
                                        <h4 className="text-sm font-bold text-yellow-700 mb-1 flex items-center">
                                            <LightBulbIcon className="h-4 w-4 mr-1" /> Tips for Growth
                                        </h4>
                                        <p className="text-sm text-gray-700">{app.aiEvaluation.improvementTips}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Job Description Preview */}
                    <div>
                        <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wider mb-2">Job Description</h3>
                        <p className="text-gray-800 text-sm line-clamp-3">{app.jobDescription}</p>
                    </div>

                    {/* Resume */}
                    <div>
                        <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wider mb-2">Submitted Resume</h3>
                        <a
                            href={app.resumeUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                        >
                            <DocumentTextIcon className="h-5 w-5 mr-2 text-gray-500" />
                            View Resume
                        </a>
                    </div>

                    {/* Answers */}
                    <div>
                        <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wider mb-4">Your Responses</h3>
                        <div className="space-y-4">
                            {app.answers.map((ans, idx) => (
                                <div key={idx} className="bg-gray-50 p-4 rounded-lg border border-gray-100">
                                    <p className="text-sm font-medium text-gray-700 mb-2">{ans.questionText}</p>
                                    <p className="text-sm text-gray-600">{ans.answerText}</p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Withdraw Action */}
                    {app.status === 'SUBMITTED' && (
                        <div className="border-t pt-6">
                            <button
                                onClick={handleWithdraw}
                                disabled={withdrawing}
                                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50"
                            >
                                <XCircleIcon className="-ml-1 mr-2 h-5 w-5" />
                                {withdrawing ? 'Withdrawing...' : 'Withdraw Application'}
                            </button>
                            <p className="mt-2 text-xs text-gray-500">
                                You can only withdraw your application while it is still in the "Submitted" stage.
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ApplicationView;
