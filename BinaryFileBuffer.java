package externalSort;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BinaryFileBuffer {

	public static int BUFFERSIZE = 2048;
	public BufferedReader buffer_r;
	public File arq_original;
	private String registro;
	private boolean vazio;

	public BinaryFileBuffer(File f) throws IOException {
		arq_original = f;
		buffer_r = new BufferedReader(new FileReader(f), BUFFERSIZE);
		recarregar();
	}

	public boolean vazio() {
		return vazio;
	}

	private void recarregar() throws IOException { // Usado para ler uma linha do arquivo e atualizar os valores de registro e vazio.
		try {
			if((this.registro = buffer_r.readLine()) == null){
				vazio = true;
				registro = null;
			}
			else
				vazio = false;
			
		} catch(EOFException eof) {
			vazio = true;
			registro = null;
		}
	}

	public void close() throws IOException {
		buffer_r.close();
	}

	public String registro() {
		if(vazio()) return null;
		return registro.toString();
	}

	public String estouro() throws IOException {
		String s = registro();
		recarregar();
		return s;
	}
}
