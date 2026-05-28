package com.example.demo.Logica.Controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class ControllerMappingUniquenessTest {

    @Test
    void controllersShouldNotDeclareDuplicateHttpMethodAndPathMappings() {
        List<Class<?>> controllers = List.of(
                AdminController.class,
                CalificacionController.class,
                ClienteController.class,
                LocalController.class,
                PedidoController.class,
                ReclamoController.class,
                UsuarioController.class);

        Map<String, List<String>> declarationsByMapping = new LinkedHashMap<>();

        for (Class<?> controller : controllers) {
            List<String> basePaths = classLevelPaths(controller);

            for (Method method : controller.getDeclaredMethods()) {
                MappingDeclaration mapping = methodLevelMapping(method);
                if (mapping == null) {
                    continue;
                }

                for (String httpMethod : mapping.httpMethods()) {
                    for (String basePath : basePaths) {
                        for (String methodPath : mapping.paths()) {
                            String key = httpMethod + " " + normalizePath(basePath, methodPath);
                            declarationsByMapping
                                    .computeIfAbsent(key, ignored -> new ArrayList<>())
                                    .add(controller.getSimpleName() + "#" + method.getName());
                        }
                    }
                }
            }
        }

        String duplicates = declarationsByMapping.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " -> " + String.join(", ", entry.getValue()))
                .collect(Collectors.joining(System.lineSeparator()));

        assertTrue(duplicates.isBlank(), () -> "Duplicate controller mappings found:"
                + System.lineSeparator() + duplicates);
    }

    private static List<String> classLevelPaths(Class<?> controller) {
        RequestMapping requestMapping = controller.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            return List.of("");
        }

        String[] paths = nonEmptyOrDefault(requestMapping.path(), requestMapping.value());
        return Arrays.asList(paths.length == 0 ? new String[] { "" } : paths);
    }

    private static MappingDeclaration methodLevelMapping(Method method) {
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            return new MappingDeclaration(List.of("GET"), paths(getMapping.path(), getMapping.value()));
        }

        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            return new MappingDeclaration(List.of("POST"), paths(postMapping.path(), postMapping.value()));
        }

        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            return new MappingDeclaration(List.of("PUT"), paths(putMapping.path(), putMapping.value()));
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            return new MappingDeclaration(List.of("DELETE"), paths(deleteMapping.path(), deleteMapping.value()));
        }

        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            return new MappingDeclaration(List.of("PATCH"), paths(patchMapping.path(), patchMapping.value()));
        }

        return null;
    }

    private static List<String> paths(String[] path, String[] value) {
        String[] paths = nonEmptyOrDefault(path, value);
        return Arrays.asList(paths.length == 0 ? new String[] { "" } : paths);
    }

    private static String[] nonEmptyOrDefault(String[] primary, String[] fallback) {
        if (primary.length > 0) {
            return primary;
        }
        return fallback;
    }

    private static String normalizePath(String basePath, String methodPath) {
        String joined = ("/" + trimSlashes(basePath) + "/" + trimSlashes(methodPath))
                .replaceAll("/{2,}", "/");
        return joined.length() > 1 && joined.endsWith("/")
                ? joined.substring(0, joined.length() - 1)
                : joined;
    }

    private static String trimSlashes(String path) {
        return path == null ? "" : path.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private record MappingDeclaration(List<String> httpMethods, List<String> paths) {
    }
}
