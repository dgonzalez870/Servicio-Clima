# Servicios Android

Un [servicio](http://developer.android.com/guide/components/services.html) es un componente de aplicación que no posee interfaz gráfica y puede ejecutar largas operaciones en segundo plano[1]. Otros componentes como **Activities** pueden iniciar un **servicio** y este puede permanecer activo aun cuano el usuario cambie de aplicación. Los componentes de aplicación con interfaz gráfica como un **Activity** pueden enlazarse a un servicio y obtener datos de este para ser presentados al usuario.

Un **servicio** puede tomar dos formas:

* **Iniciado:** Un servicio es iniciado cuando otro componente de aplicación como un **Activity**, un **BroadcastReciver** u otro **servicio** invocan el método `startService()`, una vez iniciado el servicio puede correr en segundo plano indefinidamente o hasta terminar la operación para la cual fue programado.
* **Enlazado** Un servicio es **enlazado** cuando otro componente de aplicación invoca el método `bindService()`, al ser enlazado se crea una interfaz **cliente-servidor** que permíte a los componentes interactuar con el servicio, enviar solicitudes y recibir respuestas.

Existen dos clases de las que se puede heredar para crear un servicio:

a. **Service** Es la clase base para todos los servicios, si se crea un **servicio** a partir de esta clase es importante crear un nuevo **hilo** en el cual se realice todo el trabajo del servicio, de lo contrario se bloquea la interfaz de usuario y se reduce el desempeño del sistema.

b. **IntentService** Es una subclase de **Service** que utiliza un **hilo** exclusivo para manejar las solicitudes (Esta clase solo maneja una solicitud a la vez).

##Procedimiento básico para el trabajo con IntentService en Android

###Creación del Servicio
1. Crear una subclase de **IntentService**.
2. Implementar un constructor **sin parámetros** que invoque el constructor de la clase madre (`super(String nombre)`)
3. Implementar el método `onHandleIntent(Intent intent)`, en este método se deben especificar las operaciones que el servicio realiza en segundo plano.

###Iniciar el Servicio
El servicio es iniciado cuando desde cualquier otro componente se invoca el método `startService()`, entonces el sistema llama el método `onStartCommand()` del servicio, el **IntentService** tiene una implementación por defecto de este método. Por ejemplo sis se tiene una subclase de **IntentService** con el nombre **ServicioPrueba** el código para iniciarlo sería el siguiente

```java
 Intent intent=new Intent(this, ServicioPrueba.class);
 startService(intent);
```

###Enlazar el servicio

Al enlazar el servicio se obtiene una interfaz de interacción entre el cliente y el servidor, los pasos para enlazar el servicio se enumeran a continuación:
1. Crear en la clase cliente un **Handler** para manejar la comunicación entre el **Servicio** y el **Activity**.
2. Crear un objeto **Messenger** para enviar mensajes al **Handler** del servicio.
3. Crear un objeto ServiceConnection e implementar el método `onServiceConnected()` que recibe como parámetro un **IBinder**. En el método `onServiceConnected()` registrar los objetos `Messenger` a través de los cuales se comunican el cliente y el servidor.
4. Invocar el método `bindService(Intent, ServiceConnection, flag)`, al ser invocado este método el sistema llama el método `onBind()` del servicio.

La comunicación entre el **servicio** y el **Activity** se lleva a cabo através de los objetos Messenger y Handler.
![imagen](/capturas/servicio.pdf)

***

##Referencias

1. [Android Services](http://developer.android.com/guide/components/services.html)

***