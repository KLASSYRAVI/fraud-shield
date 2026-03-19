import React, { useState, useEffect } from 'react';
import api from '../api/axios';

const FlaggedCases = () => {
  const [cases, setCases] = useState([]);

  useEffect(() => {
    fetchCases();
  }, []);

  const fetchCases = async () => {
    try {
      const res = await api.get('/transactions/pending-review');
      setCases(res.data);
    } catch (err) { console.error(err); }
  };

  const handleAction = async (id, action) => {
    try {
      await api.patch(`/transactions/${id}/${action}`);
      fetchCases();
    } catch (err) { console.error(err); }
  };

  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
  };

  return (
    <div className="card" style={{ animation: 'fadeUp 0.4s ease-out' }}>
      <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>MANUAL OVERSIGHT REQUIRED</h3>
      {cases.length === 0 ? (
        <p className="dim mono">QUEUE CLEAR</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Origin</th>
              <th>Value</th>
              <th>Score</th>
              <th>Trigger</th>
              <th>Status</th>
              <th>Override</th>
            </tr>
          </thead>
          <tbody>
            {cases.map((tx) => (
              <tr key={tx.id}>
                <td className="mono dim" style={{fontSize: '0.85rem'}}>{new Date(tx.createdAt).toISOString().substring(11,23)}</td>
                <td className="mono">{tx.location.toUpperCase()}</td>
                <td className="amount">{formatAmount(tx.amount)}</td>
                <td><span className="badge high">{(tx.fraudProbability * 100).toFixed(1)}%</span></td>
                <td className="mono dim" style={{fontSize: '0.75rem', maxWidth: '150px'}}>{tx.flagReason}</td>
                <td><span className="badge medium">PENDING</span></td>
                <td>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleAction(tx.id, 'approve')} className="btn btn-success" style={{ padding: '0.4rem 0.8rem', fontSize: '0.7rem' }}>CONFIRM</button>
                    <button onClick={() => handleAction(tx.id, 'reject')} className="btn btn-danger" style={{ padding: '0.4rem 0.8rem', fontSize: '0.7rem' }}>BLOCK</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default FlaggedCases;
