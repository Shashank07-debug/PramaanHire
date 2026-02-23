import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { toast } from 'react-toastify';
import {
    PencilIcon,
    ArrowLeftIcon,
    UserGroupIcon,
    DocumentTextIcon,
    LockClosedIcon,
    ArrowDownTrayIcon,
    PlusIcon,
    TrashIcon,
    MagnifyingGlassIcon,
    FunnelIcon,
    BoltIcon,
    ChevronLeftIcon,
    ChevronRightIcon
} from '@heroicons/react/24/outline';

const JobDetails = () => {
    const { jobId } = useParams();
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('overview');
    const [job, setJob] = useState(null);
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [hasApplications, setHasApplications] = useState(false);

    // Filter & Pagination State
    const [filters, setFilters] = useState({
        search: '',
        status: '',
        page: 0,
        size: 10,
        sort: 'aiScore,desc'
    });
    const [pagination, setPagination] = useState({
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true
    });

    // Bulk Action State
    const [showBulkModal, setShowBulkModal] = useState(false);
    const [topN, setTopN] = useState(5);
    const [bulkProcessing, setBulkProcessing] = useState(false);

    // Edit Form State
    const [editForm, setEditForm] = useState({});

    useEffect(() => {
        fetchJobDetails();
        // Fetch applications initially to get the count for the badge
        fetchApplications();
    }, [jobId]);

    // Fetch applications when filters change (but not on initial load as it's handled above)
    useEffect(() => {
        // Only fetch if we are already on the applications tab OR if filters changed
        // But to keep it simple and ensure data is fresh, we can just fetch.
        // However, to avoid double fetch on mount, we can rely on the first useEffect.
        // Let's just fetch when filters change.
        if (activeTab === 'applications') {
             fetchApplications();
        }
    }, [activeTab, filters.page, filters.size, filters.sort, filters.status, filters.search]);

    const fetchJobDetails = async () => {
        try {
            const response = await api.get(`/hr/jobs/${jobId}`);
            setJob(response.data);
            setEditForm(response.data);
        } catch (error) {
            toast.error("Failed to load job details");
            navigate('/hr/dashboard');
        }
    };

    const fetchApplications = async () => {
        // Don't set main loading to true if we are just refreshing the list in background or tab switch
        // But for initial load we might want to.
        // Let's keep it simple.
        try {
            const params = {
                page: filters.page,
                size: filters.size,
                sort: filters.sort,
                search: filters.search || undefined,
                status: filters.status || undefined
            };

            const response = await api.get(`/hr/jobs/${jobId}/applications`, { params });
            setApplications(response.data.content || []);
            setPagination({
                totalPages: response.data.totalPages,
                totalElements: response.data.totalElements,
                first: response.data.first,
                last: response.data.last
            });
            setHasApplications(response.data.totalElements > 0);
        } catch (error) {
            console.error("Failed to load applications");
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value, page: 0 });
    };

    const handlePageChange = (newPage) => {
        setFilters({ ...filters, page: newPage });
    };

    const handleBulkShortlist = async () => {
        if (!window.confirm(`This will shortlist the top ${topN} candidates by AI score and REJECT the rest. Are you sure?`)) return;

        setBulkProcessing(true);
        try {
            await api.post(`/hr/jobs/${jobId}/shortlist-top`, { topN: parseInt(topN) });
            toast.success("Bulk action completed successfully");
            setShowBulkModal(false);
            fetchApplications(); // Refresh list
        } catch (error) {
            toast.error(error.response?.data?.error || "Failed to perform bulk action");
        } finally {
            setBulkProcessing(false);
        }
    };

    // ... (Keep existing edit logic) ...
    const handleEditChange = (e) => {
        setEditForm({ ...editForm, [e.target.name]: e.target.value });
    };
    const handleQuestionChange = (index, field, value) => {
        const newQuestions = [...editForm.questions];
        newQuestions[index][field] = value;
        setEditForm({ ...editForm, questions: newQuestions });
    };
    const addQuestion = () => {
        setEditForm({
            ...editForm,
            questions: [...editForm.questions, { questionText: '', isMandatory: false, maxLength: 200, displayOrder: editForm.questions.length + 1 }]
        });
    };
    const removeQuestion = (index) => {
        const newQuestions = editForm.questions.filter((_, i) => i !== index);
        newQuestions.forEach((q, i) => q.displayOrder = i + 1);
        setEditForm({ ...editForm, questions: newQuestions });
    };
    const handleUpdateJob = async (e) => {
        e.preventDefault();
        try {
            const payload = { ...editForm };
            if (payload.applicationDeadline && payload.applicationDeadline.includes('T')) { }
            await api.put(`/hr/jobs/${jobId}`, payload);
            toast.success("Job updated successfully");
            setIsEditing(false);
            fetchJobDetails();
        } catch (error) {
            toast.error(error.response?.data?.error || "Update failed");
        }
    };

    const handleExportExcel = async () => {
        try {
            const response = await api.get(`/hr/jobs/${jobId}/applications/export`, {
                responseType: 'blob',
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `applications_job_${jobId}.xlsx`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            toast.error("Failed to export Excel");
        }
    };

    if (!job) return <div className="p-10 text-center">Loading...</div>;

    return (
        <div className="max-w-7xl mx-auto pb-10">
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4">
                <div className="flex items-center">
                    <button
                        onClick={() => navigate('/hr/dashboard')}
                        className="mr-4 p-2 rounded-full hover:bg-gray-100 text-gray-500"
                    >
                        <ArrowLeftIcon className="h-5 w-5" />
                    </button>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">{job.title}</h1>
                        <div className="flex items-center text-sm text-gray-500 mt-1">
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium mr-2 ${
                                job.status === 'OPEN' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                            }`}>
                                {job.status}
                            </span>
                            <span>Posted on {new Date(job.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                </div>

                <div className="flex space-x-3">
                    {activeTab === 'applications' && (
                        <>
                            <button
                                onClick={() => setShowBulkModal(true)}
                                className="inline-flex items-center px-4 py-2 border border-purple-300 shadow-sm text-sm font-medium rounded-md text-purple-700 bg-white hover:bg-purple-50"
                            >
                                <BoltIcon className="-ml-1 mr-2 h-5 w-5 text-purple-500" />
                                AI Shortlist
                            </button>
                            <button
                                onClick={handleExportExcel}
                                className="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                            >
                                <ArrowDownTrayIcon className="-ml-1 mr-2 h-5 w-5 text-gray-500" />
                                Export Excel
                            </button>
                        </>
                    )}
                    {activeTab === 'overview' && !isEditing && (
                        <button
                            onClick={() => setIsEditing(true)}
                            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                        >
                            <PencilIcon className="-ml-1 mr-2 h-5 w-5" />
                            Edit Job
                        </button>
                    )}
                </div>
            </div>

            {/* Tabs */}
            <div className="border-b border-gray-200 mb-6">
                <nav className="-mb-px flex space-x-8">
                    <button
                        onClick={() => setActiveTab('overview')}
                        className={`${
                            activeTab === 'overview'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                        } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center`}
                    >
                        <DocumentTextIcon className="h-5 w-5 mr-2" />
                        Overview & Settings
                    </button>
                    <button
                        onClick={() => setActiveTab('applications')}
                        className={`${
                            activeTab === 'applications'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                        } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center`}
                    >
                        <UserGroupIcon className="h-5 w-5 mr-2" />
                        Applications
                        <span className="ml-2 bg-gray-100 text-gray-600 py-0.5 px-2.5 rounded-full text-xs">
                            {pagination.totalElements}
                        </span>
                    </button>
                </nav>
            </div>

            {/* Content */}
            {activeTab === 'overview' ? (
                <div className="bg-white shadow rounded-lg p-6">
                    {isEditing ? (
                        <form onSubmit={handleUpdateJob} className="space-y-6">
                            {/* ... (Same Edit Form as before) ... */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-gray-700">Job Title</label>
                                    <input name="title" type="text" required className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2" value={editForm.title} onChange={handleEditChange} />
                                </div>
                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-gray-700">Description</label>
                                    <textarea name="description" rows={4} required className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2" value={editForm.description} onChange={handleEditChange} />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Location {hasApplications && <span className="ml-2 inline-flex items-center text-xs text-amber-600"><LockClosedIcon className="h-3 w-3 mr-1" />Locked</span>}</label>
                                    <input name="location" type="text" disabled={hasApplications} className={`mt-1 block w-full rounded-md shadow-sm sm:text-sm border p-2 ${hasApplications ? 'bg-gray-100 text-gray-500 border-gray-200 cursor-not-allowed' : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'}`} value={editForm.location} onChange={handleEditChange} />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Status</label>
                                    <select name="status" className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2 bg-white" value={editForm.status} onChange={handleEditChange}>
                                        <option value="OPEN">Open</option>
                                        <option value="CLOSED">Closed</option>
                                        <option value="ON_HOLD">On Hold</option>
                                    </select>
                                </div>
                            </div>
                            {!hasApplications ? (
                                <div className="border-t pt-6">
                                    <div className="flex justify-between items-center mb-4">
                                        <h3 className="text-lg font-medium text-gray-900">Edit Screening Questions</h3>
                                        <button type="button" onClick={addQuestion} className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200"><PlusIcon className="-ml-0.5 mr-2 h-4 w-4" />Add Question</button>
                                    </div>
                                    <div className="space-y-4">
                                        {editForm.questions && editForm.questions.map((q, index) => (
                                            <div key={index} className="bg-gray-50 p-4 rounded-lg border border-gray-200 relative">
                                                <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
                                                    <div className="md:col-span-8"><label className="block text-xs font-medium text-gray-500 mb-1">Question {index + 1}</label><input type="text" required className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border" value={q.questionText} onChange={(e) => handleQuestionChange(index, 'questionText', e.target.value)} /></div>
                                                    <div className="md:col-span-2"><label className="block text-xs font-medium text-gray-500 mb-1">Max Length</label><input type="number" className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border" value={q.maxLength} onChange={(e) => handleQuestionChange(index, 'maxLength', parseInt(e.target.value))} /></div>
                                                    <div className="md:col-span-2 flex items-center pt-5"><input type="checkbox" className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded" checked={q.isMandatory} onChange={(e) => handleQuestionChange(index, 'isMandatory', e.target.checked)} /><label className="ml-2 block text-sm text-gray-900">Mandatory</label></div>
                                                </div>
                                                <button type="button" onClick={() => removeQuestion(index)} className="absolute top-2 right-2 text-gray-400 hover:text-red-500 p-1"><TrashIcon className="h-5 w-5" /></button>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            ) : (
                                <div className="bg-amber-50 border-l-4 border-amber-400 p-4"><div className="flex"><div className="flex-shrink-0"><LockClosedIcon className="h-5 w-5 text-amber-400" aria-hidden="true" /></div><div className="ml-3"><p className="text-sm text-amber-700">Screening questions and location cannot be edited because candidates have already applied.</p></div></div></div>
                            )}
                            <div className="flex justify-end space-x-3 pt-4 border-t"><button type="button" onClick={() => { setIsEditing(false); setEditForm(job); }} className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50">Cancel</button><button type="submit" className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">Save Changes</button></div>
                        </form>
                    ) : (
                        <div className="space-y-6">
                            {/* ... (Same View Details as before) ... */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div><h4 className="text-sm font-medium text-gray-500">Description</h4><p className="mt-1 text-sm text-gray-900 whitespace-pre-wrap">{job.description}</p></div>
                                <div className="space-y-4">
                                    <div><h4 className="text-sm font-medium text-gray-500">Location</h4><p className="mt-1 text-sm text-gray-900">{job.location}</p></div>
                                    <div><h4 className="text-sm font-medium text-gray-500">Employment Type</h4><p className="mt-1 text-sm text-gray-900">{job.employmentType}</p></div>
                                    <div><h4 className="text-sm font-medium text-gray-500">Deadline</h4><p className="mt-1 text-sm text-gray-900">{job.applicationDeadline ? new Date(job.applicationDeadline).toLocaleDateString() : 'No Deadline'}</p></div>
                                </div>
                            </div>
                            <div className="border-t pt-6"><h4 className="text-sm font-medium text-gray-500 mb-4">Screening Questions</h4><ul className="space-y-3">{job.questions.map((q, i) => (<li key={i} className="bg-gray-50 p-3 rounded border border-gray-200 text-sm"><span className="font-medium text-gray-900">{i + 1}. {q.questionText}</span><div className="mt-1 flex space-x-4 text-xs text-gray-500">{q.isMandatory && <span className="text-red-600 font-medium">Mandatory</span>}<span>Max Length: {q.maxLength || 'Unlimited'}</span></div></li>))}</ul></div>
                        </div>
                    )}
                </div>
            ) : (
                <div className="space-y-4">
                    {/* Filters */}
                    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-col md:flex-row gap-4 items-center justify-between">
                        <div className="flex flex-1 gap-4 w-full">
                            <div className="relative flex-1">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    type="text"
                                    name="search"
                                    className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                    placeholder="Search candidates..."
                                    value={filters.search}
                                    onChange={handleFilterChange}
                                />
                            </div>
                            <div className="w-48">
                                <select
                                    name="status"
                                    className="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md border bg-white"
                                    value={filters.status}
                                    onChange={handleFilterChange}
                                >
                                    <option value="">All Statuses</option>
                                    <option value="SUBMITTED">Submitted</option>
                                    <option value="UNDER_REVIEW">Under Review</option>
                                    <option value="SHORTLISTED">Shortlisted</option>
                                    <option value="REJECTED">Rejected</option>
                                    <option value="HIRED">Hired</option>
                                </select>
                            </div>
                        </div>
                        <div className="flex items-center text-sm text-gray-500">
                            <FunnelIcon className="h-5 w-5 mr-1" />
                            Filters
                        </div>
                    </div>

                    {/* Applications Table */}
                    <div className="bg-white shadow rounded-lg overflow-hidden border border-gray-200">
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Candidate</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">AI Score</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Applied</th>
                                        <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                    {loading ? (
                                        <tr><td colSpan="5" className="px-6 py-10 text-center">Loading...</td></tr>
                                    ) : applications.length === 0 ? (
                                        <tr><td colSpan="5" className="px-6 py-10 text-center text-sm text-gray-500">No applications found matching your filters.</td></tr>
                                    ) : (
                                        applications.map((app) => (
                                            <tr key={app.applicationId} className="hover:bg-gray-50">
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="text-sm font-medium text-gray-900">{app.candidateName}</div>
                                                    <div className="text-sm text-gray-500">{app.candidateEmail}</div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                                        app.status === 'HIRED' ? 'bg-green-100 text-green-800' :
                                                        app.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                                                        app.status === 'SHORTLISTED' ? 'bg-indigo-100 text-indigo-800' :
                                                        'bg-blue-100 text-blue-800'
                                                    }`}>
                                                        {app.status}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="text-sm text-gray-900 font-bold">{app.aiScore || 'N/A'}</div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    {new Date(app.submittedAt).toLocaleDateString()}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                    <button
                                                        onClick={() => navigate(`/hr/applications/${app.applicationId}`)}
                                                        className="text-blue-600 hover:text-blue-900"
                                                    >
                                                        View Details
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                                <div>
                                    <p className="text-sm text-gray-700">
                                        Showing <span className="font-medium">{filters.page * filters.size + 1}</span> to <span className="font-medium">{Math.min((filters.page + 1) * filters.size, pagination.totalElements)}</span> of <span className="font-medium">{pagination.totalElements}</span> results
                                    </p>
                                </div>
                                <div>
                                    <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                        <button
                                            onClick={() => handlePageChange(filters.page - 1)}
                                            disabled={pagination.first}
                                            className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            <ChevronLeftIcon className="h-5 w-5" />
                                        </button>
                                        <button
                                            onClick={() => handlePageChange(filters.page + 1)}
                                            disabled={pagination.last}
                                            className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                                        >
                                            <ChevronRightIcon className="h-5 w-5" />
                                        </button>
                                    </nav>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Bulk Action Modal */}
            {showBulkModal && (
                <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
                        <h3 className="text-lg font-medium text-gray-900 mb-4">Auto-Shortlist Candidates</h3>
                        <p className="text-sm text-gray-500 mb-4">
                            This will automatically shortlist the top candidates based on AI score and REJECT the rest.
                        </p>
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">Top N Candidates</label>
                            <input
                                type="number"
                                min="1"
                                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 sm:text-sm border p-2"
                                value={topN}
                                onChange={(e) => setTopN(e.target.value)}
                            />
                        </div>
                        <div className="flex justify-end space-x-3">
                            <button
                                onClick={() => setShowBulkModal(false)}
                                className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleBulkShortlist}
                                disabled={bulkProcessing}
                                className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700 disabled:opacity-50"
                            >
                                {bulkProcessing ? 'Processing...' : 'Run Auto-Shortlist'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default JobDetails;
