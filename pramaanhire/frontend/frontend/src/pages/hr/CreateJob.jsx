import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import { toast } from 'react-toastify';
import {
    PlusIcon,
    TrashIcon,
    ArrowLeftIcon
} from '@heroicons/react/24/outline';

const CreateJob = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        location: '',
        employmentType: 'FULL_TIME',
        status: 'OPEN',
        applicationDeadline: '',
        questions: [
            { questionText: 'Tell us about yourself.', isMandatory: true, maxLength: 500, displayOrder: 1 }
        ]
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleQuestionChange = (index, field, value) => {
        const newQuestions = [...formData.questions];
        newQuestions[index][field] = value;
        setFormData({ ...formData, questions: newQuestions });
    };

    const addQuestion = () => {
        setFormData({
            ...formData,
            questions: [
                ...formData.questions,
                {
                    questionText: '',
                    isMandatory: false,
                    maxLength: 200,
                    displayOrder: formData.questions.length + 1
                }
            ]
        });
    };

    const removeQuestion = (index) => {
        const newQuestions = formData.questions.filter((_, i) => i !== index);
        // Re-order
        newQuestions.forEach((q, i) => q.displayOrder = i + 1);
        setFormData({ ...formData, questions: newQuestions });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        // Format deadline to ISO string if present
        const payload = {
            ...formData,
            applicationDeadline: formData.applicationDeadline ? new Date(formData.applicationDeadline).toISOString().slice(0, 19) : null
        };

        try {
            await api.post('/hr/jobs', payload);
            toast.success('Job posted successfully!');
            navigate('/hr/dashboard');
        } catch (error) {
            toast.error(error.response?.data?.error || 'Failed to create job');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto">
            <button
                onClick={() => navigate('/hr/dashboard')}
                className="flex items-center text-gray-600 hover:text-blue-600 mb-6 transition-colors"
            >
                <ArrowLeftIcon className="h-5 w-5 mr-2" />
                Back to Dashboard
            </button>

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                <div className="p-6 border-b border-gray-200 bg-gray-50">
                    <h1 className="text-2xl font-bold text-gray-900">Post a New Job</h1>
                    <p className="text-gray-500 mt-1">Define the role and screening questions.</p>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-8">
                    {/* Basic Info */}
                    <div className="space-y-6">
                        <h3 className="text-lg font-medium text-gray-900 border-b pb-2">Job Details</h3>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Job Title</label>
                                <input
                                    name="title"
                                    type="text"
                                    required
                                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                    placeholder="e.g. Senior Backend Engineer"
                                    value={formData.title}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                                <textarea
                                    name="description"
                                    required
                                    rows={4}
                                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                    placeholder="Describe the role, responsibilities, and requirements..."
                                    value={formData.description}
                                    onChange={handleChange}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Location</label>
                                <input
                                    name="location"
                                    type="text"
                                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                    placeholder="e.g. Remote, Pune"
                                    value={formData.location}
                                    onChange={handleChange}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Employment Type</label>
                                <select
                                    name="employmentType"
                                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border bg-white"
                                    value={formData.employmentType}
                                    onChange={handleChange}
                                >
                                    <option value="FULL_TIME">Full Time</option>
                                    <option value="PART_TIME">Part Time</option>
                                    <option value="CONTRACT">Contract</option>
                                    <option value="INTERN">Internship</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Application Deadline</label>
                                <input
                                    name="applicationDeadline"
                                    type="datetime-local"
                                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                    value={formData.applicationDeadline}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Screening Questions */}
                    <div className="space-y-6">
                        <div className="flex justify-between items-center border-b pb-2">
                            <h3 className="text-lg font-medium text-gray-900">Screening Questions</h3>
                            <button
                                type="button"
                                onClick={addQuestion}
                                className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                            >
                                <PlusIcon className="-ml-0.5 mr-2 h-4 w-4" />
                                Add Question
                            </button>
                        </div>

                        <div className="space-y-4">
                            {formData.questions.map((q, index) => (
                                <div key={index} className="bg-gray-50 p-4 rounded-lg border border-gray-200 relative group">
                                    <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
                                        <div className="md:col-span-8">
                                            <label className="block text-xs font-medium text-gray-500 mb-1">Question {index + 1}</label>
                                            <input
                                                type="text"
                                                required
                                                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                                placeholder="e.g. How many years of Java experience do you have?"
                                                value={q.questionText}
                                                onChange={(e) => handleQuestionChange(index, 'questionText', e.target.value)}
                                            />
                                        </div>
                                        <div className="md:col-span-2">
                                            <label className="block text-xs font-medium text-gray-500 mb-1">Max Length</label>
                                            <input
                                                type="number"
                                                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                                value={q.maxLength}
                                                onChange={(e) => handleQuestionChange(index, 'maxLength', parseInt(e.target.value))}
                                            />
                                        </div>
                                        <div className="md:col-span-2 flex items-center pt-5">
                                            <input
                                                id={`mandatory-${index}`}
                                                type="checkbox"
                                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                                                checked={q.isMandatory}
                                                onChange={(e) => handleQuestionChange(index, 'isMandatory', e.target.checked)}
                                            />
                                            <label htmlFor={`mandatory-${index}`} className="ml-2 block text-sm text-gray-900">
                                                Mandatory
                                            </label>
                                        </div>
                                    </div>

                                    {formData.questions.length > 1 && (
                                        <button
                                            type="button"
                                            onClick={() => removeQuestion(index)}
                                            className="absolute top-2 right-2 text-gray-400 hover:text-red-500 p-1"
                                        >
                                            <TrashIcon className="h-5 w-5" />
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Submit */}
                    <div className="pt-5 border-t border-gray-200 flex justify-end">
                        <button
                            type="button"
                            onClick={() => navigate('/hr/dashboard')}
                            className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 mr-3"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                        >
                            {loading ? 'Posting...' : 'Post Job'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateJob;
