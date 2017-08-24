package com.izv.exploradordearchivos;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdaptadorListView extends ArrayAdapter<File>{
	
	private Context contexto;
	private ArrayList<File> lista;
	
	private TextView tvNombre, tvModificado;
	private ImageView ivIcono;
	
	private GregorianCalendar fecha;
	
	public AdaptadorListView(Context c, ArrayList<File> l) {
		super(c, R.layout.item_listview, l);
		this.contexto=c;
		this.lista=l;
	}
	
	@Override
	public View getView(int posicion, View vista, ViewGroup padre){
		
		//Dibuja las lineas
		if(vista==null){ //Este if optimiza el getView
			LayoutInflater i=(LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vista=i.inflate(R.layout.item_listview, null);
		}
		
		tvNombre=(TextView)vista.findViewById(R.id.tvNombre);
		tvModificado=(TextView)vista.findViewById(R.id.tvModificado);
		ivIcono=(ImageView)vista.findViewById(R.id.ivIcono);
		
		//Guardamos en f cada File
		File f=lista.get(posicion);
		
		//Se le asigna el nombre
		tvNombre.setText(f.getName());
			
		////////////////////SE CALCULA LA FECHA DE MODIFICACION///////////////
		fecha=new GregorianCalendar();
		fecha.setTimeInMillis(f.lastModified());
		
		tvModificado.setText(fecha.get(GregorianCalendar.DAY_OF_MONTH)+"/"+
				(fecha.get(GregorianCalendar.MONTH)+1)+"/"+
				fecha.get(GregorianCalendar.YEAR));
		//////////////////////////////////////////////////////////////////////
			
		//Dibujamos los iconos de la carpeta o archivo
		if(f.isFile()){
			ivIcono.setImageDrawable(vista.getResources().getDrawable(R.drawable.archivo));
		} else{			
			ivIcono.setImageDrawable(vista.getResources().getDrawable(R.drawable.carpeta));			
		} 
		
		return vista;
		
	}
	
}