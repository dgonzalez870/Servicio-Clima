package com.prueba.servicioclima;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class TermometroActivity extends Activity {

	TermometroView termometro;
	Messenger messengerActivity2Servicio;
	boolean iniciado=false;
	Handler activityHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ServicioClima.PUBLICAR_DATOS:
				termometro.setNivel(msg.getData().getDouble("temp"));
				break;
			default:
				break;
			}
		}
	};
	Messenger messengerActivity=new Messenger(activityHandler);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_clima);
		termometro = (TermometroView) findViewById(R.id.termometroView1);
	}
		@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
			Intent intent1=new Intent(getApplicationContext(), ServicioClima.class);
			bindService(intent1, conexion, Context.BIND_AUTO_CREATE);
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
		try {
			messengerActivity2Servicio.send(Message.obtain(null, ServicioClima.FINALIZAR_SERVICIO));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		super.onBackPressed();
	}
}
