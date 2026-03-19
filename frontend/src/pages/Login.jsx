import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { username, password });
      localStorage.setItem('token', response.data.token);
      navigate('/dashboard');
    } catch (err) {
      setError('INVALID CREDENTIALS OR ACCESS DENIED');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Risk Terminal</h2>
        <p className="dim mono" style={{ marginBottom: '2rem', fontSize: '0.85rem' }}>SYSTEM.AUTH.REQUIRED</p>
        
        {error && <div style={{ color: 'var(--red)', border: '1px solid var(--red)', background: 'rgba(255, 68, 102, 0.1)', marginBottom: '1.5rem', padding: '0.75rem', borderRadius: '4px', fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }}>{error}</div>}
        
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Operator ID</label>
            <input 
              type="text" 
              value={username} 
              onChange={(e) => setUsername(e.target.value)} 
              required 
            />
          </div>
          <div className="form-group">
            <label>Access Key</label>
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </div>
          <button type="submit" className="btn btn-primary" style={{marginTop: '1rem', width: '100%'}} disabled={loading}>
            {loading ? 'AUTHENTICATING...' : 'INITIALIZE SESSION'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
