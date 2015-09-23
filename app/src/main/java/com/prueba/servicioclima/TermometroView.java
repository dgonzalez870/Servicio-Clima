package com.prueba.servicioclima;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class TermometroView extends View {

	private static final double ESCALA_MINIMA = 5.0;
	private static final double ESCALA_MAXIMA = 45.0;
	float diametroT, yind;
	int temperatura=20;
	Paint paint;

	public TermometroView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint=new Paint();
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec )
	{
		int parentHeight=MeasureSpec.getSize(heightSpec);
		int parentWidth=MeasureSpec.getSize(widthSpec);
		setMeasuredDimension(parentWidth, parentHeight);
	}
	
	protected void onDraw(Canvas c) {
		int alto = this.getHeight();
		diametroT=alto/11;
		dibujarTermometro(c, paint);
	}
	
	public void dibujarTermometro(Canvas g, Paint p) {
		float xoRectExt=10,yoRectExt=10;
		float x1RectExt=(getWidth()-10),y1RectExt=(getHeight()-10);
		float xoCuerpo=(getWidth()/2-diametroT/2),yoCuerpo=diametroT;
		float x1Cuerpo=(xoCuerpo+diametroT), y1Cuerpo=(yoCuerpo+8*diametroT);
		float xCentro=getWidth()/2, yCentro=(float) (9.5*diametroT);
		float radioTerm=diametroT/2;
		

		p.setColor(Color.WHITE);
		p.setStyle(Style.STROKE);
		// dibuja los indicadores de escala
		p.setTextAlign(Align.LEFT);
		p.setTextSize(radioTerm);
		for (int i = 0; i < 9; i++) {
			g.drawText("" + (ESCALA_MINIMA + 5 * i), x1Cuerpo +  diametroT, y1Cuerpo - i * diametroT, p);
			g.drawLine(xoCuerpo, y1Cuerpo-i*diametroT, x1Cuerpo+diametroT, y1Cuerpo-i*diametroT, p);
		}
		p.setStrokeWidth(2);
		// dibuja el rectangulo externo
		g.drawRect(xoRectExt, yoRectExt, x1RectExt,y1RectExt, p);
		// dibuja el cuerpo del termómetro
		g.drawRect(xoCuerpo, yoCuerpo, x1Cuerpo, y1Cuerpo, p);
		g.drawCircle(xCentro, yCentro,diametroT, p);
		// dibuja el bulbo del termómetro
		p.setColor(Color.GRAY);
		p.setStyle(Style.FILL);
		g.drawCircle(xCentro, yCentro,diametroT, p);
		// dibuja el nivel actual de la temperatura
		g.drawRect(xoCuerpo,yind,x1Cuerpo,y1Cuerpo, p);
		
		p.setStrokeWidth(0);
		p.setColor(Color.WHITE);
		p.setStyle(Style.STROKE);
		// dibuja los indicadores de escala
		p.setTextAlign(Align.LEFT);
		p.setTextSize(radioTerm);
		for (int i = 0; i < 9; i++) {
			g.drawText("" + (ESCALA_MINIMA + 5 * i), x1Cuerpo +  diametroT, y1Cuerpo - i * diametroT, p);
			g.drawLine(xoCuerpo, y1Cuerpo-i*diametroT, x1Cuerpo+diametroT, y1Cuerpo-i*diametroT, p);
		}
		p.setTextSize(diametroT);
		g.drawText(""+temperatura/10.0, xoCuerpo-3*diametroT, yind, p);
	}
	
	public void setNivel(double temperatura) {
		this.temperatura=(int) Math.round(10*temperatura);
		yind =  (float) (diametroT*(9.0-(8.0* (temperatura - ESCALA_MINIMA))
				/ (ESCALA_MAXIMA - ESCALA_MINIMA)));
		invalidate();
	}


}
