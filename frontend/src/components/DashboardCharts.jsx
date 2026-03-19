import React from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer } from 'recharts';

const DashboardCharts = ({ trends }) => {
  if (!trends) return null;

  const chartTheme = {
    fontFamily: 'var(--font-mono)',
    fontSize: '0.75rem',
    fill: 'var(--text-secondary)'
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginTop: '1.5rem', animation: 'fadeUp 0.6s ease-out 0.6s both' }}>
      
      <div className="card">
        <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>24H TRAJECTORY</h3>
        <div style={{ height: 250 }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={trends.hourlyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="hour" tick={chartTheme} stroke="var(--border)" />
              <YAxis tick={chartTheme} stroke="var(--border)" unit="%" />
              <RechartsTooltip />
              <Line type="monotone" dataKey="fraudRate" stroke="var(--green)" strokeWidth={3} dot={{ fill: 'var(--bg-base)', strokeWidth: 2 }} activeDot={{ r: 6, fill: 'var(--green)' }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>VECTORS</h3>
        <div style={{ height: 250 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={Object.entries(trends.deviceBreakdown).map(([k,v]) => ({name: k.toUpperCase(), value: v}))}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="name" tick={chartTheme} stroke="var(--border)" />
              <YAxis tick={chartTheme} stroke="var(--border)" />
              <RechartsTooltip cursor={{ fill: 'rgba(255,255,255,0.05)' }} />
              <Bar dataKey="value" fill="var(--blue)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>HOTSPOTS</h3>
        <table style={{ width: '100%' }}>
          <tbody>
            {trends.topRiskyLocations.map((loc, idx) => (
              <tr key={loc} style={{ background: 'transparent' }}>
                <td style={{ padding: '0.75rem 0', borderBottom: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <span className="mono" style={{ 
                      color: 'var(--red)', 
                      marginRight: '1rem',
                      fontSize: '1.2rem'
                    }}>0{idx + 1}</span>
                    <strong style={{ fontSize: '1.1rem', letterSpacing: '0.05em' }}>{loc.toUpperCase()}</strong>
                  </div>
                </td>
              </tr>
            ))}
            {trends.topRiskyLocations.length === 0 && (
              <tr><td className="dim mono">NO HOTSPOTS</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DashboardCharts;
