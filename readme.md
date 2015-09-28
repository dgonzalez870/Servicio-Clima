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
![imagen](/capturas/servicio.png)

# Ejemplo de aplicación de un servicio.

En este ejemplo se desarrolla un **IntentService** que se conecta a un servicio web RestFul, el API [OpenWeather](http://openweathermap.org/current) y realiza una consulta de estado del clima cada 10 segundos.

###Requerimientos de funcionamiento:

1. El servicio debe iniciarse de dos maneras, al hacer click sobre un botón en la interfaz gráfica de una actividad (Activity) y al encender el dispositivo.
2. La actividad debe enlazarce al servicio y presentar la información del clima en panatalla.
3. Cuando la actividad no es visible el servicio debe enviar una notificación en la barra de tareas.
4. Al hacer click sobre la notificación se despliega una actividad que se enlaza al servicio y presenta en pantalla la información del clima.

###Desarrollo

El desarrollo de la interfaz gráfica (layout y menu) del **Activity** y la implementación de la biblioteca **GSON** será el mismo descrito en el ejemplo [**ClienteServicioRestFul**](https://github.com/dgonzalez870/ClienteServicioRestFul).

####Desarrollo del Activity Cliente
1. Crear un **Activity** de nombre **ServicioClimaActivity**, En el método `onCreate` cragar la vista **layout_cliente_clima**, `setContentView(R.layout.layout_cliente_clima);`.
2. Obtener instancias de los **TextViews** en los que se representan los datos.
```java
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_cliente_clima);
		tvCiudad =(TextView) findViewById(R.id.tvCiudad);
		tvTemperatura =(TextView) findViewById(R.id.tvTemperatura);
		tvHumedad =(TextView) findViewById(R.id.tvHumedad);
		tvPresion =(TextView) findViewById(R.id.tvPresion);
	}
```
<!-- incluir el enlace-->
3. Crear un objeto **Handler** que reciba los **mensajes** enviados desde el **servicio**. Através de este **Handler** se presentará en pantalla los datos enviados por el **servicio** que se ejecuta en segundo plano y al cual el **Activity** se enlaza como **cliente**
```java
	Handler activityHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ServicioClima.PUBLICAR_DATOS:
				tvTemperatura.setText(msg.getData().getDouble("temp") + "");
				tvHumedad.setText(msg.getData().getDouble("temp_max") + "");
				tvPresion.setText(msg.getData().getDouble("temp_min") + "");
				tvCiudad.setText(msg.getData().getDouble("pressure") + "");
				break;
			default:
				break;
			}
		}
	};
```
4. Declarar un objeto **Messenger** para recibir mensajes del **servicio Android**
``` java
Messenger messengerActivity=new Messenger(activityHandler);`
```
El **Messenger** es asociado al handler de mensajes.
5. Declarar un **Messenger** para enviar **mensajes** al servicio
```java
Messenger messengerActivity2Servicio;
```
6. Crear un objeto **ServiceConnection** e implementar el método `onServiceConnected`.

```java
	private ServiceConnection conexion=new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder service) {
			//Crea un mensaje
			Message mensaje=Message.obtain(null, ServicioClima.REGISTRAR_CLIENTE);
			//Asigna el messenger que recibirá las respuestas al mensaje
			mensaje.replyTo=messengerActivity;
			//crea el messenger que transmitirá los mensajes al servicio
			messengerActivity2Servicio =new Messenger(service);
			try {
				//envía un mensaje al servicio.
				messengerActivity2Servicio.send(mensaje);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {

		}
	};
```
El método `onServiceConnected` recibe como parámetro un objeto [**IBinder**](http://developer.android.com/reference/android/os/IBinder.html), una interface para llamadas a procedimientos remotos **(RPC)**, en este método se asocian los **Messenger** de transmisión y recepción y se registra el **Activity** como cliente del **servicio.**

7. En el método `onStart` de **ServicioClienteActivity** enlazar el servicio.
```java
	protected void onStart() {
		super.onStart();
		Intent intent1=new Intent(getApplicationContext(), ServicioClima.class);
		if(bindService(intent1, conexion, Context.BIND_AUTO_CREATE))
			Toast.makeText(this,"Enlazado", LENGTH_LONG).show();
	}
```
La instrucción `bindService`  lleva a la ejecución del método `onBind` en el **Servicio**, crea la interfaz de comunicación entre el cliente(Activity) y el servidor(Servicio), sin embargo el servicio se inicia solo cuando se invoca la instrucción `startService`.
8. Crear un método de acción para el botón de la interfaz gráfica que inicia las solicitudes al servicio
```java
	public void accionBotonSolicitar(View v){
		//Verifica la conexión de red
		ConnectivityManager conectividadManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo informacion=conectividadManager.getActiveNetworkInfo();
		//Si está conectado a la red entonces inicia el servicio
		if(State.CONNECTED.equals(informacion.getState())){
			Intent intent=new Intent(getApplicationContext(), ServicioClima.class);
			startService(intent);
		}else{
			Toast.makeText(this, "No hay conexion de red", LENGTH_LONG).show();
		}
	}
```
9. Sobreescribir el metodo `onBackPressed` para desenlazar  **ServicioClienteActivity** del **Servicio** a través del método `unbindService(ServiceConnection)`
```java
	public void onBackPressed() {
		if(messengerActivity2Servicio !=null){
			Message msg=new Message();
			msg.what=ServicioClima.DESVINCULAR_CLIENTE;
			try {
				messengerActivity2Servicio.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			unbindService(conexion);
		}
		super.onBackPressed();
	}
```

####Desarrollo del Servicio

1. Crear una clase de nombre **ServicioClima** que extienda de la clase **IntentService**.
2. Crear un constructor **sin argumentos**  para la clase.
```java
    public ServicioClima() {
        super("com.prueba.ServicioClima");
    }
```
3. Implementar en el servicio **ServicioClima** el método `solicitarClima(String)` descrito en el ejemplo [**ClienteServicioRestFul**](https://github.com/dgonzalez870/ClienteServicioRestFul).
```java
    //Conecta al servicio openWeatherMap y solicita el clima para el nombre de ciudad que se pasa
    // como parametro
    public DatosClima solicitarClima(String ciudad) {
        DatosClima datosClima = null;
        try {
            URL url = new URL(urlServicio + ciudad + "&units=metric");
            URLConnection conexion = url.openConnection();
            InputStream inputStream = conexion.getInputStream();
            //buffer en el que se guardan los datos leidos del inputStream
            byte[] buffer = new byte[1024];
            //lee los datos del inputStream
            int cantidadLeidos = inputStream.read(buffer);
            //cierra el inputStream
            inputStream.close();
            //Construye una cadena de texto JSON a partir de los bytes leidos
            String respuestaServicio = new String(buffer, 0, cantidadLeidos);
            //Obtiene un objeto DatosClima utilizando la librería GSON de Google
            datosClima = (new Gson()).fromJson(respuestaServicio, DatosClima.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datosClima;
    }
```
La clase [DatosClima] representa los datos de interés que retorna el servicio RESTFul [OpenWeathermap](http://openweathermap.org/current) y que son mapeados a travles de la bibilioteca [GSON](https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/Gson.html).
4. Implementar el método `onHandleIntent` de **ServicioClima** para manejar las solicitudes al servicio
```java
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        while (!finalizar) {

            DatosClima datosClima = solicitarClima("Caracas");
            Message message = Message.obtain(null, PUBLICAR_DATOS);
            Bundle data = new Bundle();
            data.putDouble("temp", datosClima.getMain().getTemp());
            data.putDouble("temp_min", datosClima.getMain().getTemp_max());
            data.putDouble("temp_max", datosClima.getMain().getTemp_min());
            data.putDouble("pressure", datosClima.getMain().getPressure());
            message.setData(data);
            if (messengerServicio2Activity != null) {

                try {
                    messengerServicio2Activity.send(message);
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else {
                notificar();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
```
Realiza conexiones cada 10 segundos al servicio RESTFul mienttras el booleano **finalizar** sea falso, envía un mensaje con los datos obtenidos al cliente.
5. Crear un objeto **Handler** en **ServicioClima** que maneje los mensajes enviados desde los clientes.
```java
    Handler servicioHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case REGISTRAR_CLIENTE:
                    Log.i(TAG, "Se ha registrado el cliente");
                    messengerServicio2Activity = msg.replyTo;
                    break;
                case DESVINCULAR_CLIENTE:
                    messengerServicio2Activity = null;
                    Log.i(TAG, "Se ha desvinculado el cliente");
                    break;
                case FINALIZAR_SERVICIO:
                    Log.i(TAG, "Finalizar servicio");
                    finalizar = true;
                    break;
                default:
                    break;
            }
        }

    };
```
6. Sobreescribir el método `onBind` para retornar la interfaz de conexión al servicio.
```java
   public IBinder onBind(Intent intent) {
        return messengerServicio.getBinder();
    }
```
7. Crear un método `notificar` para enviar notificaciones a la barra de tareas cuando el servicio no tenga ningún cliente enlazado.
```java
    public void notificar() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_menu_help)
                        .setContentTitle("Informacion del Clima")
                        .setContentText("Hay nueva informacion del Clima").setAutoCancel(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, TermometroActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ServicioClienteActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
```

####Desarrollo de TermómetroActivity

**TermometroActivity** es un **Activity** que presenta en pantalla el dato de temperatura recibido desde el **Servicio**.

![inicial](/capturas/inicial.png) ![iniservicio](/capturas/iniservicio.png) ![notificacion](/capturas/notificacion.png) ![termometro](/capturas/termometro.png)

##Referencias

1. [Android Services](http://developer.android.com/guide/components/services.html)

***