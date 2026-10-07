import { pool } from "../db.js";

const FIND_BY_CODE = `
  SELECT codigo_estudiante,
         documento,
         nombres,
         apellidos,
         correo_institucional,
         programa,
         estado,
         habilitado_votacion
  FROM estudiantes_institucionales
  WHERE codigo_estudiante = ?
  LIMIT 1
`;

export async function findByStudentCode(studentCode) {
  const [rows] = await pool.execute(FIND_BY_CODE, [studentCode]);
  return rows[0] ?? null;
}
