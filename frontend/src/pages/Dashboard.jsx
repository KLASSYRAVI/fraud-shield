import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../api/axios';
import TransactionTable from '../components/TransactionTable';
import FraudChart from '../components/FraudChart';
import DashboardCharts from '../components/DashboardCharts';
import FlaggedCases from '../components/FlaggedCases';
import AuditLog from '../components/AuditLog';

const AnimatedNumber = ({ value, isPercent, isDecimal }) => {
  const [display, setDisplay] = useState(0);
  useEffect(() => {
    const end = parseFloat(value);
    const duration = 1000;
    const steps = 30;
    const stepTime = Math.abs(Math.floor(duration / steps));
    let current = 0;
    const increment = end / steps;
    
    if (end === 0) return setDisplay(0);
    
    const timer = setInterval(() => {
      current += increment;
      if (current >= end) {
        setDisplay(end);
        clearInterval(timer);
      } else {
        setDisplay(current);
      }
    }, stepTime);
    
    return () => clearInterval(timer);
  }, [value]);

  if (isDecimal) return (display).toFixed(2) + (isPercent ? '%' : '');
  return Math.floor(display).toLocaleString() + (isPercent ? '%' : '');
};

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [stats, setStats] = useState(null);
  const [trends, setTrends] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [simulating, setSimulating] = useState(false);
  
  const navigate = useNavigate();
  const clientRef = useRef(null);

  const fetchData = async () => {
    try {
      const [statsRes, txRes, trendsRes] = await Promise.all([
        api.get('/dashboard/stats'),
        api.get('/transactions?page=0&size=20'),
        api.get('/dashboard/trends')
      ]);
      setStats(statsRes.data);
      setTransactions(txRes.data.content);
      setTrends(trendsRes.data);
    } catch (err) {
      if (err.response && err.response.status === 403) {
        localStorage.removeItem('token');
        navigate('/login');
      }
    }
  };

  useEffect(() => {
    fetchData();

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      onConnect: () => {
        client.subscribe('/topic/transactions', (message) => {
          const newTx = JSON.parse(message.body);
          newTx.isNew = true; 
          setTransactions((prev) => [newTx, ...prev].slice(0, 20));
          fetchData(); 
        });
      },
      onStompError: (frame) => console.error(frame)
    });

    client.activate();
    clientRef.current = client;

    const interval = setInterval(fetchData, 10000);

    return () => {
      clearInterval(interval);
      if (clientRef.current) clientRef.current.deactivate();
    };
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const handleSimulate = async () => {
    setSimulating(true);
    try { await api.post('/transactions/simulate'); }
    catch (err) { console.error('Simulation failed', err); }
    finally { setSimulating(false); }
  };
  
  const handleExport = () => {
    api.get('/dashboard/export', { responseType: 'blob' })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'transactions_export.csv');
        document.body.appendChild(link);
        link.click();
      });
  };

  return (
    <div className="dashboard-container">
      <div className="header">
        <h1>
          <div className="live-indicator"></div>
          RISK.TERMINAL
        </h1>
        <button onClick={handleLogout} className="btn btn-ghost" style={{borderColor: 'var(--red)', color: 'var(--red)'}}>TERMINATE SESSION</button>
      </div>

      <div className="tabs">
        <button className={`tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Overview</button>
        <button className={`tab ${activeTab === 'cases' ? 'active' : ''}`} onClick={() => setActiveTab('cases')}>Flagged Cases</button>
        <button className={`tab ${activeTab === 'audit' ? 'active' : ''}`} onClick={() => setActiveTab('audit')}>Audit Log</button>
      </div>

      {activeTab === 'overview' && (
        <React.Fragment>
          {stats && (
            <div className="stats-grid">
              <div className="stat-card">
                <h3>Total Scans</h3>
                <p className="value"><AnimatedNumber value={stats.totalTransactions} /></p>
              </div>
              <div className="stat-card" style={{ borderTopColor: 'var(--red)' }}>
                <h3 style={{ color: 'var(--red)' }}>Anomalies</h3>
                <p className="value" style={{ color: 'var(--red)' }}><AnimatedNumber value={stats.flaggedCount} /></p>
              </div>
              <div className="stat-card" style={{ borderTopColor: 'var(--amber)' }}>
                <h3 style={{ color: 'var(--amber)' }}>Fraud Rate</h3>
                <p className="value" style={{ color: 'var(--amber)' }}><AnimatedNumber value={stats.fraudRate} isPercent isDecimal /></p>
              </div>
              <div className="stat-card" style={{ borderTopColor: 'var(--green)' }}>
                <h3 style={{ color: 'var(--green)' }}>Avg Risk</h3>
                <p className="value" style={{ color: 'var(--green)' }}><AnimatedNumber value={stats.avgRiskScore * 100} isPercent isDecimal /></p>
              </div>
            </div>
          )}

          <div className="actions-bar">
            <h2>STREAM OBSERVER</h2>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={handleExport} className="btn btn-ghost">EXPORT.CSV</button>
              <button onClick={handleSimulate} className="btn btn-primary" disabled={simulating}>
                {simulating ? 'INJECTING...' : 'INJECT TRANSACTION'}
              </button>
            </div>
          </div>

          <div className="content-grid" style={{ marginBottom: '1.5rem' }}>
            <TransactionTable transactions={transactions} />
            <FraudChart stats={stats} />
          </div>
          
          <DashboardCharts trends={trends} />
        </React.Fragment>
      )}

      {activeTab === 'cases' && <FlaggedCases />}
      {activeTab === 'audit' && <AuditLog />}
    </div>
  );
};

export default Dashboard;
