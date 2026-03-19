import React, { useState, useEffect } from 'react';
import api from '../api/axios';

const AuditLog = () => {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    try {
      const res = await api.get('/admin/audit-logs?size=100');
      setLogs(res.data.content);
    } catch (err) { console.error(err); }
  };

  const getActionBadge = (action) => {
    if (action.includes('HIGH_RISK')) return 'badge high';
    if (action.includes('LOGIN')) return 'badge info';
    if (action.includes('REVIEW')) return 'badge medium';
    return 'badge neutral';
  };

  return (
    <div className="card" style={{ animation: 'fadeUp 0.4s ease-out' }}>
      <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>SYSTEM TELEMETRY</h3>
      <div style={{ maxHeight: '600px', overflowY: 'auto' }}>
        <table>
          <thead style={{ position: 'sticky', top: 0, background: 'var(--bg-card)', zIndex: 1 }}>
            <tr>
              <th>Timestamp</th>
              <th>Event Code</th>
              <th>Target</th>
              <th>Operator</th>
              <th>Data</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td className="mono dim" style={{fontSize: '0.85rem'}}>{new Date(log.timestamp).toISOString()}</td>
                <td><span className={getActionBadge(log.action)}>{log.action}</span></td>
                <td className="mono" style={{fontSize: '0.85rem'}}>{log.entityType.toUpperCase()} {log.entityId ? `[${log.entityId.substring(0,6)}]` : ''}</td>
                <td className="mono" style={{color: 'var(--green)'}}>{log.performedBy.toUpperCase()}</td>
                <td className="mono dim" style={{fontSize: '0.75rem', maxWidth: '250px'}}>{log.details}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AuditLog;
