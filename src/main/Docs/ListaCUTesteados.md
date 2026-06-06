-Solicitar Hab local(crea el local siempre como habilitado, faltaria crear el caso contrario)
-Alta Plato
-CU-L04 / CU-L05 Apertura y cierre de local
        Estado actual
        Apertura OK y cierre OK sin pedidos pendientes.
        Tambien quedaron cubiertos los alternativos basicos:
        - no abrir si ya estaba abierto
        - no cerrar si ya estaba cerrado
        - no cerrar si hay pedidos pendientes

        Bugs corregidos
        - LocalRepositorioImpl no mapeaba el id del Local y eso rompia apertura/cierre al actualizar.
        - PedidoRepositorioImpl consultaba la tabla equivocada (pedidos en vez de pedido) para validar pendientes.
        - docker-compose no estaba pasando variables MP_* al contenedor app y la aplicacion no iniciaba.

        Que falta para cerrar del todo el caso de uso
        - confirmacion explicita de apertura/cierre segun el caso de uso
        - advertencia funcional con cantidad de pedidos pendientes antes de cerrar
        - manejo HTTP correcto de errores de negocio (hoy varias validaciones terminan en 500)
-Crear Cliente (puede mejorarse con los puntos subsiguientes):
        2) Qué hay que corregir en ClienteService.registrarUsuario
        Archivo: src/main/java/com/example/demo/Logica/Service/ClienteService.java

        Problema 1 — Guarda la contraseña sin encoder
        Hoy hace esto:

        cliente.setPasswd(dtCliente.getPasswd());
        Eso significa que la contraseña entra tal cual al objeto antes de persistirse.

        Y en ApplicationConfig sí existe PasswordEncoder, así que NO hay excusa técnica para no usarlo.

        Por qué está mal
        Porque autenticación sin hash fuerte es una mala base de seguridad. Y además en las reglas del proyecto se pide almacenamiento seguro.

        Qué corregir
        inyectar PasswordEncoder
        guardar:
        passwordEncoder.encode(dtCliente.getPasswd())
        Problema 2 — El flujo de activación está mal modelado
        En GuiaCasosDeUso.md el CU-CL01 dice que:

        se crea la cuenta
        se envía correo de activación
        la cuenta queda activa recién cuando el usuario entra al enlace
        Pero tu servicio hace esto inmediatamente:

        cliente.setEstado(EstadoCuenta.Activo);
        .activo(true);
        luego envía el mail
        Por qué está mal
        Porque estás marcando la cuenta como activa ANTES de la activación. Eso contradice el caso de uso.

        Qué corregir
        Aquí hay una decisión de diseño que el equipo debe tomar bien:

        Alternativa A — agregar un estado tipo PendienteAprobacion / PendienteActivacion
        Pro: modela explícitamente el flujo
        Contra: toca enum, seguridad y login

        Alternativa B — usar un flag de activación separado
        Por ejemplo usar activo como activación y estado solo para bloqueo.

        Pro: menos invasivo
        Contra: separa el concepto en dos campos y puede confundir

        Mi recomendación: si el dominio exige activación por email, el modelo tiene que representarlo de forma explícita. NO lo tapes con un booleano sin una convención clara.

        Problema 3 — El tipo de usuario es frágil
        Hoy pones:

        cliente.setTipo("Cliente");
        Y luego la seguridad hace:

        "ROLE_" + usuario.getTipo().toUpperCase()
        O sea que dependes de un string libre.

        Por qué está mal
        Porque "Cliente", "cliente" o "CLIENTE" te cambian comportamiento o te obligan a normalizar en todos lados.

        Qué corregir
        usar una convención única y explícita
        idealmente enum
        si no, al menos persistir siempre el mismo valor (CLIENTE)
        Problema 4 — Inyección por campo
        Hoy ClienteService usa @Autowired en campos.

        Por qué está mal
        Porque hace el servicio más difícil de testear y contradice buenas prácticas de Spring.

        Qué corregir
        usar inyección por constructor
        dependencias private final
        Problema 5 — Falta prueba específica del caso de uso
        Para un método que registra usuario, deberías tener tests que validen:

        email duplicado → excepción
        documento duplicado → excepción
        password se guarda encodeada
        se llama a usuarioRepositorio.guardar
        se llama a clienteRepositorio.guardar
        se llama a emailService.enviarMailDeActivacion
        estado inicial correcto según la decisión de activación
        Eso hoy no está cubierto.
-
