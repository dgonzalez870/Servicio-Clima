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

public class ServicioClienteActivity extends Activity {

	TextView tvTemperatura;
	TextView tvHumedad;
	TextView tvPresion;
	TextView tvCiudad;

	Messenger messengerActivity2Servicio;
	Handler activityHandler=new Handler(){

		@Override
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
			Toast.makeText(this,"correcto",Toast.LENGTH_LONG).show();
	}
	
	public void accionBotonSolicitar(View v){
		ConnectivityManager conectividadManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo informacion=conectividadManager.getActiveNetworkInfo();
		if(State.CONNECTED.equals(informacion.getState())){
			Intent intent=new Intent(getApplicationContext(), ServicioClima.class);
			startService(intent);
		}else{
			Toast.makeText(this, "No hay conexion de red", Toast.LENGTH_LONG).show();
		}
	}
	
	public void accionDesconectar(View v){
		try {
			messengerActivity2Servicio.send(Message.obtain(null, ServicioClima.FINALIZAR_SERVICIO));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	ServiceConnection conexion=new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Message mensaje=Message.obtain(null, ServicioClima.REGISTRAR_CLIENTE);
			mensaje.replyTo=messengerActivity;
			messengerActivity2Servicio =new Messenger(service);
			try {
				messengerActivity2Servicio.send(mensaje);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
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
