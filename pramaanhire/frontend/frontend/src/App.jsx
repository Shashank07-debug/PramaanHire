import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Login from './pages/Login';
import Signup from './pages/Signup';
import HrLayout from './components/HrLayout';
import HrDashboard from './pages/hr/HrDashboard';
import CreateJob from './pages/hr/CreateJob';
import JobDetails from './pages/hr/JobDetails';
import ApplicationDetails from './pages/hr/ApplicationDetails';
import MyJobs from './pages/hr/MyJobs';
import CandidateLayout from './components/CandidateLayout';
import CandidateDashboard from './pages/candidate/CandidateDashboard';
import FindJobs from './pages/candidate/FindJobs';
import JobView from './pages/candidate/JobView';
import MyApplications from './pages/candidate/MyApplications';
import ApplicationView from './pages/candidate/ApplicationView';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <ToastContainer position="top-right" autoClose={3000} />
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* HR Routes */}
          <Route path="/hr" element={<HrLayout />}>
            <Route path="dashboard" element={<HrDashboard />} />
            <Route path="jobs" element={<MyJobs />} />
            <Route path="create-job" element={<CreateJob />} />
            <Route path="jobs/:jobId" element={<JobDetails />} />
            <Route path="applications/:applicationId" element={<ApplicationDetails />} />
          </Route>

          {/* Candidate Routes */}
          <Route path="/candidate" element={<CandidateLayout />}>
            <Route path="dashboard" element={<CandidateDashboard />} />
            <Route path="jobs" element={<FindJobs />} />
            <Route path="jobs/:jobId" element={<JobView />} />
            <Route path="my-applications" element={<MyApplications />} />
            <Route path="my-applications/:applicationId" element={<ApplicationView />} />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
