import { validateStudent } from "../services/studentService.js";

export async function validate(req, res) {
  const result = await validateStudent(req.body);
  res.status(200).json({ data: result });
}
