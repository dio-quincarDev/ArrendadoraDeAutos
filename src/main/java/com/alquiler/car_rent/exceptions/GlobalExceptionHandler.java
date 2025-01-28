package com.alquiler.car_rent.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	  @ExceptionHandler(NotFoundException.class)
   	    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
	      Map<String, Object> body = new HashMap<>(); 
	      body.put("message", ex.getMessage());
          body.put("status", HttpStatus.NOT_FOUND.value());
          return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	   }
	
	  @ExceptionHandler(BadRequestException.class)
	    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
	        Map<String, Object> body = new HashMap<>();
	        body.put("message", ex.getMessage());
	        body.put("status", HttpStatus.BAD_REQUEST.value());
	        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	    }
	  
	  @ExceptionHandler(Exception.class)
	    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
	        Map<String, Object> body = new HashMap<>();
	        body.put("message", "Ha ocurrido un error inesperado.");
	        body.put("error", ex.getMessage());
	        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	

}
