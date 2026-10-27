package br.edu.pokedex;

import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<?> duplicate() { return error(409, "Já existe um Pokémon com esse número."); }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> invalid(Exception e) { return error(400, "Dados inválidos. Preencha todos os campos e confira os limites indicados."); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException e) { return error(e.getStatusCode().value(), e.getReason()); }
    @ExceptionHandler(DataAccessResourceFailureException.class)
    ResponseEntity<?> unavailable() { return error(503, "MongoDB indisponível. Confira a conexão com o banco."); }
    private ResponseEntity<?> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
