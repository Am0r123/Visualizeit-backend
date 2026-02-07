package com.example.demo.controllers;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/python")
@CrossOrigin(origins = "*")
public class CodeExecutorController {

    @PostMapping("/run")
    public Map<String, Object> executePython(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        List<Integer> array = (List<Integer>) request.get("array");

        try {
            File tempFile = File.createTempFile("visualizer", ".py");

            String pythonCode = generateInstrumentedCode(code, array);
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(pythonCode);
            }

            ProcessBuilder pb = new ProcessBuilder("python", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line);
            }

            int exitCode = process.waitFor();
            tempFile.delete();

            String output = outputBuilder.toString().trim();
            Map<String, Object> response = new HashMap<>();
            response.put("exitCode", exitCode);

            // ✅ Parse JSON for steps and code
            if (output.startsWith("{") || output.startsWith("[")) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> json = mapper.readValue(output, Map.class);
                response.putAll(json);
            } else {
                response.put("output", output);
                response.put("steps", Collections.emptyList());
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("exitCode", 1, "error", e.getMessage());
        }
    }

    private String generateInstrumentedCode(String userCode, List<Integer> array) {
        String indentedCode = indentCode(userCode, 1);

        return """
import json

arr = %s
steps = []

def log_step(line, arr_snapshot, changed, idx):
    steps.append({'line': line, 'array': arr_snapshot, 'changed': changed, 'currentIndex': idx})

import traceback

code_lines = %s

last_arr = None
index_names = ['i','j','k','left','right','cur','index']

def tracer(frame, event, arg):
    global last_arr
    if event != 'line':
        return tracer
    lineno = frame.f_lineno
    locs = frame.f_locals
    arr_snapshot = list(locs['arr']) if 'arr' in locs and isinstance(locs['arr'], list) else None
    changed = arr_snapshot != last_arr
    idx = None
    for name in index_names:
        if name in locs and isinstance(locs[name], int):
            idx = locs[name]
            break
    log_step(lineno, arr_snapshot, changed, idx)
    last_arr = arr_snapshot
    return tracer

try:
    import sys
    sys.settrace(tracer)
%s
    log_step(-1, list(arr), True, None)
    print(json.dumps({'steps': steps, 'code': code_lines}))
except Exception as e:
    tb = traceback.extract_tb(e.__traceback__)
    line = tb[-1].lineno if tb else None
    print(json.dumps({'steps': steps, 'code': code_lines, 'exception': {'message': str(e), 'line': line}}))
finally:
    import sys
    sys.settrace(None)
""" .formatted(array.toString(), Arrays.toString(userCode.split("\n")), indentedCode);
    }

    private String indentCode(String code, int level) {
        String indent = "    ".repeat(level);
        return Arrays.stream(code.split("\n"))
                .map(line -> indent + line)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}
