import { defineConfig } from 'vite';
import fs from 'fs';
import path from 'path';

function resolveDemoDeploymentId() {
  // 1. Direct environment variable (from GitHub Actions CI secret POOKIE_DEMO_DEPLOYMENT_ID)
  if (process.env.POOKIE_DEMO_DEPLOYMENT_ID && process.env.POOKIE_DEMO_DEPLOYMENT_ID.trim()) {
    return process.env.POOKIE_DEMO_DEPLOYMENT_ID.trim();
  }

  // 2. Vite prefixed env var
  if (process.env.VITE_POOKIE_DEMO_DEPLOYMENT_ID && process.env.VITE_POOKIE_DEMO_DEPLOYMENT_ID.trim()) {
    return process.env.VITE_POOKIE_DEMO_DEPLOYMENT_ID.trim();
  }

  // 3. Local fallback for development: read local.properties in parent directory
  try {
    const localPropsPath = path.resolve(__dirname, '../local.properties');
    if (fs.existsSync(localPropsPath)) {
      const content = fs.readFileSync(localPropsPath, 'utf-8');
      const match = content.match(/POOKIE_DEMO_DEPLOYMENT_ID\s*=\s*([^\r\n]+)/);
      if (match && match[1]) {
        return match[1].trim();
      }
    }
  } catch (e) {
    // Ignore read errors
  }

  return '';
}

export default defineConfig({
  base: './', // Ensures assets load cleanly on GitHub Pages relative subpaths
  define: {
    __POOKIE_DEMO_DEPLOYMENT_ID__: JSON.stringify(resolveDemoDeploymentId()),
  },
  server: {
    port: 5173,
    host: true
  }
});
