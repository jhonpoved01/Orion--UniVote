import { app } from "./app.js";
import { config } from "./config.js";
import { pool } from "./db.js";

const server = app.listen(config.apiPort, "127.0.0.1", () => {
  console.log(`University API escuchando en http://127.0.0.1:${config.apiPort}`);
});

async function shutdown(signal) {
  console.log(`${signal}: cerrando University API`);
  server.close(async () => {
    await pool.end();
    process.exit(0);
  });
}

process.on("SIGINT", () => shutdown("SIGINT"));
process.on("SIGTERM", () => shutdown("SIGTERM"));
