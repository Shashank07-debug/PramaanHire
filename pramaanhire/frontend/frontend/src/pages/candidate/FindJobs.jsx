import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import {
    MagnifyingGlassIcon,
    MapPinIcon,
    BriefcaseIcon,
    ClockIcon,
    FunnelIcon,
    ChevronLeftIcon,
    ChevronRightIcon,
    CheckCircleIcon
} from '@heroicons/react/24/outline';

const FindJobs = () => {
    const navigate = useNavigate();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);

    // Filters
    const [filters, setFilters] = useState({
        title: '',
        location: '',
        datePosted: '',
        page: 0,
        size: 9
    });

    const [pagination, setPagination] = useState({
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true
    });

    useEffect(() => {
        fetchJobs();
    }, [filters.page, filters.title, filters.location, filters.datePosted]);

    const fetchJobs = async () => {
        setLoading(true);
        try {
            const params = {
                page: filters.page,
                size: filters.size,
                title: filters.title || undefined,
                location: filters.location || undefined,
                datePosted: filters.datePosted || undefined
            };

            // Use the public endpoint
            const response = await api.get('/jobs', { params });
            setJobs(response.data.content || []);
            setPagination({
                totalPages: response.data.totalPages,
                totalElements: response.data.totalElements,
                first: response.data.first,
                last: response.data.last
            });
        } catch (error) {
            console.error("Failed to fetch jobs");
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

    return (
        <div className="max-w-7xl mx-auto space-y-8">
            {/* Header */}
            <div className="text-center max-w-2xl mx-auto">
                <h1 className="text-3xl font-bold text-gray-900">Find Your Dream Job</h1>
                <p className="text-gray-500 mt-2">Browse hundreds of open positions and apply with AI-powered feedback.</p>
            </div>

            {/* Search & Filters */}
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div className="md:col-span-2 relative">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
                        </div>
                        <input
                            type="text"
                            name="title"
                            className="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm transition duration-150"
                            placeholder="Search by job title, skill, or keyword..."
                            value={filters.title}
                            onChange={handleFilterChange}
                        />
                    </div>
                    <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <MapPinIcon className="h-5 w-5 text-gray-400" />
                        </div>
                        <input
                            type="text"
                            name="location"
                            className="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm transition duration-150"
                            placeholder="Location (e.g. Remote)"
                            value={filters.location}
                            onChange={handleFilterChange}
                        />
                    </div>
                    <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <ClockIcon className="h-5 w-5 text-gray-400" />
                        </div>
                        <select
                            name="datePosted"
                            className="block w-full pl-10 pr-10 py-2.5 border border-gray-300 rounded-lg leading-5 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm appearance-none transition duration-150"
                            value={filters.datePosted}
                            onChange={handleFilterChange}
                        >
                            <option value="">Any Time</option>
                            <option value="24h">Last 24 Hours</option>
                            <option value="7d">Last 7 Days</option>
                            <option value="30d">Last 30 Days</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Job Grid */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                </div>
            ) : jobs.length === 0 ? (
                <div className="text-center py-20 bg-white rounded-xl border border-gray-200 border-dashed">
                    <BriefcaseIcon className="mx-auto h-12 w-12 text-gray-300" />
                    <h3 className="mt-2 text-sm font-medium text-gray-900">No jobs found</h3>
                    <p className="mt-1 text-sm text-gray-500">Try adjusting your search or filters.</p>
                    <button
                        onClick={() => setFilters({ title: '', location: '', datePosted: '', page: 0, size: 9 })}
                        className="mt-6 inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                    >
                        Clear Filters
                    </button>
                </div>
            ) : (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {jobs.map((job) => (
                        <div key={job.id} className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200 flex flex-col h-full group relative">
                            <div className="p-6 flex-1">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
                                        <BriefcaseIcon className="h-6 w-6" />
                                    </div>
                                    {job.hasApplied && (
                                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 border border-green-200">
                                            <CheckCircleIcon className="h-3 w-3 mr-1" />
                                            Applied
                                        </span>
                                    )}
                                </div>

                                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 mb-3">
                                    {job.employmentType.replace('_', ' ')}
                                </span>

                                <h3 className="text-lg font-bold text-gray-900 group-hover:text-blue-600 transition-colors">
                                    {job.title}
                                </h3>
                                <div className="mt-2 flex items-center text-sm text-gray-500">
                                    <MapPinIcon className="flex-shrink-0 mr-1.5 h-4 w-4 text-gray-400" />
                                    {job.location || 'Remote'}
                                </div>
                                <div className="mt-4 flex items-center text-xs text-gray-500">
                                    <ClockIcon className="flex-shrink-0 mr-1.5 h-3.5 w-3.5 text-gray-400" />
                                    Posted {new Date(job.createdAt).toLocaleDateString()}
                                </div>
                            </div>
                            <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 rounded-b-xl">
                                <button
                                    onClick={() => navigate(`/candidate/jobs/${job.id}`)}
                                    className={`w-full inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors ${
                                        job.hasApplied
                                            ? 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500'
                                            : 'text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'
                                    }`}
                                >
                                    {job.hasApplied ? 'View Application' : 'View Details'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            {jobs.length > 0 && (
                <div className="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6 rounded-lg shadow-sm">
                    <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm text-gray-700">
                                Showing <span className="font-medium">{filters.page * filters.size + 1}</span> to <span className="font-medium">{Math.min((filters.page + 1) * filters.size, pagination.totalElements)}</span> of <span className="font-medium">{pagination.totalElements}</span> results
                            </p>
                        </div>
                        <div>
                            <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                                <button
                                    onClick={() => handlePageChange(filters.page - 1)}
                                    disabled={pagination.first}
                                    className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                                >
                                    <span className="sr-only">Previous</span>
                                    <ChevronLeftIcon className="h-5 w-5" aria-hidden="true" />
                                </button>
                                <button
                                    onClick={() => handlePageChange(filters.page + 1)}
                                    disabled={pagination.last}
                                    className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                                >
                                    <span className="sr-only">Next</span>
                                    <ChevronRightIcon className="h-5 w-5" aria-hidden="true" />
                                </button>
                            </nav>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FindJobs;
