package com.example.demo.controllers;

import com.example.demo.service.GeminiCodeTranslatorService;
import com.example.demo.service.LanguageDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/translate")
@CrossOrigin
public class TranslateController {

	@Autowired
	private GeminiCodeTranslatorService geminiCodeTranslatorService;

    @Autowired
    private LanguageDetectionService languageDetectionService;

	@PostMapping
	public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, String> payload) {
		String code = payload.get("code");
		String from = payload.getOrDefault("from", "detect"); 
        String resolvedSourceLanguage = from;

		if (code == null || code.isBlank()) {
			return new ResponseEntity<>(
				Map.of("status", "error", "message", "Missing or empty 'code' parameter in request body."), 
				HttpStatus.BAD_REQUEST
			);
		}

        if (from.equalsIgnoreCase("detect")) {
            String detectedLanguage = languageDetectionService.detectLanguage(code);

            if (detectedLanguage.startsWith("Error")) {
                return new ResponseEntity<>(
                    Map.of("status", "error", "message", "Language detection failed: " + detectedLanguage),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
            resolvedSourceLanguage = detectedLanguage;
        }
        
		String pythonCode = geminiCodeTranslatorService.translateToPython(resolvedSourceLanguage, code);

		if (pythonCode.startsWith("Error")) {
			return new ResponseEntity<>(
				Map.of("status", "error", "message", pythonCode), 
				HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
		
		return new ResponseEntity<>(
			Map.of(
				"status", "success",
                "source_language", resolvedSourceLanguage,
				"python_code", pythonCode
			),
			HttpStatus.OK
		);
	}
}
