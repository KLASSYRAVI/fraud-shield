import React from 'react';

const TransactionTable = ({ transactions }) => {
  const getRiskBadge = (level) => {
    if (!level) return <span className="badge low">LOW</span>;
    return <span className={`badge ${level.toLowerCase()}`}>{level}</span>;
  };

  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
  };

  return (
    <div className="card">
      <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>RECENT EVENTS</h3>
      <table>
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>Location</th>
            <th>Amount</th>
            <th>Device</th>
            <th>Confidence</th>
            <th>Threat</th>
            <th>Heuristic</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((tx) => (
            <tr key={tx.id} className={tx.isNew ? 'new-row' : ''}>
              <td className="mono dim" style={{ fontSize: '0.85rem' }}>{new Date(tx.createdAt).toISOString().substring(11,23)}</td>
              <td className="mono">{tx.location}</td>
              <td className="amount">{formatAmount(tx.amount)}</td>
              <td className="mono" style={{ fontSize: '0.85rem' }}>{tx.device.toUpperCase()}</td>
              <td className="mono">{(tx.fraudProbability * 100).toFixed(1)}%</td>
              <td>{getRiskBadge(tx.riskLevel)}</td>
              <td className="mono dim" style={{ fontSize: '0.75rem', maxWidth: '180px' }}>
                {tx.flagReason || 'N/A'}
              </td>
            </tr>
          ))}
          {transactions.length === 0 && (
            <tr><td colSpan="7" className="dim mono" style={{ textAlign: 'center', padding: '2rem' }}>AWAITING DATA...</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default TransactionTable;
