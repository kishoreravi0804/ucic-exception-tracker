import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { vendorService } from '../api/exceptionService';

function VendorPortal() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const [rootCause, setRootCause] = useState('DUPLICATE_PAN');
  const [note, setNote] = useState('');

  useEffect(() => {
    if (!token) {
      setError('No token provided. This link is invalid.');
      setLoading(false);
      return;
    }
    fetchGroup();
  }, [token]);

  const fetchGroup = async () => {
    try {
      setLoading(true);
      const response = await vendorService.getByToken(token);
      setGroup(response.data);
      setError(null);
    } catch (err) {
      setError('This link is invalid or has expired. Please contact the issuing team.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!note.trim()) {
      alert('Please add a resolution note.');
      return;
    }
    try {
      setSubmitting(true);
      await vendorService.resolveByToken(token, { rootCause, resolutionNote: note });
      setSubmitted(true);
    } catch (err) {
      alert('Failed to submit: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="container"><p>Loading...</p></div>;
  if (error) return <div className="container"><p className="error">{error}</p></div>;
  if (submitted) {
    return (
      <div className="container">
        <div className="vendor-success">
          <h2>✅ Submitted Successfully</h2>
          <p>Thank you. The exception group has been marked as resolved.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>Vendor Review — {group.groupRef}</h1>
      <p className="vendor-subtitle">Please review the duplicate records below and submit your findings.</p>

      <table className="exception-table">
        <thead>
          <tr>
            <th>Customer ID</th>
            <th>Name</th>
            <th>PAN</th>
            <th>Voter ID</th>
            <th>Aadhaar</th>
            <th>Mobile</th>
          </tr>
        </thead>
        <tbody>
          {group.members?.map((member) => (
            <tr key={member.id}>
              <td>{member.customerId}</td>
              <td>{member.fullName}</td>
              <td>{member.pan || '-'}</td>
              <td>{member.voterId || '-'}</td>
              <td>{member.aadhaar || '-'}</td>
              <td>{member.mobile || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <form onSubmit={handleSubmit} className="vendor-form">
        <h3>Submit Resolution</h3>

        <label>Root Cause:</label>
        <select value={rootCause} onChange={(e) => setRootCause(e.target.value)}>
          <option value="DUPLICATE_PAN">Duplicate PAN</option>
          <option value="DUPLICATE_CUSTOMER_ID">Duplicate Customer ID</option>
          <option value="KYC_MISMATCH_VOTER">KYC Mismatch — Voter ID</option>
          <option value="KYC_MISMATCH_AADHAAR">KYC Mismatch — Aadhaar</option>
          <option value="MULTIPLE_KYC_ISSUES">Multiple KYC Issues</option>
          <option value="UNIDENTIFIED">Unidentified</option>
        </select>

        <label>Resolution Note:</label>
        <textarea
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="Describe your findings..."
          rows={4}
        />

        <button type="submit" disabled={submitting} className="btn-primary">
          {submitting ? 'Submitting...' : 'Submit Resolution'}
        </button>
      </form>
    </div>
  );
}

export default VendorPortal;
