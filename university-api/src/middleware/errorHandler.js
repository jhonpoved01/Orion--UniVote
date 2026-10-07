import { AppError } from "../errors/AppError.js";

export function notFoundHandler(req, res) {
  res.status(404).json({
    error: {
      code: "NOT_FOUND",
      message: "El recurso solicitado no existe."
    }
  });
}

export function errorHandler(error, req, res, next) {
  if (res.headersSent) {
    return next(error);
  }

  if (error instanceof AppError) {
    return res.status(error.status).json({
      error: {
        code: error.code,
        message: error.message
      }
    });
  }

  console.error("University API error:", error?.name ?? "Error");
  return res.status(500).json({
    error: {
      code: "INTERNAL_ERROR",
      message: "No fue posible completar la operación."
    }
  });
}
