import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { toast } from 'react-toastify';
import {
    BriefcaseIcon,
    MapPinIcon,
    ClockIcon,
    ArrowLeftIcon,
    PaperClipIcon,
    CheckCircleIcon
} from '@heroicons/react/24/outline';

const JobView = () => {
    const { jobId } = useParams();
    const navigate = useNavigate();
    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [applying, setApplying] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    // Application Form State
    const [resume, setResume] = useState(null);
    const [answers, setAnswers] = useState({}); // Map: questionId -> answerText

    useEffect(() => {
        fetchJobDetails();
    }, [jobId]);

    const fetchJobDetails = async () => {
        try {
            // First fetch public details (no questions)
            const response = await api.get(`/jobs/${jobId}`);
            setJob(response.data);
        } catch (error) {
            toast.error("Failed to load job details");
            navigate('/candidate/jobs');
        } finally {
            setLoading(false);
        }
    };

    const handleStartApplication = async () => {
        if (job.hasApplied) {
            toast.info("You have already applied for this job.");
            return;
        }
        setApplying(true);
        try {
            // Fetch full details WITH questions (requires auth)
            const response = await api.get(`/candidate/jobs/${jobId}/prepare-application`);
            setJob(response.data); // Update job with questions
        } catch (error) {
            toast.error("Please login to apply");
            navigate('/login');
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file && file.type !== 'application/pdf') {
            toast.error("Only PDF files are allowed");
            return;
        }
        setResume(file);
    };

    const handleAnswerChange = (questionId, text) => {
        setAnswers({ ...answers, [questionId]: text });
    };

    const handleSubmitApplication = async (e) => {
        e.preventDefault();
        if (!resume) {
            toast.error("Please upload your resume");
            return;
        }

        setSubmitting(true);

        // Prepare FormData
        const formData = new FormData();
        formData.append('resume', resume);

        // Convert answers map to list format expected by backend
        const answersList = job.questions.map(q => ({
            questionId: q.questionId,
            answerText: answers[q.questionId] || ''
        }));

        formData.append('answers', JSON.stringify(answersList));

        try {
            const response = await api.post(`/candidate/jobs/${jobId}/apply`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            toast.success("Application submitted successfully!");
            // Navigate to the specific application details page using the ID returned from submission
            navigate(`/candidate/my-applications/${response.data.applicationId}`);
        } catch (error) {
            toast.error(error.response?.data?.error || error.response?.data?.message || "Failed to submit application");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="p-10 text-center">Loading...</div>;
    if (!job) return null;

    return (
        <div className="max-w-4xl mx-auto pb-10">
            <button
                onClick={() => navigate('/candidate/jobs')}
                className="flex items-center text-gray-600 hover:text-blue-600 mb-6 transition-colors"
            >
                <ArrowLeftIcon className="h-5 w-5 mr-2" />
                Back to Jobs
            </button>

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                {/* Job Header */}
                <div className="p-8 border-b border-gray-200 bg-gray-50">
                    <div className="flex justify-between items-start">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">{job.title}</h1>
                            <div className="mt-4 flex flex-wrap gap-4 text-sm text-gray-600">
                                <div className="flex items-center">
                                    <BriefcaseIcon className="h-5 w-5 mr-1.5 text-gray-400" />
                                    {job.employmentType.replace('_', ' ')}
                                </div>
                                <div className="flex items-center">
                                    <MapPinIcon className="h-5 w-5 mr-1.5 text-gray-400" />
                                    {job.location || 'Remote'}
                                </div>
                                <div className="flex items-center">
                                    <ClockIcon className="h-5 w-5 mr-1.5 text-gray-400" />
                                    Posted {new Date(job.createdAt).toLocaleDateString()}
                                </div>
                            </div>
                        </div>

                        {job.hasApplied ? (
                            <div className="flex flex-col items-end">
                                <span className="inline-flex items-center px-4 py-2 rounded-md text-sm font-medium bg-green-100 text-green-800 border border-green-200">
                                    <CheckCircleIcon className="h-5 w-5 mr-2" />
                                    Applied
                                </span>
                                <button
                                    onClick={() => navigate(`/candidate/my-applications/${job.applicationId}`)}
                                    className="mt-2 text-sm text-blue-600 hover:underline"
                                >
                                    View Status
                                </button>
                            </div>
                        ) : (
                            !applying && (
                                <button
                                    onClick={handleStartApplication}
                                    className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                >
                                    Apply Now
                                </button>
                            )
                        )}
                    </div>
                </div>

                {/* Job Description */}
                <div className="p-8">
                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Job Description</h3>
                    <div className="prose max-w-none text-gray-600 whitespace-pre-wrap">
                        {job.description}
                    </div>
                </div>

                {/* Application Form */}
                {applying && !job.hasApplied && (
                    <div className="border-t border-gray-200 bg-blue-50 p-8">
                        <h3 className="text-xl font-bold text-gray-900 mb-6">Complete Your Application</h3>
                        <form onSubmit={handleSubmitApplication} className="space-y-8">

                            {/* Resume Upload */}
                            <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm">
                                <label className="block text-sm font-medium text-gray-700 mb-2">Upload Resume (PDF)</label>
                                <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md hover:border-blue-400 transition-colors">
                                    <div className="space-y-1 text-center">
                                        <PaperClipIcon className="mx-auto h-12 w-12 text-gray-400" />
                                        <div className="flex text-sm text-gray-600">
                                            <label htmlFor="file-upload" className="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                                                <span>Upload a file</span>
                                                <input id="file-upload" name="file-upload" type="file" className="sr-only" accept="application/pdf" onChange={handleFileChange} />
                                            </label>
                                            <p className="pl-1">or drag and drop</p>
                                        </div>
                                        <p className="text-xs text-gray-500">PDF up to 5MB</p>
                                        {resume && (
                                            <p className="text-sm text-green-600 font-medium mt-2 flex items-center justify-center">
                                                <CheckCircleIcon className="h-4 w-4 mr-1" />
                                                {resume.name}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Screening Questions */}
                            {job.questions && job.questions.length > 0 && (
                                <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm space-y-6">
                                    <h4 className="text-lg font-medium text-gray-900 border-b pb-2">Screening Questions</h4>
                                    {job.questions.map((q) => (
                                        <div key={q.questionId}>
                                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                                {q.questionText} {q.isMandatory && <span className="text-red-500">*</span>}
                                            </label>
                                            <textarea
                                                rows={3}
                                                required={q.isMandatory}
                                                maxLength={q.maxLength}
                                                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md border p-2"
                                                placeholder="Type your answer here..."
                                                value={answers[q.questionId] || ''}
                                                onChange={(e) => handleAnswerChange(q.questionId, e.target.value)}
                                            />
                                            {q.maxLength && (
                                                <p className="text-xs text-gray-500 mt-1 text-right">
                                                    {(answers[q.questionId] || '').length}/{q.maxLength} characters
                                                </p>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}

                            <div className="flex justify-end">
                                <button
                                    type="button"
                                    onClick={() => setApplying(false)}
                                    className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 mr-3"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="inline-flex justify-center py-2 px-6 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                                >
                                    {submitting ? 'Submitting...' : 'Submit Application'}
                                </button>
                            </div>
                        </form>
                    </div>
                )}
            </div>
        </div>
    );
};

export default JobView;
