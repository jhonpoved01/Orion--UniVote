import { Router } from "express";
import { validate } from "../controllers/studentController.js";

export const studentRouter = Router();

studentRouter.post("/validate", validate);
