/**
 * Optional Backend Server
 * Demonstrates how to proxy Home Assistant API calls
 * Run: node backend/server.js
 */

import express from 'express';
import axios from 'axios';
import cors from 'cors';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

/**
 * Proxy Home Assistant API calls
 * Allows frontend to connect via backend instead of directly
 */

app.get('/api/states', async (req, res) => {
  try {
    const { HA_URL, HA_TOKEN } = req.query;

    if (!HA_URL || !HA_TOKEN) {
      return res.status(400).json({ error: 'Missing HA_URL or HA_TOKEN' });
    }

    const response = await axios.get(`${HA_URL}/api/states`, {
      headers: {
        'Authorization': `Bearer ${HA_TOKEN}`,
        'Content-Type': 'application/json',
      },
    });

    res.json(response.data);
  } catch (error) {
    console.error('API Error:', error.message);
    res.status(error.response?.status || 500).json({
      error: error.message,
    });
  }
});

app.post('/api/services/:domain/:service', async (req, res) => {
  try {
    const { HA_URL, HA_TOKEN } = req.query;
    const { domain, service } = req.params;

    if (!HA_URL || !HA_TOKEN) {
      return res.status(400).json({ error: 'Missing HA_URL or HA_TOKEN' });
    }

    const response = await axios.post(
      `${HA_URL}/api/services/${domain}/${service}`,
      req.body,
      {
        headers: {
          'Authorization': `Bearer ${HA_TOKEN}`,
          'Content-Type': 'application/json',
        },
      }
    );

    res.json(response.data);
  } catch (error) {
    console.error('API Error:', error.message);
    res.status(error.response?.status || 500).json({
      error: error.message,
    });
  }
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`[Backend] Server running on http://localhost:${PORT}`);
  console.log(`[Backend] Mode: Proxy for Home Assistant API`);
  console.log(`[Backend] Usage: Pass HA_URL and HA_TOKEN as query parameters`);
});

