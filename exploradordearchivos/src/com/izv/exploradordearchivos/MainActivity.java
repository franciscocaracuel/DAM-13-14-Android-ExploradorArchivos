package com.izv.exploradordearchivos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private AdaptadorListView adaptador;
	private ListView lv;
	
	private ArrayList<File> lista;
	
	private LinearLayout lySubir;
	private TextView tvPadre, tvNombreArchivo;

	private String pathPadre, path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		inicio();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//La lista del listview se pierde al girar y aparece de nuevo "/"
	//Como para pasar un arrayList es necesario implementar Parcelable al tipo de Arraylist y en
	//este caso es File se saca uno a uno y se envia como String
	@Override
    protected void onSaveInstanceState(Bundle savingInstanceState) {
    	
    	super.onSaveInstanceState(savingInstanceState);
    	
    	for(int i=0;i<lista.size();i++){
    		
    		savingInstanceState.putString(i+"", lista.get(i).getAbsolutePath());
    		
    	}    	
    	
    	//Se envia el tamaño del arrayList para poder hacer el for en el restore
    	savingInstanceState.putInt("total", lista.size());
    	
    	//Se envia la ruta actual
    	savingInstanceState.putString("path", path);
    	
    }
	
	//Se vuelve a armar el arraylist y se llama a cargar el listview
	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	
		super.onRestoreInstanceState(savedInstanceState);
		
		int tamaño = savedInstanceState.getInt("total");
		
		lista=new ArrayList<File>();
		
		for(int i=0;i<tamaño;i++){
			
			lista.add(new File(savedInstanceState.getString(i+"")));
			
		}
		
    	cargarListView();
    	
    	//Para que no se pierda la opcion de subir carpeta ".."
    	path=savedInstanceState.getString("path");    	
    	existePadre(new File(path));
    	
    }
	
	public void cargarListView(){
		
		//Se crea el adaptador que ira dibujando los datos en el listView
		adaptador=new AdaptadorListView(getApplicationContext(), lista);
		
		//Se indica cual es el listView que recibira los datos
		lv=(ListView)findViewById(R.id.lvExplorador);
		lv.setAdapter(adaptador);
		
	}
	
	public void inicio(){
		
		tvNombreArchivo=(TextView)findViewById(R.id.tvNombreArchivo);		
				
		//Esto hace saltar a SuperSu para que le demos permiso de super usuario. Para ver la raiz no nos hace falta
		/*try {
			Process root = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
		}*/
		
		//Esto no inicia en la sdcard
		//File file=Environment.getExternalStorageDirectory();
		
		//Se crea el archivo que empezara por raiz
		File file=new File("/");
		
		//Se llama al metodo que muestra los archivos
		cargarArchivos(file);
		
		//Cuando se pulse en el listview se hara lo de dentro
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				
				//la variable pos indica el elemento seleccionado
				File fSelec = lista.get(pos);
				
				//Se pasa el archivo que hemos creado
				cargarArchivos(fSelec);
								
			}
			
		});
		
	}	
	
	public void cargarArchivos(File file){
						
		//Si es directorio se saca un listado con todos los archivos hijos
		if(file.isDirectory()){
			
			//El metodo listFile devuelve un array, habra que convertirlo a arrayList
			File[] listaSelecAux=file.listFiles();
			
			//Se comprueba si ha devuelto algo el listFiles, si no devuelve nada es que no tenemos permisos. CREO.
			if(listaSelecAux!=null){
				
				//Se inicializa el arrayList donde iran todos los Files hijos de la carpeta seleccionada
				lista=new ArrayList<File>();
				
				//Se recorre todo el array y se va creando el arraylist
				for(File f: listaSelecAux){
					
					lista.add(f);
					
				}
				
				//Se ordena el arraylist, por defecto ordena los File por nombre
				Collections.sort(lista);
				
				//Se envia el arraylist para que arme la ventana
				cargarListView();
				
				//Si es raiz nada, pero si no aparecera la carpeta para subir
				existePadre(file);
				
				//Guardamos en path la ruta del File actual que nos hara falta para el saved instance
				path=file.getAbsolutePath();
				
			} else{
				Toast.makeText(getApplicationContext(), R.string.toast_no_permisos_carpeta, Toast.LENGTH_SHORT).show();
			}
			
		//Si es archivo
		} else{
			
			//Se hace el try porque puede haber archivos que no pueda abrir
			try{
				
				//Se crea el intent que va a abrir algo
				Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
								
				//Se consigue la extension del archivo
				String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			
				//A partir de la extension se obtiene el tipo MIME
				String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			
				//Se asigna el archivo y la extension que se va a abrir
				myIntent.setDataAndType(Uri.fromFile(file),mimetype);
			
				//Se inicia el intent
				startActivity(myIntent);
			
			} catch(ActivityNotFoundException error){
				Toast.makeText(getApplicationContext(), R.string.toast_no_extension_encontrada, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}

	public void existePadre(File file){
		
		//Se le pone el nombre del archivo actual en el textview de arriba del todo
		//Si el arhivo devuelve vacio es que estamos en "/"
		if(file.getName().equals("")){
			tvNombreArchivo.setText("/");
		} else{
			tvNombreArchivo.setText(file.getAbsolutePath());
		}		
		
		lySubir=(LinearLayout)findViewById(R.id.lySubir);
		tvPadre=(TextView)findViewById(R.id.tvPadre);
		
		//Obtenemos el padre del File actual
		File padre=file.getParentFile();
		
		//Si es null significa que ya estabamos en "/"
		if(padre!=null){
			
			//Si no es null el layout se pone visible
			lySubir.setVisibility(View.VISIBLE);
			
			//Se pone el nombre del padre en el textview pequeño
			if(padre.getName().equals("")){
				pathPadre="/";
				tvPadre.setText(this.getString(R.string.go_to)+" "+pathPadre);
			} else{
				pathPadre=padre.getAbsolutePath();
				tvPadre.setText(this.getString(R.string.go_to)+" "+pathPadre);
			}
					
		//Si estamos en la raiz se oculta el layout y ademas ocupan su espacio. 
		//Si no queremos que ocupe el espacio se pone View.INVISIBLE
		} else{
			lySubir.setVisibility(View.GONE);
		}
		
	}
	
	public void subir(View v){
		
		//Cuando le demos al layout con ".." cargara el listview pero del File padre
		cargarArchivos(new File(pathPadre));		
		
	}
	
	//Sobreescribe la accion de los botones
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		
		//Si el botones pulsado es el de atras
		if ((keyCode == KeyEvent.KEYCODE_BACK)){
	
			//Si estamos en la raiz se sale
			if(path.equals("/")){
				return super.onKeyDown(keyCode, event);
				
			//Si no estamos en la raiz subimos un nivel
			} else{
				cargarArchivos(new File(pathPadre));
				return true;
			}
			
		}

		//Si se ha pulsado otro boton pues hara lo que tenga predefinido
		return super.onKeyDown(keyCode, event);
	
	}
	
}