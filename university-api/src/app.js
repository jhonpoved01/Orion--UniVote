import express from "express";
import { studentRouter } from "./routes/studentRoutes.js";
import { errorHandler, notFoundHandler } from "./middleware/errorHandler.js";

export const app = express();

app.disable("x-powered-by");
app.use(express.json({ limit: "16kb" }));

app.get("/api/health", (req, res) => {
  res.status(200).json({
    data: {
      service: "univote-university-api",
      status: "UP"
    }
  });
});

app.use("/api/students", studentRouter);
app.use(notFoundHandler);
app.use(errorHandler);
