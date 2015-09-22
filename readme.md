# Servicios Android

Un [servicio](http://developer.android.com/guide/components/services.html) es un componente de aplicaci�n que no posee interfaz gr�fica y puede ejecutar largas operaciones en segundo plano[1]. Otros componentes como **Activities** pueden iniciar un **servicio** y este puede permanecer activo aun cuano el usuario cambie de aplicaci�n. Los componentes de aplicaci�n con interfaz gr�fica como un **Activity** pueden enlazarse a un servicio y obtener datos de este para ser presentados al usuario.

Un **servicio** puede tomar dos formas:

* **Iniciado:** Un servicio es iniciado cuando otro componente de aplicaci�n como un **Activity**, un **BroadcastReciver** u otro **servicio** invocan el m�todo `startService()`, una vez iniciado el servicio puede correr en segundo plano indefinidamente o hasta terminar la operaci�n para la cual fue programado.
* **Enlazado** Un servicio es **enlazado** cuando otro componente de aplicaci�n invoca el m�todo `bindService()`, al ser enlazado se crea una interfaz **cliente-servidor** que perm�te a los componentes interactuar con el servicio, enviar solicitudes y recibir respuestas.

Existen dos clases de las que se puede heredar para crear un servicio:

a. **Service** Es la clase base para todos los servicios, si se crea un **servicio** a partir de esta clase es importante crear un nuevo **hilo** en el cual se realice todo el trabajo del servicio, de lo contrario se bloquea la interfaz de usuario y se reduce el desempe�o del sistema.

b. **IntentService** Es una subclase de **Service** que utiliza un **hilo** exclusivo para manejar las solicitudes (Esta clase solo maneja una solicitud a la vez).

##Procedimiento b�sico para el trabajo con IntentService en Android

###Creaci�n del Servicio
1. Crear una subclase de **IntentService**.
2. Implementar un constructor **sin par�metros** que invoque el constructor de la clase madre (`super(String nombre)`)
3. Implementar el m�todo `onHandleIntent(Intent intent)`, en este m�todo se deben especificar las operaciones que el servicio realiza en segundo plano.

###Iniciar el Servicio
El servicio es iniciado cuando desde cualquier otro componente se invoca el m�todo `startService()`, entonces el sistema llama el m�todo `onStartCommand()` del servicio, el **IntentService** tiene una implementaci�n por defecto de este m�todo. Por ejemplo sis se tiene una subclase de **IntentService** con el nombre **ServicioPrueba** el c�digo para iniciarlo ser�a el siguiente

```java
 Intent intent=new Intent(this, ServicioPrueba.class);
 startService(intent);
```

###Enlazar el servicio

Al enlazar el servicio se obtiene una interfaz de interacci�n entre el cliente y el servidor, los pasos para enlazar el servicio se enumeran a continuaci�n:
1. Crear en la clase cliente un **Handler** para manejar la comunicaci�n entre el **Servicio** y el **Activity**.
2. Crear un objeto **Messenger** para enviar mensajes al **Handler** del servicio.
3. Crear un objeto ServiceConnection e implementar el m�todo `onServiceConnected()` que recibe como par�metro un **IBinder**. En el m�todo `onServiceConnected()` registrar los objetos `Messenger` a trav�s de los cuales se comunican el cliente y el servidor.
4. Invocar el m�todo `bindService(Intent, ServiceConnection, flag)`, al ser invocado este m�todo el sistema llama el m�todo `onBind()` del servicio.

La comunicaci�n entre el **servicio** y el **Activity** se lleva a cabo atrav�s de los objetos Messenger y Handler.
![imagen](/capturas/servicio.pdf)

***

##Referencias

1. [Android Services](http://developer.android.com/guide/components/services.html)

***