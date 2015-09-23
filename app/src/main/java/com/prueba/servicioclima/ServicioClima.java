package com.prueba.servicioclima;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;

public class ServicioClima extends IntentService{

	//Ruta al API OpenWeatherMap Inc. que provee información del clima.
	public static final String urlServicio="http://api.openweathermap.org/data/2.5/weather?q=";
	public static final String TAG="ServicioClima";
	public static final int REGISTRAR_CLIENTE=0;
	public static final int DESVINCULAR_CLIENTE=1;
	public static final int PUBLICAR_DATOS=2;
	public static final int FINALIZAR_SERVICIO=3;
	
	private boolean finalizar=false;
	Messenger messengerServicio2Activity;
	Handler servicioHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REGISTRAR_CLIENTE:
				Log.i(TAG,"Se ha registrado el cliente");
				messengerServicio2Activity =msg.replyTo;
				break;
			case DESVINCULAR_CLIENTE:
				messengerServicio2Activity =null;
				Log.i(TAG,"Se ha desvinculado el cliente");
				break;
			case FINALIZAR_SERVICIO:
				Log.i(TAG, "Finalizar servicio");
				finalizar=true;
				break;
			default:
				break;
			}
		}
		
	};
	Messenger messengerServicio=new Messenger(servicioHandler);
	
	ServicioClienteActivity activity=new ServicioClienteActivity();
	public ServicioClima() {
		super("com.prueba.ServicioClima");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		while(!finalizar){
			
			DatosClima datosClima=solicitarClima("Caracas");
			Message message=Message.obtain(null, PUBLICAR_DATOS);
			Bundle data=new Bundle();
			data.putDouble("temp",datosClima.getMain().getTemp());
			data.putDouble("temp_min",datosClima.getMain().getTemp_max());
			data.putDouble("temp_max",datosClima.getMain().getTemp_min());
			data.putDouble("pressure",datosClima.getMain().getPressure());
			message.setData(data);
			if(messengerServicio2Activity !=null){
				
			try {
				messengerServicio2Activity.send(message);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}else{
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
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Destruyendo el servicio", Toast.LENGTH_LONG).show();
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Toast.makeText(this, "Iniciando servicioClima", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messengerServicio.getBinder();
	}

	//Conecta al servicio openWeatherMap y solicita el clima para el nombre de ciudad que se pasa
	// como parametro
	public DatosClima solicitarClima(String ciudad){
		DatosClima datosClima=null;
		try {
			URL url=new URL(urlServicio+ciudad+"&units=metric");
			URLConnection conexion=url.openConnection();
			InputStream inputStream=conexion.getInputStream();
			//buffer en el que se guardan los datos leidos del inputStream
			byte[] buffer=new byte[1024];
			//lee los datos del inputStream
			int cantidadLeidos=inputStream.read(buffer);
			//cierra el inputStream
			inputStream.close();
			//Construye una cadena de texto JSON a partir de los bytes leidos
			String respuestaServicio=new String(buffer,0,cantidadLeidos);
			//Obtiene un objeto DatosClima utilizando la librería GSON de Google
			datosClima=(new Gson()).fromJson(respuestaServicio, DatosClima.class);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return datosClima;
	}
public void notificar(){
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
	
}
