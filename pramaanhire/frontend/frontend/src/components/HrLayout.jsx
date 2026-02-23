import { useContext } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import {
    HomeIcon,
    PlusCircleIcon,
    ArrowRightOnRectangleIcon,
    UserCircleIcon,
    BriefcaseIcon
} from '@heroicons/react/24/outline';

const HrLayout = () => {
    const { logout, user } = useContext(AuthContext);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        { name: 'Dashboard', path: '/hr/dashboard', icon: HomeIcon },
        { name: 'My Jobs', path: '/hr/jobs', icon: BriefcaseIcon },
        { name: 'Post New Job', path: '/hr/create-job', icon: PlusCircleIcon },
    ];

    return (
        <div className="min-h-screen bg-gray-50 flex">
            {/* Sidebar */}
            <aside className="w-64 bg-white border-r border-gray-200 hidden md:flex flex-col">
                <div className="p-6 border-b border-gray-200">
                    <h1 className="text-2xl font-bold text-blue-600">PramaanHire</h1>
                    <p className="text-xs text-gray-500 mt-1">HR Portal</p>
                </div>

                <nav className="flex-1 p-4 space-y-1">
                    {navItems.map((item) => {
                        const isActive = location.pathname === item.path;
                        return (
                            <Link
                                key={item.name}
                                to={item.path}
                                className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors ${
                                    isActive
                                        ? 'bg-blue-50 text-blue-700'
                                        : 'text-gray-700 hover:bg-gray-100'
                                }`}
                            >
                                <item.icon className="h-5 w-5 mr-3" />
                                {item.name}
                            </Link>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-gray-200">
                    <div className="flex items-center mb-4 px-4">
                        <UserCircleIcon className="h-8 w-8 text-gray-400 mr-2" />
                        <div>
                            <p className="text-sm font-medium text-gray-700">HR Admin</p>
                            <p className="text-xs text-gray-500">Recruiter</p>
                        </div>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="w-full flex items-center px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    >
                        <ArrowRightOnRectangleIcon className="h-5 w-5 mr-3" />
                        Sign Out
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
                {/* Mobile Header */}
                <div className="md:hidden bg-white border-b border-gray-200 p-4 flex justify-between items-center">
                    <h1 className="text-xl font-bold text-blue-600">PramaanHire</h1>
                    <button onClick={handleLogout} className="text-gray-500">
                        <ArrowRightOnRectangleIcon className="h-6 w-6" />
                    </button>
                </div>

                <div className="flex-1 overflow-y-auto p-4 sm:p-8">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default HrLayout;
