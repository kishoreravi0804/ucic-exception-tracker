import { useState, useEffect } from 'react';
import { exceptionService } from '../api/exceptionService';
import { Link } from 'react-router-dom';

function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const response = await exceptionService.getDashboard();
      setStats(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load dashboard. Is the backend running?');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRunAnalysis = async () => {
    try {
      setAnalyzing(true);
      await exceptionService.runAnalysis();
      await fetchDashboard();
    } catch (err) {
      alert('Analysis failed: ' + err.message);
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading) return <div className="container"><p>Loading dashboard...</p></div>;
  if (error) return <div className="container"><p className="error">{error}</p></div>;

  return (
    <div className="container">
      <div className="header-row">
        <h1>UCIC Exception Tracker</h1>
        <button onClick={handleRunAnalysis} disabled={analyzing} className="btn-primary">
          {analyzing ? 'Running Analysis...' : 'Run Analysis'}
        </button>
      </div>

      <div className="stats-grid">
        <StatCard label="Total Customers" value={stats.totalCustomers} color="blue" />
        <StatCard label="Total Groups" value={stats.totalGroups} color="purple" />
        <StatCard label="Pending" value={stats.pending} color="orange" />
        <StatCard label="Sent to Vendor" value={stats.sentToVendor} color="yellow" />
        <StatCard label="Resolved" value={stats.resolved} color="green" />
        <StatCard label="Rejected" value={stats.rejected} color="red" />
      </div>

      {stats.lastRunDurationMs && (
        <div className="last-run-info">
          Last analysis: {stats.lastRunGroupsFound} groups found in {stats.lastRunDurationMs}ms
        </div>
      )}

      <Link to="/exceptions" className="btn-secondary">
        View All Exceptions →
      </Link>
    </div>
  );
}

function StatCard({ label, value, color }) {
  return (
    <div className={`stat-card stat-${color}`}>
      <div className="stat-value">{value ?? 0}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}

export default Dashboard;
