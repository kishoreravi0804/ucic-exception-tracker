import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { exceptionService } from '../api/exceptionService';

function GroupDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchGroup();
  }, [id]);

  const fetchGroup = async () => {
    try {
      setLoading(true);
      const response = await exceptionService.getGroupById(id);
      setGroup(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSendToVendor = async () => {
    try {
      setActionLoading(true);
      await exceptionService.sendToVendor(id);
      await fetchGroup();
    } catch (err) {
      alert('Failed to send to vendor: ' + err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleResolve = async () => {
    const rootCause = group.rootCause;
    const note = prompt('Enter resolution note:');
    if (note === null) return;

    try {
      setActionLoading(true);
      await exceptionService.resolveManually(id, { rootCause, resolutionNote: note });
      await fetchGroup();
    } catch (err) {
      alert('Failed to resolve: ' + err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    const note = prompt('Enter rejection reason:');
    if (note === null) return;

    try {
      setActionLoading(true);
      await exceptionService.rejectGroup(id, note);
      await fetchGroup();
    } catch (err) {
      alert('Failed to reject: ' + err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const copyVendorLink = () => {
    const link = `${window.location.origin}/vendor/review?token=${group.vendorToken}`;
    navigator.clipboard.writeText(link);
    alert('Vendor link copied to clipboard!');
  };

  if (loading) return <div className="container"><p>Loading...</p></div>;
  if (!group) return <div className="container"><p>Group not found.</p></div>;

  return (
    <div className="container">
      <div className="header-row">
        <h1>{group.groupRef}</h1>
        <Link to="/exceptions" className="btn-secondary">← Back to List</Link>
      </div>

      <div className="detail-card">
        <div className="detail-row">
          <span className="detail-label">Status:</span>
          <span className={`badge badge-${group.status.toLowerCase()}`}>{group.status}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">Root Cause:</span>
          <span>{group.rootCause}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">Records in Group:</span>
          <span>{group.recordCount}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">Detected At:</span>
          <span>{new Date(group.detectedAt).toLocaleString()}</span>
        </div>
        {group.resolutionNote && (
          <div className="detail-row">
            <span className="detail-label">Resolution Note:</span>
            <span>{group.resolutionNote}</span>
          </div>
        )}
      </div>

      <div className="action-row">
        {group.status === 'PENDING' && (
          <button onClick={handleSendToVendor} disabled={actionLoading} className="btn-primary">
            Send to Vendor
          </button>
        )}
        {group.status === 'SENT_TO_VENDOR' && (
          <>
            <button onClick={copyVendorLink} className="btn-secondary">Copy Vendor Link</button>
            <button onClick={handleResolve} disabled={actionLoading} className="btn-primary">Mark Resolved</button>
            <button onClick={handleReject} disabled={actionLoading} className="btn-danger">Reject</button>
          </>
        )}
      </div>

      <h2>Duplicate Customer Records ({group.members?.length || 0})</h2>
      <table className="exception-table">
        <thead>
          <tr>
            <th>Customer ID</th>
            <th>Name</th>
            <th>PAN</th>
            <th>Voter ID</th>
            <th>Aadhaar</th>
            <th>Mobile</th>
            <th>Master</th>
          </tr>
        </thead>
        <tbody>
          {group.members?.map((member) => (
            <tr key={member.id} className={member.isMaster ? 'row-master' : ''}>
              <td>{member.customerId}</td>
              <td>{member.fullName}</td>
              <td>{member.pan || '-'}</td>
              <td>{member.voterId || '-'}</td>
              <td>{member.aadhaar || '-'}</td>
              <td>{member.mobile || '-'}</td>
              <td>{member.isMaster ? '⭐' : ''}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default GroupDetail;
