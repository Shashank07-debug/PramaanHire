import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { toast } from 'react-toastify';
import {
    ArrowLeftIcon,
    DocumentArrowDownIcon,
    CheckCircleIcon,
    XCircleIcon,
    UserIcon,
    ChatBubbleLeftRightIcon,
    LightBulbIcon
} from '@heroicons/react/24/outline';

const ApplicationDetails = () => {
    const { applicationId } = useParams();
    const navigate = useNavigate();
    const [app, setApp] = useState(null);
    const [actions, setActions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [notes, setNotes] = useState('');
    const [processing, setProcessing] = useState(false);

    useEffect(() => {
        fetchData();
    }, [applicationId]);

    const fetchData = async () => {
        try {
            const [appRes, actionsRes] = await Promise.all([
                api.get(`/hr/applications/${applicationId}`),
                api.get(`/hr/applications/${applicationId}/actions`)
            ]);
            setApp(appRes.data);
            setActions(actionsRes.data.allowedTransitions);
            setNotes(appRes.data.hrNotes || '');
        } catch (error) {
            toast.error("Failed to load application details");
            navigate('/hr/dashboard');
        } finally {
            setLoading(false);
        }
    };

    const handleStatusUpdate = async (newStatus) => {
        if (!window.confirm(`Are you sure you want to mark this candidate as ${newStatus}?`)) return;

        setProcessing(true);
        try {
            await api.patch(`/hr/applications/${applicationId}/status`, {
                status: newStatus,
                hrNotes: notes
            });
            toast.success(`Status updated to ${newStatus}`);
            fetchData(); // Refresh
        } catch (error) {
            toast.error("Failed to update status");
        } finally {
            setProcessing(false);
        }
    };

    if (loading) return <div className="p-10 text-center">Loading...</div>;
    if (!app) return null;

    return (
        <div className="max-w-5xl mx-auto pb-10">
            {/* Header */}
            <div className="mb-6">
                <button
                    onClick={() => navigate(`/hr/jobs/${app.jobId}`)}
                    className="flex items-center text-gray-500 hover:text-gray-700 mb-4"
                >
                    <ArrowLeftIcon className="h-4 w-4 mr-1" />
                    Back to Job
                </button>
                <div className="flex justify-between items-start">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">{app.candidateName}</h1>
                        <p className="text-gray-500 mt-1">Applied for <span className="font-medium text-gray-700">{app.jobTitle}</span></p>
                        <div className="mt-2 flex items-center space-x-4">
                            <a
                                href={app.candidateEmail ? `mailto:${app.candidateEmail}` : '#'}
                                className="text-sm text-blue-600 hover:underline"
                            >
                                {app.candidateEmail}
                            </a>
                            <span className="text-gray-300">|</span>
                            <span className="text-sm text-gray-500">Submitted: {new Date(app.submittedAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                    <div className="text-right">
                        <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                            app.status === 'HIRED' ? 'bg-green-100 text-green-800' :
                            app.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                            app.status === 'SHORTLISTED' ? 'bg-indigo-100 text-indigo-800' :
                            'bg-blue-100 text-blue-800'
                        }`}>
                            {app.status}
                        </div>
                        {app.aiScore && (
                            <div className="mt-2 text-sm font-bold text-gray-700">
                                AI Score: <span className="text-blue-600 text-lg">{app.aiScore}</span>/100
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Column: AI & Q&A */}
                <div className="lg:col-span-2 space-y-8">
                    {/* AI Evaluation */}
                    <div className="bg-white shadow rounded-xl overflow-hidden border border-gray-200">
                        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4 border-b border-gray-200 flex items-center">
                            <LightBulbIcon className="h-5 w-5 text-blue-600 mr-2" />
                            <h2 className="text-lg font-semibold text-gray-800">AI Evaluation</h2>
                        </div>
                        <div className="p-6 space-y-6">
                            <div>
                                <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wider mb-2">Summary</h3>
                                <p className="text-gray-800 leading-relaxed">{app.aiSummary || "No summary available."}</p>
                            </div>

                            {app.aiEvaluation && (
                                <>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div className="bg-green-50 p-4 rounded-lg border border-green-100">
                                            <h3 className="text-sm font-bold text-green-800 mb-2 flex items-center">
                                                <CheckCircleIcon className="h-4 w-4 mr-1" /> Strengths
                                            </h3>
                                            <p className="text-sm text-green-900">{app.aiEvaluation.strengths}</p>
                                        </div>
                                        <div className="bg-red-50 p-4 rounded-lg border border-red-100">
                                            <h3 className="text-sm font-bold text-red-800 mb-2 flex items-center">
                                                <XCircleIcon className="h-4 w-4 mr-1" /> Weaknesses
                                            </h3>
                                            <p className="text-sm text-red-900">{app.aiEvaluation.weaknesses}</p>
                                        </div>
                                    </div>
                                    <div className="bg-yellow-50 p-4 rounded-lg border border-yellow-100">
                                        <h3 className="text-sm font-bold text-yellow-800 mb-2">💡 Improvement Tips</h3>
                                        <p className="text-sm text-yellow-900">{app.aiEvaluation.improvementTips}</p>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>

                    {/* Screening Questions */}
                    <div className="bg-white shadow rounded-xl overflow-hidden border border-gray-200">
                        <div className="px-6 py-4 border-b border-gray-200 flex items-center bg-gray-50">
                            <ChatBubbleLeftRightIcon className="h-5 w-5 text-gray-500 mr-2" />
                            <h2 className="text-lg font-semibold text-gray-800">Screening Responses</h2>
                        </div>
                        <div className="divide-y divide-gray-100">
                            {app.answers.map((ans, idx) => (
                                <div key={idx} className="p-6">
                                    <p className="text-sm font-medium text-gray-500 mb-2">Q: {ans.questionText}</p>
                                    <p className="text-gray-900 bg-gray-50 p-3 rounded-lg border border-gray-100">{ans.answerText}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Right Column: Actions & Resume */}
                <div className="space-y-6">
                    {/* Resume */}
                    <div className="bg-white shadow rounded-xl p-6 border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4">Resume</h3>
                        <a
                            href={app.resumeUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center justify-center w-full px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                        >
                            <DocumentArrowDownIcon className="h-5 w-5 mr-2 text-gray-500" />
                            Download PDF
                        </a>
                    </div>

                    {/* Actions */}
                    <div className="bg-white shadow rounded-xl p-6 border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4">Take Action</h3>

                        <div className="space-y-3">
                            {actions.map(status => (
                                <button
                                    key={status}
                                    onClick={() => handleStatusUpdate(status)}
                                    disabled={processing}
                                    className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 ${
                                        status === 'REJECTED' ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' :
                                        status === 'HIRED' ? 'bg-green-600 hover:bg-green-700 focus:ring-green-500' :
                                        'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'
                                    }`}
                                >
                                    Mark as {status.replace('_', ' ')}
                                </button>
                            ))}

                            {actions.length === 0 && (
                                <p className="text-sm text-gray-500 text-center">No further actions available.</p>
                            )}
                        </div>

                        <div className="mt-6">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Internal HR Notes</label>
                            <textarea
                                rows={4}
                                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md border p-2"
                                placeholder="Add private notes here..."
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                            />
                            {/* We could add a "Save Notes" button, but currently notes are saved with status update.
                                Ideally, we should have a separate save button or auto-save.
                                For MVP, let's just save it when status changes. */}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ApplicationDetails;
