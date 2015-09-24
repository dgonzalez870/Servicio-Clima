package com.prueba.servicioclima;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

public class ServicioClienteActivity extends Activity {

	TextView tvTemperatura;
	TextView tvHumedad;
	TextView tvPresion;
	TextView tvCiudad;

	//objeto Messenger que sirve para enviar mensajes desde el Activity (Cliente) al Servicio (Servidor)
	Messenger messengerActivity2Servicio;
	//Handler que maneja los mensajes recibidos por el Activity
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

	Messenger messengerActivity=new Messenger(activityHandler);

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_cliente_clima);
		tvCiudad =(TextView) findViewById(R.id.tvCiudad);
		tvTemperatura =(TextView) findViewById(R.id.tvTemperatura);
		tvHumedad =(TextView) findViewById(R.id.tvHumedad);
		tvPresion =(TextView) findViewById(R.id.tvPresion);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent1=new Intent(getApplicationContext(), ServicioClima.class);
		if(bindService(intent1, conexion, Context.BIND_AUTO_CREATE))
			Toast.makeText(this,"correcto", LENGTH_LONG).show();
	}
	
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
	
	public void accionDesconectar(View v){
		try {
			messengerActivity2Servicio.send(Message.obtain(null, ServicioClima.FINALIZAR_SERVICIO));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
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

	@Override
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
	
}
