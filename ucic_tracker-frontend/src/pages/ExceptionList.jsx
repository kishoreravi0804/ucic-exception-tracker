import { useState, useEffect } from 'react';
import { exceptionService } from '../api/exceptionService';
import { Link } from 'react-router-dom';

function ExceptionList() {
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    fetchGroups();
  }, [statusFilter]);

  const fetchGroups = async () => {
    try {
      setLoading(true);
      const response = await exceptionService.getAllGroups(statusFilter || null);
      setGroups(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="header-row">
        <h1>Exception Groups</h1>
        <Link to="/" className="btn-secondary">← Back to Dashboard</Link>
      </div>

      <div className="filter-row">
        <label>Filter by status: </label>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All</option>
          <option value="PENDING">Pending</option>
          <option value="SENT_TO_VENDOR">Sent to Vendor</option>
          <option value="RESOLVED">Resolved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table className="exception-table">
          <thead>
            <tr>
              <th>Group Ref</th>
              <th>Root Cause</th>
              <th>Status</th>
              <th>Records</th>
              <th>Detected At</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {groups.map((group) => (
              <tr key={group.id}>
                <td>{group.groupRef}</td>
                <td>{group.rootCause}</td>
                <td><StatusBadge status={group.status} /></td>
                <td>{group.recordCount}</td>
                <td>{new Date(group.detectedAt).toLocaleString()}</td>
                <td>
                  <Link to={`/exceptions/${group.id}`} className="link-view">View →</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {!loading && groups.length === 0 && <p>No exception groups found.</p>}
    </div>
  );
}

function StatusBadge({ status }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>;
}

export default ExceptionList;
