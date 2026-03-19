import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const FraudChart = ({ stats }) => {
  if (!stats) return null;

  const data = [
    { name: 'Legitimate', value: stats.totalTransactions - stats.flaggedCount },
    { name: 'Anomalies', value: stats.flaggedCount },
  ];

  /* Match CSS variables --green and --red */
  const COLORS = ['#00ff88', '#ff4466'];

  return (
    <div className="card">
      <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>DISTRIBUTION</h3>
      <div style={{ height: 350 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={80}
              outerRadius={100}
              stroke="none"
              paddingAngle={2}
              dataKey="value"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend wrapperStyle={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default FraudChart;
