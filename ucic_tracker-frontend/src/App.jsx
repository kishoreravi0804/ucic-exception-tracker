import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import ExceptionList from './pages/ExceptionList';
import GroupDetail from './pages/GroupDetail';
import VendorPortal from './pages/VendorPortal';
import './index.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/exceptions" element={<ExceptionList />} />
        <Route path="/exceptions/:id" element={<GroupDetail />} />
         <Route path="/vendor/review" element={<VendorPortal />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;