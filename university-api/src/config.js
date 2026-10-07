function required(name) {
  const value = process.env[name];
  if (!value || !value.trim()) {
    throw new Error(`Falta la variable de entorno obligatoria: ${name}`);
  }
  return value.trim();
}

function positiveInteger(name, fallback) {
  const raw = process.env[name] ?? String(fallback);
  const value = Number.parseInt(raw, 10);
  if (!Number.isInteger(value) || value <= 0) {
    throw new Error(`La variable ${name} debe ser un entero positivo`);
  }
  return value;
}

export const config = Object.freeze({
  apiPort: positiveInteger("UNIVERSITY_API_PORT", 8081),
  db: Object.freeze({
    host: process.env.UNIVERSITY_DB_HOST?.trim() || "127.0.0.1",
    port: positiveInteger("UNIVERSITY_DB_PORT", 3306),
    database: process.env.UNIVERSITY_DB_NAME?.trim() || "universidad_institucional",
    user: required("UNIVERSITY_DB_USER"),
    password: required("UNIVERSITY_DB_PASSWORD")
  })
});
