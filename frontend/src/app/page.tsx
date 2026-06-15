"use client";

// TODO: remove — temp CORS smoke test page
import { useState } from "react";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export default function Home() {
  const [result, setResult] = useState<string | null>(null);

  async function ping() {
    try {
      const res = await fetch(`${API_URL}/ping`);
      const data = await res.json();
      setResult(`✓ ${JSON.stringify(data)}`);
    } catch (e) {
      setResult(`✗ ${e}`);
    }
  }

  return (
    <main style={{ padding: 32, fontFamily: "monospace" }}>
      <p style={{ marginBottom: 8, color: "#888" }}>
        Backend: <code>{API_URL}</code>
      </p>
      <button onClick={ping} style={{ marginBottom: 16, padding: "8px 16px" }}>
        Ping backend
      </button>
      {result && <pre>{result}</pre>}
    </main>
  );
}
