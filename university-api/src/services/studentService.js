import { AppError } from "../errors/AppError.js";
import * as studentRepository from "../repositories/studentRepository.js";

const GENERIC_VALIDATION_MESSAGE =
  "No fue posible validar al estudiante con los datos proporcionados.";

function normalized(value) {
  return typeof value === "string" ? value.trim() : "";
}

export async function validateStudent(input) {
  const studentCode = normalized(input?.studentCode);
  const document = normalized(input?.document);
  const institutionalEmail = normalized(input?.institutionalEmail).toLowerCase();

  if (!studentCode || !document || !institutionalEmail) {
    throw new AppError(400, "INVALID_REQUEST", "Código, documento y correo son obligatorios.");
  }

  const student = await studentRepository.findByStudentCode(studentCode);
  const eligible =
    student &&
    student.documento === document &&
    student.correo_institucional.toLowerCase() === institutionalEmail &&
    student.estado === "ACTIVO" &&
    Boolean(student.habilitado_votacion);

  if (!eligible) {
    throw new AppError(422, "STUDENT_NOT_ELIGIBLE", GENERIC_VALIDATION_MESSAGE);
  }

  return {
    eligible: true,
    student: {
      studentCode: student.codigo_estudiante,
      document: student.documento,
      firstNames: student.nombres,
      lastNames: student.apellidos,
      institutionalEmail: student.correo_institucional,
      program: student.programa
    }
  };
}
